package io.github.plixo2.framework.abstractions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.lwjgl.opengl.ARBPipelineStatisticsQuery;

import java.util.function.Function;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL33.GL_TIME_ELAPSED;
import static org.lwjgl.opengl.GL33.glGetQueryObjectui64v;

public final class GpuQuery<T> {
    private static final int QUERY_POOL_SIZE = 10;
    static {
        if (QUERY_POOL_SIZE < 2) throw new IllegalArgumentException("QUERY_POOL_SIZE >= 2 required");
    }

    private final GLQueryPool pool;
    private final QueryMapper<T> mapper;
    private final T defaultValue;
    private final boolean enabled;

    private int frame = 0;
    private boolean open = false;

    public GpuQuery(
            Query<T> query
    ) {
        this.pool = new GLQueryPool(QUERY_POOL_SIZE, query.type);
        this.enabled = query.enabled;
        this.mapper = query.mapper;
        this.defaultValue = query.defaultValue;

        if (this.enabled) {
            this.pool.init();
        }
    }

    public void begin() {
        if (!this.enabled) {
            return;
        }
        if (this.open) {
            throw new IllegalStateException("Query still open");
        }
        this.frame += 1;
        this.open = true;
        this.pool.beginQuery(this.frame);
    }

    public void end() {
        if (!this.enabled) {
            return;
        }
        if (!this.open) {
            throw new IllegalStateException("Query not open");
        }
        this.open = false;
        this.pool.endQuery();
    }

    public T poll() {
        if (!this.enabled) {
            return this.defaultValue;
        }
        var q = this.pool.getQuery(this.frame);
        var isAvailable = GLQueryPool.isQueryResultAvailable(q);
        if (!isAvailable) {
            return this.defaultValue;
        }
        return this.mapper.map(q);
    }

    public void destroy() {
        this.pool.destroy();
    }

    private static class GLQueryPool {
        private final int[] queries;
        private final int delay;
        private final int type;

        public GLQueryPool(int poolSize, int type) {
            this.type = type;
            this.delay = poolSize - 1;
            this.queries = new int[poolSize];
            for (int i = 0; i < poolSize; i++) {
                this.queries[i] = glGenQueries();
            }
        }
        private void init() {
            for (var i = 0; i < this.queries.length; i++) {
                this.beginQuery(i);
                this.endQuery();
            }
        }

        private int getQuery(int frame) {
            int readIdx = (frame + this.queries.length - this.delay) % this.queries.length;
            return this.queries[readIdx];
        }

        private void beginQuery(int frame) {
            int q = this.queries[frame % this.queries.length];
            glBeginQuery(this.type, q);
        }

        private void endQuery() {
            glEndQuery(this.type);
        }

        private void destroy() {
            for (int q : this.queries) {
                glDeleteQueries(q);
            }
        }

        private static boolean isQueryResultAvailable(int q) {
            if (q == 0) return false;
            int[] available = new int[1];
            glGetQueryObjectiv(q, GL_QUERY_RESULT_AVAILABLE, available);
            return available[0] == GL_TRUE;
        }
    }

    public interface QueryMapper<T> {
        T map(int q);

        default <R> QueryMapper<R> map(Function<T, R> mapper) {
            return q -> mapper.apply(this.map(q));
        }

        record IntQuery() implements QueryMapper<Integer> {
            @Override
            public Integer map(int q) {
                int[] ns = new int[1];
                glGetQueryObjectiv(q, GL_QUERY_RESULT, ns);
                return ns[0];
            }
        }
        record LongQuery() implements QueryMapper<Long> {
            @Override
            public Long map(int q) {
                long[] ns = new long[1];
                glGetQueryObjectui64v(q, GL_QUERY_RESULT, ns);
                return ns[0];
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Query<T> {
        private final int type;
        private final boolean enabled;
        private final QueryMapper<T> mapper;
        private final T defaultValue;
    }

    public static GpuQuery<Double> timer() {
        var q = new Query<>(
                GL_TIME_ELAPSED,
                true,
                new QueryMapper.LongQuery().map(ns -> ns / 1_000_000.0),
                0.0
        );
        return new GpuQuery<>(q);
    }
    public static GpuQuery<Integer> samplesPassed() {
        var q = new Query<>(
                GL_SAMPLES_PASSED,
                true,
                new QueryMapper.IntQuery(),
                0
        );
        return new GpuQuery<>(q);
    }

    public static GpuQuery<Integer> shaderInvocations() {
        var q = new Query<Integer>(
                ARBPipelineStatisticsQuery.GL_FRAGMENT_SHADER_INVOCATIONS_ARB,
                Capabilities.get().pipelineStatistics(),
                new QueryMapper.IntQuery(),
                0
        );
        return new GpuQuery<>(q);
    }

    public static GpuQuery<Integer> verticesSubmitted() {
        var q = new Query<>(
                ARBPipelineStatisticsQuery.GL_VERTICES_SUBMITTED_ARB,
                Capabilities.get().pipelineStatistics(),
                new QueryMapper.IntQuery(),
                0
        );
        return new GpuQuery<>(q);
    }

    public static GpuQuery<Integer> primitivesSubmitted() {
        var q =  new Query<>(
                ARBPipelineStatisticsQuery.GL_PRIMITIVES_SUBMITTED_ARB,
                Capabilities.get().pipelineStatistics(),
                new QueryMapper.IntQuery(),
                0
        );
        return new GpuQuery<>(q);
    }


}

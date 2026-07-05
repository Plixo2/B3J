package io.github.plixo2.box3d;

import lombok.Getter;
import org.box2d.box3d.b3EnqueueTaskCallback;
import org.box2d.box3d.b3FinishTaskCallback;
import org.box2d.box3d.b3TaskCallback;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class TaskPool<T> implements b3EnqueueTaskCallback.Function, b3FinishTaskCallback.Function, AutoCloseable {

    @Getter
    private final int workerCount;

    protected TaskPool(int workerCount) {
        this.workerCount = workerCount;
    }

    private final Map<Long, T> pending = new HashMap<>();
    private long counter = 1;

    public abstract T enqueue(Runnable runnable);
    public abstract void finish(T task);
    public abstract void close();


    /// b3EnqueueTaskCallback
    @Override
    public MemorySegment apply(
            MemorySegment task,
            MemorySegment taskContext,
            MemorySegment userContext,
            MemorySegment taskName
    ) {
        var future = enqueue(() -> b3TaskCallback.invoke(task, taskContext));
        this.pending.put(this.counter, future);
        return MemorySegment.ofAddress(this.counter++);
    }

    /// b3FinishTaskCallback
    @Override
    public void apply(MemorySegment userTask, MemorySegment userContext) {
        var task = this.pending.remove(userTask.address());
        finish(task);
    }

    static Allocated allocate(TaskPool<?> pool) {
        return new Allocated(pool);
    }


    public static class Default extends TaskPool<Future<?>> {
        private final ExecutorService executor;

        public Default(int workerCount, ExecutorService executor) {
            super(workerCount);
            this.executor = executor;
        }

        @Override
        public Future<?> enqueue(Runnable runnable) {
            return this.executor.submit(runnable);
        }

        @Override
        public void finish(Future<?> task) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(System.err);
            }
        }

        @Override
        public void close() {
            this.executor.shutdown();
        }

    }


    /// Keep the TaskPool and allocated
    /// callbacks alive until the world is destroyed
    static class Allocated {
        private TaskPool<?> taskPool;
        private Arena arena;

        @Getter
        private MemorySegment enqueueTaskCallback;
        @Getter
        private MemorySegment finishTaskCallback;

        private Allocated(TaskPool<?> taskPool) {
            this.arena = Arena.ofConfined();
            this.taskPool = taskPool;
            this.enqueueTaskCallback = b3EnqueueTaskCallback.allocate(taskPool, this.arena);
            this.finishTaskCallback = b3FinishTaskCallback.allocate(taskPool, this.arena);
        }

        public void close() {
            this.taskPool.close();
            this.taskPool = null;
            this.arena.close();
            this.arena = null;
            this.enqueueTaskCallback = null;
            this.finishTaskCallback = null;
        }

    }

}

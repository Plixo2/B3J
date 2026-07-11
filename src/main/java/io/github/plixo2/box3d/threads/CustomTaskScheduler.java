package io.github.plixo2.box3d.threads;

import lombok.Getter;
import org.box2d.box3d.b3EnqueueTaskCallback;
import org.box2d.box3d.b3FinishTaskCallback;
import org.box2d.box3d.b3TaskCallback;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.lang.foreign.MemorySegment;

import static io.github.plixo2.box3d.internal.Internal.assertU32;

public abstract non-sealed class CustomTaskScheduler<T>
        implements
            TaskScheduler,
            AutoCloseable,
            b3EnqueueTaskCallback.Function,
            b3FinishTaskCallback.Function
{

    @Getter
    private final int workerCount;

    protected CustomTaskScheduler(int workerCount) {
        if (workerCount < 1) {
            throw new IllegalArgumentException("workerCount must be at least 1");
        }

        this.workerCount = workerCount;
    }

    private final LongObjectHashMap<T> pending = new LongObjectHashMap<>();
    private long counter = 1;

    public abstract T enqueue(Runnable runnable);
    public abstract void finish(T task);
    public abstract void close();


    /// b3EnqueueTaskCallback
    @Override
    public final MemorySegment apply(
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
    public final void apply(MemorySegment userTask, MemorySegment userContext) {
        var task = this.pending.remove(userTask.address());
        finish(task);
    }



}

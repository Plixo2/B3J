package io.github.plixo2.box3d.threads;

import io.github.plixo2.box3d.internal.Internal;
import lombok.Getter;
import org.box2d.box3d.b3EnqueueTaskCallback;
import org.box2d.box3d.b3FinishTaskCallback;
import org.box2d.box3d.b3TaskCallback;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;

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

package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.tasks.CustomTaskScheduler;
import lombok.Getter;
import org.box2d.box3d.b3EnqueueTaskCallback;
import org.box2d.box3d.b3FinishTaskCallback;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class AllocatedTaskCallbacks implements AutoCloseable {

    private CustomTaskScheduler<?> taskPool;
    private Arena arena;

    @Getter
    private MemorySegment enqueueTaskCallback;
    @Getter
    private MemorySegment finishTaskCallback;

    private AllocatedTaskCallbacks(CustomTaskScheduler<?> taskPool) {
        this.arena = Arena.ofConfined();
        this.taskPool = taskPool;
        this.enqueueTaskCallback = b3EnqueueTaskCallback.allocate(taskPool, this.arena);
        this.finishTaskCallback = b3FinishTaskCallback.allocate(taskPool, this.arena);
    }

    public static AllocatedTaskCallbacks createCallbacks(CustomTaskScheduler<?> taskPool) {
        return new AllocatedTaskCallbacks(taskPool);
    }

    @Override
    public void close() {
        this.taskPool.close();
        this.taskPool = null;
        this.arena.close();
        this.arena = null;
        this.enqueueTaskCallback = null;
        this.finishTaskCallback = null;
    }

}

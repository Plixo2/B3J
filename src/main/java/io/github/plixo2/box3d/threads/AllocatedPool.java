package io.github.plixo2.box3d.threads;

import lombok.Getter;
import org.box2d.box3d.b3EnqueueTaskCallback;
import org.box2d.box3d.b3FinishTaskCallback;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/// Keep the TaskPool and callbacks alive until the world is destroyed
public class AllocatedPool {


    private TaskPool<?> taskPool;
    private Arena arena;

    @Getter
    private MemorySegment enqueueTaskCallback;
    @Getter
    private MemorySegment finishTaskCallback;

    AllocatedPool(TaskPool<?> taskPool) {
        this.arena = Arena.ofConfined();
        this.taskPool = taskPool;
        this.enqueueTaskCallback = b3EnqueueTaskCallback.allocate(taskPool, this.arena);
        this.finishTaskCallback = b3FinishTaskCallback.allocate(taskPool, this.arena);
    }

    public static AllocatedPool of(TaskPool<?> taskPool) {
        return new AllocatedPool(taskPool);
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

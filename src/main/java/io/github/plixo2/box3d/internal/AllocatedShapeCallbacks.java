package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.DebugShapeCallbacks;
import io.github.plixo2.box3d.tasks.CustomTaskScheduler;
import lombok.Getter;
import org.box2d.box3d.b3CreateDebugShapeCallback;
import org.box2d.box3d.b3DestroyDebugShapeCallback;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class AllocatedShapeCallbacks implements AutoCloseable {

    @SuppressWarnings("unused")
    private DebugShapeCallbacks<?> collection; // keep alive
    private Arena arena;

    @Getter
    private MemorySegment creation;
    @Getter
    private MemorySegment deletion;

    private AllocatedShapeCallbacks(
            DebugShapeCallbacks<?> collection,
            b3CreateDebugShapeCallback.Function create,
            b3DestroyDebugShapeCallback.Function delete
    ) {
        this.collection = collection;
        this.arena = Arena.ofConfined();
        this.creation = b3CreateDebugShapeCallback.allocate(create, this.arena);
        this.deletion = b3DestroyDebugShapeCallback.allocate(delete, this.arena);
    }

    public static AllocatedShapeCallbacks createCallbacks(
            DebugShapeCallbacks<?> taskPool,
            b3CreateDebugShapeCallback.Function create,
            b3DestroyDebugShapeCallback.Function delete
    ) {
        return new AllocatedShapeCallbacks(taskPool, create, delete);
    }

    @Override
    public void close() {
        this.arena.close();
        this.arena = null;
        this.collection = null;
        this.creation = null;
        this.deletion = null;
    }
}

package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.MoverFilterFcn;
import io.github.plixo2.box3d.ShapeID;
import org.box2d.box3d.b3MoverFilterFcn;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

import static org.box2d.box3d.box3d_h.b3World_CastMover;

public final class ScratchMoverFilterFcn implements b3MoverFilterFcn.Function {

    private @Nullable volatile MoverFilterFcn function = null;

    private final MemorySegment segment;

    public ScratchMoverFilterFcn(Arena parent) {
        this.segment = b3MoverFilterFcn.allocate(this, parent);
    }

    public float invoke(
            MemorySegment worldId,
            MemorySegment origin,
            MemorySegment mover,
            MemorySegment translation,
            MemorySegment filter,
            @Nullable MoverFilterFcn fcn
    ) {
        this.function = fcn;

        try {
            return b3World_CastMover(
                    worldId,
                    origin,
                    mover,
                    translation,
                    filter,
                    fcn != null ? this.segment : MemorySegment.NULL,
                    MemorySegment.NULL
            );
        } finally {
            this.function = null;
        }

    }


    @Override
    public boolean apply(
            MemorySegment shapeId,
            MemorySegment context
    ) {

        try {
            var function1 = this.function;
            Objects.requireNonNull(function1, "function not set");

            var packedID = PrimitiveMemOps.packID(shapeId);
            var shape = ShapeID.fromUnknown(packedID);

            return function1.filter(shape);
        } catch(Exception e) {
            B3JUtil.unhandledCallbackException(e);
            return false;
        }
    }


}

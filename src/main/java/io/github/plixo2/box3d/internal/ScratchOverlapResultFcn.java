package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.OverlapResultFcn;
import io.github.plixo2.box3d.ShapeID;
import org.box2d.box3d.b3OverlapResultFcn;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.Objects;

import static org.box2d.box3d.box3d_h.b3World_OverlapShape;

public final class ScratchOverlapResultFcn implements b3OverlapResultFcn.Function {

    private @Nullable volatile OverlapResultFcn function = null;

    private final MemorySegment segment;

    public ScratchOverlapResultFcn(Arena parent) {
        this.segment = b3OverlapResultFcn.allocate(this, parent);
    }

    public MemorySegment invokeShape(
            SegmentAllocator returnArena,
            MemorySegment worldId,
            MemorySegment origin,
            MemorySegment proxy,
            MemorySegment filter,
            OverlapResultFcn fcn
    ) {
        this.function = fcn;

        try {
            return b3World_OverlapShape(
                    returnArena,
                    worldId,
                    origin,
                    proxy,
                    filter,
                    this.segment,
                    MemorySegment.NULL
            );
        } finally {
            this.function = null;
        }
    }

    @Override
    public boolean apply(MemorySegment shapeId, MemorySegment context) {
        try {
            var function1 = this.function;
            Objects.requireNonNull(function1, "function not set");

            var packedID = PrimitiveMemOps.packID(shapeId);
            var shape = ShapeID.fromUnknown(packedID);

            return function1.onOverlap(shape);
        } catch (Exception e) {
            B3JUtil.unhandledCallbackException(e);
            return false;
        }
    }

}

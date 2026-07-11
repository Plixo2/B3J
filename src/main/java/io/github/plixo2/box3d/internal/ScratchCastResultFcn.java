package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.*;
import org.box2d.box3d.b3CastResultFcn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.Objects;

import static org.box2d.box3d.box3d_h.b3World_CastRay;

public final class ScratchCastResultFcn implements b3CastResultFcn.Function {

    private @Nullable volatile CastResult function = null;

    private final MemorySegment segment;

    private final Vector3f v1 = new Vector3f();
    private final Vector3f v2 = new Vector3f();

    public ScratchCastResultFcn(Arena parent) {
        this.segment = b3CastResultFcn.allocate(this, parent);
    }

    public MemorySegment invoke(
            SegmentAllocator returnArena,
            MemorySegment worldId,
            MemorySegment origin,
            MemorySegment translation,
            MemorySegment filter,
            CastResult fcn
    ) {
        this.function = fcn;

        try {
            return b3World_CastRay(
                    returnArena,
                    worldId,
                    origin,
                    translation,
                    filter,
                    this.segment,
                    MemorySegment.NULL
            );
        } finally {
            this.function = null;
        }

    }


    @Override
    public float apply(
            MemorySegment shapeId,
            MemorySegment point,
            MemorySegment normal,
            float fraction,
            long userMaterialId,
            int triangleIndex,
            int childIndex,
            MemorySegment context
    ) {
        var function1 = this.function;
        Objects.requireNonNull(function1, "function not set");
        PrimitiveMemOps.setVec3(this.v1, point);
        PrimitiveMemOps.setVec3(this.v2, normal);

        var packedID = PrimitiveMemOps.packID(shapeId);
        var shape = ShapeID.fromUnknown(packedID);

        return function1.onHit(
                shape,
                this.v1,
                this.v2,
                fraction,
                userMaterialId,
                triangleIndex,
                childIndex
        );
    }


}

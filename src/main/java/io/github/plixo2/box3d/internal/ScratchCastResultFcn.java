package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.*;
import org.box2d.box3d.b3CastResultFcn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Function;

public class ScratchCastResultFcn implements b3CastResultFcn.Function, AutoCloseable {

    private @Nullable CastResult function = null;

    private final MemorySegment segment;

    private final Vector3f v1 = new Vector3f();
    private final Vector3f v2 = new Vector3f();

    public ScratchCastResultFcn(Arena parent) {
        this.segment = b3CastResultFcn.allocate(this, parent);
    }

    public MemorySegment set(CastResult function) {
        this.function = function;
        return this.segment;
    }

    @Override
    public void close() {
        this.function = null;
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
        Objects.requireNonNull(this.function, "function not set");
        PrimitiveMemOps.setVec3(this.v1, point);
        PrimitiveMemOps.setVec3(this.v2, normal);

        var packedID = PrimitiveMemOps.packShapeID(shapeId);
        var shape = ShapeID.fromUnknown(packedID);

        return this.function.onHit(
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

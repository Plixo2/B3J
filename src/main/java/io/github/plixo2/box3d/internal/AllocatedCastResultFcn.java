package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.CastResult;
import io.github.plixo2.box3d.ShapeID;
import org.box2d.box3d.b3CastResultFcn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.Function;

public class AllocatedCastResultFcn implements b3CastResultFcn.Function, AutoCloseable {

    private @Nullable CastResult function = null;

    private final MemorySegment segment;
    private final Function<MemorySegment, ShapeID> shapeCreator;
    private final Vector3f v1 = new Vector3f();
    private final Vector3f v2 = new Vector3f();

    public AllocatedCastResultFcn(Arena parent, Function<MemorySegment, ShapeID> shapeCreator) {
        this.segment = b3CastResultFcn.allocate(this, parent);
        this.shapeCreator = shapeCreator;
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
        Objects.requireNonNull(this.function, "CastResult function is not set");
        PrimitveMemOps.setVec3(this.v1, point);
        PrimitveMemOps.setVec3(this.v2, normal);
        ShapeID shape = this.shapeCreator.apply(shapeId);

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

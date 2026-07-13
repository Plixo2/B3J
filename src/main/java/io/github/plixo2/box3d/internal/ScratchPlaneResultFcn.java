package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.*;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

import static org.box2d.box3d.box3d_h.b3World_CollideMover;

public final class ScratchPlaneResultFcn implements b3PlaneResultFcn.Function {

    private @Nullable volatile PlaneResultFcn function = null;

    private final MemorySegment segment;

    private final PlaneResult result = new PlaneResult();

    public ScratchPlaneResultFcn(Arena parent) {
        this.segment = b3PlaneResultFcn.allocate(this, parent);
    }

    public void invoke(
            MemorySegment worldId,
            MemorySegment origin,
            MemorySegment mover,
            MemorySegment filter,
            PlaneResultFcn fcn
    ) {
        this.function = fcn;

        try {
            b3World_CollideMover(
                    worldId,
                    origin,
                    mover,
                    filter,
                    this.segment,
                    MemorySegment.NULL
            );
        } finally {
            this.function = null;
        }
    }


    @Override
    public boolean apply(
            MemorySegment shapeId,
            MemorySegment plane,
            int planeCount,
            MemorySegment context
    ) {

        try {
            var function1 = this.function;
            Objects.requireNonNull(function1, "function not set");

            var packedID = PrimitiveMemOps.packID(shapeId);
            var shape = ShapeID.fromUnknown(packedID);

            var planes = new MemoryIterator<>(
                    this.result,
                    plane,
                    planeCount,
                    b3PlaneResult.sizeof(),
                    ScratchPlaneResultFcn::set
            );

            return function1.onPlane(
                    shape,
                    planes
            );
        } catch(Exception e) {
            B3JUtil.unhandledCallbackException(e);
            return false;
        }

    }

    private static void set(PlaneResult result, MemorySegment segment) {

        PrimitiveMemOps.setVec3(result.point(), segment, b3PlaneResult.point$offset());

        var planeSegment = b3PlaneResult.plane(segment);
        var normalSegment = b3Plane.normal(planeSegment);

        var plane = result.plane();
        plane.normalX = b3Vec3.x(normalSegment);
        plane.normalY = b3Vec3.y(normalSegment);
        plane.normalZ = b3Vec3.z(normalSegment);
        plane.offset = b3Plane.offset(planeSegment);


    }

}

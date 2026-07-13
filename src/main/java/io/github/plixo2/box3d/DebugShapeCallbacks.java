package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.AllocatedShapeCallbacks;
import lombok.Getter;
import org.box2d.box3d.*;

import java.lang.foreign.MemorySegment;

/// Abstraction for both `b3CreateDebugShapeCallback` and `b3DestroyDebugShapeCallback`
public abstract class DebugShapeCallbacks<T> {

    @Getter
    private final UserData.OfShape<T> userShapes;

    public DebugShapeCallbacks(UserData.OfShape<T> userShapes) {
        this.userShapes = userShapes;
    }

    /// b3CreateDebugShapeCallback
    protected abstract T create(ShapeID shapeID, ShapeType.Shape shape);

    /// b3DestroyDebugShapeCallback
    protected abstract void delete(T object);


    private void delete(MemorySegment ptr, MemorySegment userContext) {
        var shape = this.userShapes.remove(ptr.address());
        if (shape != null) {
            this.delete(shape);
        }
    }

    private MemorySegment create(MemorySegment debugShape, MemorySegment userContext) {
        var shapeID = ShapeID.of(b3DebugShape.shapeId(debugShape));
        var type = ShapeType.fromCode(b3DebugShape.type(debugShape));

        ShapeType.Shape shape = switch (type) {
            case CAPSULE -> {
                var capsule = b3DebugShape.capsule(debugShape);
                var center1 = b3Capsule.center1(capsule);
                var center2 = b3Capsule.center2(capsule);
                var radius = b3Capsule.radius(capsule);

                var x1 = b3Vec3.x(center1);
                var y1 = b3Vec3.y(center1);
                var z1 = b3Vec3.z(center1);
                var x2 = b3Vec3.x(center2);
                var y2 = b3Vec3.y(center2);
                var z2 = b3Vec3.z(center2);

                yield new Capsule(radius, x1, y1, z1, x2, y2, z2);
            }
            case COMPOUND -> {
                throw new RuntimeException("Not implemented");
            }
            case HEIGHT -> {
                var heightField = b3DebugShape.heightField(debugShape);
                yield new HeightFieldData(null, null, heightField);
            }
            case HULL -> {
                var hull = b3DebugShape.hull(debugShape);
                yield new HullData(hull);
            }
            case MESH -> {
                var mesh = b3DebugShape.mesh(debugShape);
                var scale = b3Mesh.scale(mesh);

                var scaleX = b3Vec3.x(scale);
                var scaleY = b3Vec3.y(scale);
                var scaleZ = b3Vec3.z(scale);

                MeshData data = new MeshData(null, null, b3Mesh.data(mesh));
                yield new Mesh(data, scaleX, scaleY, scaleZ);
            }
            case SPHERE -> {
                var sphere = b3DebugShape.sphere(debugShape);
                var center = b3Sphere.center(sphere);
                var radius = b3Sphere.radius(sphere);

                var x = b3Vec3.x(center);
                var y = b3Vec3.y(center);
                var z = b3Vec3.z(center);

                yield new Sphere(radius, x, y, z);
            }
        };

        var object = this.create(shapeID, shape);
        this.userShapes.put(shapeID, object);

        return MemorySegment.ofAddress(shapeID.packedID());

    }

    AllocatedShapeCallbacks createCallbacks() {
        return AllocatedShapeCallbacks.createCallbacks(this, this::create, this::delete);
    }

}

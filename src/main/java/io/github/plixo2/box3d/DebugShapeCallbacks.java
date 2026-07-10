package io.github.plixo2.box3d;


import org.box2d.box3d.*;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public abstract class DebugShapeCallbacks<T> {

    private final LongObjectHashMap<T> objects = new LongObjectHashMap<>();
    private long counter = 1;

    public DebugShapeCallbacks() {}

    protected abstract T create(ShapeID shapeID, ShapeType.Shape shape);
    protected abstract void delete(T object);

    public @Nullable T get(long id) {
        return this.objects.get(id);
    }

    private void delete(MemorySegment ptr, MemorySegment userContext) {
        var shape = this.objects.remove(ptr.address());
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
        this.objects.put(this.counter, object);
        return MemorySegment.ofAddress(this.counter++);

    }


    static final class Allocated {
        private DebugShapeCallbacks<?> collection; // keep alive
        private Arena arena;

        MemorySegment creation;
        MemorySegment deletion;

        Allocated(DebugShapeCallbacks<?> collection) {
            this.collection = collection;
            this.arena = Arena.ofConfined();
            this.creation = b3CreateDebugShapeCallback.allocate(collection::create, this.arena);
            this.deletion = b3DestroyDebugShapeCallback.allocate(collection::delete, this.arena);
        }

        void close() {
            this.arena.close();
            this.arena = null;
            this.collection = null;
            this.creation = null;
            this.deletion = null;
        }
    }


}

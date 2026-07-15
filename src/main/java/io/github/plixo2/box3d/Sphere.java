package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import org.box2d.box3d.b3Sphere;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public non-sealed class Sphere implements ShapeType.Shape {

    public float radius;

    public float x;
    public float y;
    public float z;

    public Sphere() {

    }

    public Sphere(float radius) {
        this.radius = radius;
    }

    public Sphere(float radius, float z, float y, float x) {
        this.radius = radius;
        this.z = z;
        this.y = y;
        this.x = x;
    }

    public Sphere(float radius, Vector3f position) {
        this.radius = radius;
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }


    public MemorySegment create(SegmentAllocator arena) {
        var segment = b3Sphere.allocate(arena);
        PrimitiveMemOps.putVec3(b3Sphere.center(segment), this.x, this.y, this.z);
        b3Sphere.radius(segment, this.radius);
        return segment;
    }

    Sphere set(MemorySegment segment) {
        var center = b3Sphere.center(segment);
        this.x = PrimitiveMemOps.getVec3X(center);
        this.y = PrimitiveMemOps.getVec3Y(center);
        this.z = PrimitiveMemOps.getVec3Z(center);
        this.radius = b3Sphere.radius(segment);
        return this;
    }
}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import org.box2d.box3d.b3Capsule;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public non-sealed class Capsule implements ShapeType.Shape {

    public float radius;

    public float x1;
    public float y1;
    public float z1;

    public float x2;
    public float y2;
    public float z2;

    public Capsule() {

    }

    public Capsule(float radius, float x1, float y1, float z1, float x2, float y2, float z2) {
        this.radius = radius;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public Capsule(float radius, Vector3f center1, Vector3f center2) {
        this.radius = radius;
        this.x1 = center1.x;
        this.y1 = center1.y;
        this.z1 = center1.z;
        this.x2 = center2.x;
        this.y2 = center2.y;
        this.z2 = center2.z;
    }

    public Capsule(Capsule other) {
        this.radius = other.radius;
        this.x1 = other.x1;
        this.y1 = other.y1;
        this.z1 = other.z1;
        this.x2 = other.x2;
        this.y2 = other.y2;
        this.z2 = other.z2;
    }


    public MemorySegment create(SegmentAllocator arena) {
        var segment = b3Capsule.allocate(arena);
        PrimitiveMemOps.putVec3(b3Capsule.center1(segment), this.x1, this.y1, this.z1);
        PrimitiveMemOps.putVec3(b3Capsule.center2(segment), this.x2, this.y2, this.z2);
        b3Capsule.radius(segment, this.radius);
        return segment;
    }

    Capsule set(MemorySegment segment) {
        var center1 = b3Capsule.center1(segment);
        var center2 = b3Capsule.center2(segment);
        this.x1 = PrimitiveMemOps.getVec3X(center1);
        this.y1 = PrimitiveMemOps.getVec3Y(center1);
        this.z1 = PrimitiveMemOps.getVec3Z(center1);
        this.x2 = PrimitiveMemOps.getVec3X(center2);
        this.y2 = PrimitiveMemOps.getVec3Y(center2);
        this.z2 = PrimitiveMemOps.getVec3Z(center2);
        this.radius = b3Capsule.radius(segment);
        return this;
    }

}

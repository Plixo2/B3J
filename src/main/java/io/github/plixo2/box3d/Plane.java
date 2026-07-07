package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitveMemOps;
import org.box2d.box3d.b3Plane;
import org.box2d.box3d.b3Vec3;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

public class Plane {

    public float offset;

    public float normalX;
    public float normalY;
    public float normalZ;

    public Plane(float offset, float normalX, float normalY, float normalZ) {
        this.offset = offset;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
    }

    public Plane(float offset, Vector3f normal) {
        this.offset = offset;
        this.normalX = normal.x;
        this.normalY = normal.y;
        this.normalZ = normal.z;
    }

    public Plane(Plane other) {
        this.offset = other.offset;
        this.normalX = other.normalX;
        this.normalY = other.normalY;
        this.normalZ = other.normalZ;
    }

    Plane set(MemorySegment segment) {
        var normal = b3Plane.normal(segment);
        this.normalX = b3Vec3.x(normal);
        this.normalY = b3Vec3.y(normal);
        this.normalZ = b3Vec3.z(normal);
        this.offset = b3Plane.offset(segment);
        return this;
    }
    void put(MemorySegment segment) {
        PrimitveMemOps.putVec3(b3Plane.normal(segment), this.normalX, this.normalY, this.normalZ);
        b3Plane.offset(segment, this.offset);
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.ToString;
import org.box2d.box3d.b3MassData;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@ToString
public class MassData {

    public float mass;
    public final Vector3f center = new Vector3f();
    public final Matrix3f inertia = new Matrix3f();

    public MassData() {

    }

    public MassData(MassData other) {
        this.mass = other.mass;
        this.center.set(other.center);
        this.inertia.set(other.inertia);
    }

    MassData set(MemorySegment segment) {
        this.mass = b3MassData.mass(segment);
        PrimitiveMemOps.setVec3(this.center, b3MassData.center(segment));
        PrimitiveMemOps.setMat3(this.inertia, b3MassData.inertia(segment));
        return this;
    }

    void put(MemorySegment segment) {
        b3MassData.mass(segment, this.mass);
        PrimitiveMemOps.putVec3(b3MassData.center(segment), this.center);
        PrimitiveMemOps.putMat3(b3MassData.inertia(segment), this.inertia);
    }
}

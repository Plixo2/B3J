package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitveMemOps;
import org.box2d.box3d.b3AABB;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

public class AABB {
    public Vector3f lowerBound = new Vector3f();
    public Vector3f upperBound = new Vector3f();

    public AABB() {

    }

    public AABB(Vector3f lowerBound, Vector3f upperBound) {
        this.lowerBound.set(lowerBound);
        this.upperBound.set(upperBound);
    }

    AABB set(MemorySegment segment) {
        PrimitveMemOps.setVec3(this.lowerBound, b3AABB.lowerBound(segment));
        PrimitveMemOps.setVec3(this.upperBound, b3AABB.upperBound(segment));
        return this;
    }

    void put(MemorySegment segment) {
        PrimitveMemOps.putVec3(b3AABB.lowerBound(segment), this.lowerBound);
        PrimitveMemOps.putVec3(b3AABB.upperBound(segment), this.upperBound);
    }

}

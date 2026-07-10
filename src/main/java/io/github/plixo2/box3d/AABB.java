package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
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

    public AABB(
            float lowerX, float lowerY, float lowerZ,
            float upperX, float upperY, float upperZ
    ) {
        this.lowerBound.set(lowerX, lowerY, lowerZ);
        this.upperBound.set(upperX, upperY, upperZ);
    }

    AABB set(MemorySegment segment) {
        return set(segment, 0);
    }

    AABB set(MemorySegment segment, long offset) {
        var lowerOffset = offset + b3AABB.lowerBound$offset();
        var upperOffset = offset + b3AABB.upperBound$offset();
        PrimitiveMemOps.setVec3(this.lowerBound, segment, lowerOffset);
        PrimitiveMemOps.setVec3(this.upperBound, segment, upperOffset);
        return this;
    }

    void put(MemorySegment segment) {
        PrimitiveMemOps.putVec3(b3AABB.lowerBound(segment), this.lowerBound);
        PrimitiveMemOps.putVec3(b3AABB.upperBound(segment), this.upperBound);
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.ToString;
import org.box2d.box3d.b3CastOutput;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@Getter
public class CastOutput {

    private final Vector3f normal = new Vector3f();
    private final Vector3f point = new Vector3f();
    private float fraction;
    private int iterations;
    private int triangleIndex;
    private int childIndex;
    private int materialIndex;
    private boolean hit;

    public CastOutput() {

    }

    public CastOutput(CastOutput other) {
        this.normal.set(other.normal);
        this.point.set(other.point);
        this.fraction = other.fraction;
        this.iterations = other.iterations;
        this.triangleIndex = other.triangleIndex;
        this.childIndex = other.childIndex;
        this.materialIndex = other.materialIndex;
        this.hit = other.hit;
    }

    void setMiss() {
        this.hit = false;
    }

    void setOnHit(MemorySegment segment) {
        this.hit = true;
        PrimitiveMemOps.setVec3(this.normal, b3CastOutput.normal(segment));
        PrimitiveMemOps.setVec3(this.point, b3CastOutput.point(segment));
        this.fraction = b3CastOutput.fraction(segment);
        this.iterations = b3CastOutput.iterations(segment);
        this.triangleIndex = b3CastOutput.triangleIndex(segment);
        this.childIndex = b3CastOutput.childIndex(segment);
        this.materialIndex = b3CastOutput.materialIndex(segment);
    }

    static boolean hit(MemorySegment segment) {
        return b3CastOutput.hit(segment);
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.ToString;
import org.box2d.box3d.b3CastOutput;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@Getter
@ToString
public class CastOutput {

    private final Vector3f normal = new Vector3f();
    private final Vector3f point = new Vector3f();
    private float fraction;
    private int iterations;
    private int triangleIndex;
    private int childIndex;
    private int materialIndex;


    public CastOutput() {

    }

    void set(MemorySegment segment) {
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

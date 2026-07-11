package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import org.box2d.box3d.b3DistanceOutput;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@Getter
public class DistanceOutput {

    Vector3f pointA = new Vector3f();
    Vector3f pointB = new Vector3f();
    Vector3f normal = new Vector3f();
    float distance;
    int iterations;
    int simplexCount;

    public DistanceOutput() {

    }

    void set(MemorySegment segment) {
        PrimitiveMemOps.setVec3(this.pointA, b3DistanceOutput.pointA(segment));
        PrimitiveMemOps.setVec3(this.pointB, b3DistanceOutput.pointB(segment));
        PrimitiveMemOps.setVec3(this.normal, b3DistanceOutput.normal(segment));
        this.distance = b3DistanceOutput.distance(segment);
        this.iterations = b3DistanceOutput.iterations(segment);
        this.simplexCount = b3DistanceOutput.simplexCount(segment);
    }
}

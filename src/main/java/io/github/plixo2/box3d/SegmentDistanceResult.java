package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import org.box2d.box3d.b3SegmentDistanceResult;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@Getter
public class SegmentDistanceResult {

    private final Vector3f point1 = new Vector3f();
    private float fraction1;
    private final Vector3f point2 = new Vector3f();
    private float fraction2;

    public SegmentDistanceResult() {

    }

    public SegmentDistanceResult(SegmentDistanceResult other) {
        this.point1.set(other.point1);
        this.point2.set(other.point2);
        this.fraction1 = other.fraction1;
        this.fraction2 = other.fraction2;
    }

    void set(MemorySegment segment) {
        PrimitiveMemOps.setVec3(this.point1, b3SegmentDistanceResult.point1(segment));
        this.fraction1 = b3SegmentDistanceResult.fraction1(segment);
        PrimitiveMemOps.setVec3(this.point2, b3SegmentDistanceResult.point2(segment));
        this.fraction2 = b3SegmentDistanceResult.fraction2(segment);
    }

}

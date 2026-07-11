package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.U64;
import lombok.Getter;
import org.box2d.box3d.b3BodyCastResult;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@Getter
public class BodyCastResult {

    private ShapeID shapeID = ShapeID.NULL_ID;
    private final Vector3f point = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private float fraction;
    private int triangleIndex;
    private @U64 long userMaterialId;
    private int iterations;
    private boolean hit;

    public BodyCastResult() {

    }

    public BodyCastResult(BodyCastResult other) {
        this.shapeID = other.shapeID;
        this.point.set(other.point);
        this.normal.set(other.normal);
        this.fraction = other.fraction;
        this.triangleIndex = other.triangleIndex;
        this.userMaterialId = other.userMaterialId;
        this.iterations = other.iterations;
        this.hit = other.hit;
    }

    void setMiss() {
        this.hit = false;
        this.shapeID = ShapeID.NULL_ID;
    }

    void setOnHit(MemorySegment segment) {
        this.hit = true;
        this.shapeID = ShapeID.of(b3BodyCastResult.shapeId(segment));
        PrimitiveMemOps.setVec3(this.point, b3BodyCastResult.point(segment));
        PrimitiveMemOps.setVec3(this.normal, b3BodyCastResult.normal(segment));
        this.fraction = b3BodyCastResult.fraction(segment);
        this.triangleIndex = b3BodyCastResult.triangleIndex(segment);
        this.userMaterialId = b3BodyCastResult.userMaterialId(segment);
        this.iterations = b3BodyCastResult.iterations(segment);
    }

    static boolean hit(MemorySegment segment) {
        return b3BodyCastResult.hit(segment);
    }

}

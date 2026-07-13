package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.Unsigned;
import lombok.Getter;
import org.box2d.box3d.b3RayResult;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;


@Getter
public class RayResult {

    private ShapeID shapeID = ShapeID.NULL_ID;
    private final Vector3f point = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private @Unsigned long userMaterialId;
    private float fraction;
    private int triangleIndex;
    private int childIndex;
    private int leafVisits;
    private int nodeVisits;
    private boolean hit;

    public RayResult() {

    }

    public RayResult(RayResult other) {
        this.shapeID = other.shapeID;
        this.point.set(other.point);
        this.normal.set(other.normal);
        this.userMaterialId = other.userMaterialId;
        this.fraction = other.fraction;
        this.triangleIndex = other.triangleIndex;
        this.childIndex = other.childIndex;
        this.leafVisits = other.leafVisits;
        this.nodeVisits = other.nodeVisits;
        this.hit = other.hit;
    }


    public TreeStats stats(TreeStats dest) {
        dest.leafVisits = this.leafVisits;
        dest.nodeVisits = this.nodeVisits;
        return dest;
    }

    void setMiss() {
        this.hit = false;
        this.shapeID = ShapeID.NULL_ID;
    }

    void setOnHit(MemorySegment segment) {
        this.hit = true;
        this.shapeID = ShapeID.of(b3RayResult.shapeId(segment));
        PrimitiveMemOps.setVec3(this.point, b3RayResult.point(segment));
        PrimitiveMemOps.setVec3(this.normal, b3RayResult.normal(segment));
        this.userMaterialId = b3RayResult.userMaterialId(segment);
        this.fraction = b3RayResult.fraction(segment);
        this.triangleIndex = b3RayResult.triangleIndex(segment);
        this.childIndex = b3RayResult.childIndex(segment);
        this.leafVisits = b3RayResult.leafVisits(segment);
        this.nodeVisits = b3RayResult.nodeVisits(segment);
    }


    static boolean hit(MemorySegment segment) {
        return b3RayResult.hit(segment);
    }


}

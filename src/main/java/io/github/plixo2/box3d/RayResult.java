package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.U64;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.box2d.box3d.b3RayResult;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;


@Getter
@ToString
public class RayResult {

    private ShapeID shapeID;
    private final Vector3f point = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private @U64 long userMaterialId;
    private float fraction;
    private int triangleIndex;
    private int childIndex;
    private int leafVisits;
    private int nodeVisits;

    public RayResult() {

    }


    public TreeStats stats(TreeStats in) {
        in.leafVisits = this.leafVisits;
        in.nodeVisits = this.nodeVisits;
        return in;
    }

    void set(MemorySegment segment) {
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

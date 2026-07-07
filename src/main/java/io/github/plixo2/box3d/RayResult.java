package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitveMemOps;
import io.github.plixo2.box3d.internal.U64;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.box2d.box3d.b3RayResult;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@Getter
@ToString
@EqualsAndHashCode
public class RayResult {

    private final ShapeID shapeID;
    private final Vector3f point;
    private final Vector3f normal;
    private final @U64 long userMaterialId;
    private final float fraction;
    private final int triangleIndex;
    private final int childIndex;
    private final int leafVisits;
    private final int nodeVisits;

    RayResult(
            MemorySegment segment
    ) {
        this.shapeID = ShapeID.of(b3RayResult.shapeId(segment));
        this.point = new Vector3f();
        PrimitveMemOps.setVec3(this.point, b3RayResult.point(segment));
        this.normal = new Vector3f();
        PrimitveMemOps.setVec3(this.normal, b3RayResult.normal(segment));
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

    public TreeStats stats(TreeStats in) {
        in.leafVisits = this.leafVisits;
        in.nodeVisits = this.nodeVisits;
        return in;
    }

}

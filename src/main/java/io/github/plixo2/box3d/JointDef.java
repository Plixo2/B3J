package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3JointDef;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public class JointDef {

    private BodyID bodyIdA;
    private BodyID bodyIdB;
    private Matrix4f localFrameA = new Matrix4f();
    private Matrix4f localFrameB = new Matrix4f();
    private float forceThreshold;
    private float torqueThreshold;
    private float constraintHertz;
    private float constraintDampingRatio;
    private float drawScale;
    private boolean collideConnected;


    /// from `b3DefaultJointDef` (internal)
    public JointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        this.bodyIdA = bodyIdA;
        this.bodyIdB = bodyIdB;

        this.forceThreshold = Float.MAX_VALUE;
        this.torqueThreshold = Float.MAX_VALUE;
        this.constraintHertz = 60.0f;
        this.constraintDampingRatio = 2.0f;
        this.drawScale = B3.lengthUnitsPerMeter();
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3JointDef.allocate(arena);

        PrimitiveMemOps.putBodyID(b3JointDef.bodyIdA(segment), this.bodyIdA.packedID());
        PrimitiveMemOps.putBodyID(b3JointDef.bodyIdB(segment), this.bodyIdB.packedID());

        PrimitiveMemOps.putTransform(b3JointDef.localFrameA(segment), tempQuat, this.localFrameA);
        PrimitiveMemOps.putTransform(b3JointDef.localFrameB(segment), tempQuat, this.localFrameB);

        b3JointDef.forceThreshold(segment, this.forceThreshold);
        b3JointDef.torqueThreshold(segment, this.torqueThreshold);
        b3JointDef.constraintHertz(segment, this.constraintHertz);
        b3JointDef.constraintDampingRatio(segment, this.constraintDampingRatio);
        b3JointDef.drawScale(segment, this.drawScale);
        b3JointDef.collideConnected(segment, this.collideConnected);
        b3JointDef.internalValue(segment, B3.SECRET_COOKIE);

        return segment;
    }

}

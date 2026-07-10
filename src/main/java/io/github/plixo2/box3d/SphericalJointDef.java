package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3SphericalJointDef;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class SphericalJointDef extends AbstractJointDef<JointType.Spherical> {

    private boolean enableSpring;
    private float hertz;
    private float dampingRatio;
    private Quaternionf targetRotation = new Quaternionf();
    private boolean enableConeLimit;
    private float coneAngle;
    private boolean enableTwistLimit;
    private float lowerTwistAngle;
    private float upperTwistAngle;
    private boolean enableMotor;
    private float maxMotorTorque;
    private Vector3f motorVelocity = new Vector3f();

    /// @api b3DefaultSphericalJointDef
    public SphericalJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.SPHERICAL, bodyIdA, bodyIdB);
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3SphericalJointDef.allocate(arena);

        b3SphericalJointDef.base(segment, this.base.create(tempQuat, arena));
        b3SphericalJointDef.enableSpring(segment, this.enableSpring);
        b3SphericalJointDef.hertz(segment, this.hertz);
        b3SphericalJointDef.dampingRatio(segment, this.dampingRatio);
        PrimitiveMemOps.putQuat(b3SphericalJointDef.targetRotation(segment), this.targetRotation);
        b3SphericalJointDef.enableConeLimit(segment, this.enableConeLimit);
        b3SphericalJointDef.coneAngle(segment, this.coneAngle);
        b3SphericalJointDef.enableTwistLimit(segment, this.enableTwistLimit);
        b3SphericalJointDef.lowerTwistAngle(segment, this.lowerTwistAngle);
        b3SphericalJointDef.upperTwistAngle(segment, this.upperTwistAngle);
        b3SphericalJointDef.enableMotor(segment, this.enableMotor);
        b3SphericalJointDef.maxMotorTorque(segment, this.maxMotorTorque);
        PrimitiveMemOps.putVec3(b3SphericalJointDef.motorVelocity(segment), this.motorVelocity);

        return segment;
    }
}

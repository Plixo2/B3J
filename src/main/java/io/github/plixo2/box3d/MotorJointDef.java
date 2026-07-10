package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3MotorJointDef;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class MotorJointDef extends AbstractJointDef<JointType.Motor> {

    private Vector3f linearVelocity = new Vector3f();
    private float maxVelocityForce;
    private Vector3f angularVelocity = new Vector3f();
    private float maxVelocityTorque;
    private float linearHertz;
    private float linearDampingRatio;
    private float maxSpringForce;
    private float angularHertz;
    private float angularDampingRatio;
    private float maxSpringTorque;

    /// @api b3DefaultMotorJointDef
    public MotorJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.MOTOR, bodyIdA, bodyIdB);
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3MotorJointDef.allocate(arena);

        b3MotorJointDef.base(segment, this.base.create(tempQuat, arena));
        PrimitiveMemOps.putVec3(b3MotorJointDef.linearVelocity(segment), this.linearVelocity);
        b3MotorJointDef.maxVelocityForce(segment, this.maxVelocityForce);
        PrimitiveMemOps.putVec3(b3MotorJointDef.angularVelocity(segment), this.angularVelocity);
        b3MotorJointDef.maxVelocityTorque(segment, this.maxVelocityTorque);
        b3MotorJointDef.linearHertz(segment, this.linearHertz);
        b3MotorJointDef.linearDampingRatio(segment, this.linearDampingRatio);
        b3MotorJointDef.maxSpringForce(segment, this.maxSpringForce);
        b3MotorJointDef.angularHertz(segment, this.angularHertz);
        b3MotorJointDef.angularDampingRatio(segment, this.angularDampingRatio);
        b3MotorJointDef.maxSpringTorque(segment, this.maxSpringTorque);

        return segment;
    }
}

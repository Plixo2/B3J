package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3WheelJointDef;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class WheelJointDef extends AbstractJointDef<JointType.Wheel> {

    private boolean enableSuspensionSpring;
    private float suspensionHertz;
    private float suspensionDampingRatio;
    private boolean enableSuspensionLimit;
    private float lowerSuspensionLimit;
    private float upperSuspensionLimit;
    private boolean enableSpinMotor;
    private float maxSpinTorque;
    private float spinSpeed;
    private boolean enableSteering;
    private float steeringHertz;
    private float steeringDampingRatio;
    private float targetSteeringAngle;
    private float maxSteeringTorque;
    private boolean enableSteeringLimit;
    private float lowerSteeringLimit;
    private float upperSteeringLimit;

    /// @api b3DefaultWheelJointDef
    public WheelJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.WHEEL, bodyIdA, bodyIdB);
        this.enableSuspensionSpring = true;
        this.suspensionHertz = 1.0f;
        this.suspensionDampingRatio = 0.7f;
        this.steeringHertz = 1.0f;
        this.steeringDampingRatio = 0.7f;
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3WheelJointDef.allocate(arena);

        b3WheelJointDef.base(segment, this.base.create(tempQuat, arena));
        b3WheelJointDef.enableSuspensionSpring(segment, this.enableSuspensionSpring);
        b3WheelJointDef.suspensionHertz(segment, this.suspensionHertz);
        b3WheelJointDef.suspensionDampingRatio(segment, this.suspensionDampingRatio);
        b3WheelJointDef.enableSuspensionLimit(segment, this.enableSuspensionLimit);
        b3WheelJointDef.lowerSuspensionLimit(segment, this.lowerSuspensionLimit);
        b3WheelJointDef.upperSuspensionLimit(segment, this.upperSuspensionLimit);
        b3WheelJointDef.enableSpinMotor(segment, this.enableSpinMotor);
        b3WheelJointDef.maxSpinTorque(segment, this.maxSpinTorque);
        b3WheelJointDef.spinSpeed(segment, this.spinSpeed);
        b3WheelJointDef.enableSteering(segment, this.enableSteering);
        b3WheelJointDef.steeringHertz(segment, this.steeringHertz);
        b3WheelJointDef.steeringDampingRatio(segment, this.steeringDampingRatio);
        b3WheelJointDef.targetSteeringAngle(segment, this.targetSteeringAngle);
        b3WheelJointDef.maxSteeringTorque(segment, this.maxSteeringTorque);
        b3WheelJointDef.enableSteeringLimit(segment, this.enableSteeringLimit);
        b3WheelJointDef.lowerSteeringLimit(segment, this.lowerSteeringLimit);
        b3WheelJointDef.upperSteeringLimit(segment, this.upperSteeringLimit);

        return segment;
    }
}

package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3RevoluteJointDef;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class RevoluteJointDef extends AbstractJointDef<JointType.Revolute> {

    private float targetAngle;
    private boolean enableSpring;
    private float hertz;
    private float dampingRatio;
    private boolean enableLimit;
    private float lowerAngle;
    private float upperAngle;
    private boolean enableMotor;
    private float maxMotorTorque;
    private float motorSpeed;

    /// @api b3DefaultRevoluteJointDef
    public RevoluteJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.REVOLUTE, bodyIdA, bodyIdB);
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3RevoluteJointDef.allocate(arena);

        b3RevoluteJointDef.base(segment, this.base.create(tempQuat, arena));
        b3RevoluteJointDef.targetAngle(segment, this.targetAngle);
        b3RevoluteJointDef.enableSpring(segment, this.enableSpring);
        b3RevoluteJointDef.hertz(segment, this.hertz);
        b3RevoluteJointDef.dampingRatio(segment, this.dampingRatio);
        b3RevoluteJointDef.enableLimit(segment, this.enableLimit);
        b3RevoluteJointDef.lowerAngle(segment, this.lowerAngle);
        b3RevoluteJointDef.upperAngle(segment, this.upperAngle);
        b3RevoluteJointDef.enableMotor(segment, this.enableMotor);
        b3RevoluteJointDef.maxMotorTorque(segment, this.maxMotorTorque);
        b3RevoluteJointDef.motorSpeed(segment, this.motorSpeed);

        return segment;
    }

}

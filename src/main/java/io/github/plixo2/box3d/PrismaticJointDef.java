package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3PrismaticJointDef;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class PrismaticJointDef extends AbstractJointDef<JointType.Prismatic> {

    private boolean enableSpring;
    private float hertz;
    private float dampingRatio;
    private float targetTranslation;
    private boolean enableLimit;
    private float lowerTranslation;
    private float upperTranslation;
    private boolean enableMotor;
    private float maxMotorForce;
    private float motorSpeed;

    /// @api b3DefaultPrismaticJointDef
    public PrismaticJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.PRISMATIC, bodyIdA, bodyIdB);
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3PrismaticJointDef.allocate(arena);

        b3PrismaticJointDef.base(segment, this.base.create(tempQuat, arena));
        b3PrismaticJointDef.enableSpring(segment, this.enableSpring);
        b3PrismaticJointDef.hertz(segment, this.hertz);
        b3PrismaticJointDef.dampingRatio(segment, this.dampingRatio);
        b3PrismaticJointDef.targetTranslation(segment, this.targetTranslation);
        b3PrismaticJointDef.enableLimit(segment, this.enableLimit);
        b3PrismaticJointDef.lowerTranslation(segment, this.lowerTranslation);
        b3PrismaticJointDef.upperTranslation(segment, this.upperTranslation);
        b3PrismaticJointDef.enableMotor(segment, this.enableMotor);
        b3PrismaticJointDef.maxMotorForce(segment, this.maxMotorForce);
        b3PrismaticJointDef.motorSpeed(segment, this.motorSpeed);

        return segment;
    }
}

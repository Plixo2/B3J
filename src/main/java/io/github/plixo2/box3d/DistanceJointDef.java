package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3DistanceJointDef;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class DistanceJointDef extends AbstractJointDef<JointType.Distance> {

    private float length;
    private boolean enableSpring;
    private float lowerSpringForce;
    private float upperSpringForce;
    private float hertz;
    private float dampingRatio;
    private boolean enableLimit;
    private float minLength;
    private float maxLength;
    private boolean enableMotor;
    private float maxMotorForce;
    private float motorSpeed;

    /// @api b3DefaultDistanceJointDef
    public DistanceJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.DISTANCE, bodyIdA, bodyIdB);
        this.lowerSpringForce = -Float.MAX_VALUE;
        this.upperSpringForce = Float.MAX_VALUE;
        this.length = 1.0f;
        this.maxLength = 1.0e5f * B3.lengthUnitsPerMeter();
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3DistanceJointDef.allocate(arena);

        b3DistanceJointDef.base(segment, this.base.create(tempQuat, arena));
        b3DistanceJointDef.length(segment, this.length);
        b3DistanceJointDef.enableSpring(segment, this.enableSpring);
        b3DistanceJointDef.lowerSpringForce(segment, this.lowerSpringForce);
        b3DistanceJointDef.upperSpringForce(segment, this.upperSpringForce);
        b3DistanceJointDef.hertz(segment, this.hertz);
        b3DistanceJointDef.dampingRatio(segment, this.dampingRatio);
        b3DistanceJointDef.enableLimit(segment, this.enableLimit);
        b3DistanceJointDef.minLength(segment, this.minLength);
        b3DistanceJointDef.maxLength(segment, this.maxLength);
        b3DistanceJointDef.enableMotor(segment, this.enableMotor);
        b3DistanceJointDef.maxMotorForce(segment, this.maxMotorForce);
        b3DistanceJointDef.motorSpeed(segment, this.motorSpeed);

        return segment;
    }
}

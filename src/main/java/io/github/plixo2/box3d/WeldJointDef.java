package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3WeldJointDef;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class WeldJointDef extends AbstractJointDef<JointType.Weld> {

    private float linearHertz;
    private float angularHertz;
    private float linearDampingRatio;
    private float angularDampingRatio;

    /// @api b3DefaultWeldJointDef
    public WeldJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.WELD, bodyIdA, bodyIdB);
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3WeldJointDef.allocate(arena);

        b3WeldJointDef.base(segment, this.base.create(tempQuat, arena));
        b3WeldJointDef.linearHertz(segment, this.linearHertz);
        b3WeldJointDef.angularHertz(segment, this.angularHertz);
        b3WeldJointDef.linearDampingRatio(segment, this.linearDampingRatio);
        b3WeldJointDef.angularDampingRatio(segment, this.angularDampingRatio);

        return segment;
    }
}

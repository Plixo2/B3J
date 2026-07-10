package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3ParallelJointDef;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class ParallelJointDef extends AbstractJointDef<JointType.Parallel> {

    private float hertz;
    private float dampingRatio;
    private float maxTorque;

    /// @api b3DefaultParallelJointDef
    public ParallelJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.PARALLEL, bodyIdA, bodyIdB);
        this.hertz = 1.0f;
        this.dampingRatio = 1.0f;
        this.maxTorque = Float.MAX_VALUE;
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3ParallelJointDef.allocate(arena);

        b3ParallelJointDef.base(segment, this.base.create(tempQuat, arena));
        b3ParallelJointDef.hertz(segment, this.hertz);
        b3ParallelJointDef.dampingRatio(segment, this.dampingRatio);
        b3ParallelJointDef.maxTorque(segment, this.maxTorque);

        return segment;
    }
}

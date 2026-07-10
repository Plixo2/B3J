package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3FilterJointDef;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public final class FilterJointDef extends AbstractJointDef<JointType.Filter> {

    /// @api b3DefaultFilterJointDef
    public FilterJointDef(
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        super(JointType.FILTER, bodyIdA, bodyIdB);
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        var segment = b3FilterJointDef.allocate(arena);

        b3FilterJointDef.base(segment, this.base.create(tempQuat, arena));

        return segment;
    }
}

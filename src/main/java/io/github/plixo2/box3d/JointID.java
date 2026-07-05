package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U16;
import org.box2d.box3d.b3JointId;

import java.lang.foreign.MemorySegment;

public record JointID(int index1, @U16 int world0, @U16 int generation) {
    public static final JointID NULL_ID = new JointID(0, 0, 0);

    static JointID of(MemorySegment segment) {
        var index1 = b3JointId.index1(segment);
        var world0 = b3JointId.world0(segment);
        var generation = b3JointId.generation(segment);
        return new JointID(index1, Short.toUnsignedInt(world0), Short.toUnsignedInt(generation));
    }

}

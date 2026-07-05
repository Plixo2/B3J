package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U16;
import org.box2d.box3d.b3BodyId;

import java.lang.foreign.MemorySegment;

public record BodyID(int index1, @U16 int world0, @U16 int generation) {
    public static final BodyID NULL_ID = new BodyID(0, 0, 0);

    static BodyID of(MemorySegment segment) {
        var index1 = b3BodyId.index1(segment);
        var world0 = b3BodyId.world0(segment);
        var generation = b3BodyId.generation(segment);
        return new BodyID(index1, Short.toUnsignedInt(world0), Short.toUnsignedInt(generation));
    }

}

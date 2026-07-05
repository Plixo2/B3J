package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U16;
import org.box2d.box3d.b3ShapeId;

import java.lang.foreign.MemorySegment;

public record ShapeID(int index1, @U16 int world0, @U16 int generation) {
    public static final ShapeID NULL_ID = new ShapeID(0, 0, 0);

    static ShapeID of(MemorySegment segment) {
        var index1 = b3ShapeId.index1(segment);
        var world0 = b3ShapeId.world0(segment);
        var generation = b3ShapeId.generation(segment);
        return new ShapeID(index1, Short.toUnsignedInt(world0), Short.toUnsignedInt(generation));
    }
}

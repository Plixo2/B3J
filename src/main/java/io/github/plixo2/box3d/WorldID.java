package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U16;
import org.box2d.box3d.b3WorldId;

import java.lang.foreign.MemorySegment;

public record WorldID(@U16 int index1, @U16 int generation) implements AutoCloseable {


    static WorldID of(MemorySegment segment) {
        var index1 = b3WorldId.index1(segment);
        var generation = b3WorldId.generation(segment);
        return new WorldID(Short.toUnsignedInt(index1), Short.toUnsignedInt(generation));
    }

    @Override
    public void close() {
        B3.get().destroyWorld(this);
    }
}

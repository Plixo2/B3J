package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U8;
import org.box2d.box3d.b3HullFace;

import java.lang.foreign.MemorySegment;

import static io.github.plixo2.box3d.internal.Internal.assertU8;

public class HullFace {

    public @U8 int edge;

    public HullFace(@U8 int edge) {
        assertU8(edge, "edge");
        this.edge = edge;
    }

    public HullFace(HullFace other) {
        this.edge = other.edge;
    }

    HullFace set(MemorySegment segment) {
        this.edge = Byte.toUnsignedInt(b3HullFace.edge(segment));
        return this;
    }

}

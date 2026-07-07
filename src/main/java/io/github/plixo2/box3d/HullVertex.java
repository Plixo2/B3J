package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U8;
import org.box2d.box3d.b3HullVertex;

import java.lang.foreign.MemorySegment;

import static io.github.plixo2.box3d.internal.Internal.assertU8;

public class HullVertex {
    public @U8 int edge;

    public HullVertex(@U8 int edge) {
        assertU8(edge, "edge");
        this.edge = edge;
    }

    public HullVertex(HullVertex other) {
        this.edge = other.edge;
    }

    HullVertex set(MemorySegment segment) {
        this.edge = Byte.toUnsignedInt(b3HullVertex.edge(segment));
        return this;
    }

}

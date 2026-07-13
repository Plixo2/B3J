package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Unsigned;
import org.box2d.box3d.b3HullVertex;

import java.lang.foreign.MemorySegment;



public class HullVertex {
    public @Unsigned byte edge;

    public HullVertex(@Unsigned byte edge) {
        this.edge = edge;
    }

    public HullVertex() {
        this.edge = 0;
    }

    public HullVertex(HullVertex other) {
        this.edge = other.edge;
    }

    void set(MemorySegment segment) {
        this.edge = b3HullVertex.edge(segment);
    }

}

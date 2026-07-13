package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.Unsigned;
import org.box2d.box3d.b3HullFace;

import java.lang.foreign.MemorySegment;


public class HullFace {

    public @Unsigned byte edge;

    public HullFace(@Unsigned byte edge) {
        this.edge = edge;
    }

    public HullFace() {
        this.edge = 0;
    }

    public HullFace(HullFace other) {
        this.edge = other.edge;
    }

    void set(MemorySegment segment) {
        this.edge = b3HullFace.edge(segment);
    }

}

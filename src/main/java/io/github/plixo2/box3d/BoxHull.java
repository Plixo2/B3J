package io.github.plixo2.box3d;

import org.box2d.box3d.b3BoxHull;

import java.lang.foreign.MemorySegment;

public class BoxHull {

    final MemorySegment segment;

    BoxHull(
            MemorySegment segment
    ) {
        this.segment = segment;
    }

    public HullData base() {
        return new HullData(b3BoxHull.base(this.segment));
    }

}

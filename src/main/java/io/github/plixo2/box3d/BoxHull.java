package io.github.plixo2.box3d;

import lombok.Getter;
import org.box2d.box3d.b3BoxHull;

import java.lang.foreign.MemorySegment;

public class BoxHull {

    final MemorySegment segment;

    @Getter
    private final HullData base;

    BoxHull(
            MemorySegment segment
    ) {
        this.segment = segment;
        this.base = new HullData(b3BoxHull.base(segment));
    }


}

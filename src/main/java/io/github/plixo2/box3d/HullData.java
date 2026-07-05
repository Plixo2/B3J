package io.github.plixo2.box3d;

import java.lang.foreign.MemorySegment;

public class HullData {

    final MemorySegment segment;


    HullData(
            MemorySegment segment
    ) {
        this.segment = segment;
    }



}

package io.github.plixo2.box3d;

import java.lang.foreign.MemorySegment;


public interface AllocNFreeFcn {

    MemorySegment alloc(int size, int alignment);

    void free(MemorySegment mem);

}

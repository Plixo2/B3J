package io.github.plixo2.box3d.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class ConstAllocator extends SlicingAllocator {

    public ConstAllocator(Arena parent, long size) {
        super(parent.allocate(size));
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        this.reset();
        return super.allocate(byteSize, byteAlignment);
    }

}

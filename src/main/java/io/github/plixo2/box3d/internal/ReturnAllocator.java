package io.github.plixo2.box3d.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public final class ReturnAllocator implements SegmentAllocator  {

    private final MemorySegment segment;
    private final long size;

    public ReturnAllocator(Arena parent, long size) {
        this.segment = parent.allocate(size, 64);
        this.size = size;
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        if (byteSize > this.size) {
            throw new IllegalArgumentException("Requested size exceeds allocated size of " + this.size);
        }
        return this.segment;
    }

}

package io.github.plixo2.box3d.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public class ConstArena implements SegmentAllocator {
    private final long size;
    private final MemorySegment segment;
    private final long address;

    public ConstArena(Arena parent, long size) {
        this.size = size;
        this.segment = parent.allocate(size);
        this.address = this.segment.address();
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {

        var offset = alignedOffset(byteAlignment);
        var end = offset + byteSize;
        if (end > this.size || end < offset) {
            throw new IllegalArgumentException("Requested size exceeds the allocated size of ConstArena");
        }

        return this.segment.asSlice(offset, byteSize);
    }

    private long alignedOffset(long byteAlignment) {
        var remainder = Math.floorMod(this.address, byteAlignment);
        return Math.floorMod(byteAlignment - remainder, byteAlignment);
    }
}

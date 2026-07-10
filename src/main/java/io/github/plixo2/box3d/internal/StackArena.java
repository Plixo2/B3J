package io.github.plixo2.box3d.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public final class StackArena implements SegmentAllocator, AutoCloseable {
    private final long limit;
    private final MemorySegment segment;
    private final long adress;

    private long offset = 0;

    public StackArena(Arena parent, long size) {
        this.limit = size;
        this.segment = parent.allocate(size);
        this.adress = this.segment.address();
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        var address = this.adress + this.offset;
        var padding = Math.floorMod(-address, byteAlignment);
        var alignedOffset = this.offset + padding;
        var newOffset = alignedOffset + byteSize;

        if (newOffset > this.limit || newOffset < alignedOffset) {
            throw new IllegalArgumentException("Requested size exceeds the allocated size of StackArena");
        }

        this.offset = newOffset;
        return this.segment.asSlice(alignedOffset, byteSize);
    }

    @Override
    public void close() {
        this.offset = 0;
    }
}

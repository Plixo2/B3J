package io.github.plixo2.box3d.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class ConstAllocator extends SlicingAllocator {
    private long max = 0;

    public ConstAllocator(Arena parent, long size) {
        super(parent.allocate(size));
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        if (this.offset() > this.max) {
            this.max = this.offset();
            System.out.println("ConstAllocator max: " + this.max);
        }

        this.reset();
        return super.allocate(byteSize, byteAlignment);
    }

}

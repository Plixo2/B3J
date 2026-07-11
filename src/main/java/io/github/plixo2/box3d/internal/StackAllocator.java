package io.github.plixo2.box3d.internal;

import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class StackAllocator extends SlicingAllocator implements AutoCloseable {

    // confined arena to help out with big allocations
    private @Nullable Arena helper;

    public StackAllocator(Arena parent, long size) {
        super(parent.allocate(size));
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {


        validateParameters(byteSize, byteAlignment);
        var min = this.address;
        var start = alignUp(min + this.sp, byteAlignment) - min;

        if (start + byteSize > this.limit) {
            if (this.helper == null) {
                this.helper = Arena.ofConfined();
            }
            return this.helper.allocate(byteSize, byteAlignment);
        }

        var slice = this.segment.asSlice(start, byteSize, byteAlignment);
        this.sp = start + byteSize;
        return slice;

    }

    @Override
    public void close() {
        this.reset();
        if (this.helper != null) {
            this.helper.close();
            this.helper = null;
        }
    }
}

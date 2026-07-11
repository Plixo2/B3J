package io.github.plixo2.box3d.internal;


import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

// public & modified version of jdk.internal.foreign.SlicingAllocator
public class SlicingAllocator implements SegmentAllocator {

    protected final MemorySegment segment;
    protected final long address;
    protected final long limit;

    protected long sp = 0L;

    public SlicingAllocator(MemorySegment segment) {
        this.segment = segment;
        this.address = segment.address();
        this.limit = segment.byteSize();
    }

    public long offset() {
        return this.sp;
    }

    public long remaining() {
        return this.limit - this.sp;
    }

    public void resetTo(long offset) {
        if (offset < 0 || offset > this.sp) {
            throw new IllegalArgumentException(String.format("offset %d should be in [0, %d] ", offset, this.sp));
        }
        this.sp = offset;
    }


    public void reset() {
        this.sp = 0L;
    }

    public boolean canAllocate(long byteSize, long byteAlignment) {
        if (byteSize < 0 || byteAlignment <= 0) {
            return false;
        }
        var min = this.address;
        var start = alignUp(min + this.sp, byteAlignment) - min;
        return start + byteSize <= this.limit;
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {
        validateParameters(byteSize, byteAlignment);

        var min = this.address;
        var start = alignUp(min + this.sp, byteAlignment) - min;

        if (start + byteSize > this.limit) {
            throw new IllegalArgumentException(
                    "Requested size of "
                    + byteSize
                    + " bytes with alignment "
                    + byteAlignment
                    + " exceeds the remaining capacity of the allocator ("
                    + remaining()
                    + " bytes left)."
            );
        }

        var slice = this.segment.asSlice(start, byteSize, byteAlignment);
        this.sp = start + byteSize;
        return slice;

    }
    protected final void validateParameters(long byteSize, long byteAlignment) {
        if (byteSize < 0) {
            throw new IllegalArgumentException("The provided allocation size is negative: " + byteSize);
        }
        if (byteAlignment <= 0 || ((byteAlignment & (byteAlignment - 1)) != 0L)) {
            throw new IllegalArgumentException("Invalid alignment constraint : " + byteAlignment);
        }
    }

    public static long alignUp(long n, long alignment) {
        return (n + alignment - 1) & -alignment;
    }


}

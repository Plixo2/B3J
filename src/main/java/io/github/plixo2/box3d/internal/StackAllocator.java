package io.github.plixo2.box3d.internal;

import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;


/// Reusable scratch [java.lang.foreign.SegmentAllocator] backed by a fixed native segment.
///
/// This allocator is intended for short-lived native call arguments. Calling [#close()] resets the
/// stack pointer and closes the temporary helper arena, but it does not release the fixed backing
/// segment, which remains owned by the parent arena.
///
/// If an allocation does not fit in the fixed segment, it falls back to a temporary confined arena.
///
/// <span style="color:red"><strong>Warning:</strong></span>
/// > Returned segments may be larger than the requested size.
///
/// <br>
///
/// ### Implementation Notes
/// The backing segment and stack pointer are kept 16-byte aligned.
/// 256 MemorySegment's are cached, to avoid creating new segments all the time (16 offsets for 16 sizes).
/// Using 16 offsets and 16 sizes means that, any combination of 256 bytes at a time, can be handled by the cache.
///
/// Apart from allocations created by [B3JUtil#ensureOffHeap],
/// the biggest allocation i observed by the time of writing, is `SphericalJointDef` with 192 bytes
///
public final class StackAllocator extends SlicingAllocator implements AutoCloseable {

    private final @Nullable MemorySegment[] precomputed = new MemorySegment[256];

    // confined arena to help out with big allocations
    private @Nullable Arena helper;

    public StackAllocator(Arena parent, long size) {
        var segment = parent.allocate(size, 16);
        assert segment.address() % 16 == 0;
        super(segment);
    }

    @Override
    public void resetTo(long offset) {
        if ((offset & 15) != 0) {
            throw new IllegalArgumentException("offset must be 16-byte aligned: " + offset);
        }
        super.resetTo(offset);
    }

    @Override
    public MemorySegment allocate(long byteSize, long byteAlignment) {

        validateParameters(byteSize, byteAlignment);
        var upByteAlignment = Math.max(byteAlignment, 8);
        var upByteSize = alignUp(byteSize, 16);

        var min = this.address;
        var start = alignUp(min + this.sp, upByteAlignment) - min;

        if (upByteSize > this.limit - start) {
            if (this.helper == null) {
                this.helper = Arena.ofConfined();
            }
            return this.helper.allocate(byteSize, byteAlignment);
        }

        // upByteSize is a multiple of 16
        // this.address is a multiple of 16
        // thus start is a multiple of 16 and will always be
        // incremented by a multiple of 16

        var i = start >> 4;
        var j = upByteSize >> 4;
        if (upByteAlignment == 8 && (i < 16 && j < 16)) {
            var toIndex = (int) (i << 4 | j);
            var slice = this.precomputed[toIndex];
            if (slice == null) {
                this.precomputed[toIndex] = slice = this.segment.asSlice(start, upByteSize, 8);
            }
            this.sp = start + upByteSize;
            return slice;
        }

        var slice = this.segment.asSlice(start, upByteSize, upByteAlignment);
        this.sp = start + upByteSize;
        return slice;

    }

    @Override
    public void reset() {
        super.reset();
        if (this.helper != null) {
            this.helper.close();
            this.helper = null;
        }
    }

    @Override
    public void close() {
        reset();
    }
}

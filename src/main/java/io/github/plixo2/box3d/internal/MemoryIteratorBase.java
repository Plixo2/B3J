package io.github.plixo2.box3d.internal;

import lombok.Getter;

import java.lang.foreign.MemorySegment;

public abstract sealed class MemoryIteratorBase<T> implements Iterable<T>
        permits
            MemoryIterator,
            MemoryIterator.OfPrimitive
{

    protected final long bytesPerElement;
    protected final MemorySegment segment;
    @Getter
    protected int length;

    public MemoryIteratorBase(
            MemorySegment segment,
            long bytesPerElement
    ) {
        this.segment = segment;
        this.bytesPerElement = bytesPerElement;

        if (segment.address() == 0) {
            this.length = 0;
        } else {
            this.length = assertSize(segment, bytesPerElement);
        }
    }

    public MemoryIteratorBase(
            MemorySegment segment,
            long count,
            long bytesPerElement
    ) {

        if (segment.address() == 0) {
            this.length = 0;
        } else {
            this.length = Math.toIntExact(count);
            if ((count * bytesPerElement) > segment.byteSize()) {
                segment = segment.reinterpret(count * bytesPerElement);
            }
        }

        this.segment = segment;
        this.bytesPerElement = bytesPerElement;

    }


    protected final void checkIndex(int index) {
        if (index < 0 || index >= this.length) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for length " + this.length);
        }
    }

    private static int assertSize(MemorySegment segment, long elementBytes) {
        long size = segment.byteSize();
        if (size % elementBytes != 0) {
            throw new IllegalArgumentException("Segment size " + size + " is not a multiple of element size " + elementBytes);
        }
        return Math.toIntExact(size / elementBytes);
    }

}

package io.github.plixo2.box3d.internal;

import org.box2d.box3d.b3jshimFunctionArgBuffer;

import java.lang.foreign.MemorySegment;

public class ShimArgBuffer {

    private final MemorySegment segment;

    public ShimArgBuffer(
            MemorySegment segment
    ) {
        this.segment = segment;
    }

    public MemorySegment data() {
        return b3jshimFunctionArgBuffer.data(this.segment).reinterpret(byteCount());
    }
    public @U64 long byteCount() {
        return b3jshimFunctionArgBuffer.size(this.segment);
    }
    public @U64 long elementCount() {
        return b3jshimFunctionArgBuffer.count(this.segment);
    }


}

package io.github.plixo2.box3d.internal;

import org.box2d.box3d.b3jshimFunctionArgBuffer;
import org.box2d.box3d.box3d_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class ShimArgBuffer {

    private final MemorySegment segment;

    public ShimArgBuffer(
            MemorySegment segment
    ) {
        this.segment = segment;
    }

    public ShimArgBuffer(
            Arena arena
    ) {
        var segment = box3d_h.b3jshim_create_arg_buffer();
        this.segment = segment.reinterpret(
                arena,
                box3d_h::b3jshim_destroy_arg_buffer
        );
    }

    public MemorySegment pointer() {
        return this.segment;
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

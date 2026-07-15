package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Unsigned;
import org.box2d.box3d.b3Filter;

import java.lang.foreign.MemorySegment;

public class Filter {

    public @Unsigned long categoryBits;
    public @Unsigned long maskBits;
    public int groupIndex;

    /// @api b3DefaultFilter
    public Filter() {
        this.categoryBits = B3.DEFAULT_CATEGORY_BITS;
        this.maskBits = B3.DEFAULT_MASK_BITS;
        this.groupIndex = 0;
    }

    public Filter(Filter other) {
        this.categoryBits = other.categoryBits;
        this.maskBits = other.maskBits;
        this.groupIndex = other.groupIndex;
    }

    Filter set(MemorySegment segment) {
        this.categoryBits = b3Filter.categoryBits(segment);
        this.maskBits = b3Filter.maskBits(segment);
        this.groupIndex = b3Filter.groupIndex(segment);
        return this;
    }

    void put(MemorySegment segment) {
        b3Filter.categoryBits(segment, this.categoryBits);
        b3Filter.maskBits(segment, this.maskBits);
        b3Filter.groupIndex(segment, this.groupIndex);
    }

}

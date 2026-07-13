package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.B3JUtil;
import io.github.plixo2.box3d.internal.Unsigned;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3QueryFilter;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Getter
@Setter
public class QueryFilter {

    private @Unsigned long categoryBits;
    private @Unsigned long maskBits;
    private @Unsigned long id;
    private @Nullable String name;

    /// @api b3DefaultQueryFilter
    public QueryFilter() {
        this(B3.DEFAULT_CATEGORY_BITS, B3.DEFAULT_MASK_BITS);
    }

    public QueryFilter(@Unsigned long categoryBits, @Unsigned long maskBits) {
        this.categoryBits = categoryBits;
        this.maskBits = maskBits;
    }

    MemorySegment create(SegmentAllocator arena) {
        var segment = b3QueryFilter.allocate(arena);

        b3QueryFilter.categoryBits(segment, this.categoryBits);
        b3QueryFilter.maskBits(segment, this.maskBits);
        b3QueryFilter.name(segment, B3JUtil.allocNullString(arena, this.name));
        b3QueryFilter.id(segment, this.id);

        return segment;
    }

}

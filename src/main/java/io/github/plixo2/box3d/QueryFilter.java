package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.Internal;
import io.github.plixo2.box3d.internal.U64;
import org.box2d.box3d.b3QueryFilter;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public class QueryFilter {

    final MemorySegment segment;
    private @Nullable String name = null;

    /// @api b3DefaultQueryFilter
    public QueryFilter() {
        this(B3.DEFAULT_CATEGORY_BITS, B3.DEFAULT_MASK_BITS);
    }

    public QueryFilter(@U64 long categoryBits, @U64 long maskBits) {
        this.segment = b3QueryFilter.allocate(Arena.ofAuto());
        categoryBits(categoryBits);
        maskBits(maskBits);
    }

    public void categoryBits(@U64 long categoryBits) {
        b3QueryFilter.categoryBits(this.segment, categoryBits);
    }

    public void maskBits(@U64 long maskBits) {
        b3QueryFilter.maskBits(this.segment, maskBits);
    }

    public void id(@U64 long id) {
        b3QueryFilter.id(this.segment, id);
    }

    public void name(@Nullable String name) {
        if (Objects.equals(this.name, name)) {
            return;
        }

        this.name = name;
        try (var arena = Arena.ofConfined()) {
            b3QueryFilter.name(this.segment, Internal.allocNullString(arena, name));
        }
    }

    public @U64 long categoryBits() {
        return b3QueryFilter.categoryBits(this.segment);
    }
    public @U64 long maskBits() {
        return b3QueryFilter.maskBits(this.segment);
    }
    public @U64 long id() {
        return b3QueryFilter.id(this.segment);
    }

    public @Nullable String name() {
        return this.name;
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.MemoryIterator;
import io.github.plixo2.box3d.internal.U16;
import org.box2d.box3d.b3SimplexCache;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class SimplexCache {

    final MemorySegment segment;

    /// @api b3_emptyDistanceCache;
    public SimplexCache() {
        this.segment = b3SimplexCache.allocate(Arena.ofAuto());
    }

    public void clear() {
        b3SimplexCache.metric(this.segment, 0);
        b3SimplexCache.count(this.segment, (short) 0);
    }

    public float metric() {
        return b3SimplexCache.metric(this.segment);
    }

    public @U16 int count() {
        return Short.toUnsignedInt(b3SimplexCache.count(this.segment));
    }

    public MemoryIterator.OfU8 indexA() {
        var indexA = b3SimplexCache.indexA(this.segment);
        return new MemoryIterator.OfU8(indexA, 4);
    }

    public MemoryIterator.OfU8 indexB() {
        var indexB = b3SimplexCache.indexB(this.segment);
        return new MemoryIterator.OfU8(indexB, 4);
    }


}

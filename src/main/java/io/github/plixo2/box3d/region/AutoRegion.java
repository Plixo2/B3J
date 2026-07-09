package io.github.plixo2.box3d.region;

import io.github.plixo2.box3d.internal.AllocState;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.ref.Cleaner;
import java.util.function.Consumer;

final class AutoRegion implements Region {
    private static final Cleaner CLEANER = Cleaner.create();

    private final FreeList freeList;

    AutoRegion(FreeList freeList) {
        this.freeList = freeList;
    }

    @Override
    public void register(AllocState owner, Runnable cleanup) {
        CLEANER.register(owner, () -> this.freeList.add(cleanup));
    }


    @Override
    public void close() {
        throw new UnsupportedOperationException("Attempted to close the auto region");
    }
}

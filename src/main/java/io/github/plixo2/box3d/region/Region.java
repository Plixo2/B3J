package io.github.plixo2.box3d.region;

import io.github.plixo2.box3d.internal.AllocState;

import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

public sealed interface Region
        extends
            AutoCloseable
        permits
            AutoRegion,
            ConfinedRegion,
            GlobalRegion
{

    static Region global() {
        interface Holder {
            Region INSTANCE = new GlobalRegion();
        }
        return Holder.INSTANCE;
    }

    static Region ofAuto(FreeList freeList) {
        return new AutoRegion(freeList);
    }

    static Region ofConfined() {
        return new ConfinedRegion();
    }

    static Region ofConfined(Region parent) {
        return new ConfinedRegion(parent);
    }


    void register(AllocState owner, Runnable cleanup);

    default void register(AllocState owner, MemorySegment segment, Consumer<MemorySegment> cleanup) {
        register(owner, () -> cleanup.accept(segment));
    }

    /// @throws UnsupportedOperationException if the region cannot be closed manually
    @Override
    void close();
}

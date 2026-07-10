package io.github.plixo2.box3d.region;


import io.github.plixo2.box3d.internal.AllocState;


final class GlobalRegion implements Region {

    @Override
    public void register(AllocState owner, Runnable cleanup) {
        // nothing
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Attempted to close the global region");
    }
}

package io.github.plixo2.box3d.region;


final class GlobalRegion implements Region {
    GlobalRegion() {}

    @Override
    public void register(Lifetime owner, Runnable cleanup) {
        // nothing
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Attempted to close the global region");
    }
}

package io.github.plixo2.box3d.internal;

public final class AllocState {

    private boolean destroyed = false;

    private AllocState() {

    }

    public synchronized boolean guard() {
        var wasDestroyed = this.destroyed;
        this.destroyed = true;
        return !wasDestroyed;
    }

    public synchronized void once() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed");
        }
        this.destroyed = true;
    }

    public synchronized void ensureAccess() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed");
        }
    }

    public Runnable guard(Runnable runnable) {
        return () -> {
            if (!this.destroyed) {
                runnable.run();
                this.destroyed = true;
            }
        };
    }

    public static AllocState create() {
        return new AllocState();
    }


}

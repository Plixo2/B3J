package io.github.plixo2.box3d.region;

public final class Lifetime {

    private boolean destroyed = false;

    private Lifetime() {

    }

    public boolean isAlive() {
        return !this.destroyed;
    }

    public synchronized void markAsDestroyed() {
        ensureAccess();
        this.destroyed = true;
    }

    public synchronized void ensureAccess() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed");
        }
    }

    Runnable createGuard(Runnable runnable) {
        return () -> {
            if (!this.destroyed) {
                runnable.run();
                this.destroyed = true;
            }
        };
    }

    public static Lifetime create() {
        return new Lifetime();
    }


}

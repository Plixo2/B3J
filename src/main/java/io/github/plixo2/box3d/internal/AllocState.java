package io.github.plixo2.box3d.internal;

import java.util.function.Consumer;

public class AllocState {

    public boolean destroyed = false;

    private AllocState() {

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

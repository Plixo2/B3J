package io.github.plixo2.box3d.internal;

import java.util.function.Consumer;

public class AllocState {

    public boolean destroyed = false;

    private AllocState() {

    }

    /// get and set the destroyed state
    public synchronized boolean guard() {
        var p = this.destroyed;
        this.destroyed = true;
        return !p;
    }

    public synchronized void once() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed");
        }
        this.destroyed = true;
    }

    public synchronized boolean get() {
        return this.destroyed;
    }

    public synchronized void ensureAccess() {
        if (this.destroyed) {
            throw new IllegalStateException("Already destroyed");
        }
    }

    public Runnable guard(Runnable runnable) {
        return () -> {
            if (this.guard()) {
                runnable.run();
            }
        };
    }
    public Runnable once(Runnable runnable) {
        return () -> {
            this.once();
            runnable.run();
        };
    }

    public <T> Consumer<T> guard(Consumer<T> consumer) {
        return (t) -> {
            if (this.guard()) {
                consumer.accept(t);
            }
        };
    }
    public <T> Consumer<T> once(Consumer<T> consumer) {
        return (t) -> {
            this.once();
            consumer.accept(t);
        };
    }

    public static AllocState create() {
        return new AllocState();
    }


}

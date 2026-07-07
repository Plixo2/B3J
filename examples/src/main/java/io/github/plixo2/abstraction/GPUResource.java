package io.github.plixo2.abstraction;

import java.lang.ref.Cleaner;

public abstract class GPUResource {
    protected Cleaner.Cleanable remover;
    private boolean freed = false;

    public final void free() {
        if (this.remover == null) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " has forgotten to initialize its resource cleaner");
        }
        this.freed = true;
        this.remover.clean();
    }

    protected void ensureAllocated() {
        if (this.freed) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " has already been freed");
        }
    }

}

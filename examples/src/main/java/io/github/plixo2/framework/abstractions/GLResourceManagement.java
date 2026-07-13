package io.github.plixo2.framework.abstractions;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GLResourceManagement {
    public static boolean ENABLED = true;
    private static final GLResourceManagement INSTANCE = new GLResourceManagement();


    private final List<GCResource> toClean = new ArrayList<>();
    private final Cleaner cleaner = Cleaner.create();
    private long lastCleanup = System.currentTimeMillis();

    private int removed = 0;
    private int added = 0;
    private AtomicInteger generation = new AtomicInteger(0);


    public static Cleaner.Cleanable add(Object owner, GCResource resource) {
        return INSTANCE.addResource(owner, resource);
    }

    /// should be called every frame
    public static void cleanup() {
        if (!ENABLED) return;

       INSTANCE.clearOne();
    }
    public static long timeSinceLastFree() {
        return INSTANCE.timeSinceLastCleanup();
    }
    public static Stats stats() {
        return INSTANCE.getStats();
    }
    public static void startNewGeneration() {
        INSTANCE.reset();
    }

    private void clearAll() {
        synchronized (this.toClean) {
            while (!this.toClean.isEmpty()) {
                this.toClean.removeLast().freeResource();
                this.removed += 1;
            }
            this.lastCleanup = System.currentTimeMillis();
        }
    }

    private void clearOne() {
        synchronized (this.toClean) {
            if (this.toClean.isEmpty()) {
                return;
            }
            this.toClean.removeLast().freeResource();
            this.removed += 1;
            this.lastCleanup = System.currentTimeMillis();
        }
    }

    private long timeSinceLastCleanup() {
        return System.currentTimeMillis() - this.lastCleanup;
    }

    private Stats getStats() {
        return new Stats(this.added, this.removed);
    }

    private void reset() {
        this.generation.incrementAndGet();
        this.toClean.clear();
        this.removed = 0;
        this.added = 0;
        this.lastCleanup = System.currentTimeMillis();
    }

    private Cleaner.Cleanable addResource(Object owner, GCResource resource) {
        this.added += 1;
        var creationGeneration = this.generation.get();
        return this.cleaner.register(
            owner, () -> {
                this.scheduleDead(resource, creationGeneration);
            }
        );
    }

    private void scheduleDead(GCResource resource, int creationGeneration) {
        synchronized (this.toClean) {
            if (this.generation.get() != creationGeneration) {
                // already dead
                return;
            }
            this.toClean.add(resource);
        }
    }

    public record Stats(
            int added,
            int removed
    ) {
        public int active() {
            return this.added - this.removed;
        }
    }
}

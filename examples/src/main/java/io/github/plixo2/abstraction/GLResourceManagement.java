package io.github.plixo2.abstraction;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;

public class GLResourceManagement {
    public static boolean ENABLED = true;
    private static final GLResourceManagement INSTANCE = new GLResourceManagement();


    private final List<GCResource> toClean = new ArrayList<>();
    private final Cleaner cleaner = Cleaner.create();
    private long lastCleanup = System.currentTimeMillis();

    private int removed = 0;
    private int added = 0;


    public static Cleaner.Cleanable add(Object owner, GCResource resource) {
        return INSTANCE.addResource(owner, resource);
    }
    ///  should be called periodically on the render thread
    public static void cleanup() {
        if (!ENABLED) return;

       INSTANCE.clear();
    }
    public static long timeSinceLastFree() {
        return INSTANCE.timeSinceLastCleanup();
    }
    public static Stats stats() {
        return INSTANCE.getStats();
    }


    private void clear() {
        var toClean = this.toClean;
        synchronized (toClean) {
            if (toClean.isEmpty()) {
                return;
            }
            var toRemove = toClean.removeLast();
            toRemove.freeResource();
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

    private Cleaner.Cleanable addResource(Object owner, GCResource resource) {
        this.added += 1;
        return this.cleaner.register(
            owner, () -> {
                this.scheduleDead(resource);
            }
        );
    }
    private void scheduleDead(GCResource resource) {
        var toClean = this.toClean;
        synchronized (toClean) {
            toClean.add(resource);
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

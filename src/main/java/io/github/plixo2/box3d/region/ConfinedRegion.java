package io.github.plixo2.box3d.region;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ConfinedRegion implements Region {

    private @Nullable ArrayList<Runnable> resources = null;

    private final Lifetime lifetime = Lifetime.create();

    ConfinedRegion() {

    }

    ConfinedRegion(Region parent) {
        parent.register(this.lifetime, this::close);
    }

    @Override
    public void register(Lifetime owner, Runnable cleanup) {
        this.lifetime.ensureAccess();
        if (this.resources == null) {
            this.resources = new ArrayList<>(4);
        }
        this.resources.add(owner.createGuard(cleanup));
    }

    @Override
    public boolean isClosed() {
        return !this.lifetime.isAlive();
    }

    @Override
    public void close() {
        this.lifetime.markAsDestroyed();
        if (this.resources != null) {
            releaseList(this.resources);
            this.resources.clear();
            this.resources.trimToSize();
            this.resources = null;
        }
    }


    private static void releaseList(List<Runnable> resources) {
        for (var i = resources.size() - 1; i >= 0; i--) {
            var resource = resources.get(i);
            resource.run();
        }
    }
}

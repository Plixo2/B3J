package io.github.plixo2.box3d.region;


import io.github.plixo2.box3d.internal.AllocState;

import java.util.ArrayList;
import java.util.List;

final class ConfinedRegion implements Region {

    private List<Runnable> resources;

    private final AllocState state = AllocState.create();

    ConfinedRegion() {
        this.resources = new ArrayList<>();
    }

    ConfinedRegion(Region parent) {
        var list = this.resources = new ArrayList<>();
        parent.register(this.state, () -> releaseList(list));
    }


    @Override
    public void register(AllocState owner, Runnable cleanup) {
        this.state.ensureAccess();
        this.resources.add(owner.guard(cleanup));
    }

    @Override
    public void close() {
        this.state.once();
        releaseList(this.resources);
        this.resources = null;
    }


    private static void releaseList(List<Runnable> resources) {
        for (var i = resources.size() - 1; i >= 0; i--) {
            var resource = resources.get(i);
            resource.run();
        }
        resources.clear();
    }
}

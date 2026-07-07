package io.github.plixo2.box3d.region;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FreeList {

    private final Queue<Runnable> resources = new ConcurrentLinkedQueue<>();

    public FreeList() {

    }

    void add(Runnable resource) {
        this.resources.add(resource);
    }

    public void drain() {
        Runnable resource;
        while ((resource = this.resources.poll()) != null) {
            resource.run();
        }
    }

}

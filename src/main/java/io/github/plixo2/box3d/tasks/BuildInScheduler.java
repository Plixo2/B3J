package io.github.plixo2.box3d.tasks;

import io.github.plixo2.box3d.B3;

public record BuildInScheduler(int workerCount) implements TaskScheduler {

    public BuildInScheduler {
        if (workerCount < 1 || workerCount > B3.MAX_WORKERS) {
            throw new IllegalArgumentException("workerCount must be between 1 and " + B3.MAX_WORKERS);
        }
    }


    public BuildInScheduler() {
        // cannot easily get the number of physical cores or efficiency cores
        // does not matter anyway

        var cores = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        // dont subtract 1, the thread that calls b3World_Step will be counted!

        this(cores);
    }

}

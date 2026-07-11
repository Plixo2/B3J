package io.github.plixo2.box3d.threads;

public record BuildInScheduler(int workerCount) implements TaskScheduler {

    public BuildInScheduler {
        if (workerCount < 1) {
            throw new IllegalArgumentException("workerCount must be at least 1");
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

package io.github.plixo2.box3d.threads;

// "I recommend to use the core count of the CPU as the worker count,
// not counting hyper-threads or efficiency cores."
public record BuildInScheduler(int workerCount) implements TaskScheduler {

    public BuildInScheduler {
        if (workerCount < 1) {
            throw new IllegalArgumentException("workerCount must be at least 1");
        }
    }


    public BuildInScheduler() {
        // cannot easily get the number of physical cores or efficiency cores
        // and it does not matter anyway

        var cores = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        // dont subtract 1, the thread that calls b3World_Step will be counted!

        this(cores);
    }

}

package io.github.plixo2.box3d.tasks;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorTaskPool extends CustomTaskScheduler<Future<?>> {
    private final ExecutorService executor;

    public ExecutorTaskPool() {
        // leave one for awaiting, task stealing is not implemented
        var cores = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);

        var factory = Thread.ofPlatform().daemon(true).name("box3d-worker-", 0).factory();
        this(cores, Executors.newFixedThreadPool(cores, factory));
    }


    public ExecutorTaskPool(int workerCount, ExecutorService executor) {
        super(workerCount);
        this.executor = executor;
    }

    /// {@inheritDoc}
    @Override
    public @Nullable Future<?> enqueue(Runnable runnable) {
        return this.executor.submit(runnable);
    }

    /// {@inheritDoc}
    // TODO implement work stealing
    @Override
    public void finish(Future<?> task) {
        try {
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void close() {
        this.executor.shutdown();
    }

}
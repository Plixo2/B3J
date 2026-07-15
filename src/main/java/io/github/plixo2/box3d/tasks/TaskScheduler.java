package io.github.plixo2.box3d.tasks;


// interface for `enqueueTask`, `finishTask`, `userTaskContext` and `workerCount`.
public sealed interface TaskScheduler
    permits
        BuildInScheduler,
        CustomTaskScheduler
{
    int workerCount();
}

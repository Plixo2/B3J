package io.github.plixo2.box3d.tasks;

public sealed interface TaskScheduler
    permits
        BuildInScheduler,
        CustomTaskScheduler
{
    int workerCount();
}

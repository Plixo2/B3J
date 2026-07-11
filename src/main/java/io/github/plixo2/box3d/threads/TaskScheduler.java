package io.github.plixo2.box3d.threads;

public sealed interface TaskScheduler
    permits
        BuildInScheduler,
        CustomTaskScheduler
{
    int workerCount();
}

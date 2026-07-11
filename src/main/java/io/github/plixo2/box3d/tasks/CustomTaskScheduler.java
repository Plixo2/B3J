package io.github.plixo2.box3d.tasks;

import io.github.plixo2.box3d.B3;
import io.github.plixo2.box3d.internal.Internal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.box2d.box3d.b3EnqueueTaskCallback;
import org.box2d.box3d.b3FinishTaskCallback;
import org.box2d.box3d.b3TaskCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract non-sealed class CustomTaskScheduler<T>
        implements
            TaskScheduler,
            AutoCloseable,
            b3EnqueueTaskCallback.Function,
            b3FinishTaskCallback.Function
{

    @Getter
    private final int workerCount;

    protected CustomTaskScheduler(int workerCount) {
        if (workerCount < 1 || workerCount > B3.MAX_WORKERS) {
            throw new IllegalArgumentException("workerCount must be between 1 and " + B3.MAX_WORKERS);
        }

        this.workerCount = workerCount;
    }

    private final Tasks<T> tasks = new Tasks<>();


    /// null should be returned if the task was handled synchronously,
    /// otherwise returns a object that will be passed to [#finish] to be awaited for completion.
    /// This method will be called from the thread that called [B3#worldStep].
    public abstract @Nullable T enqueue(Runnable runnable);

    /// You should always await the task, before returning from this method.
    /// This method will be called from the thread that called [B3#worldStep].
    public abstract void finish(T task);

    public abstract void close();


    /// b3EnqueueTaskCallback
    @Override
    public final MemorySegment apply(
            MemorySegment task,
            MemorySegment taskContext,
            MemorySegment userContext,
            MemorySegment taskName
    ) {
        synchronized (this.tasks) {
            try {
                var slot = this.tasks.getFree();
                slot.reset(task, taskContext);

                T future;
                try {
                    future = enqueue(slot);
                } catch (Exception e) {
                    if (!slot.wasStarted().get()) {
                        try {
                            slot.run(); // try to recover
                        } catch (Exception e2) {
                            // ignore
                        }
                        slot.markFree();
                    }
                    throw e;
                }

                if (future == null) {
                    if (!slot.wasStarted().get()) {
                        try {
                            slot.run(); // try to recover
                        } catch (Exception e2) {
                            // ignore
                        }
                        slot.markFree();

                        throw new RuntimeException(
                                "enqueue() returned null, but the task was not executed synchronously"
                        );
                    } else if (!slot.wasCompleted().get()) {
                        slot.markFree();
                        throw new RuntimeException(
                                "enqueue() returned null, but the task did not complete synchronously"
                        );
                    } else {
                        slot.markFree();
                        return MemorySegment.NULL;
                    }
                }

                slot.setUserTask(future);

                var index = slot.index + 1;  // 0 means task was handled synchronously, so we offset by 1
                return MemorySegment.ofAddress(index);
            } catch(Exception e) {
                Internal.unhandledCallbackException(e);
                return MemorySegment.NULL;
            }
        }
    }

    /// b3FinishTaskCallback
    @Override
    public final void apply(MemorySegment userTask, MemorySegment userContext) {

        var index = userTask.address() - 1; // offset by 1 to get the original index

        synchronized (this.tasks) {
            try {
                var slot = this.tasks.get(index);
                try {
                    if (slot.userTask == null) {
                        throw new IllegalStateException("Task was already finished or never enqueued");
                    }

                    try {
                        finish(slot.userTask);
                    } catch (Exception e) {
                        if (!slot.wasStarted().get()) {
                            try {
                                slot.run(); // try to recover
                            } catch (Exception e2) {
                                // ignore
                            }
                        }
                        throw e;
                    }

                    if (!slot.wasStarted().get()) {
                        try {
                            slot.run(); // try to recover
                        } catch(Exception e) {
                            // ignore
                        }
                        throw new IllegalStateException("finish() returned, but the task was not even started");
                    }
                    if (!slot.wasCompleted().get()) {
                        throw new IllegalStateException("finish() returned, but the task was not complete");
                    }
                } finally {
                    slot.markFree(); // mark free regardless
                }
            } catch(Exception e) {
                Internal.unhandledCallbackException(e);
            }
        }


    }


    private static final class Tasks<T> {
        private final int count;
        private final Slot<T>[] tasks;

        Tasks() {
            this.count = B3.MAX_TASKS;
            //noinspection unchecked
            this.tasks = new Slot[this.count];
            for (int i = 0; i < this.count; i++) {
                this.tasks[i] = new Slot<>(i);
            }
        }

        Slot<T> getFree() {

            for (int i = 0; i < this.count; i++) {
                var task = this.tasks[i];
                if (task.task == null) {
                    return task;
                }
            }

            throw new RuntimeException(
                    "No free tasks available. "
                    + "Number of queued tasks exceeded the maximum of "
                    + "B3.MAX_TASKS = " + B3.MAX_TASKS + " tasks."
            );
        }

        Slot<T> get(long index) {
            if (index < 0 || index >= this.count) {
                throw new IndexOutOfBoundsException("Invalid task pointer: " + index);
            }
            return this.tasks[Math.toIntExact(index)];
        }

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Slot<T> implements Runnable {
        private final int index;

        private volatile @Nullable MemorySegment task;
        private volatile @Nullable MemorySegment taskContext;
        private final AtomicBoolean started = new AtomicBoolean(false);
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private volatile @Nullable T userTask;

        private void reset(
                MemorySegment task,
                MemorySegment taskContext
        ) {
            this.task = task;
            this.taskContext = taskContext;
            this.started.set(false);
            this.completed.set(false);
        }
        private void setUserTask(@NotNull T userTask) {
            this.userTask = userTask;
        }

        private AtomicBoolean wasStarted() {
            return this.started;
        }

        private AtomicBoolean wasCompleted() {
            return this.completed;
        }

        private void markFree() {
            this.task = null;
            this.taskContext = null;
            this.userTask = null;
        }


        @Override
        public void run() {
            if (this.started.getAndSet(true)) {
                throw new IllegalStateException("Task called under suspicious circumstances: Task was already called");
            }
            var task = this.task;
            var taskContext = this.taskContext;
            if (task == null) {
                throw new IllegalStateException("Task called under suspicious circumstances: Task pointer is not set");
            } else if (taskContext == null) {
                throw new IllegalStateException("Task called under suspicious circumstances: Task context pointer is not set");
            }

            try {
                b3TaskCallback.invoke(task, taskContext);
            } finally {
                this.completed.set(true);
            }
        }
    }

}

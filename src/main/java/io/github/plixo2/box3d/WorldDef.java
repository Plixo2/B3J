package io.github.plixo2.box3d;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.AllocatedPool;
import io.github.plixo2.box3d.threads.BuildInScheduler;
import io.github.plixo2.box3d.threads.CustomTaskScheduler;
import io.github.plixo2.box3d.threads.TaskScheduler;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3WorldDef;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

// https://github.com/erincatto/box3d/blob/29bf523ce7bc4590aba9f17c9db791cdc5c4397e/src/types.c#L11
@Getter
@Setter
public class WorldDef {

    Vector3f gravity;
    float hitEventThreshold;
    float restitutionThreshold;
    float contactSpeed;
    float contactHertz;
    float contactDampingRatio;
    float maximumLinearSpeed;
    @Nullable Object frictionCallback;
    @Nullable Object restitutionCallback;
    boolean enableSleep;
    boolean enableContinuous;
    @Nullable TaskScheduler taskPool;
    @Nullable DebugShapeCallbacks<?> debugShapeCollection;
    @Nullable Object userDebugShapeContext;
    Capacity capacity = new Capacity();


    /// @api b3DefaultWorldDef
    public WorldDef() {
        float lengthUnits = B3.lengthUnitsPerMeter();

        this.gravity = new Vector3f();
        this.gravity.x = 0.0f;
        this.gravity.y = -10.0f;
        this.hitEventThreshold = 1.0f * lengthUnits;
        this.restitutionThreshold = 1.0f * lengthUnits;
        this.contactSpeed = 3.0f * lengthUnits;
        this.contactHertz = 30.0f;
        this.contactDampingRatio = 10.0f;

        // 400 meters per second, faster than the speed of sound
        this.maximumLinearSpeed = 400.0f * lengthUnits;

        this.enableSleep = true;
        this.enableContinuous = true;

    }


    CreationResult create(SegmentAllocator arena) {
        var segment = b3WorldDef.allocate(arena);

        AllocatedPool taskPool = null;
        var enqueueTask = MemorySegment.NULL;
        var finishTask = MemorySegment.NULL;

        var workerCount = switch (this.taskPool) {
            case BuildInScheduler(var wc) -> wc;
            case CustomTaskScheduler<?> scheduler -> {
                taskPool = AllocatedPool.of(scheduler);
                enqueueTask = taskPool.enqueueTaskCallback();
                finishTask = taskPool.finishTaskCallback();
                yield scheduler.workerCount();
            }
            case null -> 0;
        };

        DebugShapeCallbacks.Allocated shapes = null;
        var createDebugShape = MemorySegment.NULL;
        var destroyDebugShape = MemorySegment.NULL;

        if (this.debugShapeCollection != null) {
            shapes = new DebugShapeCallbacks.Allocated(this.debugShapeCollection);
            createDebugShape = shapes.creation;
            destroyDebugShape = shapes.deletion;
        }


        PrimitiveMemOps.putVec3(b3WorldDef.gravity(segment), this.gravity);
        b3WorldDef.restitutionThreshold(segment, this.restitutionThreshold);
        b3WorldDef.hitEventThreshold(segment, this.hitEventThreshold);
        b3WorldDef.contactHertz(segment, this.contactHertz);
        b3WorldDef.contactDampingRatio(segment, this.contactDampingRatio);
        b3WorldDef.contactSpeed(segment, this.contactSpeed);
        b3WorldDef.maximumLinearSpeed(segment, this.maximumLinearSpeed);
        b3WorldDef.frictionCallback(segment, nls(this.frictionCallback));
        b3WorldDef.restitutionCallback(segment, nls(this.restitutionCallback));
        b3WorldDef.enableSleep(segment, this.enableSleep);
        b3WorldDef.enableContinuous(segment, this.enableContinuous);
        b3WorldDef.workerCount(segment, workerCount);
        b3WorldDef.enqueueTask(segment, enqueueTask);
        b3WorldDef.finishTask(segment, finishTask);
        b3WorldDef.userTaskContext(segment, MemorySegment.NULL);
        b3WorldDef.createDebugShape(segment, createDebugShape);
        b3WorldDef.destroyDebugShape(segment, destroyDebugShape);
        b3WorldDef.userDebugShapeContext(segment, nls(this.userDebugShapeContext));
        this.capacity.put(b3WorldDef.capacity(segment));
        b3WorldDef.internalValue(segment, B3.SECRET_COOKIE);

        return new CreationResult(shapes, taskPool, segment);
    }
    record CreationResult(
            @Nullable DebugShapeCallbacks.Allocated shapes,
            @Nullable AllocatedPool taskPool,
            MemorySegment segment
    ) {}


    private MemorySegment nls(Object object) {
        if (object != null) {
            throw new RuntimeException("Not implemented");
        }
        return MemorySegment.NULL;
    }

}

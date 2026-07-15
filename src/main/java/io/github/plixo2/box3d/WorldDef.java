package io.github.plixo2.box3d;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

import io.github.plixo2.box3d.internal.AllocatedShapeCallbacks;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.AllocatedTaskCallbacks;
import io.github.plixo2.box3d.tasks.BuildInScheduler;
import io.github.plixo2.box3d.tasks.CustomTaskScheduler;
import io.github.plixo2.box3d.tasks.TaskScheduler;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3WorldDef;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Getter
@Setter
public class WorldDef {

    private Vector3f gravity;
    private float hitEventThreshold;
    private float restitutionThreshold;
    private float contactSpeed;
    private float contactHertz;
    private float contactDampingRatio;
    private float maximumLinearSpeed;
    private @Nullable Object frictionCallback;
    private @Nullable Object restitutionCallback;
    private boolean enableSleep;
    private boolean enableContinuous;

    // replaces `enqueueTask`, `finishTask`, `userTaskContext` and `workerCount`.
    private @Nullable TaskScheduler taskPool;

    /// replaces `createDebugShape` and `destroyDebugShape`
    private @Nullable DebugShapeCallbacks<?> debugShapes;

    private @Nullable Object userDebugShapeContext;
    private Capacity capacity = new Capacity();


    /// @api b3DefaultWorldDef
    public WorldDef() {
        float lengthUnits = B3.getLengthUnitsPerMeter();

        this.gravity = new Vector3f();
        this.gravity.x = 0.0f;
        this.gravity.y = -10.0f;
        this.hitEventThreshold = 1.0f * lengthUnits;
        this.restitutionThreshold = 1.0f * lengthUnits;
        this.contactSpeed = 3.0f * lengthUnits;
        this.contactHertz = 30.0f;
        this.contactDampingRatio = 10.0f;

        this.maximumLinearSpeed = 400.0f * lengthUnits;

        this.enableSleep = true;
        this.enableContinuous = true;

    }


    CreationResult create(SegmentAllocator arena) {
        var segment = b3WorldDef.allocate(arena);

        AllocatedTaskCallbacks taskPool = null;
        var enqueueTask = MemorySegment.NULL;
        var finishTask = MemorySegment.NULL;

        AllocatedShapeCallbacks shapes = null;
        var createDebugShape = MemorySegment.NULL;
        var destroyDebugShape = MemorySegment.NULL;

        var workerCount = switch (this.taskPool) {
            case BuildInScheduler(var wc) -> wc;
            case CustomTaskScheduler<?> scheduler -> {
                taskPool = AllocatedTaskCallbacks.createCallbacks(scheduler);
                enqueueTask = taskPool.enqueueTaskCallback();
                finishTask = taskPool.finishTaskCallback();
                yield scheduler.workerCount();
            }
            case null -> 0;
        };

        if (this.debugShapes != null) {
            shapes = this.debugShapes.createCallbacks();
            createDebugShape = shapes.creation();
            destroyDebugShape = shapes.deletion();
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

        var worldStateValues = new WorldID.StateValues(taskPool, shapes);
        return new CreationResult(segment, worldStateValues);
    }

    record CreationResult(
            MemorySegment segment,
            WorldID.StateValues worldStateValues
    ) {}

    private MemorySegment nls(Object object) {
        if (object != null) {
            throw new RuntimeException("Not implemented");
        }
        return MemorySegment.NULL;
    }

}

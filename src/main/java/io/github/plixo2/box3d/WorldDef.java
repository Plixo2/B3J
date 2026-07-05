package io.github.plixo2.box3d;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

import io.github.plixo2.box3d.internal.U32;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3WorldDef;
import org.jetbrains.annotations.Nullable;

import static io.github.plixo2.box3d.internal.Internal.assertU32;

// https://github.com/erincatto/box3d/blob/29bf523ce7bc4590aba9f17c9db791cdc5c4397e/src/types.c#L11
@Getter
@Setter
public class WorldDef {

    Vec3 gravity;
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
    @Nullable TaskPool taskPool;
    @Nullable Object userData;
    @Nullable Object createDebugShape;
    @Nullable Object destroyDebugShape;
    @Nullable Object userDebugShapeContext;
    Capacity capacity = new Capacity();


    public WorldDef() {
        float lengthUnits = B3.get().getLengthUnitsPerMeter();

        this.gravity = new Vec3();
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

        TaskPool.Allocated taskPool = null;
        var workerCount = 0;
        var enqueueTask = MemorySegment.NULL;
        var finishTask = MemorySegment.NULL;
        if (this.taskPool != null) {
            taskPool = TaskPool.allocate(this.taskPool);
            workerCount = this.taskPool.workerCount();
            enqueueTask = taskPool.enqueueTaskCallback();
            finishTask = taskPool.finishTaskCallback();
        }


        this.gravity.put(b3WorldDef.gravity(segment));
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
        b3WorldDef.userData(segment, nls(this.userData));
        b3WorldDef.createDebugShape(segment, nls(this.createDebugShape));
        b3WorldDef.destroyDebugShape(segment, nls(this.destroyDebugShape));
        b3WorldDef.userDebugShapeContext(segment, nls(this.userDebugShapeContext));
        this.capacity.put(b3WorldDef.capacity(segment));
        b3WorldDef.internalValue(segment, B3.SECRET_COOKIE);

        return new CreationResult(taskPool, segment);
    }
    record CreationResult(@Nullable TaskPool.Allocated taskPool, MemorySegment segment) {}


    private MemorySegment nls(Object object) {
        if (object != null) {
            throw new RuntimeException("Not implemented");
        }
        return MemorySegment.NULL;
    }

}

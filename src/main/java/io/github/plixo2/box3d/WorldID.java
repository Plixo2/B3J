package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.U16;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.box3d.internal.AllocatedPool;
import org.box2d.box3d.b3WorldId;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class WorldID {
    public static final WorldID NULL_ID = new WorldID(null, null, null, null, 0, 0);

    final @U16 int index1;
    final @U16 int generation;

    final AllocState state = AllocState.create();

    final @Nullable AllocatedPool taskPool;               // keep alive
    final @Nullable DebugShapeCallbacks.Allocated shapes; // keep alive

    private WorldID(
            @Nullable B3 instance,
            @Nullable Region region,
            @Nullable AllocatedPool taskPool,
            @Nullable DebugShapeCallbacks.Allocated shapes,
            @U16 int index1,
            @U16 int generation
    ) {

        this.index1 = index1;
        this.generation = generation;

        this.taskPool = taskPool;
        this.shapes = shapes;

        if (instance != null && region != null) {
            region.register(this.state, () -> {
                instance.destroyWorld(index1, generation);
                if (taskPool != null) {
                    taskPool.close();
                }
                if (shapes != null) {
                    shapes.close();
                }
            });
        }
    }

    void ensureAccess() {
        this.state.ensureAccess();
    }



    public WorldID reinterpret(B3 instance, Region region) {
        return new WorldID(
                Objects.requireNonNull(instance),
                Objects.requireNonNull(region),
                this.taskPool, this.shapes,
                this.index1, this.generation
        );
    }

    public @U16 int index1() {
        this.state.ensureAccess();
        return this.index1;
    }
    public @U16 int generation() {
        this.state.ensureAccess();
        return this.generation;
    }


    @Override
    public String toString() {
        return toString(this.index1, this.generation);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WorldID worldID)) {
            return false;
        }
        return this.index1 == worldID.index1 && this.generation == worldID.generation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.index1, this.generation);
    }

    static WorldID of(
            B3 instance,
            Region region,
            @Nullable AllocatedPool taskPool,
            @Nullable DebugShapeCallbacks.Allocated shapes,
            MemorySegment segment
    ) {
        var index1 = Short.toUnsignedInt(b3WorldId.index1(segment));
        var generation = Short.toUnsignedInt(b3WorldId.generation(segment));
        return new WorldID(
                instance,
                region,
                taskPool,
                shapes,
                index1,
                generation
        );
    }


    static String toString(@U16 int index1, @U16 int generation) {
        return "WorldID{"
                + "index1=" + index1
                + ", generation=" + generation
                + '}';
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.U16;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.box3d.internal.AllocatedPool;
import org.box2d.box3d.b3WorldId;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class WorldID {
    public static final WorldID NULL_ID = new WorldID(null, null, null, null, 0);

    private final long packedID;

    final AllocState state = AllocState.create();

    final @Nullable AllocatedPool taskPool;               // keep alive
    final @Nullable DebugShapeCallbacks.Allocated shapes; // keep alive

    private WorldID(
            @Nullable B3 instance,
            @Nullable Region region,
            @Nullable AllocatedPool taskPool,
            @Nullable DebugShapeCallbacks.Allocated shapes,
            long packedID
    ) {

        this.packedID = packedID;

        this.taskPool = taskPool;
        this.shapes = shapes;

        if (instance != null && region != null) {
            region.register(this.state, () -> {
                instance.destroyWorld(packedID);
                if (taskPool != null) {
                    taskPool.close();
                }
                if (shapes != null) {
                    shapes.close();
                }
            });
        }
    }

    public long packedID() {
        this.state.ensureAccess();
        return this.packedID;
    }

    @Override
    public String toString() {
        return toString(this.packedID);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WorldID worldID)) {
            return false;
        }
        return this.packedID == worldID.packedID;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.packedID);
    }

    static WorldID of(
            B3 instance,
            Region region,
            @Nullable AllocatedPool taskPool,
            @Nullable DebugShapeCallbacks.Allocated shapes,
            MemorySegment segment
    ) {
        var identifier = PrimitiveMemOps.packWorldID(segment);
        return new WorldID(
                instance,
                region,
                taskPool,
                shapes,
                identifier
        );
    }


    static String toString(long packedID) {
        return "WorldID{"
                + "index1=" + PrimitiveMemOps.getWorldIDIndexFromPackedID(packedID)
                + ", generation=" + PrimitiveMemOps.getWorldIDGenerationFromPackedID(packedID)
                + '}';
    }

}

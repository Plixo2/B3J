package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.AllocatedShapeCallbacks;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.box3d.internal.AllocatedTaskCallbacks;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;

public final class WorldID {

    public static final WorldID NULL_ID = new WorldID(null, null, StateValues.none(), 0);

    public static WorldID fromUnknown(long packedID) {
        return fromUnknown(packedID, null);
    }

    public static WorldID fromUnknown(long packedID, @Nullable Region region) {
        B3 b3 = null;
        if (region != null) {
            b3 = B3.get();
        }
        return new WorldID(
                b3,
                region,
                StateValues.none(),
                packedID
        );
    }



    private final long packedID;

    final AllocState state = AllocState.create();

    // hold variables from the WorldDef to keep them alive
    final StateValues stateValues;

    private WorldID(
            @Nullable B3 instance,
            @Nullable Region region,
            StateValues values,
            long packedID
    ) {

        this.packedID = packedID;

        this.stateValues = values;

        if (instance != null && region != null) {
            region.register(this.state, () -> {
                values.closeReferences();
                instance.destroyWorld(packedID);
            });
        }

    }

    public long packedID() {
        this.state.ensureAccess();
        return this.packedID;
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

    @Override
    public String toString() {
        return toString(this.packedID);
    }



    static WorldID of(
            B3 instance,
            Region region,
            StateValues worldStateValues,
            MemorySegment segment
    ) {
        var identifier = PrimitiveMemOps.packWorldID(segment);
        return new WorldID(
                instance,
                region,
                worldStateValues,
                identifier
        );
    }

    static WorldID of(MemorySegment segment) {
        var identifier = PrimitiveMemOps.packWorldID(segment);
        return new WorldID(
                null,
                null,
                StateValues.none(),
                identifier
        );
    }

    static String toString(long packedID) {
        return "WorldID{"
                + "index1=" + PrimitiveMemOps.getWorldIDIndexFromPackedID(packedID)
                + ", generation=" + PrimitiveMemOps.getWorldIDGenerationFromPackedID(packedID)
                + '}';
    }

    // Class to hold on and close objects created my
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    static final class StateValues {

        private @Nullable AllocatedTaskCallbacks taskPool; // keep alive
        private @Nullable AllocatedShapeCallbacks shapes;  // keep alive

        static StateValues none() {
            return new StateValues(null, null);
        }

        public void closeReferences() {
            var taskPool = this.taskPool;
            var shapes = this.shapes;

            if (taskPool != null) {
                this.taskPool = null;
                taskPool.close();
            }
            if (shapes != null) {
                this.shapes = null;
                shapes.close();
            }
        }
    }


}

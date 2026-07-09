package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.U16;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.b3JointId;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;

public final class JointID {
    public static final JointID NULL_ID = new JointID(null, null, 0);

    private final long packedID;

    final AllocState state = AllocState.create();

    private JointID(
            @Nullable B3 instance,
            @Nullable Region region,
            long packedID
    ) {
        this.packedID = packedID;

        if (instance != null && region != null) {
            region.register(this.state, () -> {
                instance.destroyJoint(packedID);
            });
        }

    }

    public long packedID() {
        this.state.ensureAccess();
        return this.packedID;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JointID JointID)) {
            return false;
        }
        return this.packedID == JointID.packedID;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.packedID);
    }

    @Override
    public String toString() {
        return toString(this.packedID);
    }

    static JointID of(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        var identifier = PrimitiveMemOps.packJointID(segment);
        return new JointID(instance, region, identifier);
    }

    static String toString(long packedID) {
        return "JointID{" +
                "index1=" + PrimitiveMemOps.getJointIDIndexFromPacked(packedID) +
                ", world0=" + PrimitiveMemOps.getJointIDWorldFromPacked(packedID) +
                ", generation=" + PrimitiveMemOps.getJointIDGenerationFromPacked(packedID) +
                '}';
    }

}

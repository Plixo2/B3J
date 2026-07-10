package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.U16;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.b3JointId;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;

public final class JointID<T extends JointType> {
    public static final JointID<?> NULL_ID = new JointID<>(null, null, 0);

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

    @SuppressWarnings("unchecked")
    public JointID<JointType.Parallel> cast(JointType.Parallel type) {
        return (JointID<JointType.Parallel>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Distance> cast(JointType.Distance type) {
        return (JointID<JointType.Distance>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Filter> cast(JointType.Filter type) {
        return (JointID<JointType.Filter>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Motor> cast(JointType.Motor type) {
        return (JointID<JointType.Motor>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Prismatic> cast(JointType.Prismatic type) {
        return (JointID<JointType.Prismatic>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Revolute> cast(JointType.Revolute type) {
        return (JointID<JointType.Revolute>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Spherical> cast(JointType.Spherical type) {
        return (JointID<JointType.Spherical>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Weld> cast(JointType.Weld type) {
        return (JointID<JointType.Weld>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Wheel> cast(JointType.Wheel type) {
        return (JointID<JointType.Wheel>)this;
    }


    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JointID<?> JointID)) {
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

    static <T extends JointType> JointID<T> of(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        var identifier = PrimitiveMemOps.packJointID(segment);
        return new JointID<>(instance, region, identifier);
    }

    static String toString(long packedID) {
        return "JointID{" +
                "index1=" + PrimitiveMemOps.getJointIDIndexFromPacked(packedID) +
                ", world0=" + PrimitiveMemOps.getJointIDWorldFromPacked(packedID) +
                ", generation=" + PrimitiveMemOps.getJointIDGenerationFromPacked(packedID) +
                '}';
    }

}

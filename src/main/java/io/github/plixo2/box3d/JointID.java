package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;

import java.lang.foreign.MemorySegment;

public final class JointID<T extends JointType> {

    public static final JointID<?> NULL_ID = new JointID<>(0);

    @SuppressWarnings("unchecked")
    public static <T extends JointType> JointID<T> NULL_ID() {
        return (JointID<T>) NULL_ID;
    }

    public static <T extends JointType> JointID<T> fromUnknown(long packedID) {
        return new JointID<>(packedID);
    }


    private final long packedID;

    private JointID(long packedID) {
        this.packedID = packedID;
    }

    public long packedID() {
        return this.packedID;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Parallel> reinterpret(JointType.Parallel type) {
        return (JointID<JointType.Parallel>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Distance> reinterpret(JointType.Distance type) {
        return (JointID<JointType.Distance>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Filter> reinterpret(JointType.Filter type) {
        return (JointID<JointType.Filter>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Motor> reinterpret(JointType.Motor type) {
        return (JointID<JointType.Motor>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Prismatic> reinterpret(JointType.Prismatic type) {
        return (JointID<JointType.Prismatic>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Revolute> reinterpret(JointType.Revolute type) {
        return (JointID<JointType.Revolute>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Spherical> reinterpret(JointType.Spherical type) {
        return (JointID<JointType.Spherical>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Weld> reinterpret(JointType.Weld type) {
        return (JointID<JointType.Weld>)this;
    }

    @SuppressWarnings("unchecked")
    public JointID<JointType.Wheel> reinterpret(JointType.Wheel type) {
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
            MemorySegment segment
    ) {
        var identifier = PrimitiveMemOps.packID(segment);
        return new JointID<>(identifier);
    }

    static String toString(long packedID) {
        return "JointID{" +
                "index1=" + PrimitiveMemOps.getIndexFromPacked(packedID) +
                ", world0=" + PrimitiveMemOps.getWorldFromPacked(packedID) +
                ", generation=" + PrimitiveMemOps.getGenerationFromPacked(packedID) +
                '}';
    }

}

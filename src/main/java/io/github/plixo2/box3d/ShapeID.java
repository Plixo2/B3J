package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;

import java.lang.foreign.MemorySegment;

public final class ShapeID {
    public static final ShapeID NULL_ID = new ShapeID(0);

    private final long packedID;

    private ShapeID(long packedID) {
        this.packedID = packedID;
    }

    public long packedID() {
        return this.packedID;
    }


    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ShapeID shapeID)) {
            return false;
        }
        return this.packedID == shapeID.packedID;
    }


    @Override
    public int hashCode() {
        return Long.hashCode(this.packedID);
    }

    @Override
    public String toString() {
        return "ShapeID{" +
                "index1=" + PrimitiveMemOps.getIndexFromPacked(this.packedID) +
                ", world0=" + PrimitiveMemOps.getWorldFromPacked(this.packedID) +
                ", generation=" + PrimitiveMemOps.getGenerationFromPacked(this.packedID) +
                '}';
    }

    static ShapeID of(MemorySegment segment) {
        var identifier = PrimitiveMemOps.packID(segment);
        return new ShapeID(identifier);
    }

    public static ShapeID fromUnknown(long packedID) {
        return new ShapeID(packedID);
    }

}

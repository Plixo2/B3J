package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.U16;
import io.github.plixo2.box3d.region.Region;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;

public final class BodyID {
    public static final BodyID NULL_ID = new BodyID(null, null, 0);

    private final long packedID;

    final AllocState state = AllocState.create();

    private BodyID(
            @Nullable B3 instance,
            @Nullable Region region,
            long packedID
    ) {
        this.packedID = packedID;

        if (instance != null && region != null) {
            region.register(this.state, () -> {
                instance.destroyBody(packedID);
            });
        }

    }

    public long packedID() {
        this.state.ensureAccess();
        return this.packedID;
    }

    public int index1() {
        return PrimitiveMemOps.getBodyIDIndexFromPacked(this.packedID);
    }
    public @U16 int world0() {
        return PrimitiveMemOps.getBodyIDWorldFromPacked(this.packedID);
    }
    public @U16 int generation() {
        return PrimitiveMemOps.getBodyIDGenerationFromPacked(this.packedID);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BodyID bodyID)) {
            return false;
        }
        return this.packedID == bodyID.packedID;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.packedID);
    }

    @Override
    public String toString() {
        return toString(this.packedID);
    }

    static BodyID of(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        return of(instance, region, segment, 0);
    }

    static BodyID of(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment,
            long offset
    ) {
        var identifier = PrimitiveMemOps.packBodyID(segment, offset);

        return new BodyID(
                instance,
                region,
                identifier
        );
    }

    static String toString(long packedID) {
        return "BodyID{" +
                "index1=" + PrimitiveMemOps.getBodyIDIndexFromPacked(packedID) +
                ", world0=" + PrimitiveMemOps.getBodyIDWorldFromPacked(packedID) +
                ", generation=" + PrimitiveMemOps.getBodyIDGenerationFromPacked(packedID) +
                '}';
    }

}

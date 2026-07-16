package io.github.plixo2.box3d;

import io.github.plixo2.box3d.region.Lifetime;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.region.Region;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;

public final class BodyID {

    public static final BodyID NULL_ID = new BodyID(null, null, 0);

    public static BodyID fromUnknown(long packedID) {
        return fromUnknown(packedID, null);
    }

    public static BodyID fromUnknown(long packedID, @Nullable Region region) {
        B3 b3 = null;
        if (region != null) {
            b3 = B3.get();
        }
        return new BodyID(
                b3,
                region,
                packedID
        );
    }


    private final long packedID;

    @Getter
    private final Lifetime lifetime = Lifetime.create();

    private BodyID(
            @Nullable B3 instance,
            @Nullable Region region,
            long packedID
    ) {

        this.packedID = packedID;

        if (instance != null && region != null) {
            region.register(this.lifetime, () -> {
                instance.destroyBody(this);
            });
        }

    }

    public long packedID() {
        this.lifetime.ensureAccess();
        return this.packedID;
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
            B3 instance,
            Region region,
            MemorySegment segment
    ) {
        var identifier = PrimitiveMemOps.packID(segment);

        return new BodyID(
                instance,
                region,
                identifier
        );
    }

    static BodyID of(
            MemorySegment segment
    ) {
        return of(segment, 0);
    }


    static BodyID of(
            MemorySegment segment,
            long offset
    ) {
        var identifier = PrimitiveMemOps.packID(segment, offset);
        return new BodyID(
                null,
                null,
                identifier
        );
    }

    static String toString(long packedID) {
        return "BodyID{" +
                "index1=" + PrimitiveMemOps.getIndexFromPacked(packedID) +
                ", world0=" + PrimitiveMemOps.getWorldFromPacked(packedID) +
                ", generation=" + PrimitiveMemOps.getGenerationFromPacked(packedID) +
                '}';
    }

}

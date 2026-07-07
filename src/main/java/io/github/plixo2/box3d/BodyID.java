package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.U16;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.b3BodyId;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class BodyID {
    public static final BodyID NULL_ID = new BodyID(null, null, 0, 0, 0);

    final int index1;
    final @U16 int world0;
    final @U16 int generation;

    private final int hashCode;

    private final AllocState state = AllocState.create();

    private BodyID(
            @Nullable B3 instance,
            @Nullable Region region,
            int index1,
            int world0,
            int generation
    ) {
        this.index1 = index1;
        this.world0 = world0;
        this.generation = generation;
        this.hashCode = Objects.hash(index1, world0, generation);

        if (instance != null && region != null) {
            region.register(this.state, () -> {
                instance.destoryBody(this.index1, this.world0, this.generation);
            });
        }

    }

    void ensureAccess() {
        this.state.ensureAccess();
    }

    public int index1() {
        this.state.ensureAccess();
        return this.index1;
    }
    public @U16 int world0() {
        this.state.ensureAccess();
        return this.world0;
    }
    public @U16 int generation() {
        this.state.ensureAccess();
        return this.generation;
    }



    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BodyID bodyID)) {
            return false;
        }
        return this.index1 == bodyID.index1
                && this.world0 == bodyID.world0
                && this.generation == bodyID.generation;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "BodyID{"
                + "index1=" + this.index1
                + ", world0=" + this.world0
                + ", generation=" + this.generation +
                '}';
    }

    static BodyID of(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        var index1 = b3BodyId.index1(segment);
        var world0 = Short.toUnsignedInt(b3BodyId.world0(segment));
        var generation =  Short.toUnsignedInt(b3BodyId.generation(segment));

        return new BodyID(
                instance,
                region,
                index1,
                world0,
                generation
        );
    }

}

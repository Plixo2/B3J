package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U16;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.box2d.box3d.b3ShapeId;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShapeID {

    public static final ShapeID NULL_ID = new ShapeID(0, 0, 0);

    private final int index1;
    private final @U16 int world0;
    private final @U16 int generation;

    public int index1() {
        return this.index1;
    }
    public @U16 int world0() {
        return this.world0;
    }
    public @U16 int generation() {
        return this.generation;
    }


    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ShapeID shapeID)) {
            return false;
        }
        return this.index1 == shapeID.index1 && this.world0 == shapeID.world0 &&
                this.generation == shapeID.generation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.index1, this.world0, this.generation);
    }

    @Override
    public String toString() {
        return "ShapeID{"
                + "index1=" + this.index1
                + ", world0=" + this.world0
                + ", generation=" + this.generation
                + '}';
    }

    static ShapeID of(MemorySegment segment) {
        var index1 = b3ShapeId.index1(segment);
        var world0 = b3ShapeId.world0(segment);
        var generation = b3ShapeId.generation(segment);
        return new ShapeID(index1, Short.toUnsignedInt(world0), Short.toUnsignedInt(generation));
    }

    static ShapeID copy(ShapeID shapeID) {
        return new ShapeID(shapeID.index1, shapeID.world0, shapeID.generation);
    }


}

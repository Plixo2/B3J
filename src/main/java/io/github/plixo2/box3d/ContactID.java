package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Unsigned;
import org.box2d.box3d.b3ContactId;

import java.lang.foreign.MemorySegment;

public record ContactID(int index1, @Unsigned short world0, @Unsigned int generation) {
    public static final ContactID NULL_ID = new ContactID(0, (short) 0, 0);

    public long packedID() {
        throw new RuntimeException("Not implemented");
    }

    static ContactID of(MemorySegment segment) {
        var index1 = b3ContactId.index1(segment);
        var world0 = b3ContactId.world0(segment);
        var generation = b3ContactId.generation(segment);
        return new ContactID(index1, world0, generation);
    }

}

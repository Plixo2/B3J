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
        return of(segment, 0);
    }

    static ContactID of(MemorySegment segment, long offset) {
        var index1 = segment.get(b3ContactId.index1$layout(), offset + b3ContactId.index1$offset());
        var world0 = segment.get(b3ContactId.world0$layout(), offset + b3ContactId.world0$offset());
        var generation = segment.get(b3ContactId.generation$layout(), offset + b3ContactId.generation$offset());
        return new ContactID(index1, world0, generation);
    }

}

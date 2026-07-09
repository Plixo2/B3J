package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3RayCastInput;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public class RayCastInput {

    private Vector3f origin;
    private Vector3f translation;

    private float maxFraction;

    public RayCastInput(Vector3f origin, Vector3f translation, float maxFraction) {
        this.origin = origin;
        this.translation = translation;
        this.maxFraction = maxFraction;
    }

    MemorySegment create(SegmentAllocator arena) {
        var segment = b3RayCastInput.allocate(arena);
        PrimitiveMemOps.putVec3(b3RayCastInput.origin(segment), this.origin);
        PrimitiveMemOps.putVec3(b3RayCastInput.translation(segment), this.translation);
        b3RayCastInput.maxFraction(segment, this.maxFraction);
        return segment;
    }

}

package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3RayCastInput;
import org.box2d.box3d.b3ShapeCastInput;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Setter
@Getter
public class ShapeCastInput {

    private ShapeProxy proxy;
    private Vector3f translation;
    private float maxFraction;
    private boolean canEncroach;

    public ShapeCastInput(
            ShapeProxy proxy,
            Vector3f translation,
            float maxFraction,
            boolean canEncroach
    ) {
        this.proxy = proxy;
        this.translation = translation;
        this.maxFraction = maxFraction;
        this.canEncroach = canEncroach;
    }

    MemorySegment create(SegmentAllocator arena) {
        var segment = b3ShapeCastInput.allocate(arena);

        b3ShapeCastInput.proxy(segment, this.proxy.create(arena));
        PrimitiveMemOps.putVec3(b3ShapeCastInput.translation(segment), this.translation);
        b3ShapeCastInput.maxFraction(segment, this.maxFraction);
        b3ShapeCastInput.canEncroach(segment, this.canEncroach);

        return segment;
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3DistanceInput;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Getter
@Setter
public class DistanceInput {

    private ShapeProxy proxyA;
    private ShapeProxy proxyB;

    private Matrix4f transform = new Matrix4f();

    private boolean useRadii;

    public DistanceInput(
            ShapeProxy proxyA,
            ShapeProxy proxyB
    ) {
        this.proxyA = proxyA;
        this.proxyB = proxyB;
    }

    MemorySegment create(Quaternionf tempQuat, SegmentAllocator arena) {
        MemorySegment segment = b3DistanceInput.allocate(arena);
        b3DistanceInput.proxyA(segment, this.proxyA.create(arena));
        b3DistanceInput.proxyB(segment, this.proxyB.create(arena));
        PrimitiveMemOps.putTransform(b3DistanceInput.transform(segment), tempQuat, this.transform);
        b3DistanceInput.useRadii(segment, this.useRadii);
        return segment;
    }

}

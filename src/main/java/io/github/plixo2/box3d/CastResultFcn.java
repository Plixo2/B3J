package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Unsigned;
import org.joml.Vector3f;

@FunctionalInterface
public interface CastResultFcn {

    float onHit(
            ShapeID shapeId,
            Vector3f point,
            Vector3f normal,
            float fraction,
            @Unsigned long userMaterialId,
            int triangleIndex,
            int childIndex
    );

}

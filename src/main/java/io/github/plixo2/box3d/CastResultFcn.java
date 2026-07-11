package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U64;
import org.joml.Vector3f;

public interface CastResultFcn {

    float onHit(
            ShapeID shapeId,
            Vector3f point,
            Vector3f normal,
            float fraction,
            @U64 long userMaterialId,
            int triangleIndex,
            int childIndex
    );

}

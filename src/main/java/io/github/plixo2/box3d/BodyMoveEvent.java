package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitveMemOps;
import lombok.Getter;
import org.box2d.box3d.b3BodyMoveEvent;
import org.joml.Matrix4f;

import java.lang.foreign.MemorySegment;

@Getter
public class BodyMoveEvent {

    private BodyID bodyID;
    private boolean fellAsleep;
    private final Matrix4f transform = new Matrix4f();
    private long userData;

    BodyMoveEvent() {

    }

    BodyMoveEvent set(MemorySegment segment) {
        this.bodyID = BodyID.of(null, null, b3BodyMoveEvent.bodyId(segment));
        this.fellAsleep = b3BodyMoveEvent.fellAsleep(segment);
        PrimitveMemOps.setTransform(this.transform, b3BodyMoveEvent.transform(segment));
        this.userData = b3BodyMoveEvent.userData(segment).address();
        return this;
    }

}

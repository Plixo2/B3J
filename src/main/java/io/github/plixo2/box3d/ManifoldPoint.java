package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.Unsigned;
import lombok.Getter;
import org.box2d.box3d.b3ManifoldPoint;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ManifoldPoint {

    @Getter
    private final Vector3f anchorA = new Vector3f();

    @Getter
    private final Vector3f anchorB = new Vector3f();

    @Getter
    private float separation;

    @Getter
    private float baseSeparation;

    @Getter
    private float normalImpulse;

    @Getter
    private float totalNormalImpulse;

    @Getter
    private float normalVelocity;

    @Getter
    private @Unsigned int featureID;

    @Getter
    private int triangleIndex;

    @Getter
    private boolean persisted;

    ManifoldPoint() {}

    public ManifoldPoint(ManifoldPoint other) {
        this.anchorA.set(other.anchorA);
        this.anchorB.set(other.anchorB);
        this.separation = other.separation;
        this.baseSeparation = other.baseSeparation;
        this.normalImpulse = other.normalImpulse;
        this.totalNormalImpulse = other.totalNormalImpulse;
        this.normalVelocity = other.normalVelocity;
        this.featureID = other.featureID;
        this.triangleIndex = other.triangleIndex;
        this.persisted = other.persisted;
    }

    ManifoldPoint set(MemorySegment segment, long offset) {
        PrimitiveMemOps.setVec3(this.anchorA, segment, offset + b3ManifoldPoint.anchorA$offset());
        PrimitiveMemOps.setVec3(this.anchorB, segment, offset + b3ManifoldPoint.anchorB$offset());
        this.separation = segment.get(ValueLayout.JAVA_FLOAT, offset + b3ManifoldPoint.separation$offset());
        this.baseSeparation = segment.get(ValueLayout.JAVA_FLOAT, offset + b3ManifoldPoint.baseSeparation$offset());
        this.normalImpulse = segment.get(ValueLayout.JAVA_FLOAT, offset + b3ManifoldPoint.normalImpulse$offset());
        this.totalNormalImpulse = segment.get(ValueLayout.JAVA_FLOAT, offset + b3ManifoldPoint.totalNormalImpulse$offset());
        this.normalVelocity = segment.get(ValueLayout.JAVA_FLOAT, offset + b3ManifoldPoint.normalVelocity$offset());
        this.featureID = segment.get(ValueLayout.JAVA_INT, offset + b3ManifoldPoint.featureId$offset());
        this.triangleIndex = segment.get(ValueLayout.JAVA_INT, offset + b3ManifoldPoint.triangleIndex$offset());
        this.persisted = segment.get(ValueLayout.JAVA_BOOLEAN, offset + b3ManifoldPoint.persisted$offset());
        return this;
    }

}

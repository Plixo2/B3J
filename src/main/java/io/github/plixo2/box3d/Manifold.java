package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import org.box2d.box3d.b3Manifold;
import org.box2d.box3d.b3ManifoldPoint;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Manifold {

    @Getter
    private final ManifoldPoint[] points = createPoints();

    @Getter
    private final Vector3f normal = new Vector3f();

    @Getter
    private float twistImpulse;

    @Getter
    private final Vector3f frictionImpulse = new Vector3f();

    @Getter
    private final Vector3f rollingImpulse = new Vector3f();

    @Getter
    private int pointCount;

    Manifold() {}

    public Manifold(Manifold other) {
        for (var i = 0; i < this.points.length; i++) {
            if (i < other.pointCount) {
                this.points[i] = new ManifoldPoint(other.points[i]);
            } else {
                this.points[i] = new ManifoldPoint();
            }
        }
        this.normal.set(other.normal);
        this.twistImpulse = other.twistImpulse;
        this.frictionImpulse.set(other.frictionImpulse);
        this.rollingImpulse.set(other.rollingImpulse);
        this.pointCount = other.pointCount;
    }

    Manifold set(MemorySegment segment, long offset) {
        PrimitiveMemOps.setVec3(this.normal, segment, offset + b3Manifold.normal$offset());
        this.twistImpulse = segment.get(ValueLayout.JAVA_FLOAT, offset + b3Manifold.twistImpulse$offset());
        PrimitiveMemOps.setVec3(this.frictionImpulse, segment, offset + b3Manifold.frictionImpulse$offset());
        PrimitiveMemOps.setVec3(this.rollingImpulse, segment, offset + b3Manifold.rollingImpulse$offset());
        this.pointCount = segment.get(ValueLayout.JAVA_INT, offset + b3Manifold.pointCount$offset());
        var pointOffset = offset + b3Manifold.points$offset();
        var pointSize = b3ManifoldPoint.sizeof();
        for (var i = 0; i < this.pointCount; i++) {
            this.points[i].set(segment, pointOffset + i * pointSize);
        }
        return this;
    }

    private static ManifoldPoint[] createPoints() {
        var points = new ManifoldPoint[B3.MAX_MANIFOLD_POINTS];
        for (var i = 0; i < points.length; i++) {
            points[i] = new ManifoldPoint();
        }
        return points;
    }

}

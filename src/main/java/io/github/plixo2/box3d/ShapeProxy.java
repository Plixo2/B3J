package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import org.box2d.box3d.b3ShapeProxy;
import org.box2d.box3d.b3Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;

public class ShapeProxy {

    private MemorySegment points;
    private final @Nullable Vector3f singlePoint;
    private final int count;
    private final float radius;

    public ShapeProxy(Vector3f point, float radius) {
        this.singlePoint = Objects.requireNonNull(point);
        this.count = 1;
        this.radius = radius;
        this.points = null;
    }

    public ShapeProxy(
            MemorySegment points,
            int count,
            float radius
    ) {
        this.points = points;
        this.singlePoint = null;
        this.count = count;
        this.radius = radius;

        if (points.byteSize() != (long) count * 3 * Float.BYTES) {
            throw new IllegalArgumentException("Points segment size must be equal to count * 3 * Float.BYTES");
        }

    }

    public ShapeProxy(
            float[] points,
            float radius
    ) {
        if (points.length % 3 != 0) {
            throw new IllegalArgumentException("Points array length must be a multiple of 3");
        }
        this.points = Arena.ofAuto().allocateFrom(ValueLayout.JAVA_FLOAT, points);
        this.singlePoint = null;
        this.count = points.length / 3;
        this.radius = radius;
    }

    public ShapeProxy(
            FloatBuffer points,
            float radius
    ) {
        if (points.remaining() % 3 != 0) {
            throw new IllegalArgumentException("Points buffer remaining must be a multiple of 3");
        }
        this.points = Arena.ofAuto().allocate(ValueLayout.JAVA_FLOAT, points.remaining());
        this.points.copyFrom(MemorySegment.ofBuffer(points));
        this.singlePoint = null;
        this.count = points.remaining() / 3;
        this.radius = radius;
    }

    MemorySegment create(SegmentAllocator arena) {
        var segment = b3ShapeProxy.allocate(arena);
        if (this.singlePoint != null) {
            var vec = b3Vec3.allocate(arena);
            PrimitiveMemOps.putVec3(vec, this.singlePoint);
            b3ShapeProxy.points(segment, vec);
        } else {
            b3ShapeProxy.points(segment, this.points);
        }
        b3ShapeProxy.count(segment, this.count);
        b3ShapeProxy.radius(segment, this.radius);
        return segment;
    }

}

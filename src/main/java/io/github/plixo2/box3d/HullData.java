package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.MemoryIterator;
import io.github.plixo2.box3d.internal.U32;
import io.github.plixo2.box3d.internal.U64;
import org.box2d.box3d.*;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;


public non-sealed class HullData implements ShapeType.Shape {

    final MemorySegment segment;

    HullData(
            MemorySegment segment
    ) {
        this.segment = segment;
    }

    public @U64 long version() {
        return b3HullData.version(this.segment);
    }

    public int byteCount() {
        return b3HullData.byteCount(this.segment);
    }

    public @U32 int hash() {
        return b3HullData.hash(this.segment);
    }

    public AABB aabb(AABB in) {
        return in.set(b3HullData.aabb(this.segment));
    }

    public float surfaceArea() {
        return b3HullData.surfaceArea(this.segment);
    }

    public float volume() {
        return b3HullData.volume(this.segment);
    }

    public float innerRadius() {
        return b3HullData.innerRadius(this.segment);
    }

    public Vector3f center(Vector3f in) {
        return PrimitiveMemOps.setVec3(in, b3HullData.center(this.segment));
    }

    public Matrix3f centralInertia(Matrix3f in) {
        return PrimitiveMemOps.setMat3(in, b3HullData.centralInertia(this.segment));
    }

    public int vertexCount() {
        return b3HullData.vertexCount(this.segment);
    }

    public int vertexOffset() {
        return b3HullData.vertexOffset(this.segment);
    }

    public int pointOffset() {
        return b3HullData.pointOffset(this.segment);
    }

    public int edgeCount() {
        return b3HullData.edgeCount(this.segment);
    }

    public int edgeOffset() {
        return b3HullData.edgeOffset(this.segment);
    }

    public int faceCount() {
        return b3HullData.faceCount(this.segment);
    }

    public int faceOffset() {
        return b3HullData.faceOffset(this.segment);
    }

    public int planeOffset() {
        return b3HullData.planeOffset(this.segment);
    }

    /// b3HullVertex*
    public MemorySegment vertices() {
        var offset = vertexOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }
        var bytesPerVertex = b3HullVertex.sizeof();
        return this.segment.asSlice(offset, (long) vertexCount() * bytesPerVertex);
    }

    /// b3Vec3*
    public MemorySegment points() {
        var offset = pointOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerPoint = b3Vec3.sizeof();
        return this.segment.asSlice(offset, (long) vertexCount() * bytesPerPoint);
    }

    /// b3HullHalfEdge*
    public MemorySegment edges() {
        var offset = edgeOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerEdge = b3HullHalfEdge.sizeof();
        return this.segment.asSlice(offset, (long) edgeCount() * bytesPerEdge);
    }

    /// b3HullFace*
    public MemorySegment faces() {
        var offset = faceOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerFace = b3HullFace.sizeof();
        return this.segment.asSlice(offset, (long) faceCount() * bytesPerFace);
    }

    /// b3Plane*
    public MemorySegment planes() {
        var offset = planeOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerPlane = b3Plane.sizeof();
        return this.segment.asSlice(offset, (long) faceCount() * bytesPerPlane);
    }

    public MemoryIterator<HullVertex> vertexIterator(HullVertex in) {
        var segment = vertices();
        return new MemoryIterator<>(
                in,
                segment,
                b3HullVertex.sizeof(),
                HullVertex::set
        );
    }

    public MemoryIterator<Vector3f> pointIterator(Vector3f in) {
        var segment = points();
        return new MemoryIterator<>(
                in,
                segment,
                b3Vec3.sizeof(),
                PrimitiveMemOps::setVec3
        );
    }

    public MemoryIterator<HullHalfEdge> edgeIterator(HullHalfEdge in) {
        var segment = edges();
        return new MemoryIterator<>(
                in,
                segment,
                b3HullHalfEdge.sizeof(),
                HullHalfEdge::set
        );
    }

    public MemoryIterator<HullFace> faceIterator(HullFace in) {
        var segment = faces();
        return new MemoryIterator<>(
                in,
                segment,
                b3HullFace.sizeof(),
                HullFace::set
        );
    }

    public MemoryIterator<Plane> planeIterator(Plane in) {
        var segment = planes();
        return new MemoryIterator<>(
                in,
                segment,
                b3Plane.sizeof(),
                Plane::set
        );
    }

}

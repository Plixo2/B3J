package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import org.box2d.box3d.b3Mesh;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public non-sealed class Mesh implements ShapeType.Shape {
    public MeshData data;
    public float scaleX;
    public float scaleY;
    public float scaleZ;

    public Mesh(MeshData data, float scaleX, float scaleY, float scaleZ) {
        this.data = data;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public Mesh(MeshData data, Vector3f scale) {
        this(data, scale.x, scale.y, scale.z);
    }

    public Mesh(Mesh other) {
        this.data = other.data;
        this.scaleX = other.scaleX;
        this.scaleY = other.scaleY;
        this.scaleZ = other.scaleZ;
    }

    MemorySegment create(SegmentAllocator arena) {
        var segment = b3Mesh.allocate(arena);
        b3Mesh.data(segment, this.data.segment());
        PrimitiveMemOps.putVec3(b3Mesh.scale(segment), this.scaleX, this.scaleY, this.scaleZ);
        return segment;
    }

    static Mesh of(MemorySegment segment) {
        var data = new MeshData(null, null, b3Mesh.data(segment));
        var scale = b3Mesh.scale(segment);
        var scaleX = PrimitiveMemOps.getVec3X(scale);
        var scaleY = PrimitiveMemOps.getVec3Y(scale);
        var scaleZ = PrimitiveMemOps.getVec3Z(scale);
        return new Mesh(data, scaleX, scaleY, scaleZ);
    }

}

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
    public Mesh(MeshData data) {
        this(data, 1.0f, 1.0f, 1.0f);
    }

    MemorySegment create(SegmentAllocator arena) {
        var segment = b3Mesh.allocate(arena);
        b3Mesh.data(segment, this.data.segment());
        PrimitiveMemOps.putVec3(b3Mesh.scale(segment), this.scaleX, this.scaleY, this.scaleZ);
        return segment;
    }

}

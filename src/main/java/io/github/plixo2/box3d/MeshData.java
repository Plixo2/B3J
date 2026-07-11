package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.MemoryIterator;
import io.github.plixo2.box3d.internal.U32;
import io.github.plixo2.box3d.internal.U64;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;


public class MeshData {
    private final MemorySegment segment;

    final AllocState state =  AllocState.create();

    MeshData(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        this.segment = segment;

        if (instance != null && region != null) {
            region.register(this.state, segment, instance::destroyMesh);
        }
    }

    MemorySegment segment() {
        this.state.ensureAccess();
        return this.segment;
    }

    public @U64 long version() {
        return b3MeshData.version(segment());
    }

    public int byteCount() {
        return b3MeshData.byteCount(segment());
    }

    public @U32 int hash() {
        return b3MeshData.hash(segment());
    }

    public AABB bounds(AABB in) {
        return in.set(b3MeshData.bounds(segment()));
    }

    public float surfaceArea() {
        return b3MeshData.surfaceArea(segment());
    }

    public int treeHeight() {
        return b3MeshData.treeHeight(segment());
    }

    public int degenerateCount() {
        return b3MeshData.degenerateCount(segment());
    }

    public int nodeOffset() {
        return b3MeshData.nodeOffset(segment());
    }

    public int nodeCount() {
        return b3MeshData.nodeCount(segment());
    }

    public int vertexOffset() {
        return b3MeshData.vertexOffset(segment());
    }

    public int vertexCount() {
        return b3MeshData.vertexCount(segment());
    }

    public int triangleOffset() {
        return b3MeshData.triangleOffset(segment());
    }

    public int triangleCount() {
        return b3MeshData.triangleCount(segment());
    }

    public int materialOffset() {
        return b3MeshData.materialOffset(segment());
    }

    public int materialCount() {
        return b3MeshData.materialCount(segment());
    }

    public int flagsOffset() {
        return b3MeshData.flagsOffset(segment());
    }

    /// b3MeshNode*
    public MemorySegment nodes() {
        var offset = nodeOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerNode = b3MeshNode.sizeof();
        return segment().asSlice(offset, (long) nodeCount() * bytesPerNode);
    }

    /// b3Vec3*
    public MemorySegment vertices() {
        var offset = vertexOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerVertex = b3Vec3.sizeof();
        return segment().asSlice(offset, (long) vertexCount() * bytesPerVertex);
    }

    /// b3MeshTriangle*
    public MemorySegment triangles() {
        var offset = triangleOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerTriangle = b3MeshTriangle.sizeof();
        return segment().asSlice(offset, (long) triangleCount() * bytesPerTriangle);
    }

    /// uint8_t*
    public MemorySegment materials() {
        var offset = materialOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        return segment().asSlice(offset, (long) triangleCount() * Byte.BYTES);
    }

    /// uint8_t*
    public MemorySegment flags() {
        var offset = flagsOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        return segment().asSlice(offset, (long) triangleCount() * Byte.BYTES);
    }

    public MemoryIterator<MeshNode> nodeIterator(MeshNode in) {
        var segment = nodes();
        return new MemoryIterator<>(
                in,
                segment,
                b3MeshNode.sizeof(),
                MeshNode::set
        );
    }

    public MemoryIterator<Vector3f> vertexIterator(Vector3f in) {
        var segment = vertices();
        return new MemoryIterator<>(
                in,
                segment,
                b3Vec3.sizeof(),
                PrimitiveMemOps::setVec3
        );
    }

    public MemoryIterator<MeshTriangle> triangleIterator(MeshTriangle in) {
        var segment = triangles();
        return new MemoryIterator<>(
                in,
                segment,
                b3MeshTriangle.sizeof(),
                MeshTriangle::set
        );
    }

    public MemoryIterator.OfU8 materialIterator() {
        var materials = materials();
        return new MemoryIterator.OfU8(materials);
    }

    public MemoryIterator.OfU8 flagIterator() {
        var flags = flags();
        return new MemoryIterator.OfU8(flags);
    }

}

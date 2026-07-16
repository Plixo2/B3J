package io.github.plixo2.box3d;

import io.github.plixo2.box3d.region.Lifetime;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.MemoryIterator;
import io.github.plixo2.box3d.internal.Unsigned;
import io.github.plixo2.box3d.region.Region;
import lombok.Getter;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;


public class MeshData {
    private final MemorySegment segment;

    @Getter
    private final Lifetime lifetime =  Lifetime.create();

    MeshData(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        this.segment = segment;

        if (instance != null && region != null) {
            region.register(this.lifetime, () -> {
                instance.destroyMesh(this);
            });
        }
    }

    MemorySegment segment() {
        this.lifetime.ensureAccess();
        return this.segment;
    }

    public @Unsigned long version() {
        return b3MeshData.version(segment());
    }

    public int byteCount() {
        return b3MeshData.byteCount(segment());
    }

    public @Unsigned int hash() {
        return b3MeshData.hash(segment());
    }

    public AABB bounds(AABB dest) {
        return dest.set(b3MeshData.bounds(segment()));
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

    public MemoryIterator<MeshNode> nodeIterator() {
        var segment = nodes();
        return new MemoryIterator<>(
                new MeshNode(),
                segment,
                b3MeshNode.sizeof(),
                MeshNode::set
        );
    }

    public MemoryIterator<Vector3f> vertexIterator() {
        var segment = vertices();
        return new MemoryIterator<>(
                new Vector3f(),
                segment,
                b3Vec3.sizeof(),
                (MemoryIterator.OffsetConsumer<Vector3f>) PrimitiveMemOps::setVec3
        );
    }

    public MemoryIterator<MeshTriangle> triangleIterator() {
        var segment = triangles();
        return new MemoryIterator<>(
                new MeshTriangle(),
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

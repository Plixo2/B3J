package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Internal;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3HeightFieldDef;
import org.box2d.box3d.b3MeshDef;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class MeshDef {

    // b3Vec3*
    private final MemorySegment vertices;
    private final int vertexCount;

    // int32_t*
    private final MemorySegment indices;
    private final int triangleCount;

    // uint8_t*
    private MemorySegment materialIndices = MemorySegment.NULL;

    @Getter
    @Setter
    float weldTolerance;

    @Getter
    @Setter
    private boolean weldVertices;

    @Getter
    @Setter
    private boolean useMedianSplit;

    @Getter
    @Setter
    private boolean identifyEdges;


    public MeshDef(
            MemorySegment vertices,
            int vertexCount,
            MemorySegment indices,
            int triangleCount
    ) {
        this.vertices = vertices;
        this.indices = indices;

        this.vertexCount = vertexCount;
        this.triangleCount = triangleCount;

        if (this.vertexCount < 3) {
            throw new IllegalArgumentException("vertexCount must be at least 3");
        }
        if (this.triangleCount < 1) {
            throw new IllegalArgumentException("triangleCount must be at least 1");
        }

    }

    public MeshDef(
            float[] vertices,
            int[] indices
    ) {
        ensureCount(vertices.length, indices.length);
        this(
                MemorySegment.ofArray(vertices),
                vertices.length / 3,
                MemorySegment.ofArray(indices),
                indices.length / 3
        );
    }

    public MeshDef(
            FloatBuffer vertices,
            IntBuffer indices
    ) {
        ensureCount(vertices.remaining(), indices.remaining());
        this(
                MemorySegment.ofBuffer(vertices),
                vertices.remaining() / 3,
                MemorySegment.ofBuffer(indices),
                indices.remaining() / 3
        );
    }


    public void materialIndices(@Nullable MemorySegment materialIndices) {
        if (materialIndices == null || materialIndices.byteSize() == 0 || materialIndices.address() == 0) {
            this.materialIndices = MemorySegment.NULL;
            return;
        }
        if (materialIndices.byteSize() != this.triangleCount) {
            throw new IllegalArgumentException("materialIndices length must be equal to triangleCount");
        }
        this.materialIndices = materialIndices;
    }

    public void materialIndices(byte[] materialIndices) {
        materialIndices(MemorySegment.ofArray(materialIndices));
    }
    public void materialIndices(ByteBuffer materialIndices) {
        materialIndices(MemorySegment.ofBuffer(materialIndices));
    }
    public void materialIndices(int[] materialIndices) {
        var bytes = new byte[materialIndices.length];
        for (int i = 0; i < materialIndices.length; i++) {
            bytes[i] = Internal.assertU8(materialIndices[i], "materialIndex");
        }
        materialIndices(bytes);
    }


    private static void ensureCount(int vertexCount, int indexCount) {
        if (vertexCount % 3 != 0) {
            throw new IllegalArgumentException("vertices length must be a multiple of 3");
        }
        if (indexCount % 3 != 0) {
            throw new IllegalArgumentException("indices length must be a multiple of 3");
        }
    }


    MemorySegment create(SegmentAllocator arena) {

        var segment = b3MeshDef.allocate(arena);

        b3MeshDef.vertices(segment, Internal.ensureOffHeap(arena, this.vertices));
        b3MeshDef.indices(segment, Internal.ensureOffHeap(arena, this.indices));

        b3MeshDef.materialIndices(segment, this.materialIndices);
        b3MeshDef.weldTolerance(segment, this.weldTolerance);
        b3MeshDef.vertexCount(segment, this.vertexCount);
        b3MeshDef.triangleCount(segment, this.triangleCount);
        b3MeshDef.weldVertices(segment, this.weldVertices);
        b3MeshDef.useMedianSplit(segment, this.useMedianSplit);
        b3MeshDef.identifyEdges(segment, this.identifyEdges);

        return segment;
    }

}

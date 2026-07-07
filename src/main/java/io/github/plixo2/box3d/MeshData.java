package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.AllocState;
import io.github.plixo2.box3d.internal.PrimitveMemOps;
import io.github.plixo2.box3d.internal.MemoryIterator;
import io.github.plixo2.box3d.internal.U32;
import io.github.plixo2.box3d.internal.U64;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

/*
typedef struct b3MeshData
{
	/// Version must be first.
	uint64_t version;

	/// The total number of bytes for this mesh.
	int byteCount;

	/// Hash of this mesh (this field is zero when the hash is computed)
	uint32_t hash;

	/// Local axis-aligned box.
	b3AABB bounds;

	/// Combined surface area of all triangles. Single-sided.
	float surfaceArea;

	/// The height of the bounding volume hierarchy.
	int treeHeight;

	/// The number of degenerate triangles. Diagnostic.
	int degenerateCount;

	/// Offset of the node array in bytes from the struct address.
	int nodeOffset;

	/// The number of BVH nodes.
	int nodeCount;

	/// Offset of the vertex array in bytes from the struct address.
	int vertexOffset;

	/// The number of vertices.
	int vertexCount;

	/// Offset of the triangle array in bytes from the struct address.
	int triangleOffset;

	/// The number of triangles.
	int triangleCount;

	/// Offset of the material array in bytes from the struct address.
	int materialOffset;

	/// The number of materials.
	int materialCount;

	/// Offset of the triangle flag array in bytes from the struct address.
	int flagsOffset;
} b3MeshData;


/// Get read only mesh BVH nodes.
B3_INLINE const b3MeshNode* b3GetMeshNodes( const b3MeshData* mesh )
{
	if ( mesh->nodeOffset == 0 )
	{
		return NULL;
	}

	return (const b3MeshNode*)( (intptr_t)mesh + mesh->nodeOffset );
}

/// Get read only mesh vertices.
B3_INLINE const b3Vec3* b3GetMeshVertices( const b3MeshData* mesh )
{
	if ( mesh->vertexOffset == 0 )
	{
		return NULL;
	}

	return (const b3Vec3*)( (intptr_t)mesh + mesh->vertexOffset );
}

/// Get read only mesh triangles.
B3_INLINE const b3MeshTriangle* b3GetMeshTriangles( const b3MeshData* mesh )
{
	if ( mesh->triangleOffset == 0 )
	{
		return NULL;
	}

	return (const b3MeshTriangle*)( (intptr_t)mesh + mesh->triangleOffset );
}

/// Get read only mesh materials. The count is equal to the triangle count.
B3_INLINE const uint8_t* b3GetMeshMaterialIndices( const b3MeshData* mesh )
{
	if ( mesh->materialOffset == 0 )
	{
		return NULL;
	}

	return (const uint8_t*)( (intptr_t)mesh + mesh->materialOffset );
}

/// Get read only mesh flags. The count is equal to the triangle count.
B3_INLINE const uint8_t* b3GetMeshFlags( const b3MeshData* mesh )
{
	if ( mesh->flagsOffset == 0 )
	{
		return NULL;
	}

	return (const uint8_t*)( (intptr_t)mesh + mesh->flagsOffset );
}


typedef struct b3MeshNode
{
	/// The lower bound of the node AABB. Strategic placement for SIMD.
	b3Vec3 lowerBound;

	/// Anonymous union.
	union
	{
		/// Internal node
		struct
		{
			/// Split axis. 0, 1, or 2.
			uint32_t axis : 2;
			/// Offset of the second child node.
			uint32_t childOffset : 30;
		} asNode;

		/// Leaf node
		struct
		{
			/// Aligned with axis above and has value of 3 if this is a leaf.
			uint32_t type : 2;

			/// The number of triangles for this leaf node.
			uint32_t triangleCount : 30;
		} asLeaf;
	} data;

	/// The upper bound of the node AABB.  Strategic placement for SIMD.
	b3Vec3 upperBound;

	/// The index of the leaf triangles.
	uint32_t triangleOffset;
} b3MeshNode;

 */
public class MeshData {
    private final MemorySegment segment;

    private final AllocState state =  AllocState.create();

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

        var bytesPerNode = b3MeshNode.layout().byteSize();
        return segment().asSlice(offset, (long) nodeCount() * bytesPerNode);
    }

    /// b3Vec3*
    public MemorySegment vertices() {
        var offset = vertexOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerVertex = b3Vec3.layout().byteSize();
        return segment().asSlice(offset, (long) vertexCount() * bytesPerVertex);
    }

    /// b3MeshTriangle*
    public MemorySegment triangles() {
        var offset = triangleOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        var bytesPerTriangle = b3MeshTriangle.layout().byteSize();
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
                b3MeshNode.layout().byteSize(),
                MeshNode::set
        );
    }

    public MemoryIterator<Vector3f> vertexIterator(Vector3f in) {
        var segment = vertices();
        return new MemoryIterator<>(
                in,
                segment,
                b3Vec3.layout().byteSize(),
                PrimitveMemOps::setVec3
        );
    }

    public MemoryIterator<MeshTriangle> triangleIterator(MeshTriangle in) {
        var segment = triangles();
        return new MemoryIterator<>(
                in,
                segment,
                b3MeshTriangle.layout().byteSize(),
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

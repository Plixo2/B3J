package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.*;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.b3HeightFieldData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

/*
typedef struct b3HeightFieldData
{
	/// Version must be first and match B3_HEIGHT_FIELD_VERSION
	uint64_t version;

	/// The total number of bytes for this height field.
	int byteCount;

	/// Hash of this height field (this field is zero when the hash is computed).
	uint32_t hash;

	/// The local axis-aligned bounding box.
	b3AABB aabb;

	/// The minimum y value.
	float minHeight;

	/// The maximum y value
	float maxHeight;

	/// The quantization scale.
	float heightScale;

	/// The overall scale.
	b3Vec3 scale;

	/// The number of grid columns along the local x-axis.
	int columnCount;

	/// The number of grid rows along the local z-axis.
	int rowCount;

	/// Offset of the compressed height array in bytes from the struct address.
	/// uint16_t, one per grid point.
	int heightsOffset;

	/// Offset of the material index array in bytes from the struct address.
	/// uint8_t, one per cell.
	int materialOffset;

	/// Offset of the flag array in bytes from the struct address.
	/// uint8_t, one per triangle.
	int flagsOffset;

	/// Triangle winding.
	bool clockwise;

	/// Explicit padding. Identity is a content hash over raw bytes, so there must
	/// be no unnamed padding for struct copies to scramble.
	uint8_t padding[3];
} b3HeightFieldData;
 */
public non-sealed class HeightFieldData implements ShapeType.Shape {

    private final MemorySegment segment;

    private final AllocState state =  AllocState.create();

    HeightFieldData(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        this.segment = segment;
        if (instance != null && region != null) {
            region.register(this.state, segment, instance::destroyHeightField);
        }
    }

    MemorySegment segment() {
        this.state.ensureAccess();
        return this.segment;
    }

    public @U64 long version() {
        return b3HeightFieldData.version(segment());
    }

    public int byteCount() {
        return b3HeightFieldData.byteCount(segment());
    }

    public @U32 int hash() {
        return b3HeightFieldData.hash(segment());
    }

    public AABB aabb(AABB in) {
        return in.set(b3HeightFieldData.aabb(segment()));
    }

    public float minHeight() {
        return b3HeightFieldData.minHeight(segment());
    }

    public float maxHeight() {
        return b3HeightFieldData.maxHeight(segment());
    }

    public float heightScale() {
        return b3HeightFieldData.heightScale(segment());
    }

    public Vector3f scale(Vector3f in) {
        return PrimitveMemOps.setVec3(in, b3HeightFieldData.scale(segment()));
    }

    public int columnCount() {
        return b3HeightFieldData.columnCount(segment());
    }

    public int rowCount() {
        return b3HeightFieldData.rowCount(segment());
    }

    public int heightsOffset() {
        return b3HeightFieldData.heightsOffset(segment());
    }

    public int materialOffset() {
        return b3HeightFieldData.materialOffset(segment());
    }

    public int flagsOffset() {
        return b3HeightFieldData.flagsOffset(segment());
    }

    public boolean clockwise() {
        return b3HeightFieldData.clockwise(segment());
    }

    /// uint16_t*
    public MemorySegment heights() {
        var offset = heightsOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }

        int gridPoints = rowCount() * columnCount();
        return this.segment.asSlice(offset, (long) gridPoints * Short.BYTES);
    }

    /// uint8_t*
    public MemorySegment materials() {
        var offset = materialOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }
        int cells = (rowCount() - 1) * (columnCount() - 1);
        return this.segment.asSlice(offset, (long) cells * Byte.BYTES);
    }

    /// uint8_t*
    public MemorySegment flags() {
        var offset = flagsOffset();
        if (offset == 0) {
            return MemorySegment.NULL;
        }
        int cells = (rowCount() - 1) * (columnCount() - 1);
        int triangles = cells * 2;
        return this.segment.asSlice(offset, (long) triangles * Byte.BYTES);
    }

    public MemoryIterator.OfU16 heightIterator() {
        var heights = heights();
        return new MemoryIterator.OfU16(heights);
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

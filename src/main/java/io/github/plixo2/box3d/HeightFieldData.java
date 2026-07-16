package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.*;
import io.github.plixo2.box3d.region.Lifetime;
import io.github.plixo2.box3d.region.Region;
import lombok.Getter;
import org.box2d.box3d.b3HeightFieldData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

public non-sealed class HeightFieldData implements ShapeType.Shape {

    private final MemorySegment segment;


    @Getter
    private final Lifetime lifetime =  Lifetime.create();

    HeightFieldData(
            @Nullable B3 instance,
            @Nullable Region region,
            MemorySegment segment
    ) {
        this.segment = segment;
        if (instance != null && region != null) {
            region.register(this.lifetime, () -> {
                instance.destroyHeightField(this);
            });
        }
    }

    MemorySegment segment() {
        this.lifetime.ensureAccess();
        return this.segment;
    }

    public @Unsigned long version() {
        return b3HeightFieldData.version(segment());
    }

    public int byteCount() {
        return b3HeightFieldData.byteCount(segment());
    }

    public @Unsigned int hash() {
        return b3HeightFieldData.hash(segment());
    }

    public AABB aabb(AABB dest) {
        return dest.set(b3HeightFieldData.aabb(segment()));
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

    public Vector3f scale(Vector3f dest) {
        return PrimitiveMemOps.setVec3(dest, b3HeightFieldData.scale(segment()));
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

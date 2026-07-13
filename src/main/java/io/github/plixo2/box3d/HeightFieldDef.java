package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.B3JUtil;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3HeightFieldDef;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class HeightFieldDef {

    // float*;
    private final MemorySegment heights;

    // uint8_t*
    private MemorySegment materialIndices = MemorySegment.NULL;

    @Getter
    @Setter
    private Vector3f scale = new Vector3f(1f);

    @Getter
    private final int countX;

    @Getter
    private final int countZ;

    @Getter
    @Setter
    private float globalMinimumHeight;
    @Getter
    @Setter
    private float globalMaximumHeight;

    @Getter
    @Setter
    private boolean clockwiseWinding;


    public HeightFieldDef(
            int countX,
            int countZ,
            MemorySegment heights
    ) {
        this.countX = countX;
        this.countZ = countZ;
        this.heights = heights;

        var expectedLength = countX * countZ * Float.BYTES;
        if (heights.byteSize() != expectedLength) {
            throw new IllegalArgumentException("heights length must be equal to countX * countZ * sizeof(float) = " + expectedLength);
        }
    }

    public HeightFieldDef(
            int countX,
            int countZ,
            float[] heights
    ) {
        this(countX, countZ, MemorySegment.ofArray(heights));
    }
    public HeightFieldDef(
            int countX,
            int countZ,
            FloatBuffer heights
    ) {
        this(countX, countZ, MemorySegment.ofBuffer(heights));
    }


    public void materialIndices(@Nullable MemorySegment materialIndices) {
        if (materialIndices == null || materialIndices.byteSize() == 0 || materialIndices.address() == 0) {
            this.materialIndices = MemorySegment.NULL;
            return;
        }
        var count = (this.countX - 1) * (this.countZ - 1);
        if (materialIndices.byteSize() != count) {
            throw new IllegalArgumentException("materialIndices length must be equal to (countX - 1) * (countZ - 1) = " + count);
        }
        this.materialIndices = materialIndices;
    }

    public void materialIndices(byte[] materialIndices) {
        materialIndices(MemorySegment.ofArray(materialIndices));
    }
    public void materialIndices(ByteBuffer materialIndices) {
        materialIndices(MemorySegment.ofBuffer(materialIndices));
    }

    MemorySegment create(SegmentAllocator arena) {

        var segment = b3HeightFieldDef.allocate(arena);

        b3HeightFieldDef.heights(segment, B3JUtil.ensureOffHeap(arena, this.heights));

        b3HeightFieldDef.materialIndices(segment, this.materialIndices);
        PrimitiveMemOps.putVec3(b3HeightFieldDef.scale(segment), this.scale);
        b3HeightFieldDef.countX(segment, this.countX);
        b3HeightFieldDef.countZ(segment, this.countZ);
        b3HeightFieldDef.globalMinimumHeight(segment, this.globalMinimumHeight);
        b3HeightFieldDef.globalMaximumHeight(segment, this.globalMaximumHeight);
        b3HeightFieldDef.clockwiseWinding(segment, this.clockwiseWinding);

        return segment;
    }

}

package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.OverlapResult;
import io.github.plixo2.box3d.ShapeID;
import org.box2d.box3d.b3ShapeId;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

import static org.box2d.box3d.box3d_h.b3jshimWorld_OverlapAABB;

public final class ScratchOverlapAABB {

    private final ShimArgBuffer buffer;

    public ScratchOverlapAABB(Arena arena) {
        this.buffer = new ShimArgBuffer(arena);
    }

    public MemorySegment invoke(
            SegmentAllocator returnArena,
            MemorySegment worldID,
            MemorySegment aabb,
            MemorySegment queryFilter,
            OverlapResult fcn
    ) {
        var stats = b3jshimWorld_OverlapAABB(
                returnArena,
                worldID,
                aabb,
                queryFilter,
                this.buffer.pointer()
        );

        var count = this.buffer.elementCount();
        if (count == 0) {
            return stats;
        }

        var total_size = b3ShapeId.sizeof();
        var data = this.buffer.data();

        for (var i = 0; i < count; i++) {
            var byteOffset = i * total_size;
            var packedID = PrimitiveMemOps.packShapeID(data, byteOffset);
            if (!fcn.onOverlap(ShapeID.fromUnknown(packedID))) {
                break;
            }
        }

        return stats;
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.region.Lifetime;
import io.github.plixo2.box3d.internal.Unsigned;
import io.github.plixo2.box3d.region.Region;
import lombok.Getter;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class DynamicTree {
    private static final long nodesOffset = b3DynamicTree.nodes$offset();
    private static final long nodeSize = b3TreeNode.sizeof();
    private static final long nodeUserDataOffset = b3TreeNode.userData$offset();
    private static final long nodeAABBOffset = b3TreeNode.aabb$offset();

    private final MemorySegment segment;

    final Arena confinedRegion;
    @Getter
    private final Lifetime lifetime = Lifetime.create();


    DynamicTree(
            @Nullable B3 instance,
            @Nullable Region region,
            Arena confinedRegion,
            MemorySegment segment
    ) {

        this.confinedRegion = confinedRegion;
        this.segment = segment;

        if (instance != null && region != null) {
            region.register(this.lifetime, () -> {
                instance.dynamicTreeDestroy(this);
            });
        }

    }

    MemorySegment segment() {
        this.lifetime.ensureAccess();
        return this.segment;
    }



    @Unsigned long getUserData(int proxyId) {
        return nodes().get(ValueLayout.JAVA_LONG, proxyId * nodeSize + nodeUserDataOffset);
    }

    AABB getAABB(AABB dest, int proxyId) {
        dest.set(nodes(), proxyId * nodeSize + nodeAABBOffset);
        return dest;
    }


    // cache
    private long lastNodeAddress = 0L;
    private int lastNodeCapacity = 0;
    private @Nullable MemorySegment lastNodeSegment = null;

    MemorySegment nodes() {
        this.lifetime.ensureAccess();

        var nodeAddress = this.segment.get(ValueLayout.JAVA_LONG, nodesOffset);
        var nodeCapacity = b3DynamicTree.nodeCapacity(this.segment);

        if (
               this.lastNodeSegment == null
            || this.lastNodeAddress != nodeAddress
            || this.lastNodeCapacity != nodeCapacity
        ) {
            this.lastNodeAddress = nodeAddress;
            this.lastNodeCapacity = nodeCapacity;

            this.lastNodeSegment = b3DynamicTree.nodes(this.segment).reinterpret(nodeCapacity * nodeSize);
        }

        return this.lastNodeSegment;
    }

}

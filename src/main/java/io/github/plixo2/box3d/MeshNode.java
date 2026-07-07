package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitveMemOps;
import io.github.plixo2.box3d.internal.U32;
import org.box2d.box3d.b3MeshNode;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class MeshNode {

    public Vector3f lowerBound = new Vector3f();

    public @U32 int axis;

    public @U32 int childOffset;

    public @U32 int type;

    public @U32 int triangleCount;

    public Vector3f upperBound = new Vector3f();

    public @U32 int triangleOffset;

    public MeshNode() {

    }

    public MeshNode(MeshNode other) {
        this.lowerBound.set(other.lowerBound);
        this.axis = other.axis;
        this.childOffset = other.childOffset;
        this.type = other.type;
        this.triangleCount = other.triangleCount;
        this.upperBound.set(other.upperBound);
        this.triangleOffset = other.triangleOffset;
    }

    public boolean leaf() {
        return this.type == 3;
    }

    MeshNode set(MemorySegment segment) {
        PrimitveMemOps.setVec3(this.lowerBound, b3MeshNode.lowerBound(segment));
        var data = b3MeshNode.data(segment).get(ValueLayout.JAVA_INT, 0);
        this.axis = data & 0x3;
        this.childOffset = data >>> 2;
        this.type = data & 0x3;
        this.triangleCount = data >>> 2;
        PrimitveMemOps.setVec3(this.upperBound, b3MeshNode.upperBound(segment));
        this.triangleOffset = b3MeshNode.triangleOffset(segment);
        return this;
    }

}

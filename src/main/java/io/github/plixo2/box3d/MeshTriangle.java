package io.github.plixo2.box3d;

import org.box2d.box3d.b3MeshTriangle;

import java.lang.foreign.MemorySegment;

public class MeshTriangle {

    public int index1;

    public int index2;

    public int index3;

    public MeshTriangle(int index1, int index2, int index3) {
        this.index1 = index1;
        this.index2 = index2;
        this.index3 = index3;
    }

    public MeshTriangle(MeshTriangle other) {
        this.index1 = other.index1;
        this.index2 = other.index2;
        this.index3 = other.index3;
    }

    MeshTriangle set(MemorySegment segment) {
        this.index1 = b3MeshTriangle.index1(segment);
        this.index2 = b3MeshTriangle.index2(segment);
        this.index3 = b3MeshTriangle.index3(segment);
        return this;
    }

}

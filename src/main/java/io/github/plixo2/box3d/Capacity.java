package io.github.plixo2.box3d;


import org.box2d.box3d.b3Capacity;

import java.lang.foreign.MemorySegment;

public class Capacity {
    public int staticShapeCount;
    public int dynamicShapeCount;
    public int staticBodyCount;
    public int dynamicBodyCount;
    public int contactCount;

    public Capacity() {

    }

    public Capacity(Capacity other) {
        this.staticShapeCount = other.staticShapeCount;
        this.dynamicShapeCount = other.dynamicShapeCount;
        this.staticBodyCount = other.staticBodyCount;
        this.dynamicBodyCount = other.dynamicBodyCount;
        this.contactCount = other.contactCount;
    }

    Capacity set(MemorySegment segment) {
        this.staticShapeCount = b3Capacity.staticShapeCount(segment);
        this.dynamicShapeCount = b3Capacity.dynamicShapeCount(segment);
        this.staticBodyCount = b3Capacity.staticBodyCount(segment);
        this.dynamicBodyCount = b3Capacity.dynamicBodyCount(segment);
        this.contactCount = b3Capacity.contactCount(segment);
        return this;
    }

    void put(MemorySegment segment) {
        b3Capacity.staticShapeCount(segment, this.staticShapeCount);
        b3Capacity.dynamicShapeCount(segment, this.dynamicShapeCount);
        b3Capacity.staticBodyCount(segment, this.staticBodyCount);
        b3Capacity.dynamicBodyCount(segment, this.dynamicBodyCount);
        b3Capacity.contactCount(segment, this.contactCount);
    }

}

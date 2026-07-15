package io.github.plixo2.box3d;

import org.box2d.box3d.b3MotionLocks;

import java.lang.foreign.MemorySegment;

public class MotionLocks {
    public boolean linearX;
    public boolean linearY;
    public boolean linearZ;
    public boolean angularX;
    public boolean angularY;
    public boolean angularZ;

    public MotionLocks() {

    }

    public MotionLocks(MotionLocks other) {
        this.linearX = other.linearX;
        this.linearY = other.linearY;
        this.linearZ = other.linearZ;
        this.angularX = other.angularX;
        this.angularY = other.angularY;
        this.angularZ = other.angularZ;
    }

    void set(MotionLocks other) {
        this.linearX = other.linearX;
        this.linearY = other.linearY;
        this.linearZ = other.linearZ;
        this.angularX = other.angularX;
        this.angularY = other.angularY;
        this.angularZ = other.angularZ;
    }

    MotionLocks set(MemorySegment segment) {
        this.linearX = b3MotionLocks.linearX(segment);
        this.linearY = b3MotionLocks.linearY(segment);
        this.linearZ = b3MotionLocks.linearZ(segment);
        this.angularX = b3MotionLocks.angularX(segment);
        this.angularY = b3MotionLocks.angularY(segment);
        this.angularZ = b3MotionLocks.angularZ(segment);
        return this;
    }

    void put(MemorySegment segment) {
        b3MotionLocks.linearX(segment, this.linearX);
        b3MotionLocks.linearY(segment, this.linearY);
        b3MotionLocks.linearZ(segment, this.linearZ);
        b3MotionLocks.angularX(segment, this.angularX);
        b3MotionLocks.angularY(segment, this.angularY);
        b3MotionLocks.angularZ(segment, this.angularZ);
    }
}

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

    void put(MemorySegment segment) {
        b3MotionLocks.linearX(segment, this.linearX);
        b3MotionLocks.linearY(segment, this.linearY);
        b3MotionLocks.linearZ(segment, this.linearZ);
        b3MotionLocks.angularX(segment, this.angularX);
        b3MotionLocks.angularY(segment, this.angularY);
        b3MotionLocks.angularZ(segment, this.angularZ);
    }
}

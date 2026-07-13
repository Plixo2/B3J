package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3CollisionPlane;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Getter
@Setter
public class CollisionPlane {

    private Plane plane;
    private float pushLimit;
    private float push;
    private boolean clipVelocity;

    public CollisionPlane(
            boolean copyPlane,
            Plane plane,
            float pushLimit,
            float push,
            boolean clipVelocity
    ) {
        if (copyPlane) {
            plane = new Plane(plane);
        }
        this.plane = plane;
        this.pushLimit = pushLimit;
        this.push = push;
        this.clipVelocity = clipVelocity;
    }

    public CollisionPlane(CollisionPlane other) {
        this.plane = new Plane(other.plane);
        this.pushLimit = other.pushLimit;
        this.push = other.push;
        this.clipVelocity = other.clipVelocity;
    }

    public void set(CollisionPlane other) {
        this.plane.set(other.plane);
        this.pushLimit = other.pushLimit;
        this.push = other.push;
        this.clipVelocity = other.clipVelocity;
    }


    void put(MemorySegment segment) {
        this.plane.put(b3CollisionPlane.plane(segment));
        b3CollisionPlane.pushLimit(segment, this.pushLimit);
        b3CollisionPlane.clipVelocity(segment, this.clipVelocity);
        // `push` needs to be written for clipVector
        b3CollisionPlane.push(segment, this.push);
    }

    void set(MemorySegment segment) {
        // only `push` is output
        this.push = b3CollisionPlane.push(segment);
    }

    static MemorySegment putPlanes(CollisionPlane[] planes, int limit, SegmentAllocator arena) {
        var planesSegment = b3CollisionPlane.allocateArray(
                limit,
                arena
        );

        for (var i = 0; i < limit; i++) {
            var plane = planes[i];
            plane.put(b3CollisionPlane.asSlice(planesSegment, i));
        }

        return planesSegment;
    }

    static void setPlanes(CollisionPlane[] planes, int limit, MemorySegment segment) {
        for (var i = 0; i < limit; i++) {
            var plane = planes[i];
            plane.set(b3CollisionPlane.asSlice(segment, i));
        }
    }

}

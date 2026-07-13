package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Unsigned;
import org.box2d.box3d.b3HullHalfEdge;

import java.lang.foreign.MemorySegment;


public class HullHalfEdge {

    public @Unsigned byte next;
    public @Unsigned byte twin;
    public @Unsigned byte origin;
    public @Unsigned byte face;

    public HullHalfEdge(@Unsigned byte next, @Unsigned byte twin, @Unsigned byte origin, @Unsigned byte face) {
        this.next = next;
        this.twin = twin;
        this.origin = origin;
        this.face = face;
    }


    public HullHalfEdge() {
        this.next = 0;
        this.twin = 0;
        this.origin = 0;
        this.face = 0;
    }

    public HullHalfEdge(HullHalfEdge other) {
        this.next = other.next;
        this.twin = other.twin;
        this.origin = other.origin;
        this.face = other.face;
    }


    void set(MemorySegment segment) {
        this.next = b3HullHalfEdge.next(segment);
        this.twin = b3HullHalfEdge.twin(segment);
        this.origin = b3HullHalfEdge.origin(segment);
        this.face = b3HullHalfEdge.face(segment);
    }

}

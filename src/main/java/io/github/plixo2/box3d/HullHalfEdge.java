package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U8;
import org.box2d.box3d.b3HullHalfEdge;

import java.lang.foreign.MemorySegment;

import static io.github.plixo2.box3d.internal.Internal.assertU8;

public class HullHalfEdge {

    public @U8 int next;

    public @U8 int twin;

    public @U8 int origin;

    public @U8 int face;

    public HullHalfEdge(@U8 int next, @U8 int twin, @U8 int origin, @U8 int face) {
        assertU8(next, "next");
        assertU8(twin, "twin");
        assertU8(origin, "origin");
        assertU8(face, "face");
        this.next = next;
        this.twin = twin;
        this.origin = origin;
        this.face = face;
    }

    public HullHalfEdge(HullHalfEdge other) {
        this.next = other.next;
        this.twin = other.twin;
        this.origin = other.origin;
        this.face = other.face;
    }

    HullHalfEdge set(MemorySegment segment) {
        this.next = Byte.toUnsignedInt(b3HullHalfEdge.next(segment));
        this.twin = Byte.toUnsignedInt(b3HullHalfEdge.twin(segment));
        this.origin = Byte.toUnsignedInt(b3HullHalfEdge.origin(segment));
        this.face = Byte.toUnsignedInt(b3HullHalfEdge.face(segment));
        return this;
    }

}

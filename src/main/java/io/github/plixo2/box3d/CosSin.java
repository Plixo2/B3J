package io.github.plixo2.box3d;


import org.box2d.box3d.b3CosSin;

import java.lang.foreign.MemorySegment;

public class CosSin {
    public float cosine;
    public float sine;

    public CosSin() {

    }
    public CosSin(CosSin other) {
        this.cosine = other.cosine;
        this.sine = other.sine;
    }

    void set(MemorySegment segment) {
        this.cosine = b3CosSin.cosine(segment);
        this.sine = b3CosSin.sine(segment);
    }

}

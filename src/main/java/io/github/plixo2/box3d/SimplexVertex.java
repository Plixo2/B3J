package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import org.box2d.box3d.b3SimplexVertex;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

@Getter
public class SimplexVertex {

    Vector3f wA = new Vector3f();
    Vector3f wB = new Vector3f();
    Vector3f w = new Vector3f();
    float a;
    int indexA;
    int indexB;

    SimplexVertex() {

    }

    public SimplexVertex(SimplexVertex other) {
        this.wA.set(other.wA);
        this.wB.set(other.wB);
        this.w.set(other.w);
        this.a = other.a;
        this.indexA = other.indexA;
        this.indexB = other.indexB;
    }

    void set(MemorySegment segment) {
        PrimitiveMemOps.setVec3(this.wA, b3SimplexVertex.wA(segment));
        PrimitiveMemOps.setVec3(this.wB, b3SimplexVertex.wB(segment));
        PrimitiveMemOps.setVec3(this.w, b3SimplexVertex.w(segment));
        this.a = b3SimplexVertex.a(segment);
        this.indexA = b3SimplexVertex.indexA(segment);
        this.indexB = b3SimplexVertex.indexB(segment);
    }

}

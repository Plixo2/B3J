package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.Unsigned;
import org.box2d.box3d.b3ExplosionDef;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public class ExplosionDef {

    public @Unsigned long maskBits;
    public Vector3f position = new Vector3f();
    public float radius;
    public float falloff;
    public float impulsePerArea;

    /// @api b3DefaultExplosionDef
    public ExplosionDef() {
        this.maskBits = B3.DEFAULT_MASK_BITS;
    }

    public ExplosionDef(ExplosionDef other) {
        this.maskBits = other.maskBits;
        this.position.set(other.position);
        this.radius = other.radius;
        this.falloff = other.falloff;
        this.impulsePerArea = other.impulsePerArea;
    }


    MemorySegment create(SegmentAllocator arena) {
        var segment = b3ExplosionDef.allocate(arena);
        b3ExplosionDef.maskBits(segment, this.maskBits);
        PrimitiveMemOps.putVec3(b3ExplosionDef.position(segment), this.position);
        b3ExplosionDef.radius(segment, this.radius);
        b3ExplosionDef.falloff(segment, this.falloff);
        b3ExplosionDef.impulsePerArea(segment, this.impulsePerArea);

        return segment;
    }

}

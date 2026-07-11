package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.U32;
import io.github.plixo2.box3d.internal.U64;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3SurfaceMaterial;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

import static io.github.plixo2.box3d.internal.Internal.assertU32;

@Getter
@Setter
public class SurfaceMaterial {

    private float friction;
    private float restitution;
    private float rollingResistance;
    private Vector3f tangentVelocity = new Vector3f();
    private @U64 long userMaterialId;
    private int customColor;

    /// @api b3DefaultSurfaceMaterial
    public SurfaceMaterial() {
        this.friction = 0.6f;
    }

    public SurfaceMaterial(SurfaceMaterial other) {
        this.friction = other.friction;
        this.restitution = other.restitution;
        this.rollingResistance = other.rollingResistance;
        this.tangentVelocity.set(other.tangentVelocity);
        this.userMaterialId = other.userMaterialId;
        this.customColor = other.customColor;
    }

    void put(MemorySegment segment) {
        b3SurfaceMaterial.friction(segment, this.friction);
        b3SurfaceMaterial.restitution(segment, this.restitution);
        b3SurfaceMaterial.rollingResistance(segment, this.rollingResistance);
        PrimitiveMemOps.putVec3(b3SurfaceMaterial.tangentVelocity(segment), this.tangentVelocity);
        b3SurfaceMaterial.userMaterialId(segment, this.userMaterialId);
        b3SurfaceMaterial.customColor(segment, this.customColor);
    }

}

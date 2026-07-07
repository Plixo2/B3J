package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitveMemOps;
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

    float friction;
    float restitution;
    float rollingResistance;
    Vector3f tangentVelocity = new Vector3f();
    @U64 long userMaterialId;
    @U32 long customColor;

    /// @api b3DefaultSurfaceMaterial
    public SurfaceMaterial() {
        this.friction = 0.6f;
    }

    public @U64 long userMaterialId() {
        return this.userMaterialId;
    }

    public @U32 long customColor() {
        return this.customColor;
    }
    public void customColor(@U32 long customColor) {
        assertU32(customColor, "customColor");
        this.customColor = customColor;
    }


    void put(MemorySegment segment) {
        b3SurfaceMaterial.friction(segment, this.friction);
        b3SurfaceMaterial.restitution(segment, this.restitution);
        b3SurfaceMaterial.rollingResistance(segment, this.rollingResistance);
        PrimitveMemOps.putVec3(b3SurfaceMaterial.tangentVelocity(segment), this.tangentVelocity);
        b3SurfaceMaterial.userMaterialId(segment, this.userMaterialId);
        b3SurfaceMaterial.customColor(segment, assertU32(this.customColor, "customColor"));
    }

}

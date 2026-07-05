package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.U32;
import io.github.plixo2.box3d.internal.U64;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3SurfaceMaterial;

import java.lang.foreign.MemorySegment;

import static io.github.plixo2.box3d.internal.Internal.assertU32;

@Getter
@Setter
public class SurfaceMaterial {

    float friction;
    float restitution;
    float rollingResistance;
    Vec3 tangentVelocity = new Vec3();
    @U64 long userMaterialId;
    @U32 long customColor;

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
        this.tangentVelocity.put(b3SurfaceMaterial.tangentVelocity(segment));
        b3SurfaceMaterial.userMaterialId(segment, this.userMaterialId);
        b3SurfaceMaterial.customColor(segment, assertU32(this.customColor, "customColor"));
    }

}

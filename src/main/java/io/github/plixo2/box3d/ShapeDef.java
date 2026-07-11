package io.github.plixo2.box3d;


import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.b3ShapeDef;
import org.box2d.box3d.b3SurfaceMaterial;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;


@Getter
@Setter
public class ShapeDef {

    SurfaceMaterial[] materials = new SurfaceMaterial[0];
    SurfaceMaterial baseMaterial;
    float density;
    float explosionScale;
    Filter filter;
    boolean enableCustomFiltering;
    boolean isSensor;
    boolean enableSensorEvents;
    boolean enableContactEvents;
    boolean enableHitEvents;
    boolean enablePreSolveEvents;
    boolean invokeContactCreation;
    boolean updateBodyMass;

    /// @api b3DefaultShapeDef
    public ShapeDef() {
        float lengthUnits = B3.lengthUnitsPerMeter();

        this.baseMaterial = new SurfaceMaterial();
        // density of water
        this.density = 1000.0f / ( lengthUnits * lengthUnits * lengthUnits );
        this.explosionScale = 1.0f;
        this.filter = new Filter();
        this.updateBodyMass = true;
        this.invokeContactCreation = true;
    }

    public ShapeDef(ShapeDef other) {
        this.materials = other.materials;
        this.baseMaterial = other.baseMaterial;
        this.density = other.density;
        this.explosionScale = other.explosionScale;
        this.filter = other.filter;
        this.enableCustomFiltering = other.enableCustomFiltering;
        this.isSensor = other.isSensor;
        this.enableSensorEvents = other.enableSensorEvents;
        this.enableContactEvents = other.enableContactEvents;
        this.enableHitEvents = other.enableHitEvents;
        this.enablePreSolveEvents = other.enablePreSolveEvents;
        this.invokeContactCreation = other.invokeContactCreation;
        this.updateBodyMass = other.updateBodyMass;
    }


    MemorySegment create(SegmentAllocator arena) {
        var segment = b3ShapeDef.allocate(arena);

        var materialCount = this.materials.length;
        MemorySegment materialArray;
        if (materialCount > 0) {
            materialArray = b3SurfaceMaterial.allocateArray(materialCount, arena);
            for (var i = 0; i < materialCount; i++) {
                var material = this.materials[i];
                material.put(b3SurfaceMaterial.asSlice(materialArray, i));
            }
        } else {
            materialArray = MemorySegment.NULL;
        }

        b3ShapeDef.materials(segment, materialArray);
        b3ShapeDef.materialCount(segment, materialCount);
        this.baseMaterial.put(b3ShapeDef.baseMaterial(segment));
        b3ShapeDef.density(segment, this.density);
        b3ShapeDef.explosionScale(segment, this.explosionScale);
        this.filter.put(b3ShapeDef.filter(segment));
        b3ShapeDef.enableCustomFiltering(segment, this.enableCustomFiltering);
        b3ShapeDef.isSensor(segment, this.isSensor);
        b3ShapeDef.enableSensorEvents(segment, this.enableSensorEvents);
        b3ShapeDef.enableContactEvents(segment, this.enableContactEvents);
        b3ShapeDef.enableHitEvents(segment, this.enableHitEvents);
        b3ShapeDef.enablePreSolveEvents(segment, this.enablePreSolveEvents);
        b3ShapeDef.invokeContactCreation(segment, this.invokeContactCreation);
        b3ShapeDef.updateBodyMass(segment, this.updateBodyMass);
        b3ShapeDef.internalValue(segment, B3.SECRET_COOKIE);

        return segment;
    }

    private MemorySegment nls(Object object) {
        if (object != null) {
            throw new RuntimeException("Not implemented");
        }
        return MemorySegment.NULL;
    }
}

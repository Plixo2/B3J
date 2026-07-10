package io.github.plixo2.framework;

import io.github.plixo2.abstraction.Color;
import io.github.plixo2.box3d.*;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MeshFactory extends DebugShapeCollection<MultiMesh.MeshRecord> {
    private final MeshRenderer buffers;
    private final Map<ShapeID, Color> customColors;

    @Override
    protected MultiMesh.MeshRecord create(ShapeID shapeID, ShapeType.Shape shape) {

        var args = switch (shape) {
            case Capsule capsule -> MeshCreator.createCapsule(capsule);
            case HeightFieldData heightFieldData -> MeshCreator.createHeightField(heightFieldData);
            case HullData hullData -> MeshCreator.createHull(hullData);
            case io.github.plixo2.box3d.Mesh mesh -> MeshCreator.createMesh(mesh);
            case Sphere sphere -> MeshCreator.createSphere(sphere);
            case CompoundData compoundData -> {
                throw new RuntimeException("Not implemented");
            }
        };

        return this.buffers.place(args, this.customColors.get(shapeID));
    }

    @Override
    protected void delete(MultiMesh.MeshRecord object) {
       // nothing ...
    }
}

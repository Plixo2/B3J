package io.github.plixo2.framework;

import io.github.plixo2.abstraction.Color;
import io.github.plixo2.box3d.*;

import java.util.Map;

public class MeshFactory extends DebugShapeCallbacks<MultiMesh.MeshRecord> {
    private final MeshRenderer buffers;
    private final Map<ShapeID, Color> customColors;

    public MeshFactory(
            MeshRenderer buffers,
            Map<ShapeID, Color> customColors
    ) {
        super(buffers.userShapes());
        this.buffers = buffers;
        this.customColors = customColors;
    }


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

package io.github.plixo2;

import io.github.plixo2.abstraction.Mesh;
import io.github.plixo2.box3d.*;

public class MeshFactory extends DebugShapeCollection<Mesh> {

    @Override
    protected Mesh create(ShapeID shapeID, ShapeType.Shape shape) {
        System.out.println("Creating shape: " + shapeID + " of type " + shape.getClass().getSimpleName());

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

        return args.createMesh();
    }

    @Override
    protected void delete(Mesh object) {
        object.free();
    }
}

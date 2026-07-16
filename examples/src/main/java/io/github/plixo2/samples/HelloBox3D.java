package io.github.plixo2.samples;

import io.github.plixo2.box3d.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static io.github.plixo2.box3d.region.Region.global;
import static io.github.plixo2.box3d.region.Region.ofConfined;

// 'Hello' example from the Box3D documentation
public class HelloBox3D {

    void main() {

        B3 b3 = B3.get();
        try (var region = ofConfined()) {

            var worldDef = new WorldDef();
            worldDef.gravity().y = -10f;

            // Multithreading
            // worldDef.taskPool(new BuildInScheduler());

            var worldID = b3.createWorld(region, worldDef);

            var groundBodyDef = new BodyDef();
            groundBodyDef.position().set(0, -10f, 0);
            var groundID = b3.createBody(global(), worldID, groundBodyDef);
            var groundBox = b3.makeBoxHull(50.0f, 10.0f, 50.0f);
            var groundShapeDef = new ShapeDef();
            var _ = b3.createHullShape(groundID, groundShapeDef, groundBox.base());

            var bodyDef = new BodyDef();
            bodyDef.type(BodyType.DYNAMIC);
            bodyDef.position().y = 4f;
            var bodyID = b3.createBody(global(), worldID, bodyDef);
            var dynamicBox = b3.makeCubeHull(1f);
            var shapeDef = new ShapeDef();
            shapeDef.density(1.0f);
            shapeDef.baseMaterial().friction(0.3f);
            var _ = b3.createHullShape(bodyID, shapeDef, dynamicBox.base());

            var timeStep = 1.0f / 60.0f;
            var subStepCount = 4;

            var position = new Vector3f();
            var rotation = new Quaternionf();

            for (int i = 0; i < 90; ++i) {

                b3.worldStep(worldID, timeStep, subStepCount);

                position = b3.bodyGetPosition(position, bodyID);
                rotation = b3.bodyGetRotation(rotation, bodyID);

                System.out.printf("%4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f\n",
                        position.x, position.y, position.z,
                        rotation.x, rotation.y, rotation.z, rotation.w
                );

                // Get rotation as axis & angle
                // var axisAngle = new AxisAngle4f(rotation);
                // axisAngle.angle
                // axisAngle.x, axisAngle.y, axisAngle.z


            }


        } // calls b3.destroyWorld

    }

}

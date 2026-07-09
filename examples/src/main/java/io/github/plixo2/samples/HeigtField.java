package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.threads.ExecutorTaskPool;
import io.github.plixo2.framework.MeshFactory;
import org.joml.SimplexNoise;
import org.joml.Vector3f;


public class HeigtField extends Example {


    @Override
    public void init(MeshFactory debugShapes) {
        var worldDef = new WorldDef();
        worldDef.debugShapeCollection(debugShapes);

        worldDef.taskPool(new ExecutorTaskPool());

        worldDef.gravity().set(0, -10f, 0);
        this.worldID = this.b3.createWorld(this.region, worldDef);


        for (var i = 0; i < 500; i++) {
            var rx = (float) Math.random() * 100 - 50;
            var ry = (float) Math.random() * 2;
            var rz = (float) Math.random() * 100 - 50;
            var sphere = spawnSphere(
                    0.5f,
                    new Vector3f(rx, 21 + ry, rz),
                    s -> {
                        s.baseMaterial().rollingResistance(0.2f);
                    }
            );
            this.b3.setBodyName(sphere, "Sphere " + i);

            if (Math.random() < 0.2) {
                spawnCapsule(
                        new Vector3f(rx - 1, 25 + ry, rz),
                        new Capsule(0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
                );

            }

        }




        var heightFieldDef = createHeightField(128, 128);
        heightFieldDef.globalMinimumHeight(-8.0f);
        heightFieldDef.globalMaximumHeight(8.0f);
        var heightFieldData = this.b3.createHeightField(this.region, heightFieldDef);

        var heightFieldBodyDef = new BodyDef();
        heightFieldBodyDef.position().set(-64, 0, -64);
        var heightFieldBody = this.b3.createBody(this.region, this.worldID, heightFieldBodyDef);

        var heightFieldShapeDef = new ShapeDef();
//        heightFieldShapeDef.baseMaterial().friction(0.5f);
        var _ = this.b3.createHeightFieldShape(heightFieldBody, heightFieldShapeDef, heightFieldData);

    }


    private HeightFieldDef createHeightField(int countX, int countZ) {

        var floatArray = new float[countX * countZ];
        for (int x = 0; x < countX; x++) {
            for (int z = 0; z < countZ; z++) {
                var a = SimplexNoise.noise(x * 0.05f, z * 0.05f);
                a += SimplexNoise.noise(7.7f + x * 0.1f, 43.3f + z * 0.1f) * 0.5f;
                a += SimplexNoise.noise(7.7f + x * 0.2f, 43.3f + z * 0.2f) * 0.25f;
                a += SimplexNoise.noise(7.7f + x * 0.4f, 43.3f + z * 0.4f) * 0.125f;
                floatArray[x * countZ + z] = a * 2;
            }
        }

        return new HeightFieldDef(countX, countZ, floatArray);

    }


    @Override
    public void onClick(Vector3f dir, Vector3f origin) {
        var b3 = this.b3;


        var result = new RayResult();
        var hit = b3.worldCastRayClosest(
                result,
                this.worldID,
                origin,
                new Vector3f(dir).mul(100),
                new QueryFilter()
        );

        if (hit) {
            var shape = result.shapeID();
            var body = b3.shapeGetBody(shape);
            var impuls = new Vector3f(dir);
            var mass = b3.bodyGetMass(body);

            impuls.mul(10.0f * mass);

            b3.bodyApplyLinearImpulse(body, impuls, result.point(), true);

        }


    }

}

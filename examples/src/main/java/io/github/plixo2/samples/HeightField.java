package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.tasks.BuildInScheduler;
import io.github.plixo2.framework.MeshFactory;
import org.joml.Random;
import org.joml.SimplexNoise;
import org.joml.Vector3f;


public class HeightField extends Example {


    @Override
    public void init(MeshFactory debugShapes) {
        initialCameraPosition(0, 15, 40);

        var worldDef = new WorldDef();
        worldDef.debugShapes(debugShapes);

        if (this.threaded) {
            worldDef.taskPool(new BuildInScheduler());
        }

        worldDef.gravity().set(0, -10f, 0);
        this.worldID = this.b3.createWorld(this.region, worldDef);

        var rd = new Random();

        for (var i = 0; i < 500; i++) {
            var rx = rd.nextFloat() * 100 - 50;
            var ry = rd.nextFloat() * 2;
            var rz = rd.nextFloat() * 100 - 50;

            ShapeDef sphereDef = new ShapeDef();
            sphereDef.density(1.0f);
            sphereDef.baseMaterial().friction(0.1f);
            sphereDef.baseMaterial().rollingResistance(0.2f);

            var sphere = spawnSphere(
                    BodyType.DYNAMIC,
                    sphereDef,
                    0.5f,
                    new Vector3f(rx, 21 + ry, rz)
            );
            this.b3.bodySetName(sphere, "Sphere " + i);

            if (rd.nextFloat() < 0.2) {
                var capsule = new Capsule(
                        0.5f,
                        0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f
                );

                var _ = spawnCapsule(
                        BodyType.DYNAMIC,
                        new Vector3f(rx - 1, 25 + ry, rz), capsule
                );
            }

            if (rd.nextFloat() < 0.2) {
                var _ = spawnCone(
                     BodyType.DYNAMIC,
                     new Vector3f(rx, 18 + ry, rz - 2),
                     0.5f + rd.nextFloat(),
                     rd.nextFloat() * 0.5f + 0.1f,
                     0.6f + rd.nextFloat() * 0.5f,
                     8 + rd.nextInt(12)
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
        var _ = this.b3.createHeightFieldShape(heightFieldBody, heightFieldShapeDef, heightFieldData);

    }


    private HeightFieldDef createHeightField(int countX, int countZ) {

        var floatArray = new float[countX * countZ];
        for (int x = 0; x < countX; x++) {
            for (int z = 0; z < countZ; z++) {
                var a = SimplexNoise.noise(x * 0.05f, z * 0.05f);
                a += SimplexNoise.noise(7.7f + x * 0.1f, 43.3f + z * 0.1f) * 0.5f;
                a += SimplexNoise.noise(2.7f + x * 0.2f, 3.3f + z * 0.2f) * 0.25f;
                a += SimplexNoise.noise(1.7f + x * 0.4f, 7.3f + z * 0.4f) * 0.125f;
                floatArray[x * countZ + z] = a * 2;
            }
        }

        return new HeightFieldDef(countX, countZ, floatArray);

    }


    @Override
    public void onClick(Vector3f dir, Vector3f origin) {
        applyClickImpuls(dir, origin, 10f);
    }

}

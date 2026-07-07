package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.MeshFactory;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.box3d.threads.ExecutorTaskPool;
import lombok.Getter;
import org.joml.Vector3f;


@Getter
public class HelloFloor extends Example {

    @Override
    public void init(MeshFactory debugShapes) {

        var worldDef = new WorldDef();
        worldDef.debugShapeCollection(debugShapes);

        worldDef.taskPool(new ExecutorTaskPool());

        worldDef.gravity().set(0, -10f, 0);
        this.worldID = this.b3.createWorld(this.region, worldDef);
        var bodyDef = new BodyDef();
        bodyDef.position().set(0, -10f, 0);

        var groundID = this.b3.createBody(this.region, this.worldID, bodyDef);

        var groundBox = this.b3.makeBoxHull(50.0f, 10.0f, 50.0f);

        ShapeDef groundShapeDef = new ShapeDef();
        var _ = this.b3.createHullShape(groundID, groundShapeDef, groundBox.base());


        var levels = 45;
        var zlevels = 35;
        var zstart = -40;
        var spacing = 2.0f;

        for (var z = 0; z < zlevels; z += 4) {
            levels -= 2;

            for (var y = 0; y < levels; y++) {
                var count = levels - y;
                var offset = (count - 1) * spacing * 0.5f;
                for (var x = 0; x < count; x++) {
                    spawnBox(new Vector3f(
                            x * spacing - offset,
                            y * spacing + 1.0f,
                            zstart + z
                    ));
                }
            }
        }





        spawnSphere(10, new Vector3f(0, 11, 22));

    }

    private void spawnBox(Vector3f position) {
        var b3 = this.b3;

        var dynBodyDef = new BodyDef();
        dynBodyDef.type(BodyType.DYNAMIC);
        dynBodyDef.position().set(position);
        BodyID bodyId = b3.createBody(this.region, this.worldID, dynBodyDef);

        var dynamicBox = b3.makeCubeHull(1.0f);

        ShapeDef dynamicShapeDef = new ShapeDef();
        dynamicShapeDef.density(1.0f);
        dynamicShapeDef.baseMaterial().friction(0.3f);
        var _ = b3.createHullShape(bodyId, dynamicShapeDef, dynamicBox.base());
    }

    private void spawnSphere(float radius, Vector3f position) {
        var b3 = this.b3;

        var dynBodyDef = new BodyDef();
        dynBodyDef.type(BodyType.DYNAMIC);
        dynBodyDef.position().set(position);
        BodyID bodyId = b3.createBody(this.region, this.worldID, dynBodyDef);

        var sphere = new Sphere(radius);
        ShapeDef dynamicShapeDef = new ShapeDef();
        dynamicShapeDef.density(1.0f);
        dynamicShapeDef.baseMaterial().friction(0.1f);

        var _ = b3.createSphereShape(bodyId, dynamicShapeDef, sphere);

    }

    @Override
    public void onClick(Vector3f dir, Vector3f origin) {
        var b3 = this.b3;


        var result = b3.worldCastRayClosest(
                this.worldID,
                origin,
                new Vector3f(dir).mul(100),
                new QueryFilter()
        );

        if (result != null) {
            var shape = result.shapeID();
            var body = b3.shapeGetBody(shape);
            var impuls = new Vector3f(dir);
            var mass = b3.bodyGetMass(body);

            impuls.mul(20.0f * mass);

            b3.bodyApplyLinearImpulse(body, impuls, result.point(), true);

        }


    }

    @Override
    public void update(float dt) {
        float timeStep = Math.min(dt, 1.0f / 60.0f);
        int subStepCount = 4;

        this.b3.worldStep(this.worldID, timeStep, subStepCount);


    }
}

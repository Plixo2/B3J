package io.github.plixo2;

import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.framework.Entry;
import io.github.plixo2.framework.MeshFactory;
import io.github.plixo2.framework.TextRenderer;
import org.joml.Vector3f;

import java.util.function.Consumer;


public abstract class Example {
    public Region region = Region.ofConfined();

    public B3 b3 = B3.get();
    public WorldID worldID;

    public abstract void init(MeshFactory debugShapes);

    public void update(float dt) {
        float timeStep = Math.min(dt, 1.0f / 60.0f);
        int subStepCount = 4;

        this.b3.worldStep(this.worldID, timeStep, subStepCount);
    }

    public void onClick(Vector3f dir, Vector3f origin) {

    }
    public void drawText(TextRenderer renderer) {

    }

    public void onKeyPress(int key) {

    }


    protected BodyID spawnBox(Vector3f position) {

        var dynBodyDef = new BodyDef();
        dynBodyDef.type(BodyType.DYNAMIC);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var dynamicBox = this.b3.makeCubeHull(1.0f);

        ShapeDef dynamicShapeDef = new ShapeDef();
        dynamicShapeDef.density(1.0f);
        dynamicShapeDef.baseMaterial().friction(0.3f);
        var _ = this.b3.createHullShape(bodyId, dynamicShapeDef, dynamicBox.base());

        return bodyId;
    }

    protected BodyID spawnCapsule(Vector3f position, Capsule capsule) {

        var dynBodyDef = new BodyDef();
        dynBodyDef.type(BodyType.DYNAMIC);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        ShapeDef dynamicShapeDef = new ShapeDef();
        dynamicShapeDef.density(1.0f);
        dynamicShapeDef.baseMaterial().friction(0.3f);
        var _ = this.b3.createCapsuleShape(bodyId, dynamicShapeDef, capsule);

        return bodyId;
    }


    protected BodyID spawnSphere(float radius, Vector3f position) {
        return spawnSphere(radius, position, _ -> {});
    }
    protected BodyID spawnSphere(float radius, Vector3f position, Consumer<ShapeDef> configure) {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(BodyType.DYNAMIC);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var sphere = new Sphere(radius);
        ShapeDef dynamicShapeDef = new ShapeDef();
        dynamicShapeDef.density(1.0f);
        dynamicShapeDef.baseMaterial().friction(0.1f);
        configure.accept(dynamicShapeDef);

        var _ = this.b3.createSphereShape(bodyId, dynamicShapeDef, sphere);

        return bodyId;
    }

    void main() {
        try (var renderer = new Entry(this)) {
            renderer.loop();
        }
        this.region.close();
    }


}

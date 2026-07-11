package io.github.plixo2;

import io.github.plixo2.abstraction.Color;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.framework.DrawConfig;
import io.github.plixo2.framework.Entry;
import io.github.plixo2.framework.MeshFactory;
import io.github.plixo2.framework.TextRenderer;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;


public abstract class Example {
    public Region region = Region.ofConfined();
    public boolean threaded = true;

    public final B3 b3 = B3.get();

    public WorldID worldID;
    public final Vector3f initialCameraPosition = new Vector3f(5, 5, 5);
    public final DrawConfig drawConfig = new DrawConfig();
    public final Map<ShapeID, Color> customColors = new HashMap<>();

    final void main() {
        try (var renderer = new Entry(this)) {
            renderer.loop();
        }
        this.region.close();
    }

    public abstract void init(MeshFactory debugShapes);

    public void onClick(Vector3f dir, Vector3f origin) {

    }

    public void drawText2D(TextRenderer.UI text) {

    }
    public void drawText3D(TextRenderer.World text) {

    }

    public void onKeyPress(int key) {

    }

    public void update(float dt) {
        float timeStep = Math.min(dt, 1.0f / 60.0f);
        int subStepCount = 4;

        this.b3.worldStep(this.worldID, timeStep, subStepCount);
    }

    public void customColor(ShapeID shapeID, Color color) {
        this.customColors.put(shapeID, color);
    }

    public void initialCameraPosition(Vector3f position) {
        this.initialCameraPosition.set(position);
    }
    public void initialCameraPosition(float x, float y, float z) {
        this.initialCameraPosition.set(x, y, z);
    }


    protected void applyClickImpuls(Vector3f dir, Vector3f origin, float strength) {

        var result = new RayResult();
        var hit = this.b3.worldCastRayClosest(
                result,
                this.worldID,
                origin,
                new Vector3f(dir).mul(100),
                new QueryFilter()
        );

        if (!hit) {
            return;
        }

        var shape = result.shapeID();
        var body = this.b3.shapeGetBody(shape);
        var impuls = new Vector3f(dir);
        var mass = this.b3.bodyGetMass(body);

        impuls.mul(strength * mass);

        this.b3.bodyApplyLinearImpulse(body, impuls, result.point(), true);

    }

    protected BodyID spawnSphere(BodyType bodyType, float radius, Vector3f position) {
        ShapeDef sphereDef = new ShapeDef();
        sphereDef.density(1.0f);
        sphereDef.baseMaterial().friction(0.1f);
        return spawnSphere(bodyType, sphereDef, radius, position);
    }

    protected BodyID spawnBox(BodyType bodyType, Vector3f position) {
        ShapeDef boxDef = new ShapeDef();
        boxDef.density(1.0f);
        boxDef.baseMaterial().friction(0.3f);
        return spawnBox(bodyType, boxDef, position);
    }
    protected BodyID spawnBox(BodyType bodyType, Vector3f size, Vector3f position) {
        ShapeDef boxDef = new ShapeDef();
        boxDef.density(1.0f);
        boxDef.baseMaterial().friction(0.3f);
        return spawnBox(bodyType, boxDef, size, position);
    }

    protected BodyID spawnCapsule(BodyType bodyType, Vector3f position, Capsule capsule) {
        ShapeDef capsuleDef = new ShapeDef();
        capsuleDef.density(1.0f);
        capsuleDef.baseMaterial().friction(0.3f);
        return spawnCapsule(bodyType, capsuleDef, position, capsule);
    }

    protected BodyID spawnCone(BodyType bodyType, Vector3f position, float height, float radius1, float radius2, int slices) {
        ShapeDef coneDef = new ShapeDef();
        coneDef.density(1.0f);
        coneDef.baseMaterial().friction(0.3f);
        return spawnCone(bodyType, coneDef, position, height, radius1, radius2, slices);
    }

    protected BodyID spawnCylinder(BodyType bodyType, Vector3f position, float height, float radius, int slices) {
        ShapeDef cylinderDef = new ShapeDef();
        cylinderDef.density(1.0f);
        cylinderDef.baseMaterial().friction(0.3f);
        return spawnCylinder(bodyType, cylinderDef, position, height, radius, slices);
    }

    protected BodyID spawnBox(BodyType bodyType, ShapeDef boxDef, Vector3f position) {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(bodyType);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var dynamicBox = this.b3.makeCubeHull(0.5f);
        var _ = this.b3.createHullShape(bodyId, boxDef, dynamicBox.base());
        return bodyId;
    }

    protected BodyID spawnBox(BodyType bodyType, ShapeDef boxDef, Vector3f size, Vector3f position) {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(bodyType);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var dynamicBox = this.b3.makeBoxHull(size.x / 2f, size.y / 2f, size.z / 2f);
        var _ = this.b3.createHullShape(bodyId, boxDef, dynamicBox.base());
        return bodyId;
    }


    protected BodyID spawnCone(BodyType bodyType, ShapeDef coneDef, Vector3f position, float height, float radius1, float radius2, int slices) {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(bodyType);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var hull = this.b3.createCone(height, radius1, radius2, slices);
        var _ = this.b3.createHullShape(bodyId, coneDef, hull);
        return bodyId;
    }

    protected BodyID spawnCylinder(BodyType bodyType, ShapeDef cylinderDef, Vector3f position, float height, float radius, int slices) {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(bodyType);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var hull = this.b3.createCylinder(height, radius, 0, slices);
        var _ = this.b3.createHullShape(bodyId, cylinderDef, hull);
        return bodyId;
    }

    protected BodyID spawnCapsule(BodyType bodyType, ShapeDef capsuleDef, Vector3f position, Capsule capsule) {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(bodyType);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var _ = this.b3.createCapsuleShape(bodyId, capsuleDef, capsule);
        return bodyId;
    }

    protected BodyID spawnSphere(BodyType bodyType, ShapeDef sphereDef, float radius, Vector3f position) {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(bodyType);
        dynBodyDef.position().set(position);
        var bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var sphere = new Sphere(radius);
        var _ = this.b3.createSphereShape(bodyId, sphereDef, sphere);
        return bodyId;
    }

}

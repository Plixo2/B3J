package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.abstraction.Color;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.internal.U64;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.box3d.threads.ExecutorTaskPool;
import io.github.plixo2.framework.DrawConfig;
import io.github.plixo2.framework.MeshFactory;
import io.github.plixo2.framework.TextRenderer;
import lombok.Getter;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


@Getter
public class Joints extends Example {
    private static final long GROUND_CATEGORY = 0x01L;

    @Override
    public void init(MeshFactory debugShapes) {
        this.drawConfig.enable(DrawConfig.Key.JOINTS);

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
        groundShapeDef.filter().categoryBits = GROUND_CATEGORY;
        groundShapeDef.enableCustomFiltering(true);
        var _ = this.b3.createHullShape(groundID, groundShapeDef, groundBox.base());


        revolute(-10);
    }

    @Override
    public void drawText3D(TextRenderer.World text) {
        text.putString("Revolute joints", 0, 0, -10, Color.WHITE);
    }

    private void revolute(float z) {
        spawnFalling(new Vector3f(5, 0, z));

        door(new Vector3f(10, 0, z), false, 0.9f, 0.2f, Math.PI_OVER_2_f);
        door(new Vector3f(15, 0, z), false, 0.2f, 1f, Math.PI_OVER_2_f * 1.2f);
        door(new Vector3f(20, 0, z), true, 0f, 0f, Math.PI_OVER_2_f);
    }

    private void door(Vector3f position, boolean enableMotor, float dampingRatio, float hertz, float limit) {

        ShapeDef boxDef = new ShapeDef();
        boxDef.density(1.0f);
        boxDef.baseMaterial().friction(0f);
        boxDef.filter().maskBits = U64.MAX & ~GROUND_CATEGORY;

        var w = new Vector3f(0.86f, 2.1f, 0.1f);
        var p = new Vector3f(w.x / 2f, w.y / 2f, 0).add(position);
        var door = spawnBox(BodyType.DYNAMIC, boxDef, w, p);
        this.b3.bodyEnableSleep(door, false);
        this.b3.bodySetName(door, "door");

        var hw = new Vector3f(0.1f, w.y, 0.1f);
        var hp = new Vector3f(-hw.x / 2f, w.y / 2f, 0).add(position);
        var hinge = spawnBox(BodyType.STATIC, hw, hp);
        this.b3.bodySetName(hinge, "hinge");

        var worldPivot = new Vector3f(w.z / 2f,  w.y / 2f, 0).add(position);

        var jointDef = new RevoluteJointDef(hinge, door);
        var localA = this.b3.bodyGetLocalPoint(new Vector3f(), hinge, worldPivot);
        var localB = this.b3.bodyGetLocalPoint(new Vector3f(), door, worldPivot);

        var axis = new Quaternionf();
        axis.rotateX(Math.PI_OVER_2_f);

        jointDef.base().localFrameA().translationRotate(localA, axis);
        jointDef.base().localFrameB().translationRotate(localB, axis);

        jointDef.lowerAngle(-limit);
        jointDef.upperAngle(limit);
        jointDef.enableLimit(true);

        if (enableMotor) {
            jointDef.maxMotorTorque(1.0f);
            jointDef.motorSpeed(1f);
            jointDef.enableMotor(true);
        } else {
            jointDef.enableSpring(true);
            jointDef.dampingRatio(dampingRatio);
            jointDef.hertz(hertz);
        }

        // is destroyed when any body is destroyed
        var _ = this.b3.createRevoluteJoint(Region.global(), this.worldID, jointDef);




    }
    private void spawnFalling(Vector3f position) {

        var cubeA = spawnBox(new Vector3f(-0.5f, 6, 0).add(position), 45f);
        var cubeB = spawnBox(new Vector3f( 0.5f, 6, 0).add(position), -45f);

        this.b3.bodySetName(cubeA, "cube-A");
        this.b3.bodySetName(cubeB, "cube-B");

        var worldPivot = new Vector3f(0, 5.5f, 0).add(position);

        var jointDef = new RevoluteJointDef(cubeA, cubeB);


        var localA = this.b3.bodyGetLocalPoint(new Vector3f(), cubeA, worldPivot);
        var localB = this.b3.bodyGetLocalPoint(new Vector3f(), cubeB, worldPivot);

        jointDef.base().localFrameA().translation(localA);
        jointDef.base().localFrameB().translation(localB);

        // is destroyed when any body is destroyed
        var _ = this.b3.createRevoluteJoint(Region.global(), this.worldID, jointDef);

        var _ = spawnSphere(BodyType.STATIC, 0.2f, new Vector3f(0, 5, -0.1f).add(position));
    }

    private BodyID spawnBox(Vector3f position, float rotation) {
        var bodyId = spawnBox(BodyType.DYNAMIC, position);

        var transform = this.b3.bodyGetTransform(new Matrix4f(), bodyId);
        var localPivot = Math.signum(rotation) * 0.5f;
        transform.translate(localPivot, -0.5f, 0)
                 .rotateZ(Math.toRadians(rotation))
                 .translate(-localPivot, 0.5f, 0);

        this.b3.bodySetTransform(bodyId, transform);

        return bodyId;
    }


    @Override
    public void onClick(Vector3f dir, Vector3f origin) {
        applyClickImpuls(dir, origin, 4f);
    }


}

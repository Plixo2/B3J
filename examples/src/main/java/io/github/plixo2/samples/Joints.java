package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.framework.abstractions.Color;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.internal.U64;
import io.github.plixo2.box3d.tasks.BuildInScheduler;
import io.github.plixo2.framework.DrawConfig;
import io.github.plixo2.framework.MeshFactory;
import io.github.plixo2.framework.TextRenderer;
import lombok.Getter;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Getter
public class Joints extends Example {
    private static final long GROUND_CATEGORY = 0x01L;
    private float time = 0;
    private final List<UpdateCallback> updates = new ArrayList<>();

    @Override
    public void init(MeshFactory debugShapes) {
        this.drawConfig.enable(DrawConfig.Key.JOINTS);
        this.updates.clear();
        this.time = 0;

        initialCameraPosition(-10, 5, 0);

        var worldDef = new WorldDef();
        worldDef.debugShapes(debugShapes);

        if (this.threaded) {
            worldDef.taskPool(new BuildInScheduler());
        }

        worldDef.gravity().set(0, -10f, 0);
        this.worldID = b3.createWorld(this.region, worldDef);
        var bodyDef = new BodyDef();
        bodyDef.position().set(0, -10f, 0);
        var groundID = b3.createBody(this.region, this.worldID, bodyDef);
        var groundBox = b3.makeBoxHull(50.0f, 10.0f, 50.0f);
        ShapeDef groundShapeDef = new ShapeDef();
        groundShapeDef.filter().categoryBits = GROUND_CATEGORY;
        groundShapeDef.enableCustomFiltering(true);
        var _ = b3.createHullShape(groundID, groundShapeDef, groundBox.base());

        var offset = 2;
        revolute  (offset + -20);
        distance  (offset + -15);
        prismatic (offset + -10);
        weld      (offset + -5);
        motor     (offset + 0);
        spherical (offset + 5);
        parallel  (offset + 10);
        filter    (offset + 15);
        wheel     (offset + 20);
    }

    @Override
    public void drawText3D(TextRenderer.World text) {
        var offset = 2;
        text.putString("Revolute joint" , 0, 0, offset + -20, Color.WHITE);
        text.putString("Distance joint" , 0, 0, offset + -15, Color.WHITE);
        text.putString("Prismatic joint", 0, 0, offset + -10, Color.WHITE);
        text.putString("Weld joint"     , 0, 0, offset + -5 , Color.WHITE);
        text.putString("Motor joint"    , 0, 0, offset + 0  , Color.WHITE);
        text.putString("Spherical joint", 0, 0, offset + 5  , Color.WHITE);
        text.putString("Parallel joint" , 0, 0, offset + 10 , Color.WHITE);
        text.putString("Filter joint"   , 0, 0, offset + 15 , Color.WHITE);
        text.putString("Wheel joint"    , 0, 0, offset + 20 , Color.WHITE);
    }

    //<editor-fold defaultstate="collapsed" desc="Filter Joint">
    private void filter(float z) {
        createFilterPair(new Vector3f(5, 3, z), new Quaternionf());
        createFilterPair(new Vector3f(9, 3, z), new Quaternionf().rotateY(Math.PI_OVER_2_f));
    }
    private void createFilterPair(Vector3f position, Quaternionf rotationB) {
        var shapeDef = new ShapeDef();
        shapeDef.density(1.0f);
        shapeDef.baseMaterial().friction(0.3f);

        var boxA = spawnBox(BodyType.DYNAMIC, shapeDef, new Vector3f(1.5f, 0.5f, 0.5f), position);
        var boxB = spawnBox(BodyType.DYNAMIC, shapeDef, new Vector3f(0.5f, 1.5f, 0.5f), position);
        b3.bodyEnableSleep(boxA, false);
        b3.bodyEnableSleep(boxB, false);
        b3.bodySetTransform(boxB, position, rotationB);

        var jointDef = new FilterJointDef(boxA, boxB);
        var _ = b3.createJoint(this.worldID, jointDef);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Parallel Joint">
    private void parallel(float z) {
        createParallelKeeper(new Vector3f(5, 2, z), 0.6f, 5.0f, 0.7f, 200.0f);
        createParallelKeeper(new Vector3f(10, 2, z), -0.8f, 2.0f, 0.3f, 50.0f);
        createParallelKeeper(new Vector3f(15, 2, z), 1.0f, 9.0f, 1.0f, 500.0f);
    }
    private void createParallelKeeper(Vector3f position, float angle, float hertz, float dampingRatio, float maxTorque) {
        var reference = createMotorController(position);
        var body = spawnBox(BodyType.DYNAMIC, new Vector3f(0.6f, 1.8f, 0.6f), position);
        b3.bodyEnableSleep(body, false);

        var transform = b3.bodyGetTransform(new Matrix4f(), body);
        transform.rotateZ(angle);
        b3.bodySetTransform(body, transform);

        var axis = new Quaternionf().rotateX(Math.PI_OVER_2_f);
        var jointDef = new ParallelJointDef(reference, body);
        jointDef.base().localFrameA().rotation(axis);
        jointDef.base().localFrameB().rotation(axis);
        jointDef.hertz(hertz);
        jointDef.dampingRatio(dampingRatio);
        jointDef.maxTorque(maxTorque);

        var _ = b3.createJoint(this.worldID, jointDef);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Spherical Joint">
    private void spherical(float z) {
        createBallSocket(new Vector3f(5, 5, z), _ -> {});

        createBallSocket(new Vector3f(10, 5, z), jointDef -> {
            jointDef.enableConeLimit(true);
            jointDef.coneAngle(0.25f * Math.PI_f);
            jointDef.enableTwistLimit(true);
            jointDef.lowerTwistAngle(-Math.PI_f / 6.0f);
            jointDef.upperTwistAngle(Math.PI_f / 6.0f);
        });

        createBallSocket(new Vector3f(15, 5, z), jointDef -> {
            jointDef.enableSpring(true);
            jointDef.hertz(5.0f);
            jointDef.dampingRatio(0.7f);
            jointDef.enableMotor(true);
            jointDef.motorVelocity().set(0.0f, 1.0f, 0.0f);
            jointDef.maxMotorTorque(100.0f);
        });
    }
    private void createBallSocket(Vector3f pivot, Consumer<SphericalJointDef> configure) {
        var anchor = spawnSphere(BodyType.STATIC, 0.2f, pivot);
        var body = spawnBox(BodyType.DYNAMIC, new Vector3f(0.5f, 1.8f, 0.5f), new Vector3f(0, -0.9f, 0).add(pivot));
        b3.bodyEnableSleep(body, false);

        var jointDef = new SphericalJointDef(anchor, body);
        var axis = new Quaternionf().rotateX(Math.PI_OVER_2_f);
        var localA = b3.bodyGetLocalPoint(new Vector3f(), anchor, pivot);
        var localB = b3.bodyGetLocalPoint(new Vector3f(), body, pivot);

        jointDef.base().localFrameA().translationRotate(localA, axis);
        jointDef.base().localFrameB().translationRotate(localB, axis);

        configure.accept(jointDef);

        var _ = b3.createJoint(this.worldID, jointDef);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Wheel Joint">
    private void wheel(float z) {
        var carGroup = -1;
        var chassisPosition = new Vector3f(0, 1.1f, z);
        var chassisDef = new ShapeDef();
        chassisDef.density(1.0f);
        chassisDef.baseMaterial().friction(0.3f);
        chassisDef.filter().groupIndex = carGroup;
        chassisDef.enableCustomFiltering(true);
        var chassis = spawnBox(BodyType.DYNAMIC, chassisDef, new Vector3f(3.5f, 0.5f, 1.4f), chassisPosition);
        b3.bodyEnableSleep(chassis, false);
        b3.bodySetName(chassis, "wheel-car");

        var angle = Math.toRadians(-45.0f);
        var angle2 = Math.toRadians(5.0f);
        var speed = -7.0f;
        var friction = 20.2f;
        var wheelBase = 2.7f;
        var trackWidth = 2.1f;
        var frontLeftOffset = new Vector3f( wheelBase / 2.0f, -0.55f,  trackWidth / 2.0f);
        var frontRightOffset = new Vector3f( wheelBase / 2.0f, -0.55f, -trackWidth / 2.0f);
        var rearLeftOffset = new Vector3f(-wheelBase / 2.0f, -0.55f,  trackWidth / 2.0f);
        var rearRightOffset = new Vector3f(-wheelBase / 2.0f, -0.55f, -trackWidth / 2.0f);

        createCarWheel(chassis, chassisPosition, frontLeftOffset, angle, true, speed, friction, carGroup);
        createCarWheel(chassis, chassisPosition, frontRightOffset, angle, true, speed, friction, carGroup);
        createCarWheel(chassis, chassisPosition, rearLeftOffset, angle2, true, speed, friction, carGroup);
        createCarWheel(chassis, chassisPosition, rearRightOffset, angle2, true, speed, friction, carGroup);
        createWheelBumps(z);
    }
    private void createWheelBumps(float z) {
        for (var i = 0; i < 7; i++) {
            var x = 5.0f;
            var y = -0.14f + 0.08f * (i % 2) ;
            var left = spawnSphere(BodyType.STATIC, 0.35f, new Vector3f(x + 0.7f , y, z + i));
            var right = spawnSphere(BodyType.STATIC, 0.35f, new Vector3f(x - 0.7f, y, z + i));
            b3.bodySetName(left, "wheel-bump");
            b3.bodySetName(right, "wheel-bump");
        }
    }
    private void createCarWheel(
            BodyID chassis,
            Vector3f chassisPosition,
            Vector3f offset,
            float steeringAngle,
            boolean steer,
            float speed,
            float friction,
            int carGroup
    ) {
        var wheelPosition = new Vector3f(chassisPosition).add(offset);
        var wheel = createWheelBody(wheelPosition, friction, carGroup);

        var jointDef = new WheelJointDef(chassis, wheel);
        var suspensionFrame = new Quaternionf().rotateZ(Math.PI_OVER_2_f);
        var wheelFrame = new Quaternionf().rotateX(-Math.PI_OVER_2_f).mul(suspensionFrame);
        var localA = b3.bodyGetLocalPoint(new Vector3f(), chassis, wheelPosition);
        var localB = b3.bodyGetLocalPoint(new Vector3f(), wheel, wheelPosition);

        jointDef.base().localFrameA().translationRotate(localA, suspensionFrame);
        jointDef.base().localFrameB().translationRotate(localB, wheelFrame);
        jointDef.base().collideConnected(false);
        jointDef.enableSuspensionSpring(true);
        jointDef.suspensionHertz(2.0f);
        jointDef.suspensionDampingRatio(0.8f);
        jointDef.enableSuspensionLimit(true);
        jointDef.lowerSuspensionLimit(-0.35f);
        jointDef.upperSuspensionLimit(0.35f);
        jointDef.enableSpinMotor(true);
        jointDef.maxSpinTorque(120.0f);
        jointDef.spinSpeed(speed);
        jointDef.enableSteering(steer);
        jointDef.steeringHertz(5.0f);
        jointDef.steeringDampingRatio(0.7f);
        jointDef.targetSteeringAngle(steeringAngle);
        jointDef.maxSteeringTorque(200.0f);

        var _ = b3.createJoint(this.worldID, jointDef);

        var filterDef = new FilterJointDef(chassis, wheel);
        var _ = b3.createJoint(this.worldID, filterDef);
    }
    private BodyID createWheelBody(Vector3f position, float friction, int carGroup) {
        var bodyDef = new BodyDef();
        bodyDef.type(BodyType.DYNAMIC);
        bodyDef.position().set(position);
        bodyDef.rotation().rotateX(Math.PI_OVER_2_f);
        var wheel = b3.createBody(this.region, this.worldID, bodyDef);

        var wheelDef = new ShapeDef();
        wheelDef.density(1.0f);
        wheelDef.baseMaterial().friction(friction);
        wheelDef.filter().groupIndex = carGroup;
        wheelDef.enableCustomFiltering(true);

        try (var region = Region.ofConfined()) {
            var cylinder = b3.createCylinder(region, 0.35f, 0.45f, -0.35f / 2f, 24);
            var _ = b3.createHullShape(wheel, wheelDef, cylinder);
        }
        b3.bodyEnableSleep(wheel, false);
        b3.bodySetName(wheel, "wheel");

        return wheel;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Motor Joint">
    private void motor(float z) {
        createVelocityMotor(new Vector3f(5, 2, z));
        createSpringMotor(new Vector3f(14, 2, z));
        createFollowerMotor(new Vector3f(23, 2, z));
    }
    private void createVelocityMotor(Vector3f position) {
        var controller = createMotorController(position);
        var driven = spawnBox(BodyType.DYNAMIC, new Vector3f(0.7f, 0.7f, 0.7f), position);
        b3.bodyEnableSleep(driven, false);

        var wall = spawnBox(
                BodyType.STATIC,
                new Vector3f(0.3f, 2.0f, 2.0f),
                new Vector3f(4, 0, 0).add(position)
        );
        b3.bodySetName(wall, "motor-block");

        var jointDef = new MotorJointDef(controller, driven);
        jointDef.linearVelocity().set(2.0f, 0.0f, 0.0f);
        jointDef.maxVelocityForce(35.0f);
        jointDef.angularVelocity().set(0.0f, 3.0f, 0.0f);
        jointDef.maxVelocityTorque(12.0f);

        var _ = b3.createJoint(this.worldID, jointDef);
    }
    private void createSpringMotor(Vector3f position) {
        var controller = createMotorController(position);
        var startPosition = new Vector3f(2.5f, 1.0f, 0).add(position);
        var driven = spawnBox(BodyType.DYNAMIC, new Vector3f(0.8f, 0.8f, 0.8f), startPosition);
        b3.bodyEnableSleep(driven, false);

        var transform = b3.bodyGetTransform(new Matrix4f(), driven);
        transform.rotateZ(Math.PI_OVER_2_f * 0.5f);
        b3.bodySetTransform(driven, transform);

        var jointDef = new MotorJointDef(controller, driven);
        jointDef.linearHertz(1.5f);
        jointDef.linearDampingRatio(0.7f);
        jointDef.maxSpringForce(60.0f);
        jointDef.angularHertz(1.0f);
        jointDef.angularDampingRatio(0.7f);
        jointDef.maxSpringTorque(20.0f);

        var _ = b3.createJoint(this.worldID, jointDef);
    }
    private void createFollowerMotor(Vector3f position) {
        var radius = 2.0f;
        var speed = 0.75f;
        var targetPosition = new Vector3f(radius, 0, 0).add(position);
        var controller = createMotorController(targetPosition, BodyType.KINEMATIC);
        var driven = spawnBox(BodyType.DYNAMIC, new Vector3f(0.65f, 0.65f, 0.65f), targetPosition);
        b3.bodyEnableSleep(driven, false);

        var velocity = new Vector3f();
        update((_) -> {
            velocity.x = -Math.sin(this.time * speed) * radius * speed;
            velocity.y =  Math.cos(this.time * speed) * radius * speed;
            velocity.z = 0.0f;
            b3.bodySetLinearVelocity(controller, velocity);
        });

        var jointDef = new MotorJointDef(controller, driven);
        jointDef.linearHertz(2.0f);
        jointDef.linearDampingRatio(0.8f);
        jointDef.maxSpringForce(80.0f);
        jointDef.angularVelocity().set(0.0f, 2.0f, 0.0f);
        jointDef.maxVelocityTorque(8.0f);

        var _ = b3.createJoint(this.worldID, jointDef);
    }
    private BodyID createMotorController(Vector3f position) {
        return createMotorController(position, BodyType.STATIC);
    }
    private BodyID createMotorController(Vector3f position, BodyType type) {
        var bodyDef = new BodyDef();
        bodyDef.type(type);
        bodyDef.position().set(position);
        return b3.createBody(this.region, this.worldID, bodyDef);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Weld Joint">
    private void weld(float z) {
        createCantilever(new Vector3f(5, 4, z),  1f, 0.0f, 0.0f,    3);
        createCantilever(new Vector3f(10, 4, z), 1f, 0.0f, 0.0f,    6);
        createCantilever(new Vector3f(17, 4, z), 1f, 0.0f, 1.0f,    6);
        createCantilever(new Vector3f(25, 4, z), 0.5f, 7f, 4f,  6);
    }
    private void createCantilever(Vector3f position, float damp, float linearHertz, float angularHertz, int count) {
        var size = new Vector3f(1.0f, 0.35f, 0.35f);
        var previous = spawnBox(BodyType.STATIC, size, position);

        for (var i = 1; i < count; i++) {
            var offset = new Vector3f(i * size.x, 0, 0);
            var segment = spawnBox(BodyType.DYNAMIC, size, new Vector3f(position).add(offset));
            b3.bodyEnableSleep(segment, false);

            var jointPosition = new Vector3f(position).add((i - 0.5f) * size.x, 0, 0);
            var jointDef = new WeldJointDef(previous, segment);
            var localA = b3.bodyGetLocalPoint(new Vector3f(), previous, jointPosition);
            var localB = b3.bodyGetLocalPoint(new Vector3f(), segment, jointPosition);

            jointDef.base().localFrameA().translation(localA);
            jointDef.base().localFrameB().translation(localB);
            jointDef.linearHertz(linearHertz);
            jointDef.angularHertz(angularHertz);
            jointDef.linearDampingRatio(damp);
            jointDef.angularDampingRatio(damp);

            var _ = b3.createJoint(this.worldID, jointDef);

            previous = segment;
        }

        var weightDef = new ShapeDef();
        weightDef.density(5.0f);
        weightDef.baseMaterial().friction(0.3f);
        var weightSize = new Vector3f(1.0f, 1.0f, 1.0f);
        var weightPosition = new Vector3f(position).add(count * size.x, 0, 0);
        var weight = spawnBox(BodyType.DYNAMIC, weightDef, weightSize, weightPosition);
        b3.bodyEnableSleep(weight, false);

        var jointPosition = new Vector3f(position).add((count - 0.5f) * size.x, 0, 0);
        var jointDef = new WeldJointDef(previous, weight);
        var localA = b3.bodyGetLocalPoint(new Vector3f(), previous, jointPosition);
        var localB = b3.bodyGetLocalPoint(new Vector3f(), weight, jointPosition);

        jointDef.base().localFrameA().translation(localA);
        jointDef.base().localFrameB().translation(localB);
        jointDef.linearHertz(linearHertz);
        jointDef.angularHertz(angularHertz);
        jointDef.linearDampingRatio(damp);
        jointDef.angularDampingRatio(damp);

        var _ = b3.createJoint(this.worldID, jointDef);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Prismatic Joint">
    private void prismatic(float z) {
        createSlider(new Vector3f(5, 1.5f, z), -2.0f, jointDef -> {
            jointDef.enableLimit(true);
            jointDef.lowerTranslation(-2.25f);
            jointDef.upperTranslation(2.25f);

            jointDef.enableMotor(true);
            jointDef.maxMotorForce(20.0f);
            jointDef.motorSpeed(1.5f);
        });

        createSlider(new Vector3f(12, 1.5f, z), 1.5f, jointDef -> {
            jointDef.enableLimit(true);
            jointDef.lowerTranslation(-2.25f);
            jointDef.upperTranslation(2.25f);

            jointDef.enableSpring(true);
            jointDef.hertz(1.0f);
            jointDef.dampingRatio(0.3f);
            jointDef.targetTranslation(-1.5f);
        });

        var frictionSlider = createSlider(new Vector3f(19, 1.5f, z), -1.5f, jointDef -> {
            jointDef.enableLimit(true);
            jointDef.lowerTranslation(-2.25f);
            jointDef.upperTranslation(2.25f);

            jointDef.enableMotor(true);
            jointDef.maxMotorForce(3.0f);
            jointDef.motorSpeed(0.0f);
        });
        b3.bodySetLinearVelocity(frictionSlider, new Vector3f(5, 0, 0));
    }
    private BodyID createSlider(Vector3f position, float initialTranslation, Consumer<PrismaticJointDef> configure) {
        var rail = spawnBox(BodyType.STATIC, new Vector3f(5.0f, 0.15f, 0.15f), position);
        var stopSize = new Vector3f(0.15f, 0.6f, 0.15f);
        var _ = spawnBox(BodyType.STATIC, stopSize, new Vector3f(-2.5f, -0.15f, 0).add(position));
        var _ = spawnBox(BodyType.STATIC, stopSize, new Vector3f( 2.5f, -0.15f, 0).add(position));

        var sliderPosition = new Vector3f(initialTranslation, 0.45f, 0).add(position);
        var slider = spawnBox(BodyType.DYNAMIC, new Vector3f(0.7f, 0.5f, 0.5f), sliderPosition);
        b3.bodyEnableSleep(slider, false);

        var jointDef = new PrismaticJointDef(rail, slider);
        var worldFrameA = new Vector3f(0, 0.45f, 0).add(position);

        var localA = b3.bodyGetLocalPoint(new Vector3f(), rail, worldFrameA);
        var localB = b3.bodyGetLocalPoint(new Vector3f(), slider, sliderPosition);

        jointDef.base().localFrameA().translation(localA);
        jointDef.base().localFrameB().translation(localB);

        configure.accept(jointDef);

        var _ = b3.createJoint(this.worldID, jointDef);

        return slider;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Distance Joint">
    private void distance(float z) {
        createSwing(new Vector3f(6, 0, z), 0.7f, 1.5f, jointDef -> {
            jointDef.enableSpring(true);
            jointDef.hertz(2.0f);
            jointDef.dampingRatio(0.5f);
        });

        createSwing(new Vector3f(8, 0, z), 2f, 4f, jointDef -> {
            jointDef.enableSpring(true);
            jointDef.hertz(0.5f);
            jointDef.dampingRatio(0.7f);
        });

        createSwing(new Vector3f(10, 0, z), 2f, 4f, jointDef -> {
            jointDef.enableSpring(true);
            jointDef.hertz(0.5f);
            jointDef.dampingRatio(0.1f);

            jointDef.enableLimit(true);
            jointDef.minLength(1.5f);
            jointDef.maxLength(2.5f);
        });

    }
    private void createSwing(Vector3f position, float speed, float distance, Consumer<DistanceJointDef> configure) {

        var control = spawnSphere(BodyType.KINEMATIC, 0.2f, new Vector3f(0, 6, 0).add(position));

        var velo = new Vector3f();
        update((_) -> {

            velo.z = Math.sin(this.time * speed) * distance;
            b3.bodySetLinearVelocity(control, velo);

        });

        var box = spawnBox(BodyType.DYNAMIC, new Vector3f(0, 4, 0).add(position));

        var jointDef = new DistanceJointDef(box, control);

        jointDef.length(2);

        configure.accept(jointDef);


        var _ = b3.createJoint(this.worldID, jointDef);

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Revolute Joint">
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
        b3.bodyEnableSleep(door, false);
        b3.bodySetName(door, "door");

        var hw = new Vector3f(0.1f, w.y, 0.1f);
        var hp = new Vector3f(-hw.x / 2f, w.y / 2f, 0).add(position);
        var hinge = spawnBox(BodyType.STATIC, hw, hp);
        b3.bodySetName(hinge, "hinge");

        var worldPivot = new Vector3f(w.z / 2f,  w.y / 2f, 0).add(position);

        var jointDef = new RevoluteJointDef(hinge, door);
        var localA = b3.bodyGetLocalPoint(new Vector3f(), hinge, worldPivot);
        var localB = b3.bodyGetLocalPoint(new Vector3f(), door, worldPivot);

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
        var _ = b3.createJoint(this.worldID, jointDef);
    }
    private void spawnFalling(Vector3f position) {

        var cubeA = spawnAngleBox(new Vector3f(-0.5f, 6, 0).add(position), 45f);
        var cubeB = spawnAngleBox(new Vector3f( 0.5f, 6, 0).add(position), -45f);

        b3.bodySetName(cubeA, "cube-A");
        b3.bodySetName(cubeB, "cube-B");

        var worldPivot = new Vector3f(0, 5.5f, 0).add(position);

        var jointDef = new RevoluteJointDef(cubeA, cubeB);


        var localA = b3.bodyGetLocalPoint(new Vector3f(), cubeA, worldPivot);
        var localB = b3.bodyGetLocalPoint(new Vector3f(), cubeB, worldPivot);

        jointDef.base().localFrameA().translation(localA);
        jointDef.base().localFrameB().translation(localB);

        // is destroyed when any body is destroyed
        var _ = b3.createJoint(this.worldID, jointDef);

        var _ = spawnSphere(BodyType.STATIC, 0.2f, new Vector3f(0, 5, -0.1f).add(position));
    }
    private BodyID spawnAngleBox(Vector3f position, float rotation) {
        var bodyId = spawnBox(BodyType.DYNAMIC, position);

        var transform = b3.bodyGetTransform(new Matrix4f(), bodyId);
        var localPivot = Math.signum(rotation) * 0.5f;
        transform.translate(localPivot, -0.5f, 0)
                 .rotateZ(Math.toRadians(rotation))
                 .translate(-localPivot, 0.5f, 0);

        b3.bodySetTransform(bodyId, transform);

        return bodyId;
    }
    //</editor-fold>

    private void update(UpdateCallback update) {
        this.updates.add(update);
    }

    @Override
    public void onClick(Vector3f dir, Vector3f origin) {
        applyClickImpuls(dir, origin, 4f);
    }

    @Override
    public void update(float dt) {
        float timeStep = 1.0f / 60.0f;
        int subStepCount = 4;
        this.time += timeStep;

        for (var update : this.updates) {
            update.update(timeStep);
        }

        b3.worldStep(this.worldID, timeStep, subStepCount);
    }


    private interface UpdateCallback {
        void update(float delta);
    }

}

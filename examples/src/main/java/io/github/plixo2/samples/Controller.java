package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.internal.MemoryIterator;
import io.github.plixo2.box3d.internal.U64;
import io.github.plixo2.box3d.tasks.BuildInScheduler;
import io.github.plixo2.framework.MeshFactory;
import io.github.plixo2.framework.SceneDrawing;
import io.github.plixo2.framework.TextRenderer;
import io.github.plixo2.framework.abstractions.Camera;
import io.github.plixo2.framework.abstractions.Color;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;
import java.util.*;

public class Controller extends Example {
    public boolean clipVelocity = true;


    private static final float CAMERA_TARGET_HEIGHT = 1.5f;
    private static final float CAMERA_DISTANCE = 3.5f;

    private static final float MOUSE_SENSITIVITY = 0.2f;


    private SampleContext context;
    private CharacterMover mover;

    private RenderCapsule renderCapsuleBody;

    @Override
    public void init(MeshFactory debugShapes) {
        var worldDef = new WorldDef();
        worldDef.debugShapes(debugShapes);
        if (this.threaded) {
            worldDef.taskPool(new BuildInScheduler());
        }
        worldDef.gravity().set(0, -10f, 0);
        this.worldID = b3.createWorld(this.region, worldDef);


        this.context = new SampleContext();
        this.context.worldID = this.worldID;
        this.mover = new CharacterMover();

        createFloor();
        var _ = spawnBox(
                BodyType.STATIC,
                new Vector3f(30.0f, 1, 30),
                new Vector3f(0, 0f, 0)
        );
        this.renderCapsuleBody = createRenderCapsule();

        var initialPosition = new Vector3f(0, 5, 0);
        this.mover.initialize(this.context, initialPosition);

        syncRenderCapsule();
    }

    @Override
    public void onKeyPress(int key) {
        if (key == GLFW.GLFW_KEY_H) {
            this.clipVelocity = !this.clipVelocity;
        }
    }

    @Override
    public void drawText2D(TextRenderer.UI text) {
        text.putString("H - Toggle Clip Velocity: " + this.clipVelocity, 10, 10, Color.WHITE);
    }

    @Override
    public void update(float dt, SceneDrawing draw) {
        var timeStep = 1.0f / 60.0f;
        var subStepCount = 4;

        this.mover.step(draw, this.clipVelocity, this.renderCapsuleBody.shape);
        syncRenderCapsule();

        b3.worldStep(this.worldID, timeStep, subStepCount);
    }

    @Override
    public boolean customCameraMovement(
            Camera camera,
            Camera.ControlInput input,
            float dt
    ) {

        if (!(camera instanceof Camera.FreeCam freeCam)) {
            return false;
        }
        updateCtx(freeCam, input);
        updateCameraPosition(freeCam);
        syncRenderCapsule();

        return true;
    }

    private void updateCtx(Camera.FreeCam camera, Camera.ControlInput input) {
        if (input.mouseDown) {
            this.context.yaw -= input.mouseX * MOUSE_SENSITIVITY;
            this.context.pitch -= input.mouseY * MOUSE_SENSITIVITY;
            this.context.pitch = Math.clamp(this.context.pitch, -89.99f, 89.99f);
        }

        camera.yaw = this.context.yaw;
        camera.pitch = this.context.pitch;

        this.context.w = input.w;
        this.context.a = input.a;
        this.context.s = input.s;
        this.context.d = input.d;
        this.context.space = input.space;
        this.context.ctrl = input.ctrl;
    }


    private void updateCameraPosition(Camera.FreeCam camera) {
        var target = getCharacterPosition().add(0.0f, CAMERA_TARGET_HEIGHT, 0.0f);
        var forward = Camera.getForward(new Vector3f(), this.context.yaw, this.context.pitch);

        camera.x = target.x - forward.x * CAMERA_DISTANCE;
        camera.y = target.y - forward.y * CAMERA_DISTANCE;
        camera.z = target.z - forward.z * CAMERA_DISTANCE;
    }


    private Vector3f getCharacterPosition() {
        var out = new Vector3f();
        return this.mover.transform.getTranslation(out);
    }


    private RenderCapsule createRenderCapsule() {
        var bodyDef = new BodyDef();
        bodyDef.type(BodyType.KINEMATIC);
        var bodyID = b3.createBody(this.region, this.worldID, bodyDef);

        b3.bodySetName(bodyID, "Render Capsule");

        var shapeDef = new ShapeDef();
        shapeDef.filter().categoryBits = 2;
        var capsule = new Capsule(
                0.3f,
                0.0f, -0.5f, 0.0f,
                0.0f, 0.5f, 0.0f
        );
        var shape = b3.createCapsuleShape(bodyID, shapeDef, capsule);

        return new RenderCapsule(shape, bodyID);
    }

    private void syncRenderCapsule() {
        b3.bodySetTransform(this.renderCapsuleBody.bodyID, getCharacterPosition(), new Quaternionf());
    }

    private void createFloor() {

        var heightFieldDef = createHeightField(64, 64);
        heightFieldDef.globalMinimumHeight(-8.0f);
        heightFieldDef.globalMaximumHeight(8.0f);
        var heightFieldData = b3.createHeightField(this.region, heightFieldDef);

        var heightFieldBodyDef = new BodyDef();
        heightFieldBodyDef.position().set(-32, 0, -32);
        var heightFieldBody = b3.createBody(this.region, this.worldID, heightFieldBodyDef);

        var heightFieldShapeDef = new ShapeDef();
        var _ = b3.createHeightFieldShape(heightFieldBody, heightFieldShapeDef, heightFieldData);

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

    private static class CharacterMover {

        static final int planeCapacity = 8;
        static final float jumpSpeed = 9.0f;
        static final float maxSpeed = 6.0f;
        static final float minSpeed = 0.01f;
        static final float stopSpeed = 1.0f;
        static final float accelerate = 30.0f;
        static final float friction = 6.0f;
        static final float gravity = 20.0f;
        static final long moverMask = U64.MAX;
        static final long skipTeamMask = U64.MAX & ~2L;

        SampleContext sample;
        Matrix4f transform = new Matrix4f();
        Vector3f velocity = new Vector3f();
        Capsule capsule;
        CollisionPlane[] collisionPlanes = new CollisionPlane[planeCapacity];
        PlaneExtra[] planeExtras = new PlaneExtra[planeCapacity];
        int planeCount;
        int totalIterations;
        float pogoVelocity;
        boolean onGround;
        boolean sprint;

        private final Set<ShapeID> ignoreShapeIds = new HashSet<>();

        void initialize(SampleContext sample, Vector3f position) {
            this.sample = sample;
            this.transform.identity().translation(position);
            this.velocity.set(0.0f, 0.0f, 0.0f);
            this.capsule = new Capsule(
                    0.3f,
                    0.0f, -0.5f, 0.0f,
                    0.0f, 0.5f, 0.0f
            );

            for (var i = 0; i < planeCapacity; i++) {
                this.collisionPlanes[i] = new CollisionPlane(false, new Plane(), 0.0f, 0.0f, false);
                this.planeExtras[i] = new PlaneExtra();
            }

            this.planeCount = 0;
            this.totalIterations = 0;
            this.pogoVelocity = 0.0f;
            this.onGround = false;
            this.sprint = false;
        }

        void solveMove(
                @Nullable SceneDrawing draw,
                float timeStep,
                Vector3f forward,
                Vector3f right,
                Vector2f throttle,
                boolean clipVelocity
        ) {
            var speed = this.velocity.length();
            if (speed < minSpeed) {
                this.velocity.x = 0.0f;
                this.velocity.y = 0.0f;
            } else {
                var control = Math.max(speed, stopSpeed);
                var drop = control * friction * timeStep;
                var newSpeed = Math.max(0.0f, speed - drop);
//                this.velocity.mul(newSpeed / speed);
                this.velocity.x *= newSpeed / speed;
                this.velocity.z *= newSpeed / speed;
            }

            var maxSpeed = this.sprint ? 1.5f * CharacterMover.maxSpeed : CharacterMover.maxSpeed;
            var desiredVelocity = new Vector3f(forward)
                    .mul(maxSpeed * throttle.x)
                    .add(new Vector3f(right).mul(maxSpeed * throttle.y));
            var desiredSpeed = desiredVelocity.length();
            var desiredDirection = new Vector3f();

            if (desiredSpeed > 0.0f) {
                desiredDirection.set(desiredVelocity).div(desiredSpeed);
            }

            if (desiredSpeed > maxSpeed) {
                desiredVelocity.mul(maxSpeed / desiredSpeed);
                desiredSpeed = maxSpeed;
            }

            if (this.onGround) {
                this.velocity.y = 0.0f;
            }

            var currentSpeed = this.velocity.dot(desiredDirection);
            var addSpeed = desiredSpeed - currentSpeed;
            if (addSpeed > 0.0f) {
                var accelSpeed = accelerate * maxSpeed * timeStep;
                if (accelSpeed > addSpeed) {
                    accelSpeed = addSpeed;
                }
                this.velocity.add(new Vector3f(desiredDirection).mul(accelSpeed));
            }

            this.velocity.y -= gravity * timeStep;

            var pogoRestLength = 3.0f * this.capsule.radius;
            var rayLength = pogoRestLength + this.capsule.radius;
            var rayOrigin = getPosition().add(this.capsule.x1, this.capsule.y1, this.capsule.z1);
            var rayTranslation = new Vector3f(0.0f, -rayLength, 0.0f);
            var skipTeamFilter = new QueryFilter(1, skipTeamMask);
            var rayResult = new RayResult();
            var hit = Controller.b3.worldCastRayClosest(
                    rayResult,
                    this.sample.worldID,
                    rayOrigin,
                    rayTranslation,
                    skipTeamFilter
            );
            var suppressPogo = this.velocity.y > 0.0f;

            if (!hit || suppressPogo) {
                this.onGround = false;
                this.pogoVelocity = 0.0f;

                if (draw != null) {
                    var rayEnd = hit ? rayResult.point() : new Vector3f(rayOrigin).add(rayTranslation);
                    draw.drawSegmentFcn(rayOrigin, rayEnd, Color.GRAY.argb());
                }
            } else {
                this.onGround = true;
                var pogoCurrentLength = rayResult.fraction() * rayLength;

                var zeta = 0.7f;
                var hertz = 4.0f;
                var omega = 2.0f * (float) Math.PI * hertz;
                var omegaH = omega * timeStep;

                this.pogoVelocity = (this.pogoVelocity - omega * omegaH * (pogoCurrentLength - pogoRestLength))
                        / (1.0f + 2.0f * zeta * omegaH + omegaH * omegaH);

                if (draw != null) {
                    draw.drawSegmentFcn(rayOrigin, rayResult.point(), Color.GREEN.argb());
                }
            }

            var startPosition = getPosition();
            var target = getPosition()
                    .add(new Vector3f(this.velocity).mul(timeStep))
                    .add(0.0f, timeStep * this.pogoVelocity, 0.0f);

            var moverFilter = new QueryFilter(1, moverMask);

            var castFilter = new QueryFilter(1, skipTeamMask);

            this.totalIterations = 0;
            var tolerance = 0.01f;

            for (var iteration = 0; iteration < 5; ++iteration) {
                this.planeCount = 0;

                Controller.b3.worldCollideMover(
                        this.sample.worldID,
                        getPosition(),
                        this.capsule,
                        moverFilter,
                        this::onPlane
                );

                var targetDelta = target.sub(getPosition(), new Vector3f());
                var delta = new Vector3f();
                this.totalIterations += Controller.b3.solvePlanes(
                        delta,
                        targetDelta,
                        this.collisionPlanes,
                        this.planeCount
                );

                var fraction = Controller.b3.worldCastMover(
                        this.sample.worldID,
                        getPosition(),
                        this.capsule,
                        delta,
                        castFilter,
                        this::filterShape
                );

                delta.mul(fraction);
                setPosition(getPosition().add(delta));

                if (delta.lengthSquared() < tolerance * tolerance) {
                    break;
                }
            }

            applyDynamicBodyImpulses();

            if (clipVelocity) {
                //noinspection DataFlowIssue
                this.velocity = Controller.b3.clipVector(
                        this.velocity,
                        this.velocity,
                        this.collisionPlanes,
                        this.planeCount
                );
            } else if (timeStep > 0.0f) {
                this.velocity.set(getPosition()).sub(startPosition).mul(1.0f / timeStep);
            }
        }

        void step(@Nullable SceneDrawing draw, boolean clipVelocity, ShapeID... ignoreShapes) {
            this.ignoreShapeIds.clear();
            Collections.addAll(this.ignoreShapeIds, ignoreShapes);

            var throttle = new Vector2f();
            var forward = Camera.getForward(new Vector3f(), this.sample.yaw, 0);
            forward.y = 0.0f;
            var right = Camera.getForward(new Vector3f(), this.sample.yaw - 90.0f, 0.0f);

            if (this.sample.w) {
                throttle.x += 1.0f;
            }

            if (this.sample.s) {
                throttle.x -= 1.0f;
            }

            if (this.sample.a) {
                throttle.y -= 1.0f;
            }

            if (this.sample.d) {
                throttle.y += 1.0f;
            }

            if (this.sample.space && this.onGround) {
                this.velocity.y = jumpSpeed;
                this.onGround = false;
            }

            if (this.onGround) {
                this.sprint = this.sample.ctrl;
            } else {
                this.sprint = false;
            }

            var timeStep = this.sample.hertz > 0.0f ? 1.0f / this.sample.hertz : 0.0f;

            solveMove(draw, timeStep, forward, right, throttle, clipVelocity);

            if (draw != null) {
                drawMove(draw);
            }

            this.ignoreShapeIds.clear();
        }

        private boolean onPlane(ShapeID shapeId, MemoryIterator<PlaneResult> planeResults) {
            if (!filterShape(shapeId)) {
                return true;
            }

            for (var planeResult : planeResults) {
                if (this.planeCount >= planeCapacity) {
                    break;
                }

                this.collisionPlanes[this.planeCount] = new CollisionPlane(
                        true,
                        planeResult.plane(),
                        Float.MAX_VALUE,
                        0.0f,
                        true
                );
                this.planeExtras[this.planeCount].point = getPosition().add(planeResult.point());
                this.planeExtras[this.planeCount].shapeId = shapeId;
                this.planeCount += 1;
            }

            return true;
        }

        private boolean filterShape(ShapeID shapeId) {
            return !this.ignoreShapeIds.contains(shapeId);
        }

        private void applyDynamicBodyImpulses() {
            var invIB = new Matrix3f();
            var point = new Vector3f();
            var normal = new Vector3f();
            var pB = new Vector3f();
            var rB = new Vector3f();
            var rnB = new Vector3f();
            var vB = new Vector3f();
            var omegaB = new Vector3f();
            var vrB = new Vector3f();
            var impulseVector = new Vector3f();

            for (var i = 0; i < this.planeCount; ++i) {
                var bodyId = Controller.b3.shapeGetBody(this.planeExtras[i].shapeId);
                var bodyType = Controller.b3.bodyGetType(bodyId);
                if (bodyType != BodyType.DYNAMIC) {
                    continue;
                }

                point.set(this.planeExtras[i].point);
                normal.set(
                        -this.collisionPlanes[i].plane().normalX,
                        -this.collisionPlanes[i].plane().normalY,
                        -this.collisionPlanes[i].plane().normalZ
                );

                var invMassA = 0.0f;
                var invMassB = Controller.b3.bodyGetInverseMass(bodyId);
                invIB = Controller.b3.bodyGetWorldInverseRotationalInertia(invIB, bodyId);

                pB = Controller.b3.bodyGetWorldCenterOfMass(pB, bodyId);
                point.sub(pB, rB);

                rB.cross(normal, rnB);
                var kNormal = invMassA + invMassB + rnB.dot(invIB.transform(new Vector3f(rnB)));
                var normalMass = kNormal > 0.0f ? 1.0f / kNormal : 0.0f;

                vB = Controller.b3.bodyGetLinearVelocity(vB, bodyId);
                omegaB = Controller.b3.bodyGetAngularVelocity(omegaB, bodyId);
                omegaB.cross(rB, vrB).add(vB);

                var vn = vrB.sub(this.velocity, new Vector3f()).dot(normal);
                var impulse = Math.max(-normalMass * vn, 0.0f);

                impulseVector.set(normal).mul(impulse);
                this.velocity.sub(new Vector3f(impulseVector).mul(invMassA));

                Controller.b3.bodyApplyLinearImpulse(bodyId, impulseVector, point, true);
            }
        }

        private void drawMove(SceneDrawing draw) {
            var position = getPosition();
            for (var i = 0; i < this.planeCount; ++i) {
                var plane = this.collisionPlanes[i].plane();
                var normal = new Vector3f(plane.normalX, plane.normalY, plane.normalZ);
                var p1 = new Vector3f(normal).mul(plane.offset - this.capsule.radius).add(position);
                var p2 = new Vector3f(normal).mul(0.1f).add(p1);

                draw.drawPointFcn(p1, 5.0f, Color.YELLOW.argb());
                draw.drawSegmentFcn(p1, p2, Color.YELLOW.argb());
            }

            var p1 = this.transform.transformPosition(
                    this.capsule.x1,
                    this.capsule.y1,
                    this.capsule.z1,
                    new Vector3f()
            );
            var p2 = this.transform.transformPosition(
                    this.capsule.x2,
                    this.capsule.y2,
                    this.capsule.z2,
                    new Vector3f()
            );

            draw.drawCapsuleFcn(p1, p2, this.capsule.radius, Color.BLUE.argb(), 1.0f);
            draw.drawSegmentFcn(position, new Vector3f(position).add(this.velocity), Color.PURPLE.argb());
            draw.drawSegmentFcn(position, new Vector3f(position).add(0.0f, this.pogoVelocity, 0.0f), Color.CYAN.argb());
        }

        private Vector3f getPosition() {
            return this.transform.getTranslation(new Vector3f());
        }

        private void setPosition(Vector3f position) {
            this.transform.setTranslation(position);
        }

    }

    private static final class PlaneExtra {
        Vector3f point = new Vector3f();
        ShapeID shapeId = ShapeID.NULL_ID;
    }

    private static final class SampleContext {
        WorldID worldID;
        final int hertz = 60;
        boolean w;
        boolean a;
        boolean s;
        boolean d;
        boolean space;
        boolean ctrl;

        float yaw;
        float pitch;



    }

    record RenderCapsule(ShapeID shape, BodyID bodyID) {}

}

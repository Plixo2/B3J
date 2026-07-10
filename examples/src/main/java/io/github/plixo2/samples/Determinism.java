package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.abstraction.Color;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.region.Region;
import io.github.plixo2.box3d.threads.ExecutorTaskPool;
import io.github.plixo2.framework.MeshFactory;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Random;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class Determinism extends Example {
    private static final boolean record = false;
    private static final int SIZE = 29;

    private boolean saved = false;

    private final List<BodyID> bodies = new ArrayList<>();
    private @Nullable BodyID spinner;

    private float time = 0;

    @Override
    public void init(MeshFactory debugShapes) {
        initialCameraPosition(5, 15, 5);
        this.time = 0;
        this.bodies.clear();
        this.saved = false;

        var worldDef = new WorldDef();
        worldDef.debugShapeCollection(debugShapes);

        worldDef.taskPool(new ExecutorTaskPool());

        worldDef.gravity().set(0, -4f, 0);
        this.worldID = this.b3.createWorld(this.region, worldDef);

        spawnBox(
                BodyType.STATIC,
                new Vector3f(30.0f, 1, 30),
                new Vector3f(0, -1, 0)
        );
        spawnBox(
                BodyType.STATIC,
                new Vector3f(1f, 30, 30),
                new Vector3f(-15.5f, 10, 0)
        );
        spawnBox(
                BodyType.STATIC,
                new Vector3f(1f, 30, 30),
                new Vector3f(15.5f, 10, 0)
        );
        spawnBox(
                BodyType.STATIC,
                new Vector3f(30, 30, 1),
                new Vector3f(0, 10, -15.5f)
        );
        spawnBox(
                BodyType.STATIC,
                new Vector3f(30, 30, 1),
                new Vector3f(0, 10, 15.5f)
        );


        var size = SIZE;
        var count = 8000;

        var r = new Random(0);
        var boxSize = new Vector3f(0.4f);

        ShapeDef boxDef = new ShapeDef();
        boxDef.density(1.0f);
        boxDef.baseMaterial().friction(0.05f);

        DataInputStream input = null;
        if (!record) {
            var inputStream = this.getClass().getResourceAsStream("/determinism/data.bin");
            if (inputStream == null) {
                throw new RuntimeException("Failed to load data.bin");
            }
            input = new DataInputStream(inputStream);
        }

        try {
            for (var i = 0; i < count; i++) {
                var x = r.nextFloat() * size - size * 0.5f;
                var z = r.nextFloat() * size - size * 0.5f;
                var y = r.nextFloat() * 60 + 10;

                spawnColorBox(
                        BodyType.DYNAMIC,
                        boxDef,
                        boxSize,
                        new Vector3f(
                                x,
                                y,
                                z
                        ),
                        input
                );

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        var ball = spawnSphere(
                BodyType.STATIC,
                0.1f,
                new Vector3f(0, 7.5f, 0)
        );


        this.spinner = spawnBox(
                BodyType.DYNAMIC,
                boxDef,
                new Vector3f(29, 14f, 0.5f),
                new Vector3f(0, 7.5f, 0)
        );

        var jointDef = new RevoluteJointDef(this.spinner, ball);
        var axis = new Quaternionf();
        axis.rotateX(Math.PI_OVER_2_f);

        jointDef.base().localFrameA().rotation(axis);
        jointDef.base().localFrameB().rotation(axis);

        jointDef.maxMotorTorque(1000000.0f);
        jointDef.motorSpeed(7f);
        jointDef.enableMotor(true);

        var _ = this.b3.createRevoluteJoint(Region.global(), this.worldID, jointDef);

    }
    private void spawnColorBox(
            BodyType bodyType,
            ShapeDef boxDef,
            Vector3f size,
            Vector3f position,
            @Nullable DataInputStream stream
    ) throws IOException {
        var dynBodyDef = new BodyDef();
        dynBodyDef.type(bodyType);
        dynBodyDef.position().set(position);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        var dynamicBox = this.b3.makeBoxHull(size.x / 2f, size.y / 2f, size.z / 2f);
        var shapeID = this.b3.createHullShape(bodyId, boxDef, dynamicBox.base());

        if (record) {
            this.bodies.add(bodyId);
        } else {
            assert stream != null;
            var color = stream.readInt();
            var c = new Color(0xFF000000 | color);
            customColor(shapeID, c);
        }


    }


    private void save() {
        var imageResource = Determinism.class.getResourceAsStream("/determinism/uvtest.jpg");
        if (imageResource == null) {
            throw new RuntimeException("Failed to load image resource: /determinism/uvtest.jpg");
        }
        BufferedImage img;
        try {
            img = ImageIO.read(imageResource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var v = new Vector3f();

        try (var out = new DataOutputStream(Files.newOutputStream(Path.of("data.bin")))) {
            for (var body : this.bodies) {
                var position = this.b3.bodyGetPosition(v, body);
                var color = sample(img, position.x, position.z);
                out.writeInt(color);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private int sample(BufferedImage image, float x, float z) {
        float relativeX = (x + SIZE / 2f) / SIZE;
        float relativeZ = (z + SIZE / 2f) / SIZE;

        var w = image.getWidth();
        var h = image.getHeight();
        return image.getRGB(
                Math.min(Math.max((int)(relativeX * w), 0), w - 1),
                Math.min(Math.max((int)(relativeZ * h), 0), h - 1)
        );
    }



    @Override
    public void update(float dt) {
        float timeStep = 1.0f / 60.0f;
        int subStepCount = 4;

        this.time += timeStep;

        if (this.time > 35 && !this.saved) {
            this.saved = true;
            if (record) {
                save();
            }
            System.out.println("Saved");
        }

        if (this.time > 10 && this.spinner != null) {
            this.b3.destroyBody(this.spinner);
            this.spinner = null;
        }



        this.b3.worldStep(this.worldID, timeStep, subStepCount);
    }
}

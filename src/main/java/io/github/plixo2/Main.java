package io.github.plixo2;



import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.region.Region;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static io.github.plixo2.box3d.internal.Internal.U64_MAX;


public class Main {

    static void main() {
        var b3 = B3.get();
        System.out.println("b3.getVersion() = " + b3.getVersion());

        var shapeCollection = new Shapes();
        var draw = new DebugDraw(shapeCollection, shapeCollection);
        draw.drawShapes = true;

        var worldDef = new WorldDef();
        worldDef.debugShapeCollection(shapeCollection);
      //  worldDef.taskPool(new ExecutorTaskPool(32, Executors.newVirtualThreadPerTaskExecutor()));

        var region = Region.ofConfined();

        worldDef.gravity().set(0, -10f, 0);
        var worldID = b3.createWorld(region, worldDef);


        var bodyDef = new BodyDef();
        bodyDef.position().set(0, -10f, 0);

        var groundID = b3.createBody(region, worldID, bodyDef);

        var groundBox = b3.makeBoxHull(50.0f, 10.0f, 50.0f);

        ShapeDef groundShapeDef = new ShapeDef();
        var _ = b3.createHullShape(groundID, groundShapeDef, groundBox.base());


        var dynBodyDef = new BodyDef();
        dynBodyDef.type(BodyType.DYNAMIC);
        dynBodyDef.position().set(0.0f, 4.0f, 0.0f);
        BodyID bodyId = b3.createBody(region, worldID, dynBodyDef);

        var dynamicBox = b3.makeCubeHull(1.0f);

        ShapeDef dynamicShapeDef = new ShapeDef();
        dynamicShapeDef.density(1.0f);
        dynamicShapeDef.baseMaterial().friction(0.3f);
        var _ = b3.createHullShape(bodyId, dynamicShapeDef, dynamicBox.base());


        float timeStep = 1.0f / 60.0f;
        int subStepCount = 4;

        System.out.println("Main " + Thread.currentThread().getName());


        var start = System.nanoTime();

        for (int i = 0; i < 90; ++i) {

            b3.worldStep(worldID, timeStep, subStepCount);
            var position = b3.bodyGetPosition(new Vector3f(), bodyId);
            var rotation = b3.bodyGetRotation(new Quaternionf(), bodyId);

            System.out.printf(
                    "%4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f\n",
                    position.x, position.y, position.z,
                    rotation.x, rotation.y, rotation.z, rotation.w
            );

            b3.worldDraw(worldID, draw, U64_MAX);

//            Vec3 axis = new Vec3();
//            float angle = b3.getAxisAngle(axis, rotation);
        }

        var end = System.nanoTime();
        System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");




        start = System.nanoTime();
        region.close();
        end = System.nanoTime();
        System.out.println("Closed in: " + (end - start) / 1_000_000.0 + " ms");

        System.out.println("End");


    }

    static class Shapes extends DebugShapeCollection<Integer> implements DebugDrawCallbacks<Integer> {


        @Override
        protected Integer create(ShapeID shapeID, ShapeType.Shape shape) {
            System.out.println("Creating shape: " + shapeID.hashCode() + " for " + shape.getClass());
            return shapeID.hashCode();
        }

        @Override
        protected void delete(Integer object) {
            System.out.println("Deleting shape: " + object);
        }
    }


}

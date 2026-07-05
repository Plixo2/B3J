package io.github.plixo2;



import io.github.plixo2.box3d.*;


public class Main {
    static void main() {
        var b3 = B3.get();
        System.out.println("b3.getVersion() = " + b3.getVersion());


        var worldDef = b3.defaultWorldDef();

//        worldDef.taskPool(new TaskPool.Default(32, Executors.newVirtualThreadPerTaskExecutor()));

        worldDef.gravity().set(0, -10f, 0);

        try(var worldID = b3.createWorld(worldDef)) {

            var bodyDef = b3.defaultBodyDef();
            bodyDef.position().set(0, -10f, 0);

            var groundID = b3.createBody(worldID, bodyDef);

            var groundBox = b3.makeBoxHull(50.0f, 10.0f, 50.0f);

            ShapeDef groundShapeDef = b3.defaultShapeDef();
            var _ = b3.createHullShape(groundID, groundShapeDef, groundBox.base());


            var dynBodyDef = b3.defaultBodyDef();
            dynBodyDef.type(BodyType.DYNAMIC);
            dynBodyDef.position().set(0.0f, 4.0f, 0.0f);
            BodyID bodyId = b3.createBody(worldID, dynBodyDef);

            var dynamicBox = b3.makeCubeHull(1.0f);

            ShapeDef dynamicShapeDef = b3.defaultShapeDef();
            dynamicShapeDef.density(1.0f);
            dynamicShapeDef.baseMaterial().friction(0.3f);
            var _ = b3.createHullShape(bodyId, dynamicShapeDef, dynamicBox.base());


            float timeStep = 1.0f / 60.0f;
            int subStepCount = 4;

            System.out.println("Main " + Thread.currentThread().getName());


            var start = System.nanoTime();
            for (int i = 0; i < 90; ++i)
            {
                b3.worldStep(worldID, timeStep, subStepCount);
                Vec3 position = b3.bodyGetPosition(new Vec3(), bodyId);
                Quat rotation = b3.bodyGetRotation(new Quat(), bodyId);

                System.out.printf(
                        "%4.2f %4.2f %4.2f %4.2f %4.2f %4.2f %4.2f\n",
                        position.x, position.y, position.z,
                        rotation.x, rotation.y, rotation.z, rotation.w
                );

    //            Vec3 axis = new Vec3();
    //            float angle = b3.getAxisAngle(axis, rotation);
            }
            var end = System.nanoTime();
            System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
        }
        System.out.println("End");

    }


}

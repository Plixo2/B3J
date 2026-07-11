package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.box3d.threads.BuildInScheduler;
import io.github.plixo2.framework.MeshFactory;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.threads.ExecutorTaskPool;
import lombok.Getter;
import org.joml.Vector3f;


@Getter
public class HelloFloor extends Example {

    @Override
    public void init(MeshFactory debugShapes) {
        initialCameraPosition(0, 15, 10);

        var worldDef = new WorldDef();
        worldDef.debugShapeCollection(debugShapes);

        if (this.threaded) {
            worldDef.taskPool(new BuildInScheduler());
        }

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
        var spacing = 1.0f;

        for (var z = 0; z < zlevels; z += 4) {
            levels -= 2;

            for (var y = 0; y < levels; y++) {
                var count = levels - y;
                var offset = (count - 1) * spacing * 0.5f;
                for (var x = 0; x < count; x++) {
                    var _ = spawnBox(
                            BodyType.DYNAMIC,
                            new Vector3f(
                                x * spacing - offset,
                                y * spacing + 0.5f,
                                zstart + z
                            )
                    );
                }
            }
        }


        var sphere = spawnSphere(BodyType.DYNAMIC, 5, new Vector3f(0, 5, 10));
        this.b3.bodySetName(sphere, "Sphere");
    }



    @Override
    public void onClick(Vector3f dir, Vector3f origin) {
        applyClickImpuls(dir, origin, 20f);
    }


}

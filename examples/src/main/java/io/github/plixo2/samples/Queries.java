package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.box3d.BodyType;
import io.github.plixo2.box3d.WorldDef;
import io.github.plixo2.box3d.threads.BuildInScheduler;
import io.github.plixo2.box3d.threads.ExecutorTaskPool;
import io.github.plixo2.framework.MeshFactory;
import org.joml.Vector3f;

public class Queries extends Example {


    @Override
    public void init(MeshFactory debugShapes) {
        var worldDef = new WorldDef();
        worldDef.debugShapeCollection(debugShapes);

        if (this.threaded) {
            worldDef.taskPool(new BuildInScheduler());
        }

        worldDef.gravity().set(0, -4f, 0);
        this.worldID = this.b3.createWorld(this.region, worldDef);

        spawnBox(
                BodyType.STATIC,
                new Vector3f(50.0f, 1, 50),
                new Vector3f(0, -1, 0)
        );

        var levels = 45;
        var zstart = -20;
        var spacing = 1.0f;

        for (var y = 0; y < levels; y++) {
            var count = levels - y;
            var offset = (count - 1) * spacing * 0.5f;
            for (var x = 0; x < count; x++) {
                var _ = spawnBox(
                        BodyType.DYNAMIC,
                        new Vector3f(
                                x * spacing - offset,
                                y * spacing + 0.5f,
                                zstart + y * 0.3f
                        )
                );
            }
        }

    }


    @Override
    public void onClick(Vector3f dir, Vector3f origin) {
        applyClickImpuls(dir, origin, 10f);
    }


    @Override
    public void update(float dt) {
        super.update(dt);


    }
}

package io.github.plixo2.samples;

import io.github.plixo2.Example;
import io.github.plixo2.box3d.WorldDef;
import io.github.plixo2.box3d.tasks.BuildInScheduler;
import io.github.plixo2.framework.MeshFactory;

public class Controller extends Example {


    @Override
    public void init(MeshFactory debugShapes) {
        initialCameraPosition(0, 15, 40);

        var worldDef = new WorldDef();
        worldDef.debugShapes(debugShapes);

        if (this.threaded) {
            worldDef.taskPool(new BuildInScheduler());
        }

        worldDef.gravity().set(0, -10f, 0);
        this.worldID = this.b3.createWorld(this.region, worldDef);

    }

}
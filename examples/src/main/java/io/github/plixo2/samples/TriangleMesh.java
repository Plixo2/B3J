package io.github.plixo2.samples;

import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import io.github.plixo2.Example;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.tasks.BuildInScheduler;
import io.github.plixo2.framework.MeshFactory;
import lombok.val;
import org.joml.Random;
import org.joml.Vector3f;


import java.io.IOException;


public class TriangleMesh extends Example {

    @Override
    public void init(MeshFactory debugShapes) {
        initialCameraPosition(0, 15, 50);

        var worldDef = new WorldDef();
        worldDef.debugShapes(debugShapes);

        if (this.threaded) {
            worldDef.taskPool(new BuildInScheduler());
        }

        worldDef.gravity().set(0, -10f, 0);
        this.worldID = this.b3.createWorld(this.region, worldDef);

        var rd = new Random();


        for (var i = 0; i < 1000; i++) {
            var rx = rd.nextFloat() * 50 - 25;
            var ry = rd.nextFloat() * 2;
            var rz = rd.nextFloat() * 50 - 25;

            ShapeDef sphereDef = new ShapeDef();
            sphereDef.density(1.0f);
            sphereDef.baseMaterial().friction(0.1f);
            sphereDef.baseMaterial().rollingResistance(0.9f);

            var sphere = spawnSphere(
                    BodyType.DYNAMIC,
                    sphereDef,
                    0.5f,
                    new Vector3f(rx, 21 + ry, rz)
            );
            this.b3.bodySetName(sphere, "Sphere " + i);
        }

        var dynBodyDef = new BodyDef();
        dynBodyDef.type(BodyType.STATIC);
        dynBodyDef.position().set(0, -10, 0);
        BodyID bodyId = this.b3.createBody(this.region, this.worldID, dynBodyDef);

        MeshDef meshDef;
        try {
            meshDef = load();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        }
        meshDef.weldVertices(true);
        meshDef.identifyEdges(true);

        var meshData = this.b3.createMesh(this.region, meshDef);
        if (meshData == null) {
            System.err.println("Failed to create mesh");
            return;
        }

        var shapeDef = new ShapeDef();
        shapeDef.density(1.0f);
        shapeDef.baseMaterial().friction(0.1f);

        var _ = this.b3.createMeshShape(bodyId, shapeDef, meshData, new Vector3f(30f));

    }

    private MeshDef load() throws IOException {
        var stream = TriangleMesh.class.getResourceAsStream("/models/monkey.obj");
        if (stream == null) {
            throw new IOException("Failed to load model");
        }
        var object = ObjReader.read(stream);
        var render = ObjUtils.convertToRenderable(object);
        val indices = ObjData.getFaceVertexIndices(render);
        val vertices = ObjData.getVertices(render);

        return new MeshDef(
                vertices,
                indices
        );
    }
}

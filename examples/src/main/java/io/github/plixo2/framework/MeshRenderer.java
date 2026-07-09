package io.github.plixo2.framework;

import io.github.plixo2.abstraction.Camera;
import io.github.plixo2.abstraction.Mesh;
import io.github.plixo2.abstraction.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MeshRenderer {
    private final List<MultiMesh> buffers = new ArrayList<>();


    private final Shader shader;
    private final Shader.Uniform<Matrix4f> u_projView;
    private final Shader.Uniform<Vector3f> u_cameraPos;


    public MeshRenderer() {
        this.shader = Shader.fromResource("/3D");
        this.u_projView = this.shader.uniform("u_projView", Matrix4f.class);
        this.u_cameraPos = this.shader.uniform("u_cameraPos", Vector3f.class);
    }


    public void draw(
            Matrix4f viewProjection,
            Vector3f cameraPosition
    ) {
        this.u_projView.loadCached(viewProjection);
        this.u_cameraPos.loadCached(cameraPosition);

        this.shader.bind();

        for (var buffer : this.buffers) {
            buffer.draw();
        }
    }

    public void free() {
        this.buffers.forEach(MultiMesh::free);
        this.buffers.clear();
    }

    MultiMesh.MeshRecord place(MeshCreator.MeshArgs mesh) {
        if (this.buffers.isEmpty()) {
            this.buffers.add(new MultiMesh(mesh.verticies().length, mesh.indices().length));
        }
        var last = this.buffers.getLast();
        if (!last.canPlace(mesh)) {
            this.buffers.add(last = new MultiMesh(mesh.verticies().length, mesh.indices().length));
        }
        return last.place(mesh);
    }

}

package io.github.plixo2.framework;

import io.github.plixo2.abstraction.Capabilities;
import io.github.plixo2.abstraction.Color;
import io.github.plixo2.abstraction.Shader;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MeshRenderer {
    private final List<MultiMesh> buffers = new ArrayList<>();

    private final Shader shader;
    private final Shader.Uniform<Matrix4f> u_projView;
    private final Shader.Uniform<Vector3f> u_cameraPos;

    private final boolean legacy;
    private final Shader.Uniform<Integer> u_legacy_instance;

    public MeshRenderer() {
        ShaderCompileResult result;
        try {
            result = compileShader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.shader = result.shader();

        this.u_projView = this.shader.uniform("u_projView", Matrix4f.class);
        this.u_cameraPos = this.shader.uniform("u_cameraPos", Vector3f.class);

        this.legacy = result.legacy();
        this.u_legacy_instance = this.shader.uniform("u_legacy_instance", Integer.class);
    }


    void draw(
            Matrix4f viewProjection,
            Vector3f cameraPosition
    ) {
        this.u_projView.loadCached(viewProjection);
        this.u_cameraPos.loadCached(cameraPosition);

        this.shader.bind();

        for (var buffer : this.buffers) {
            buffer.draw(this.u_legacy_instance);
        }

    }

    void free() {
        this.buffers.forEach(MultiMesh::free);
        this.buffers.clear();
    }

    MultiMesh.MeshRecord place(MeshCreator.MeshArgs mesh, @Nullable Color customColor) {
        if (this.buffers.isEmpty()) {
            this.buffers.add(new MultiMesh(this.legacy, mesh.verticies().length, mesh.indices().length));
        }

        var last = this.buffers.getLast();
        if (!last.canPlace(mesh)) {
            this.buffers.add(last = new MultiMesh(this.legacy, mesh.verticies().length, mesh.indices().length));
        }

        return last.place(mesh, customColor);
    }



    private static ShaderCompileResult compileShader() throws IOException {
        var vertex = IOUtils.resourceToString("/3D/vertex.glsl", Charset.defaultCharset());
        var fragment = IOUtils.resourceToString("/3D/fragment.glsl", Charset.defaultCharset());

        var capabilities = Capabilities.get();

        if (capabilities.glDrawID()) {
            try {
                return new ShaderCompileResult(new Shader(vertex, fragment), false);
            } catch(Exception e) {
                // pass
            }
        }

        if (capabilities.shaderDrawParameters()) {
            var vertex_ = vertex.replace(
                    "#version 460 core",
                    "#version 430 core\n#extension GL_ARB_shader_draw_parameters : require\n"
            );
            vertex_ = vertex_.replace("gl_DrawID", "gl_BaseInstanceARB");

            try {
                var shader = new Shader(vertex_, fragment);
                System.err.println("Shader compiled with fallback GL_ARB_shader_draw_parameters & 4.3");
                return new ShaderCompileResult(shader, false);
            } catch(Exception e) {
                // pass
            }
        }


        var vertex__ = vertex.replace(
                "#version 460 core",
                "#version 430 core"
        );
        vertex__ = vertex__.replace("gl_DrawID", "u_legacy_instance");

        System.err.println("Shader compiled with fallback u_legacy_instance & 4.3. Consider updating your GPU driver");

        return new ShaderCompileResult(new Shader(vertex__, fragment), true);
    }

    record ShaderCompileResult(Shader shader, boolean legacy) { }

}

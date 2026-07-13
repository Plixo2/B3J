package io.github.plixo2.framework;

import io.github.plixo2.framework.abstractions.MemorySide;
import io.github.plixo2.framework.abstractions.Mesh;
import io.github.plixo2.framework.abstractions.Shader;
import io.github.plixo2.framework.abstractions.ShaderBuffer;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;

public class LineRenderer {

    private float width = 0.1f;

    private final Shader shader;
    private final Shader.Uniform<Matrix4f> u_projView;
    private final Shader.Uniform<Float> u_width;

    private final Mesh mesh;

    private final ShaderBuffer buffer;
    private int count = 0;

    public LineRenderer() {
        this.shader = Shader.fromResource("/shaders/lines");
        this.u_projView = this.shader.uniform("u_projView", Matrix4f.class);
        this.u_width = this.shader.uniform("u_width", Float.class);
        this.mesh = Mesh.shaderCreatedQuad();
        this.buffer = new ShaderBuffer(128, GL_STREAM_DRAW, 0, MemorySide.CPU_AND_GPU_SIDE);
    }

    public void setWidth(float width) {
        this.width = width;
    }

    void draw(Matrix4f viewProjection) {
        if (this.count == 0) {
            return;
        }

        this.shader.bind();
        this.u_projView.loadCached(viewProjection);
        this.u_width.loadCached(this.width);

        this.buffer.upload();
        this.buffer.bind();

        var wasCullFaceEnabled = glIsEnabled(GL_CULL_FACE);
        if (wasCullFaceEnabled) {
            glDisable(GL_CULL_FACE);
        }
        this.mesh.drawInstanced(this.count);
        if (wasCullFaceEnabled) {
            glEnable(GL_CULL_FACE);
        }

        this.count = 0;
        this.buffer.clear();
    }

    public void addLine(
            float x,
            float y,
            float z,
            float x2,
            float y2,
            float z2,
            int color
    ) {
        this.buffer.putVector4f(x, y, z, 0.0f);
        this.buffer.putVector4f(x2, y2, z2, 0.0f);
        var r = ((color >> 16) & 0xff) / 255.0f;
        var g = ((color >> 8) & 0xff) / 255.0f;
        var b = ((color) & 0xff) / 255.0f;
        var a = ((color >> 24) & 0xff) / 255.0f;
        this.buffer.putVector4f(r, g, b, a);
        this.count += 1;
    }



}

package io.github.plixo2.framework;

import io.github.plixo2.abstraction.MemorySide;
import io.github.plixo2.abstraction.Mesh;
import io.github.plixo2.abstraction.Shader;
import io.github.plixo2.abstraction.ShaderBuffer;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;

public class TextRenderer {
    private final TextAtlas textAtlas;
    private final Shader shader;
    private final Shader.Uniform<Matrix4f> u_projection;
    private final Shader.Uniform<Vector3f> u_right;
    private final Shader.Uniform<Vector3f> u_up;
    private final Shader.Uniform<Integer> u_texture;
    private final Mesh mesh;

    private int count;
    private final ShaderBuffer buffer;
    @Setter
    private float scale = 1.0f;

    public TextRenderer(TextAtlas textAtlas) {
        this.textAtlas = textAtlas;
        this.shader = Shader.fromResource("/text");
        this.u_projection = this.shader.uniform("u_projection", Matrix4f.class);
        this.u_right = this.shader.uniform("u_right", Vector3f.class);
        this.u_up = this.shader.uniform("u_up", Vector3f.class);
        this.u_texture = this.shader.uniform("u_texture", Integer.class);
        this.mesh = Mesh.shaderCreatedQuad();
        this.buffer = new ShaderBuffer(128, GL_STREAM_DRAW, 0, MemorySide.CPU_AND_GPU_SIDE);
    }

    void draw(Matrix4f projection) {
        draw(projection, new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
    }

    void draw(Matrix4f projection, Vector3f right, Vector3f up) {
        if (this.count == 0) {
            return;
        }

        var texture = this.textAtlas.texture().get();
        this.shader.bind();
        this.u_projection.loadCached(projection);
        this.u_right.loadCached(right);
        this.u_up.loadCached(up);
        this.u_texture.loadCached(0);
        texture.bindTextureUnit(0);

        this.buffer.upload();
        this.buffer.bind();

        this.mesh.drawInstanced(this.count);

        this.count = 0;
        this.buffer.clear();
    }

    public void putString(
            MemorySegment cString, long offset, float x, float y, float z,
            int color
    ) {
        var metrics = this.textAtlas.metrics();
        float xPos = x;
        while (true) {
            var c = cString.get(ValueLayout.JAVA_BYTE, offset++);
            if (c == 0) {
                break;
            }
            var metric = metrics[c];
            float width = metric.width() * this.scale;
            float height = metric.height() * this.scale;

            if (c == '\t' || c == ' ') {
                xPos += width;
                continue;
            }

            putGlyphData(color, x, y, z, width, height, xPos - x, 0.0f, metric.uv());

            xPos += width;

        }

    }

    public void putString(
            String text, float x, float y, float z,
            io.github.plixo2.abstraction.Color color
    ) {
        var metrics = this.textAtlas.metrics();
        float xPos = x;
        var limit = text.length();

        var argb = color.argb();

        for (var i = 0; i < limit; i++) {
            var c = text.charAt(i);
            if (c > 255) {
                c = '?';
            }
            var metric = metrics[c];
            float width = metric.width() * this.scale;
            float height = metric.height() * this.scale;

            if (c == '\t' || c == ' ') {
                xPos += width;
                continue;
            }

            putGlyphData(argb, x, y, z, width, height, xPos - x, 0.0f, metric.uv());

            xPos += width;

        }
    }

    private void putGlyphData(
            int color,
            float x, float y, float z,
            float width,
            float height,
            float offsetX,
            float offsetY,
            Vector4f uv
    ) {
        var r = ((color >> 16) & 0xff) / 255.0f;
        var g = ((color >> 8) & 0xff) / 255.0f;
        var b = ((color) & 0xff) / 255.0f;
        var a = ((color >> 24) & 0xff) / 255.0f;

        this.buffer.putVector4f(r, g, b, a);
        this.buffer.putVector4f(uv);
        this.buffer.putVector4f(x, y, z, 1.0f);
        this.buffer.putVector4f(width, height, offsetX, offsetY);
        this.count += 1;
    }


}

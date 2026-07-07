package io.github.plixo2;


import io.github.plixo2.abstraction.Camera;
import io.github.plixo2.abstraction.Color;
import io.github.plixo2.abstraction.Mesh;
import io.github.plixo2.abstraction.Shader;
import io.github.plixo2.box3d.DebugDrawCallbacks;
import org.joml.Matrix4f;
import org.joml.Vector3f;


public class SceneRendering implements DebugDrawCallbacks<Mesh> {
    private final long window;
    private final Camera.FreeCam camera;

    private final Shader shader;
    private final Shader.Uniform<Matrix4f> u_projView;
    private final Shader.Uniform<Matrix4f> u_model;
    private final Shader.Uniform<Matrix4f> u_normal;
    private final Shader.Uniform<Color> u_color;
    private final Shader.Uniform<Vector3f> u_cameraPos;

    SceneRendering(long window) {
        this.window = window;
        this.camera = new Camera.FreeCam();
        this.camera.x = 0;
        this.camera.y = 15;
        this.camera.z = 50;

        var dir = new Vector3f(-this.camera.x, -this.camera.y, -this.camera.z).normalize();
        this.camera.yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
        this.camera.pitch = (float) Math.toDegrees(Math.asin(dir.y));

        this.shader = Shader.fromResource("/3D");
        this.u_projView = this.shader.uniform("u_projView", Matrix4f.class);
        this.u_model = this.shader.uniform("u_model", Matrix4f.class);
        this.u_normal = this.shader.uniform("u_normal", Matrix4f.class);
        this.u_color = this.shader.uniform("u_color", Color.class);
        this.u_cameraPos = this.shader.uniform("u_cameraPos", Vector3f.class);
    }

    Camera.WorldCoords screenToWorldCoords(
            int width,
            int height,
            float x,
            float y
    ) {
        var inv = new Matrix4f();
        var projection = this.camera.getProjection(width, height);
        projection.invertPerspectiveView(this.camera.getView(), inv);
        var coords = Camera.screenToWorld(inv, x / width, y / height);
        return new Camera.WorldCoords(coords.dir(), this.camera.getPosition());
    }

    void update(
            int width,
            int height,
            float mdx,
            float mdy,
            float dt
    ) {
        this.camera.move(this.window, mdx, mdy, dt * 10f);

        var projection = this.camera.getProjection(width, height);
        var view = this.camera.getView();
        var projView = projection.mul(view);
        this.u_projView.loadCached(projView);

        var cameraPosition = this.camera.getPosition();
        this.u_cameraPos.loadCached(cameraPosition);

        this.shader.bind();
    }

    private final ColorCache colorCache = new ColorCache();
    private final Matrix4f normal = new Matrix4f();


    @Override
    public boolean drawShape(Mesh shape, Matrix4f transform, int color) {

        this.u_model.loadCached(transform);
        this.u_normal.loadCached(transform.normal(this.normal));
        this.u_color.loadCached(this.colorCache.get(color));

        shape.draw();

        return true;
    }

    private String toBinaryBlocks(int color) {
        var binaryString = Integer.toBinaryString(color);
        binaryString = "0".repeat(32 - binaryString.length()) + binaryString;

        var sb = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i++) {
            if (i > 0 && i % 8 == 0) {
                sb.append("-");
            }
            sb.append(binaryString.charAt(i));
        }
        return sb.toString();
    }


}

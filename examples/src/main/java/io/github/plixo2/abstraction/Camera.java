package io.github.plixo2.abstraction;

import lombok.val;
import org.joml.*;

import java.awt.*;
import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;

public interface Camera {
    Matrix4f getView();
    Matrix4f getProjection(float width, float height);
    Matrix4f getProjection(float aspect);
    Vector3f getPosition();


    static WorldCoords screenToWorld(Matrix4f projectionViewInv, float x, float y) {
        var xNDC = (2 * (x) - 1f);
        var yNDC = (-2 * (y) + 1f);

        val near = (new Vector4f(xNDC, yNDC, 0.0f, 1)).mul(projectionViewInv);
        val far = (new Vector4f(xNDC, yNDC, 0.9f, 1)).mul(projectionViewInv);
        val near_ = new Vector3f(near.x, near.y, near.z).div(near.w);
        val far_ = new Vector3f(far.x, far.y, far.z).div(far.w);
        val dir = far_.sub(near_);
        val origin = new Vector3f(near_.x, near_.y, near_.z);
        return new WorldCoords(dir.normalize(), origin);
    }


    record WorldCoords(Vector3f dir, Vector3f origin) { }

    static Vector3f getForward(Vector3f in, float yaw, float pitch) {
        pitch = (float) Math.toRadians(pitch);
        yaw = (float) Math.toRadians(yaw);
        float x = (float) Math.cos(pitch) * (float) Math.sin(yaw);
        float y = (float) Math.sin(pitch);
        float z = (float) Math.cos(pitch) * (float) Math.cos(yaw);
        return in.set(x, y, z);
    }

    class FreeCam implements Camera {

        public float fov = 90;
        public float zNear = 0.1f;
        public float zFar = 10000f;

        public float x;
        public float y;
        public float z;

        public float pitch;
        public float yaw;

        @Override
        public Matrix4f getView() {
            var matrix4f = new Matrix4d();
            var posVec = new Vector3d(x, y, z);
            var forward = Camera.getForward(new Vector3f(), yaw, pitch);
            var forwardVec = new Vector3d(forward.x(), forward.y(), forward.z());
            matrix4f.setLookAt(posVec, new Vector3d(posVec).add(forwardVec), new Vector3d(0, 1, 0));
            return new Matrix4f(matrix4f);
        }

        @Override
        public Matrix4f getProjection(float width, float height) {
            return getProjection(width / height);
        }

        @Override
        public Matrix4f getProjection(float aspect) {
            return new Matrix4f().perspective((float) Math.toRadians(fov), aspect, zNear, zFar);
        }

        @Override
        public Vector3f getPosition() {
            return new Vector3f(x, y, z);
        }

        public void move(
                long window,
                float mx,
                float my,
                float delta
        ) {

            var mouseDown = glfwGetMouseButton(window, 1) == GLFW_PRESS;
            var w = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
            var a = glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
            var s = glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
            var d = glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
            var space = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
            var shift = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
            var ctrl = glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS;

            if (ctrl) {
                delta *= 4f;
            }

            if (mouseDown) {
                this.yaw -= (float) (mx * 0.2);
                this.pitch -= (float) (my * 0.2);
                this.pitch = Math.clamp(this.pitch, -89.99999f, 89.9999f);
            }

            var forward = Camera.getForward(new Vector3f(), this.yaw, 0);
            var sidewards = Camera.getForward(new Vector3f(), this.yaw + 90, 0);
            var dir = new Vector3f();

            if (w) {
                dir.add(forward);
            }
            if (s) {
                dir.sub(forward);
            }
            if (a) {
                dir.add(sidewards);
            }
            if (d) {
                dir.sub(sidewards);
            }
            if (dir.lengthSquared() > 0.0001) {
                dir.normalize();
            }

            if (space) {
                dir.add(0, 1, 0);
            }
            if (shift) {
                dir.sub(0, 1, 0);
            }

            dir.mul(delta);

            this.x += dir.x;
            this.y += dir.y;
            this.z += dir.z;


        }

    }

}

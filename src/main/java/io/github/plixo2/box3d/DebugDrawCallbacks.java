package io.github.plixo2.box3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface DebugDrawCallbacks<T> {

    default boolean drawShape(T shape, Matrix4f transform, int color) {
        return false;
    }

    default void drawSegment(Vector3f p1, Vector3f p2, int color) {
    }

    default void drawTransform(Matrix4f transform) {
    }

    default void drawPoint(Vector3f p, float size, int color) {
    }

    default void drawSphere(Vector3f p, float radius, int color, float alpha) {
    }

    default void drawCapsule(Vector3f p1, Vector3f p2, float radius, int color, float alpha) {
    }

    default void drawBounds(AABB aabb, int color) {
    }

    default void drawBox(Vector3f extend, Matrix4f transform, int color) {
    }

    default void drawString(Vector3f p, String text, int color) {
    }
}

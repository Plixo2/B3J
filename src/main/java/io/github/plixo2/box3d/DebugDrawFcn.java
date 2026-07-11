package io.github.plixo2.box3d;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

public interface DebugDrawFcn<T> {

    default boolean drawShapeFcn(@Nullable T userShape, Matrix4f transform, int color) {
        return false;
    }

    default void drawSegmentFcn(Vector3f p1, Vector3f p2, int color) {
    }

    default void drawTransformFcn(Matrix4f transform) {
    }

    default void drawPointFcn(Vector3f p, float size, int color) {
    }

    default void drawSphereFcn(Vector3f p, float radius, int color, float alpha) {
    }

    default void drawCapsuleFcn(Vector3f p1, Vector3f p2, float radius, int color, float alpha) {
    }

    default void drawBoundsFcn(AABB aabb, int color) {
    }

    default void drawBoxFcn(Vector3f extend, Matrix4f transform, int color) {
    }

    default void drawStringFcn(Vector3f p, MemorySegment string, long byteOffset, int color) {
    }
}

package io.github.plixo2.framework;


import io.github.plixo2.box3d.AABB;
import io.github.plixo2.box3d.DebugDrawFcn;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;


@RequiredArgsConstructor
public class SceneDrawing implements DebugDrawFcn<MultiMesh.MeshRecord> {

    private static final int CIRCLE_SEGMENTS = 32;
    private static final float[] circ_sin;
    private static final float[] circ_cos;

    static {
        circ_sin = new float[CIRCLE_SEGMENTS];
        circ_cos = new float[CIRCLE_SEGMENTS];
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            float angle = (float) ((i + 1) * Math.PI * 2.0 / CIRCLE_SEGMENTS);
            circ_sin[i] = (float) Math.sin(angle);
            circ_cos[i] = (float) Math.cos(angle);
        }
    }

    private static final float TRANSFORM_AXIS_LENGTH = 0.9f;

    private final LineRenderer lineRenderer;
    private final TextRenderer.World text3D;

    private final Matrix4f normal = new Matrix4f();
    private final Vector3f v1 = new Vector3f();
    private final Vector3f v2 = new Vector3f();

    @Override
    public boolean drawShapeFcn(@Nullable MultiMesh.MeshRecord userShape, Matrix4f transform, int color) {
        if (userShape == null) {
            System.err.println("Invalid userShape");
            return true;
        }
        var customColor = userShape.customColor();
        userShape.putDraw(
                transform,
                transform.normal(this.normal),
                customColor != null ? customColor.argb() : color
        );

        return true;
    }


    @Override
    public void drawSegmentFcn(Vector3f p1, Vector3f p2, int color) {
        addLine(
                p1.x, p1.y, p1.z,
                p2.x, p2.y, p2.z,
                makeColorCode(color, 1.0f)
        );
    }

    @Override
    public void drawTransformFcn(Matrix4f transform) {
        var x = transform.m30();
        var y = transform.m31();
        var z = transform.m32();

        addLine(
                x, y, z,
                x + transform.m00() * TRANSFORM_AXIS_LENGTH,
                y + transform.m01() * TRANSFORM_AXIS_LENGTH,
                z + transform.m02() * TRANSFORM_AXIS_LENGTH,
                0xFFFF0000
        );
        addLine(
                x, y, z,
                x + transform.m10() * TRANSFORM_AXIS_LENGTH,
                y + transform.m11() * TRANSFORM_AXIS_LENGTH,
                z + transform.m12() * TRANSFORM_AXIS_LENGTH,
                0xFF00FF00
        );
        addLine(
                x, y, z,
                x + transform.m20() * TRANSFORM_AXIS_LENGTH,
                y + transform.m21() * TRANSFORM_AXIS_LENGTH,
                z + transform.m22() * TRANSFORM_AXIS_LENGTH,
                0xFF0000FF
        );
    }


    @Override
    public void drawPointFcn(Vector3f p, float size, int color) {
        color = makeColorCode(color, 1.0f);

        var s = 0.1f;
        addLine(p.x - s, p.y, p.z, p.x + s, p.y, p.z, color);
        addLine(p.x, p.y - s, p.z, p.x, p.y + s, p.z, color);
        addLine(p.x, p.y, p.z - s, p.x, p.y, p.z + s, color);
    }

    @Override
    public void drawSphereFcn(Vector3f p, float radius, int color, float alpha) {
        color = makeColorCode(color, alpha);
        drawSphereLines(p.x, p.y, p.z, radius, color);
    }


    @Override
    public void drawCapsuleFcn(Vector3f p1, Vector3f p2, float radius, int color, float alpha) {
        color = makeColorCode(color, alpha);

        var ax = p2.x - p1.x;
        var ay = p2.y - p1.y;
        var az = p2.z - p1.z;
        var lengthSquared = ax * ax + ay * ay + az * az;
        if (lengthSquared <= 0.000001f) {
            drawSphereLines(p1.x, p1.y, p1.z, radius, color);
            return;
        }

        var inverseLength = 1.0f / (float) Math.sqrt(lengthSquared);
        ax *= inverseLength;
        ay *= inverseLength;
        az *= inverseLength;

        var tx = az;
        var ty = 0.0f;
        var tz = -ax;
        if (Math.abs(ay) >= 0.9f) {
            tx = 0.0f;
            ty = -az;
            tz = ay;
        }

        var tangentLength = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
        var inverseTangentLength = 1.0f / tangentLength;
        tx *= inverseTangentLength * radius;
        ty *= inverseTangentLength * radius;
        tz *= inverseTangentLength * radius;

        var bx = ay * tz - az * ty;
        var by = az * tx - ax * tz;
        var bz = ax * ty - ay * tx;

        drawCircle(p1.x, p1.y, p1.z, tx, ty, tz, bx, by, bz, color);
        drawCircle(p2.x, p2.y, p2.z, tx, ty, tz, bx, by, bz, color);
        drawCircle(p1.x, p1.y, p1.z, ax * radius, ay * radius, az * radius, tx, ty, tz, color);
        drawCircle(p2.x, p2.y, p2.z, ax * radius, ay * radius, az * radius, tx, ty, tz, color);
        drawCircle(p1.x, p1.y, p1.z, ax * radius, ay * radius, az * radius, bx, by, bz, color);
        drawCircle(p2.x, p2.y, p2.z, ax * radius, ay * radius, az * radius, bx, by, bz, color);

        addLine(p1.x + tx, p1.y + ty, p1.z + tz, p2.x + tx, p2.y + ty, p2.z + tz, color);
        addLine(p1.x - tx, p1.y - ty, p1.z - tz, p2.x - tx, p2.y - ty, p2.z - tz, color);
        addLine(p1.x + bx, p1.y + by, p1.z + bz, p2.x + bx, p2.y + by, p2.z + bz, color);
        addLine(p1.x - bx, p1.y - by, p1.z - bz, p2.x - bx, p2.y - by, p2.z - bz, color);
    }


    @Override
    public void drawBoundsFcn(AABB aabb, int color) {
        color = makeColorCode(color, 1.0f);

        var minX = aabb.lowerBound.x;
        var minY = aabb.lowerBound.y;
        var minZ = aabb.lowerBound.z;
        var maxX = aabb.upperBound.x;
        var maxY = aabb.upperBound.y;
        var maxZ = aabb.upperBound.z;

        addBoxLines(minX, minY, minZ, maxX, maxY, maxZ, color);
    }

    @Override
    public void drawBoxFcn(Vector3f extend, Matrix4f transform, int color) {
        color = makeColorCode(color, 1.0f);

        var x = extend.x;
        var y = extend.y;
        var z = extend.z;

        drawTransformedBoxLine(transform, -x, -y, -z, x, -y, -z, color);
        drawTransformedBoxLine(transform, -x, y, -z, x, y, -z, color);
        drawTransformedBoxLine(transform, -x, -y, z, x, -y, z, color);
        drawTransformedBoxLine(transform, -x, y, z, x, y, z, color);

        drawTransformedBoxLine(transform, -x, -y, -z, -x, y, -z, color);
        drawTransformedBoxLine(transform, x, -y, -z, x, y, -z, color);
        drawTransformedBoxLine(transform, -x, -y, z, -x, y, z, color);
        drawTransformedBoxLine(transform, x, -y, z, x, y, z, color);

        drawTransformedBoxLine(transform, -x, -y, -z, -x, -y, z, color);
        drawTransformedBoxLine(transform, x, -y, -z, x, -y, z, color);
        drawTransformedBoxLine(transform, -x, y, -z, -x, y, z, color);
        drawTransformedBoxLine(transform, x, y, -z, x, y, z, color);
    }

    @Override
    public void drawStringFcn(Vector3f p, MemorySegment string, long byteOffset, int color) {

        this.text3D.putString(
                string,
                byteOffset,
                p.x,
                p.y,
                p.z,
                0xFF000000 | color
        );

    }








    private void drawSphereLines(float x, float y, float z, float radius, int color) {
        drawCircle(
                x, y, z,
                radius, 0.0f, 0.0f,
                0.0f, radius, 0.0f,
                color
        );
        drawCircle(
                x, y, z,
                radius, 0.0f, 0.0f,
                0.0f, 0.0f, radius,
                color
        );
        drawCircle(
                x, y, z,
                0.0f, radius, 0.0f,
                0.0f, 0.0f, radius,
                color
        );
    }

    private void addBoxLines(
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            int color
    ) {
        addLine(minX, minY, minZ, maxX, minY, minZ, color);
        addLine(minX, maxY, minZ, maxX, maxY, minZ, color);
        addLine(minX, minY, maxZ, maxX, minY, maxZ, color);
        addLine(minX, maxY, maxZ, maxX, maxY, maxZ, color);

        addLine(minX, minY, minZ, minX, maxY, minZ, color);
        addLine(maxX, minY, minZ, maxX, maxY, minZ, color);
        addLine(minX, minY, maxZ, minX, maxY, maxZ, color);
        addLine(maxX, minY, maxZ, maxX, maxY, maxZ, color);

        addLine(minX, minY, minZ, minX, minY, maxZ, color);
        addLine(maxX, minY, minZ, maxX, minY, maxZ, color);
        addLine(minX, maxY, minZ, minX, maxY, maxZ, color);
        addLine(maxX, maxY, minZ, maxX, maxY, maxZ, color);
    }

    private void drawTransformedBoxLine(
            Matrix4f transform,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            int color
    ) {
        transform.transformPosition(x1, y1, z1, this.v1);
        transform.transformPosition(x2, y2, z2, this.v2);

        addLine(
                this.v1.x, this.v1.y, this.v1.z,
                this.v2.x, this.v2.y, this.v2.z,
                color
        );
    }

    private void drawCircle(
            float centerX,
            float centerY,
            float centerZ,
            float axisAX,
            float axisAY,
            float axisAZ,
            float axisBX,
            float axisBY,
            float axisBZ,
            int color
    ) {
        var lastX = centerX + axisAX;
        var lastY = centerY + axisAY;
        var lastZ = centerZ + axisAZ;

        for (var i = 0; i < CIRCLE_SEGMENTS; i++) {
            var cos = circ_cos[i];
            var sin = circ_sin[i];

            var x = centerX + axisAX * cos + axisBX * sin;
            var y = centerY + axisAY * cos + axisBY * sin;
            var z = centerZ + axisAZ * cos + axisBZ * sin;

            addLine(lastX, lastY, lastZ, x, y, z, color);

            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    private void addLine(
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            int color
    ) {
        this.lineRenderer.addLine(
                x1, y1, z1,
                x2, y2, z2,
                color
        );
    }

    private static int makeColorCode(int color, float alpha) {
        color = color & 0xFFFFFF;
        var i = (int) (Math.clamp(alpha, 0f, 1f) * 255.0f);
        return (i << 24) | color;
    }
}

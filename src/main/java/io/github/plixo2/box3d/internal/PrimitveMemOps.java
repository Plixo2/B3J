package io.github.plixo2.box3d.internal;

import org.box2d.box3d.b3Matrix3;
import org.box2d.box3d.b3Quat;
import org.box2d.box3d.b3Transform;
import org.box2d.box3d.b3Vec3;
import org.joml.*;

import java.lang.foreign.MemorySegment;

public class PrimitveMemOps {

    public static void putQuat(MemorySegment segment, Quaternionf quat) {
        putQuat(segment, quat.x(), quat.y(), quat.z(), quat.w());
    }
    public static void putQuat(MemorySegment segment, float x, float y, float z, float w) {
        b3Quat.s(segment, w);
        var vec = b3Quat.v(segment);
        putVec3(vec, x, y, z);
    }
    public static Quaternionf setQuat(Quaternionf quat, MemorySegment segment) {
        quat.w = b3Quat.s(segment);
        var vec = b3Quat.v(segment);
        quat.x = b3Vec3.x(vec);
        quat.y = b3Vec3.y(vec);
        quat.z = b3Vec3.z(vec);
        return quat;
    }


    public static void putVec3(MemorySegment segment, Vector3f vec3) {
        putVec3(segment, vec3.x, vec3.y, vec3.z);
    }

    public static void putVec3(MemorySegment segment, float x, float y, float z) {
        b3Vec3.x(segment, x);
        b3Vec3.y(segment, y);
        b3Vec3.z(segment, z);
    }
    public static Vector3f setVec3(Vector3f vec3, MemorySegment segment) {
        vec3.x = b3Vec3.x(segment);
        vec3.y = b3Vec3.y(segment);
        vec3.z = b3Vec3.z(segment);
        return vec3;
    }

    public static void putMat3(MemorySegment segment, Matrix3f matrix) {
        // * struct b3Matrix3 {
        // *     b3Vec3 cx;
        // *     b3Vec3 cy;
        // *     b3Vec3 cz;
        // * }
        putVec3(b3Matrix3.cx(segment), matrix.m00(), matrix.m01(), matrix.m02());
        putVec3(b3Matrix3.cy(segment), matrix.m10(), matrix.m11(), matrix.m12());
        putVec3(b3Matrix3.cz(segment), matrix.m20(), matrix.m21(), matrix.m22());
    }

    public static Matrix3f setMat3(Matrix3f matrix, MemorySegment segment) {
        matrix.m00(b3Vec3.x(b3Matrix3.cx(segment)));
        matrix.m01(b3Vec3.y(b3Matrix3.cx(segment)));
        matrix.m02(b3Vec3.z(b3Matrix3.cx(segment)));
        matrix.m10(b3Vec3.x(b3Matrix3.cy(segment)));
        matrix.m11(b3Vec3.y(b3Matrix3.cy(segment)));
        matrix.m12(b3Vec3.z(b3Matrix3.cy(segment)));
        matrix.m20(b3Vec3.x(b3Matrix3.cz(segment)));
        matrix.m21(b3Vec3.y(b3Matrix3.cz(segment)));
        matrix.m22(b3Vec3.z(b3Matrix3.cz(segment)));
        return matrix;
    }

    public static void putTransform(
            MemorySegment segment,
            Quaternionf tempQuat,
            Matrix4f matrix
    ) {
        var quaternion = b3Transform.q(segment);
        var position = b3Transform.p(segment);

        putVec3(position, matrix.m30(), matrix.m31(), matrix.m32());

        if ((matrix.properties() & Matrix4fc.PROPERTY_ORTHONORMAL) == 0) {
            tempQuat.setFromUnnormalized(matrix);
        } else {
            tempQuat.setFromNormalized(matrix);
        }

        putQuat(quaternion, tempQuat);
    }

    public static Matrix4f setTransform(
            Matrix4f matrix,
            MemorySegment segment
    ) {
        var quaternion = b3Transform.q(segment);
        var quatVec = b3Quat.v(quaternion);
        var position = b3Transform.p(segment);

        matrix.translationRotate(
                b3Vec3.x(position), b3Vec3.y(position), b3Vec3.z(position),
                b3Vec3.x(quatVec), b3Vec3.y(quatVec), b3Vec3.z(quatVec), b3Quat.s(quaternion)
        );
        matrix.m03(0.0f).m13(0.0f).m23(0.0f);

        return matrix;
    }

}

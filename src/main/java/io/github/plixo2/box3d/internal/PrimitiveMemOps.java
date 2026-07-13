package io.github.plixo2.box3d.internal;

import org.box2d.box3d.*;
import org.joml.*;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.joml.Matrix4fc.PROPERTY_ORTHONORMAL;

public final class PrimitiveMemOps {

    private static final long transform_pos_offset = b3Transform.p$offset();
    private static final long transform_rot_offset = b3Transform.q$offset();
    private static final long rotation_vec_offset = b3Quat.v$offset();
    private static final long rotation_w_offset = b3Quat.s$offset();

    private static final long transform_rot_w_offset = transform_rot_offset + rotation_w_offset;
    private static final long transform_rot_vec_offset = transform_rot_offset + rotation_vec_offset;

    private static final long mat3_cx_offset = b3Matrix3.cx$offset();
    private static final long mat3_cy_offset = b3Matrix3.cy$offset();
    private static final long mat3_cz_offset = b3Matrix3.cz$offset();

    public static void putQuat(MemorySegment segment, Quaternionf quat) {
        putQuat(segment, quat.x(), quat.y(), quat.z(), quat.w());
    }
    public static void putQuat(MemorySegment segment, float x, float y, float z, float w) {
        segment.set(ValueLayout.JAVA_FLOAT, rotation_vec_offset + Float.BYTES * 0, x);
        segment.set(ValueLayout.JAVA_FLOAT, rotation_vec_offset + Float.BYTES * 1, y);
        segment.set(ValueLayout.JAVA_FLOAT, rotation_vec_offset + Float.BYTES * 2, z);
        segment.set(ValueLayout.JAVA_FLOAT, rotation_w_offset, w);
    }

    public static Quaternionf setQuat(Quaternionf quat, MemorySegment segment) {
        quat.x = segment.get(ValueLayout.JAVA_FLOAT, rotation_vec_offset + Float.BYTES * 0);
        quat.y = segment.get(ValueLayout.JAVA_FLOAT, rotation_vec_offset + Float.BYTES * 1);
        quat.z = segment.get(ValueLayout.JAVA_FLOAT, rotation_vec_offset + Float.BYTES * 2);
        quat.w = segment.get(ValueLayout.JAVA_FLOAT, rotation_w_offset);
        return quat;
    }


    public static void putVec3(MemorySegment segment, Vector3f vec3) {
        putVec3(segment, vec3.x, vec3.y, vec3.z);
    }

    public static void putVec3(MemorySegment segment, float x, float y, float z) {
        segment.set(ValueLayout.JAVA_FLOAT, Float.BYTES * 0, x);
        segment.set(ValueLayout.JAVA_FLOAT, Float.BYTES * 1, y);
        segment.set(ValueLayout.JAVA_FLOAT, Float.BYTES * 2, z);
    }

    public static Vector3f setVec3(Vector3f vec3, MemorySegment segment) {
        return setVec3(vec3, segment, 0);
    }
    public static Vector3f setVec3(Vector3f vec3, MemorySegment segment, long offset) {
        vec3.x = segment.get(ValueLayout.JAVA_FLOAT, offset + Float.BYTES * 0);
        vec3.y = segment.get(ValueLayout.JAVA_FLOAT, offset + Float.BYTES * 1);
        vec3.z = segment.get(ValueLayout.JAVA_FLOAT, offset + Float.BYTES * 2);
        return vec3;
    }

    public static void putMat3(MemorySegment segment, Matrix3f matrix) {
        putMat3(segment, matrix, 0);
    }
    public static void putMat3(MemorySegment segment, Matrix3f matrix, long offset) {

        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cx_offset + Float.BYTES * 0, matrix.m00);
        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cx_offset + Float.BYTES * 1, matrix.m01);
        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cx_offset + Float.BYTES * 2, matrix.m02);

        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cy_offset + Float.BYTES * 0, matrix.m10);
        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cy_offset + Float.BYTES * 1, matrix.m11);
        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cy_offset + Float.BYTES * 2, matrix.m12);

        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cz_offset + Float.BYTES * 0, matrix.m20);
        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cz_offset + Float.BYTES * 1, matrix.m21);
        segment.set(ValueLayout.JAVA_FLOAT, offset + mat3_cz_offset + Float.BYTES * 2, matrix.m22);

    }

    public static Matrix3f setMat3(Matrix3f matrix, MemorySegment segment) {
        return setMat3(matrix, segment, 0);
    }
    public static Matrix3f setMat3(Matrix3f matrix, MemorySegment segment, long offset) {

        matrix.m00 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cx_offset + Float.BYTES * 0);
        matrix.m01 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cx_offset + Float.BYTES * 1);
        matrix.m02 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cx_offset + Float.BYTES * 2);

        matrix.m10 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cy_offset + Float.BYTES * 0);
        matrix.m11 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cy_offset + Float.BYTES * 1);
        matrix.m12 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cy_offset + Float.BYTES * 2);

        matrix.m20 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cz_offset + Float.BYTES * 0);
        matrix.m21 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cz_offset + Float.BYTES * 1);
        matrix.m22 = segment.get(ValueLayout.JAVA_FLOAT, offset + mat3_cz_offset + Float.BYTES * 2);

        return matrix;
    }

    public static void putTransform(
            MemorySegment segment,
            Quaternionf tempQuat,
            Matrix4f matrix
    ) {

        // PROPERTY_ORTHONORMAL implies PROPERTY_AFFINE
        if (
                   (matrix.properties() & PROPERTY_ORTHONORMAL) != 0
                || (matrix.determineProperties().properties() & PROPERTY_ORTHONORMAL) != 0
        ) {
            tempQuat.setFromNormalized(matrix);
        } else {
            throw new IllegalArgumentException(
                    "Matrix4f transforms must only contain translation and rotation"
            );
        }


        segment.set(ValueLayout.JAVA_FLOAT, transform_pos_offset + Float.BYTES * 0, matrix.m30());
        segment.set(ValueLayout.JAVA_FLOAT, transform_pos_offset + Float.BYTES * 1, matrix.m31());
        segment.set(ValueLayout.JAVA_FLOAT, transform_pos_offset + Float.BYTES * 2, matrix.m32());

        segment.set(ValueLayout.JAVA_FLOAT, transform_rot_vec_offset + Float.BYTES * 0, tempQuat.x);
        segment.set(ValueLayout.JAVA_FLOAT, transform_rot_vec_offset + Float.BYTES * 1, tempQuat.y);
        segment.set(ValueLayout.JAVA_FLOAT, transform_rot_vec_offset + Float.BYTES * 2, tempQuat.z);
        segment.set(ValueLayout.JAVA_FLOAT, transform_rot_w_offset, tempQuat.w);

    }

    public static Matrix4f setTransform(
            Matrix4f matrix,
            MemorySegment segment
    ) {
        return setTransform(matrix, segment, 0L);
    }

    public static Matrix4f setTransform(
            Matrix4f matrix,
            float px, float py, float pz,
            float qx, float qy, float qz, float qw
    ) {
        matrix.translationRotate(px, py, pz, qx, qy, qz, qw);
        matrix.m03(0.0f).m13(0.0f).m23(0.0f);

        return matrix;
    }
    public static Matrix4f setTransform(
            Matrix4f matrix,
            MemorySegment segment,
            long offset
    ) {
        var px = segment.get(ValueLayout.JAVA_FLOAT, offset + transform_pos_offset + Float.BYTES * 0);
        var py = segment.get(ValueLayout.JAVA_FLOAT, offset + transform_pos_offset + Float.BYTES * 1);
        var pz = segment.get(ValueLayout.JAVA_FLOAT, offset + transform_pos_offset + Float.BYTES * 2);

        var qx = segment.get(ValueLayout.JAVA_FLOAT, offset + transform_rot_vec_offset + Float.BYTES * 0);
        var qy = segment.get(ValueLayout.JAVA_FLOAT, offset + transform_rot_vec_offset + Float.BYTES * 1);
        var qz = segment.get(ValueLayout.JAVA_FLOAT, offset + transform_rot_vec_offset + Float.BYTES * 2);
        var qw = segment.get(ValueLayout.JAVA_FLOAT, offset + transform_rot_w_offset);

        return setTransform(matrix, px, py, pz, qx, qy, qz, qw);
    }

    static {
        if (b3WorldId.sizeof() != Integer.BYTES) {
            throw new RuntimeException("b3WorldId layout mismatch");
        }
        if (b3BodyId.sizeof() != Long.BYTES) {
            throw new RuntimeException("b3BodyId layout mismatch");
        }
        if (b3ShapeId.sizeof() != Long.BYTES) {
            throw new RuntimeException("b3ShapeId layout mismatch");
        }
        if (b3JointId.sizeof() != Long.BYTES) {
            throw new RuntimeException("b3JointId layout mismatch");
        }
    }

    //<editor-fold desc="Packed BodyID, ShapeID & JointID" default-state="collapsed">


    public static long packID(MemorySegment segment) {
        return packID(segment, 0);
    }
    public static long packID(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_LONG, offset);
    }

    public static void putPackedID(MemorySegment segment, long id) {
        segment.set(ValueLayout.JAVA_LONG, 0, id);
    }

    public static int getIndexFromPacked(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }
    public static int getWorldFromPacked(long packed) {
        return (int) ((packed >> 32) & 0xFFFFL);
    }
    public static int getGenerationFromPacked(long packed) {
        return (int) ((packed >> 48) & 0xFFFFL);
    }
    public static boolean isPackedIDNull(long packed) {
        return getIndexFromPacked(packed) == 0;
    }
    //</editor-fold>


    //<editor-fold desc="Packed WorldID" default-state="collapsed">


    public static long packWorldID(MemorySegment segment) {
        return packWorldID(segment, 0);
    }
    public static long packWorldID(MemorySegment segment, long offset) {
        return Integer.toUnsignedLong(segment.get(ValueLayout.JAVA_INT, offset));
    }
    public static void putPackedWorldID(MemorySegment segment, long worldID) {
        var asInt = (int) worldID;
        segment.set(ValueLayout.JAVA_INT, 0, asInt);
    }
    public static int getWorldIDIndexFromPackedID(long packedID) {
        packedID = packedID & 0xFFFFFFFFL;
        return (int) (packedID & 0xFFFFL);
    }
    public static int getWorldIDGenerationFromPackedID(long packedID) {
        packedID = packedID & 0xFFFFFFFFL;
        return (int) ((packedID >> 16) & 0xFFFFL);
    }
    public static boolean isPackedWorldIDNull(long packed) {
        return getWorldIDIndexFromPackedID(packed) == 0;
    }
    //</editor-fold>

}

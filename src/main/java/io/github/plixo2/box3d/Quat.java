package io.github.plixo2.box3d;

import org.box2d.box3d.b3Quat;
import org.box2d.box3d.b3Vec3;

import java.lang.foreign.MemorySegment;

public class Quat {

    public float x;
    public float y;
    public float z;

    public float w;

    public Quat(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    public Quat() {
        this(0, 0, 0, 1);
    }
    public Quat(Quat other) {
        this(other.x, other.y, other.z, other.w);
    }

    @Override
    public String toString() {
        return "((" + this.x + ", " + this.y + ", " + this.z + "), " + this.w + ")";
    }

    public Quat set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Quat set(Quat other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
        return this;
    }

    Quat set(MemorySegment segment) {
        this.w = b3Quat.s(segment);
        var vec = b3Quat.v(segment);
        this.x = b3Vec3.x(vec);
        this.y = b3Vec3.y(vec);
        this.z = b3Vec3.z(vec);
        return this;
    }

    public Vec3 rotateVector(Vec3 vec3, Vec3 dest) {
        var vx = vec3.x;
        var vy = vec3.y;
        var vz = vec3.z;

        var t1x = this.y * vz - this.z * vy;
        var t1y = this.z * vx - this.x * vz;
        var t1z = this.x * vy - this.y * vx;

        var t2x = t1x + this.w * vx;
        var t2y = t1y + this.w * vy;
        var t2z = t1z + this.w * vz;

        var t3x = this.y * t2z - this.z * t2y;
        var t3y = this.z * t2x - this.x * t2z;
        var t3z = this.x * t2y - this.y * t2x;

        dest.x = vx + 2.0f * t3x;
        dest.y = vy + 2.0f * t3y;
        dest.z = vz + 2.0f * t3z;
        return dest;
    }
    public Vec3 rotateVector(Vec3 vec3) {
        return rotateVector(vec3, vec3);
    }

    public Vec3 invRotateVector(Vec3 vec3, Vec3 dest) {
        var vx = vec3.x;
        var vy = vec3.y;
        var vz = vec3.z;

        var t1x = this.y * vz - this.z * vy;
        var t1y = this.z * vx - this.x * vz;
        var t1z = this.x * vy - this.y * vx;

        var t2x = t1x - this.w * vx;
        var t2y = t1y - this.w * vy;
        var t2z = t1z - this.w * vz;

        var t3x = this.y * t2z - this.z * t2y;
        var t3y = this.z * t2x - this.x * t2z;
        var t3z = this.x * t2y - this.y * t2x;

        dest.x = vx + 2.0f * t3x;
        dest.y = vy + 2.0f * t3y;
        dest.z = vz + 2.0f * t3z;
        return dest;
    }
    public Vec3 invRotateVector(Vec3 vec3) {
        return invRotateVector(vec3, vec3);
    }

    public Quat mul(Quat other, Quat dest) {
        var q1x = this.x;
        var q1y = this.y;
        var q1z = this.z;
        var q1w = this.w;

        var q2x = other.x;
        var q2y = other.y;
        var q2z = other.z;
        var q2w = other.w;

        var t1x = q1y * q2z - q1z * q2y;
        var t1y = q1z * q2x - q1x * q2z;
        var t1z = q1x * q2y - q1y * q2x;

        var t2x = t1x + q1w * q2x;
        var t2y = t1y + q1w * q2y;
        var t2z = t1z + q1w * q2z;

        dest.x = t2x + q2w * q1x;
        dest.y = t2y + q2w * q1y;
        dest.z = t2z + q2w * q1z;
        dest.w = q1w * q2w - q1x * q2x - q1y * q2y - q1z * q2z;
        return dest;
    }

    public Quat mul(Quat other) {
        return mul(other, this);
    }

    static void put(MemorySegment segment, float x, float y, float z, float w) {
        b3Quat.s(segment, w);
        var vec = b3Quat.v(segment);
        Vec3.put(vec, x, y, z);
    }
    void put(MemorySegment segment) {
        put(segment, this.x, this.y, this.z, this.w);
    }




}

package io.github.plixo2.box3d;

import org.box2d.box3d.b3Vec3;

import java.lang.foreign.MemorySegment;

public class Vec3 {
    public float x;
    public float y;
    public float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Vec3() {
        this(0, 0, 0);
    }
    public Vec3(Vec3 other) {
        this(other.x, other.y, other.z);
    }
    public Vec3(float value) {
        this(value, value, value);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public Vec3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }
    public Vec3 set(Vec3 other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        return this;
    }
    Vec3 set(MemorySegment segment) {
        this.x = b3Vec3.x(segment);
        this.y = b3Vec3.y(segment);
        this.z = b3Vec3.z(segment);
        return this;
    }

    public Vec3 add(Vec3 other, Vec3 dest) {
        dest.x = this.x + other.x;
        dest.y = this.y + other.y;
        dest.z = this.z + other.z;
        return dest;
    }
    public Vec3 add(Vec3 other) {
        return add(other, this);
    }
    public Vec3 sub(Vec3 other, Vec3 dest) {
        dest.x = this.x - other.x;
        dest.y = this.y - other.y;
        dest.z = this.z - other.z;
        return dest;
    }
    public Vec3 sub(Vec3 other) {
        return sub(other, this);
    }
    public Vec3 mul(float scalar, Vec3 dest) {
        dest.x = this.x * scalar;
        dest.y = this.y * scalar;
        dest.z = this.z * scalar;
        return dest;
    }
    public Vec3 mul(float scalar) {
        return mul(scalar, this);
    }
    public float dot(Vec3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }
    public Vec3 cross(Vec3 other, Vec3 dest) {
        dest.x = this.y * other.z - this.z * other.y;
        dest.y = this.z * other.x - this.x * other.z;
        dest.z = this.x * other.y - this.y * other.x;
        return dest;
    }
    public Vec3 cross(Vec3 other) {
        return cross(other, this);
    }
    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }
    public Vec3 normalize(Vec3 dest) {
        float lengthSquared = this.x * this.x + this.y * this.y + this.z * this.z;
        if (lengthSquared > 0.0f) {
            float invLength = 1.0f / (float) Math.sqrt(lengthSquared);
            dest.x = this.x * invLength;
            dest.y = this.y * invLength;
            dest.z = this.z * invLength;
        } else {
            dest.x = 0.0f;
            dest.y = 0.0f;
            dest.z = 0.0f;
        }
        return dest;
    }
    public Vec3 normalize() {
        return normalize(this);
    }
    public Vec3 perp(Vec3 dest) {
        if (this.x < -0.5f || 0.5f < this.x) {
            //noinspection SuspiciousNameCombination
            dest.x = this.y;
            dest.y = -this.x;
            dest.z = 0.0f;
        } else {
            dest.x = 0.0f;
            dest.y = this.z;
            dest.z = -this.y;
        }
        return dest.normalize();
    }
    public Vec3 perp() {
        return perp(this);
    }

    public Vec3 lerp(Vec3 other, float alpha, Vec3 dest) {
        assert alpha >= 0.0f && alpha <= 1.0f : "Alpha must be in the range [0, 1]";

        dest.x = (1.0f - alpha) * this.x + alpha * other.x;
        dest.y = (1.0f - alpha) * this.y + alpha * other.y;
        dest.z = (1.0f - alpha) * this.z + alpha * other.z;
        return dest;
    }
    public Vec3 lerp(Vec3 other, float alpha) {
        return lerp(other, alpha, this);
    }


    static void put(MemorySegment segment, float x, float y, float z) {
        b3Vec3.x(segment, x);
        b3Vec3.y(segment, y);
        b3Vec3.z(segment, z);
    }
    void put(MemorySegment segment) {
        put(segment, this.x, this.y, this.z);
    }






}

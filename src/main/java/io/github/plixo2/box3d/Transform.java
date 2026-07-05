package io.github.plixo2.box3d;

import org.box2d.box3d.b3Transform;

public class Transform {
    public Vec3 position;
    public Quat rotation;

    public Transform() {
        this.position = new Vec3();
        this.rotation = new Quat();
    }

    public Vec3 transformPoint(Vec3 point, Vec3 dest) {
        this.rotation.rotateVector(point, dest);
        return dest.add(this.position);
    }
    public Vec3 transformPoint(Vec3 point) {
        return transformPoint(point, point);
    }

    public Vec3 invTransformPoint(Vec3 point, Vec3 dest) {
        point.sub(this.position, dest);
        this.rotation.invRotateVector(dest, dest);
        return dest;
    }

    public Transform mul(Transform other, Transform dest) {
        var x = this.position.x;
        var y = this.position.y;
        var z = this.position.z;

        this.rotation.rotateVector(other.position, dest.position);
        dest.position.x += x;
        dest.position.y += y;
        dest.position.z += z;

        this.rotation.mul(other.rotation, dest.rotation);

        return dest;
    }

    public Transform mul(Transform other) {
        return mul(other, this);
    }



}

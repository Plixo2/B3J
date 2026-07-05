package io.github.plixo2.box3d;

public class AABB {
    private Vec3 lowerBound;
    private Vec3 upperBound;

    public AABB() {
        this.lowerBound = new Vec3();
        this.upperBound = new Vec3();
    }
}

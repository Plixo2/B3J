package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.B3JUtil;

public enum ShapeType {
    CAPSULE,
    COMPOUND,
    HEIGHT,
    HULL,
    MESH,
    SPHERE,

    ;

    public static ShapeType fromCode(int code) {
        return B3JUtil.enumValue(ShapeType.class, code);
    }

    public int code() {
        return this.ordinal();
    }

    public sealed interface Shape
            permits
                Capsule,
                CompoundData,
                HeightFieldData,
                HullData,
                Mesh,
                Sphere
    {
        default ShapeType type() {
            return switch (this) {
                case Capsule _ -> ShapeType.CAPSULE;
                case CompoundData _ -> ShapeType.COMPOUND;
                case HeightFieldData _ -> ShapeType.HEIGHT;
                case HullData _ -> ShapeType.HULL;
                case Mesh _ -> ShapeType.MESH;
                case Sphere _ -> ShapeType.SPHERE;
            };
        }

    }

}

package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.Internal;

public enum ShapeType {
    CAPSULE,
    COMPOUND,
    HEIGHT,
    HULL,
    MESH,
    SPHERE,

    ;

    public static ShapeType fromCode(int code) {
        return Internal.enumValue(ShapeType.class, code);
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

    }

}

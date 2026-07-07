package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Internal;

public enum BodyType {
    STATIC,
    KINEMATIC,
    DYNAMIC,

    ;

    public static BodyType fromCode(int code) {
        return Internal.enumValue(BodyType.class, code);
    }

    public int code() {
        return this.ordinal();
    }

}

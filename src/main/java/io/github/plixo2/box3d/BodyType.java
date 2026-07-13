package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.B3JUtil;

public enum BodyType {
    STATIC,
    KINEMATIC,
    DYNAMIC,

    ;

    public static BodyType fromCode(int code) {
        return B3JUtil.enumValue(BodyType.class, code);
    }

    public int code() {
        return this.ordinal();
    }

}

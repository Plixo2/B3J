package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.B3JUtil;

public enum DebugMaterial {
    DEFAULT,
    MATTE,
    SOFT,
    DEAD,
    GLOSSY,
    METALLIC,
    ;

    public static DebugMaterial fromColor(int color) {
        int index = (color >> 24) & 0xFF;
        return B3JUtil.enumValueOrElse(DebugMaterial.class, index, DEFAULT);
    }

    public int code() {
        return this.ordinal();
    }
}

package io.github.plixo2.box3d;

public enum BodyType {
    STATIC,
    KINEMATIC,
    DYNAMIC,

    ;

    public int code() {
        return this.ordinal();
    }

}

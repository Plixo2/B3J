package io.github.plixo2.box3d;

@FunctionalInterface
public interface MoverFilterFcn {

    boolean filter(ShapeID shapeID);

}

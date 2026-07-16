package io.github.plixo2.box3d;

@FunctionalInterface
public interface OverlapResultFcn {

    boolean onOverlap(
            ShapeID shapeID
    );

}

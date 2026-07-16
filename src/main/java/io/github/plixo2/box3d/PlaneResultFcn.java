package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.MemoryIterator;

@FunctionalInterface
public interface PlaneResultFcn {

    boolean onPlane(ShapeID shapeID, MemoryIterator<PlaneResult> planes);

}

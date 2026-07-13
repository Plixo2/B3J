package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.MemoryIterator;

public interface PlaneResultFcn {

    boolean onPlane(ShapeID shapeID, MemoryIterator<PlaneResult> planes);

}

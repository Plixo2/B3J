package io.github.plixo2.box3d;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public class PlaneResult {

    private Plane plane = new Plane();
    private Vector3f point = new Vector3f();

    public PlaneResult() {

    }

    public PlaneResult(PlaneResult other) {
        this.plane.set(other.plane);
        this.point.set(other.point);
    }

}

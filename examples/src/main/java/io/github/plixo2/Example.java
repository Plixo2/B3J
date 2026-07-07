package io.github.plixo2;

import io.github.plixo2.box3d.B3;
import io.github.plixo2.box3d.WorldID;
import io.github.plixo2.box3d.region.Region;
import org.joml.Vector3f;


public abstract class Example {
    public Region region = Region.ofConfined();

    public B3 b3 = B3.get();
    public WorldID worldID;

    public abstract void init(MeshFactory debugShapes);

    public abstract void update(float dt);

    public void onClick(Vector3f dir, Vector3f origin) {

    }

    public void onKeyPress(int key) {

    }

    void main() {
        try (var renderer = new Entry(this)) {
            renderer.loop();
        }
        this.region.close();
    }


}

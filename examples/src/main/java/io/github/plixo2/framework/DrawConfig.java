package io.github.plixo2.framework;

import io.github.plixo2.abstraction.Color;
import io.github.plixo2.box3d.DebugDraw;

import java.util.LinkedHashMap;
import java.util.Map;

public class DrawConfig {

    boolean shown = false;

    private final Map<Key, Boolean> map = new LinkedHashMap<>();

    void toggleShown() {
        this.shown = !this.shown;
    }

    private int pointer = 0;
    void moveUp() {
        if (!this.shown) {
            return;
        }
        this.pointer -= 1;
        if (this.pointer < 0) {
            this.pointer = this.map.size() - 1;
        }
    }
    void moveDown() {
        if (!this.shown) {
            return;
        }
        this.pointer += 1;
        if (this.pointer >= this.map.size()) {
            this.pointer = 0;
        }
    }
    void toggle() {
        if (!this.shown) {
            return;
        }
        var key = this.map.keySet().stream().toList().get(this.pointer);
        this.map.put(key, !get(key));
    }

    void render(TextRenderer.UI text, int height, int width) {

        var y = height - 38;



        text.putString("Config (ESC)", 10, y - 10, Color.WHITE);

        if (!this.shown) {
            return;
        }

        var i = 0;
        for (var stringBooleanEntry : this.map.entrySet()) {
            i += 1;
            var key = stringBooleanEntry.getKey();
            var value = stringBooleanEntry.getValue();
            var color = value ? Color.GREEN : Color.RED;
            text.putString(key + ": " + value, 70, y - 20 - 25 * i, color);
        }

        text.putString("  > ", 0, y - 20 - 25 * (this.pointer + 1), Color.WHITE);

    }

    public DrawConfig() {
        enable(Key.SHAPES);
    }

    void reset() {
        enable(Key.SHAPES);
    }

    public void enable(Key key) {
        this.map.put(key, true);
    }

    void update(DebugDraw debugDraw) {
        debugDraw.drawShapes = get(Key.SHAPES);
        debugDraw.drawJoints = get(Key.JOINTS);
        debugDraw.drawJointExtras = get(Key.JOINT_EXTRAS);
        debugDraw.drawAnchorA = get(Key.ANCHOR_A) ? 0 : 1;
        debugDraw.drawBodyNames = get(Key.BODY_NAMES);
        debugDraw.drawMass = get(Key.MASS);
        debugDraw.drawContacts = get(Key.CONTACTS);
        debugDraw.drawContactFeatures = get(Key.CONTACT_FEATURES);
        debugDraw.drawContactNormals = get(Key.CONTACT_NORMALS);
        debugDraw.drawContactForces = get(Key.CONTACT_FORCES);
        debugDraw.drawFrictionForces = get(Key.FRICTION_FORCES);
        debugDraw.drawGraphColors = get(Key.GRAPH_COLORS);
        debugDraw.drawBounds = get(Key.BOUNDS);
        debugDraw.drawIslands = get(Key.ISLANDS);
    }

    private boolean get(Key key) {
        return this.map.computeIfAbsent(key, _ -> false);
    }



    public enum Key {
        SHAPES,
        JOINTS,
        JOINT_EXTRAS,
        BOUNDS,
        MASS,
        BODY_NAMES,
        CONTACTS,
        ANCHOR_A,
        GRAPH_COLORS,
        CONTACT_FEATURES,
        CONTACT_NORMALS,
        CONTACT_FORCES,
        FRICTION_FORCES,
        ISLANDS,

        ;
    }

}

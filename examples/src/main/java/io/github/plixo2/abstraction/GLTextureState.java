package io.github.plixo2.abstraction;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class GLTextureState {

    private static final int MAX_ACTIVE_UNITS;
    private static final boolean TEXTURE_UNITS;
    private static final boolean DSA;


    // slots[target][unit] = textureId
    private static final int[][] target_slots;

    private static int activeTexture = -1;



    static {
        var capabilities = Capabilities.get();
        TEXTURE_UNITS = capabilities.textureUnits();
        DSA = capabilities.directStateAccess();

        MAX_ACTIVE_UNITS = capabilities.maxTotalTextureUnits();

        var targetCount = TextureType.values().length;
        target_slots = new int[targetCount][];
        for (int targetIndex = 0; targetIndex < target_slots.length; ++targetIndex) {
            var unitForTarget = new int[MAX_ACTIVE_UNITS];
            Arrays.fill(unitForTarget, -1);
            target_slots[targetIndex] = unitForTarget;
        }

    }

    public static void bind(TextureType type, int unit, int id) {
        bindUnit(type, unit, id);
    }

    private static void bindUnit(
            TextureType type,
            int unit,
            int id
    ) {
        if (unit < 0 || unit >= MAX_ACTIVE_UNITS) {
            throw new IllegalArgumentException("Texture unit " + unit + " is out of bounds (0-" + (MAX_ACTIVE_UNITS - 1) + ")");
        }
        var slots = target_slots[type.ordinal()];

        if (TEXTURE_UNITS) {
            var existing = slots[unit];
            if (existing != id) {
                glBindTextureUnit(unit, id);
                slots[unit] = id;
            }
            return;
        }

        setActiveTexture(unit);

        var state = slots[unit];
        if (state != id) {
            glBindTexture(type.target(), id);
            slots[unit] = id;
        }
    }


    /// When `directStateAccess` is disabled, but `textureUnits` is enabled,
    /// `glBindTextureUnit` does not know the target of the texture, so
    /// `glBindTexture` has to be called to set the internal target
    public static void bindNew(TextureType type, int id) {
        var unit = 0; // just use 0
        if (!DSA && TEXTURE_UNITS) {
            var slots = target_slots[type.ordinal()];
            setActiveTexture(unit);
            glBindTexture(type.target(), id);
            slots[unit] = id;
        } else {
            bind(type, unit, id); // bind for consistent behavior
        }
    }

    /// When a texture was deleted (by gc) and a new texture is created immediately after,
    /// the new texture will likely reuse the same id as the old texture, which is still stored in the slots.
    /// This method resets all slots that contain the id, so the texture can be bound again.
    ///
    /// This method should be called whenever a texture is deleted.
    public static void resetSlots(TextureType type, int identifier) {
        var slots = target_slots[type.ordinal()];
        for (int i = 0; i < slots.length; ++i) {
            if (slots[i] == identifier) {
                slots[i] = -1;
            }
        }
    }


    private static void setActiveTexture(int unit) {
        if (activeTexture != unit) {
            glActiveTexture(GL_TEXTURE0 + unit);
            activeTexture = unit;
        }
    }
}

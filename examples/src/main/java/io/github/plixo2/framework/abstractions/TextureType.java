package io.github.plixo2.framework.abstractions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_1D_ARRAY;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE_ARRAY;
import static org.lwjgl.opengl.GL40.GL_TEXTURE_CUBE_MAP_ARRAY;


@RequiredArgsConstructor
public enum TextureType {
    TEXTURE_1D(GL_TEXTURE_1D),
    TEXTURE_2D(GL_TEXTURE_2D),
    TEXTURE_3D(GL_TEXTURE_3D),
    TEXTURE_1D_ARRAY(GL_TEXTURE_1D_ARRAY),
    TEXTURE_2D_ARRAY(GL_TEXTURE_2D_ARRAY),
    TEXTURE_RECTANGLE(GL_TEXTURE_RECTANGLE),
    TEXTURE_CUBE_MAP(GL_TEXTURE_CUBE_MAP),
    TEXTURE_CUBE_MAP_ARRAY(GL_TEXTURE_CUBE_MAP_ARRAY),
    TEXTURE_BUFFER(GL_TEXTURE_BUFFER),
    TEXTURE_2D_MULTISAMPLE(GL_TEXTURE_2D_MULTISAMPLE),
    TEXTURE_2D_MULTISAMPLE_ARRAY(GL_TEXTURE_2D_MULTISAMPLE_ARRAY),
    ;

    @Getter
    @Accessors(fluent = true)
    private final int target;


    /// Does not check if it has a z dimension, but if can sample across the z plane.
    ///
    /// (Cube maps, Cube map arrays and 3D textures)
    ///
    /// Used for setting `GL_TEXTURE_WRAP_R`
    public boolean samplesAcrossZ() {
        return switch (this) {
            case TEXTURE_3D, TEXTURE_CUBE_MAP, TEXTURE_CUBE_MAP_ARRAY -> true;
            default -> false;
        };
    }

    public boolean isArray() {
        return switch (this) {
            case TEXTURE_1D_ARRAY, TEXTURE_2D_ARRAY, TEXTURE_CUBE_MAP_ARRAY, TEXTURE_2D_MULTISAMPLE_ARRAY -> true;
            default -> false;
        };
    }

    public boolean isMultisample() {
        return switch (this) {
            case TEXTURE_2D_MULTISAMPLE, TEXTURE_2D_MULTISAMPLE_ARRAY -> true;
            default -> false;
        };
    }

    /// Only supports `texelFetch` in the shader
    public boolean useOnlyTextureFetch() {
        return switch (this) {
            case TEXTURE_BUFFER, TEXTURE_2D_MULTISAMPLE, TEXTURE_2D_MULTISAMPLE_ARRAY -> true;
            default -> false;
        };
    }

    /// @apiNote TEXTURE_CUBE_MAP and TEXTURE_CUBE_MAP_ARRAY return 3 here.
    public int getTextureDimension() {
        return switch (this) {
            case TEXTURE_1D, TEXTURE_1D_ARRAY -> 1;
            case TEXTURE_2D,
                 TEXTURE_2D_ARRAY,
                 TEXTURE_RECTANGLE,
                 TEXTURE_2D_MULTISAMPLE,
                 TEXTURE_2D_MULTISAMPLE_ARRAY,
                 TEXTURE_BUFFER -> 2;
            case TEXTURE_3D,
                 TEXTURE_CUBE_MAP,
                 TEXTURE_CUBE_MAP_ARRAY -> 3;
        };
    }

    /// Dimension used for sampling in the shader.
    public int getSampleDimension() {
        var textureDimension = getTextureDimension();
        if (isArray()) {
            textureDimension += 1;
        }
        return textureDimension;
    }



}

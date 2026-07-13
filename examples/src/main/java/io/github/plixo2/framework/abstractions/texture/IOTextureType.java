package io.github.plixo2.framework.abstractions.texture;

import lombok.RequiredArgsConstructor;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL21.GL_SRGB8;
import static org.lwjgl.opengl.GL21C.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;

@RequiredArgsConstructor
public enum IOTextureType {
    RGBA(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, 4),
    RGB_NO_ALPHA(GL_RGB8, GL_RGB, GL_UNSIGNED_BYTE, 3),

    SRGBA(GL_SRGB8_ALPHA8, GL_RGBA, GL_UNSIGNED_BYTE, 4),
    SRGB_NO_ALPHA(GL_SRGB8, GL_RGB, GL_UNSIGNED_BYTE, 3),

    HDR_16F(GL_RGBA16F, GL_RGBA, GL_FLOAT, 4),
    HDR_32F(GL_RGBA32F, GL_RGBA, GL_FLOAT, 4),


    ;


    final int internalFormat;  // format of the texture on the GPU
    final int format;          // format of the pixel data in memory
    final int type;            // data type of the pixel data in memory
    final int channels;        // number of color channels in the data and the texture


    boolean hasAlpha() {
        return this.channels == 4;
    }

    boolean isHDR() {
        return this == HDR_16F || this == HDR_32F;
    }

    boolean useFloat() {
        return this == HDR_16F || this == HDR_32F;
    }
}

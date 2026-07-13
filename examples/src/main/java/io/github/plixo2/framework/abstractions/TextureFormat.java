package io.github.plixo2.framework.abstractions;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public record TextureFormat(int type, int internalFormat, int externalFormat) {
    public static TextureFormat RGBA_8I = new TextureFormat(GL_UNSIGNED_BYTE, GL_RGBA8, GL_RGBA);

    public static TextureFormat RGBA_8F = new TextureFormat(GL_FLOAT, GL_RGBA8, GL_RGBA);
    public static TextureFormat RGBA_16F = new TextureFormat(GL_FLOAT, GL_RGBA16F, GL_RGBA);
    public static TextureFormat RGBA_32F = new TextureFormat(GL_FLOAT, GL_RGBA32F, GL_RGBA);

    public static TextureFormat RGB_8F = new TextureFormat(GL_FLOAT, GL_RGB8, GL_RGB);
    public static TextureFormat RGB_16F = new TextureFormat(GL_FLOAT, GL_RGB16F, GL_RGB);
    public static TextureFormat RGB_11_11_10F = new TextureFormat(GL_FLOAT, GL_R11F_G11F_B10F, GL_RGB);
    public static TextureFormat RGB_32F = new TextureFormat(GL_FLOAT, GL_RGB32F, GL_RGB);

    public static TextureFormat DEPTH_32F = new TextureFormat(GL_FLOAT, GL_DEPTH_COMPONENT32F, GL_DEPTH_COMPONENT);


}

package io.github.plixo2.abstraction.texture;


import io.github.plixo2.abstraction.TextureFormat;
import io.github.plixo2.abstraction.TextureType;
import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.system.MemoryUtil.NULL;

public class CubeMapTexture extends Texture {

    public CubeMapTexture(int width, int height) {
        super(TextureType.TEXTURE_CUBE_MAP, width, height);
    }

    public CubeMapTexture(
            int width,
            int height,
            Filter filter,
            Wrap wrap,
            TextureFormat format
    ) {
        this(width, height);
        this.bindTextureUnit(0);
        this.setStorage(
                1,
                format.internalFormat(),
                format.externalFormat(),
                format.type()
        );
        this.setWrap(wrap);
        this.setFilter(filter);
    }

    @Override
    public void setStorage(
            int levels,
            int internalFormat,
            int format,
            int type
    ) {
        ensureNotBindless();
        if (DSA) {
            // Will allocate all 6 faces, identical `to Texture2D`
            GL45.glTextureStorage2D(this.id(), levels, internalFormat, this.width, this.height);
        } else {
            this.bindTextureUnit(0);
            for (int i = 0; i < levels; i++) {
                var w = this.width >> i;
                var h = this.height >> i;
                if (w == 0 || h == 0) {
                    break;
                }
                for (int face = 0; face < 6; face++) {
                    int faceTarget = GL_TEXTURE_CUBE_MAP_POSITIVE_X + face;
                    glTexImage2D(faceTarget, i, internalFormat, w, h, 0, format, type, NULL);
                }
            }
        }
    }
}

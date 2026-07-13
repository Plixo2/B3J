package io.github.plixo2.framework.abstractions.texture;

import io.github.plixo2.framework.abstractions.Capabilities;
import io.github.plixo2.framework.abstractions.Color;
import io.github.plixo2.framework.abstractions.HDRColor;
import io.github.plixo2.framework.abstractions.TextureType;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.system.MemoryUtil.NULL;


public non-sealed class Texture2D extends Texture implements DisplayableTexture2D {

    public Texture2D(int width, int height) {
        super(TextureType.TEXTURE_2D, width, height);
    }

    public static Texture2D createAttachmentDepth(int width, int height, int format, Filter filter, Wrap wrap) {
        return createAttachment(width, height, GL_DEPTH_COMPONENT, format, GL_FLOAT, filter, wrap);
    }

    public static Texture2D createAttachment(int width, int height, int exFormat, int internalFormat, int type, Filter filter, Wrap wrap) {
        val texture = new Texture2D(width, height);
        texture.bindTextureUnit(0);
        texture.setFilter(filter);
        texture.setWrap(wrap);
        texture.setStorage(1, internalFormat, exFormat, type);

        return texture;
    }


    public static Texture2D fromFile(@NotNull String path) throws IOException {
        return fromBufferedImg(ImageIO.read(new File(path)), true);
    }
    public static Texture2D fromResource(@NotNull String path) throws IOException {
        var read = ImageIO.read(Objects.requireNonNull(Texture2D.class.getResource(path)));
        return fromBufferedImg(read, true);
    }

    private static Texture2D fromBuffer(@NotNull ByteBuffer image, int width, int height) {
        var texture = new Texture2D(width, height);
        texture.bindTextureUnit(0);
        texture.setFilter(Filter.LINEAR);
        texture.setWrap(Wrap.CLAMP_TO_EDGE);
        texture.setStorage(1, GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE);
        texture.setData(0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, image);
        texture.generateMipmaps();
        return texture;
    }

    public static Texture2D fromBufferedImg(
            BufferedImage image,
            boolean withAlpha
    ) {
        var width = image.getWidth();
        var height = image.getHeight();
        return fromBuffer(getImageData(image, withAlpha), width, height);
    }
    public static ByteBuffer getImageData(
            BufferedImage image,
            boolean withAlpha
    ) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        int channels = withAlpha ? 4 : 3;

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
        // flip vertically while packing
        for (int y = 0; y < height; y++) {
            int row = (height - 1 - y) * width;
            for (int x = 0; x < width; x++) {
                int argb = pixels[row + x];
                buffer.put((byte) ((argb >> 16) & 0xFF)); // R
                buffer.put((byte) ((argb >> 8) & 0xFF));  // G
                buffer.put((byte) (argb & 0xFF));         // B
                if (withAlpha) {
                    buffer.put((byte) ((argb >> 24) & 0xFF)); // A
                }
            }
        }
        buffer.flip();
        return buffer;
    }

    public static Texture2D fromSolidColor(
            int width,
            int height,
            Color color
    ) {
        var buffer = dataFromSolidColor(width, height, color, true);
        return fromBuffer(buffer, width, height);
    }


    public static Texture2D fromSolidColorFloat(
            int width,
            int height,
            HDRColor color
    ) {
        var buffer = dataFromSolidColorFloat(width, height, color, true);
        var texture = new Texture2D(width, height);
        texture.bindTextureUnit(0);
        texture.setFilter(Filter.LINEAR);
        texture.setWrap(Wrap.CLAMP_TO_EDGE);
        texture.setStorage(1, GL_RGBA32F, GL_RGBA, GL_FLOAT);
        texture.setData(0, 0, 0, width, height, GL_RGBA, GL_FLOAT, buffer);
        texture.generateMipmaps();
        return texture;
    }

    public static ByteBuffer dataFromSolidColor(
            int width,
            int height,
            Color color,
            boolean withAlpha
    ) {
        var channels = withAlpha ? 4 : 3;

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
        for (var i = 0; i < width * height; i++) {
            buffer.put((byte) color.red());
            buffer.put((byte) color.green());
            buffer.put((byte) color.blue());
            if (withAlpha) {
                buffer.put((byte) color.alpha());
            }
        }

        buffer.flip();
        return buffer;
    }


    public static FloatBuffer dataFromSolidColorFloat(
            int width,
            int height,
            HDRColor color,
            boolean withAlpha
    ) {
        var channels = withAlpha ? 4 : 3;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(width * height * channels);

        for (var i = 0; i < width * height; i++) {
            buffer.put(color.red());
            buffer.put(color.green());
            buffer.put(color.blue());
            if (withAlpha) {
                buffer.put(color.alpha());
            }
        }

        buffer.flip();
        return buffer;
    }

    public void setData(
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            int type,
            ByteBuffer data
    ) {
        if (DSA) {
            GL45.glTextureSubImage2D(this.id(), level, xOffset, yOffset, width, height, format, type, data);
        } else {
            this.bindTextureUnit(0);
            GL11.glTexSubImage2D(this.type.target(), level, xOffset, yOffset, width, height, format, type, data);
        }
    }

    public void setData(
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            int type,
            FloatBuffer data
    ) {
        if (DSA) {
            GL45.glTextureSubImage2D(this.id(), level, xOffset, yOffset, width, height, format, type, data);
        } else {
            this.bindTextureUnit(0);
            GL11.glTexSubImage2D(this.type.target(), level, xOffset, yOffset, width, height, format, type, data);
        }
    }

    public void setData(
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            int type,
            int[] data
    ) {
        if (DSA) {
            GL45.glTextureSubImage2D(this.id(), level, xOffset, yOffset, width, height, format, type, data);
        } else {
            this.bindTextureUnit(0);
            GL11.glTexSubImage2D(this.type.target(), level, xOffset, yOffset, width, height, format, type, data);
        }
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
            GL45.glTextureStorage2D(this.id(), levels, internalFormat, this.width, this.height);
        } else {
            this.bindTextureUnit(0);
            for (int i = 0; i < levels; i++) {
                var w = this.width >> i;
                var h = this.height >> i;
                if (w == 0 || h == 0) {
                    break;
                }
                GL11.glTexImage2D(this.type.target(), i, internalFormat, w, h, 0, format, type, NULL);
            }

        }
    }
    public void clearTexture() {
        if (Capabilities.get().clearTexture()) {
            GL44.glClearTexImage(this.id(), 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        } else {
            this.bindTextureUnit(0);
            ByteBuffer zeroData = BufferUtils.createByteBuffer(this.width * this.height * 4);
            GL11.glTexSubImage2D(this.type.target(), 0, 0, 0, this.width, this.height, GL_RGBA, GL_UNSIGNED_BYTE, zeroData);
        }

    }

    @Override
    public Texture2D get() {
        return this;
    }
}

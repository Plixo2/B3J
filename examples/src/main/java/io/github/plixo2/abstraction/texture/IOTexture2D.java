package io.github.plixo2.abstraction.texture;

import io.github.plixo2.abstraction.Color;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBImage;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;

/**
 * Lazy Loaded Texture, either from file or from memory.
 * This class is used to load textures in a separate thread to avoid blocking the main thread.
 * It uses the STBImage library to load images from memory into a ByteBuffer.
 * Then upload the in memory texture to the GPU when it is needed on the main thread.
 */
public final class IOTexture2D implements DisplayableTexture2D {

    private final static Texture2D[] defaultMissingTextures;

    static {
        STBImage.stbi_set_flip_vertically_on_load(true);
        defaultMissingTextures = new Texture2D[IOTextureType.values().length];
    }

    private final IOTextureType type;

    /// This is set only once by the loading thread.
    /// It is volatile to ensure visibility across threads.
    /// When this is set, the texture can be created on the main thread
    /// and this field can be set to null to free the memory.
    private volatile TextureMemory data;

    private @Nullable Texture2D loaded;

    public IOTexture2D(
            ImageSource source,
            Settings settings
    ) {
        source.start(settings, data -> this.data = data);
        this.type = settings.type;
    }


    public IOTexture2D(
        Texture2D texture
    ) {
        this.loaded = Objects.requireNonNull(texture);
        this.type = IOTextureType.RGBA; // does not matter
    }

    @Override
    public Texture2D get() {
        if (this.loaded != null) {
            return this.loaded;
        } else {
            var data = this.data;
            if (data != null) {
                var texture = data.create();
                if (texture != null) {
                    this.loaded = texture;
                    this.data = null; // free memory
                    return texture;
                }
            }
        }
        return getDefaultMissingTexture(this.type);
    }

    public boolean isLoaded() {
        return this.loaded != null;
    }

    public void free() {
        var loaded = this.loaded;
        if (loaded != null) {
            loaded.free();
            this.loaded = null;
        }
    }

    public interface TextureMemory {
        @Nullable Texture2D create();
    }


    private static Texture2D getDefaultMissingTexture(IOTextureType type) {
        var index = type.ordinal();
        var existing = defaultMissingTextures[index];
        if (existing != null) {
            return existing;
        }
        var missingTexture = createDefaultMissingTexture(type, Color.MAGENTA);
        defaultMissingTextures[index] = missingTexture;

        return missingTexture;
    }

    private static Texture2D createDefaultMissingTexture(
            IOTextureType textureType,
            Color color
    ) {
        var width = 4;
        var height = 4;

        var texture = new Texture2D(width, height);
        texture.setFilter(Texture.Filter.LINEAR);
        texture.setWrap(Texture.Wrap.CLAMP_TO_EDGE);
        texture.setStorage(1, textureType.internalFormat, textureType.format, textureType.type);

        if (textureType.useFloat()) {
            var data = Texture2D.dataFromSolidColorFloat(width, height, color.unclamped(), textureType.hasAlpha());
            texture.setData(0, 0, 0, width, height, textureType.format, textureType.type, data);
        } else {
            var data = Texture2D.dataFromSolidColor(width, height, color, textureType.hasAlpha());
            texture.setData(0, 0, 0, width, height, textureType.format, textureType.type, data);
        }
        return texture;
    }


    public static Settings settings(IOTextureType type) {
        return new Settings(type);
    }

    public static class Settings {
        private int mipLevels = 1;
        IOTextureType type;
        private Texture2D.Filter filter = Texture2D.Filter.LINEAR;
        private Texture2D.Wrap wrap = Texture2D.Wrap.CLAMP_TO_EDGE;
        private boolean generateMipmaps = true;

        private Settings(IOTextureType type) {
            this.type = type;
        }
        public Settings setFilter(Texture2D.Filter filter) {
            this.filter = filter;
            return this;
        }
        public Settings setWrap(Texture2D.Wrap wrap) {
            this.wrap = wrap;
            return this;
        }
        public Settings setMipLevels(int mipLevels) {
            if (mipLevels < 1) {
                throw new IllegalArgumentException("Mip levels must be at least 1");
            }
            this.mipLevels = mipLevels;
            return this;
        }
        public Settings disableMipmapGeneration() {
            this.generateMipmaps = false ;
            return this;
        }


        public Texture2D create(
                int width,
                int height
        ) {
            var texture = new Texture2D(
                    width,
                    height
            );
            texture.setFilter(this.filter);
            texture.setWrap(this.wrap);
            var type = this.type;

            texture.setStorage(
                    this.mipLevels,
                    type.internalFormat,
                    type.format,
                    type.type
            );
            return texture;
        }
    }




    @RequiredArgsConstructor
    static class ByteTextureMemory implements TextureMemory {
        private final Settings settings;
        private final int width;
        private final int height;
        private final boolean free_stbi;
        private final Buffer data;

        @Override
        public @Nullable Texture2D create() {
            try {
                var texture = this.settings.create(
                        this.width,
                        this.height
                );
                if (this.data instanceof ByteBuffer buffer) {
                    texture.setData(
                            0,
                            0,
                            0,
                            this.width,
                            this.height,
                            this.settings.type.format,
                            this.settings.type.type,
                            buffer
                    );
                } else if (this.data instanceof FloatBuffer buffer) {
                    texture.setData(
                            0,
                            0,
                            0,
                            this.width,
                            this.height,
                            this.settings.type.format,
                            this.settings.type.type,
                            buffer
                    );
                } else {
                    throw new IllegalStateException("Unsupported buffer type: " + this.data.getClass());
                }

                if (this.settings.mipLevels > 1 && this.settings.generateMipmaps) {
                    texture.generateMipmaps();
                }
                return texture;
            } finally {
                if (this.free_stbi) {
                    if (this.data instanceof ByteBuffer buffer) {
                        STBImage.stbi_image_free(buffer);
                    } else if (this.data instanceof FloatBuffer buffer) {
                        STBImage.stbi_image_free(buffer);
                    } else if (this.data instanceof ShortBuffer buffer) {
                        STBImage.stbi_image_free(buffer);
                    }
                }
            }
        }

    }

}

package io.github.plixo2.abstraction.texture;


import io.github.plixo2.abstraction.ByteBufferUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBImage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageSource {
    private final @Nullable StackTraceElement[] creationStack;
    private final SourceType type;


    public static ImageSource file(Path path) {
        return new ImageSource(
                Thread.currentThread().getStackTrace(),
                new SourceType.File(path)
        );
    }

    public static ImageSource resource(String resourcePath) {
        return new ImageSource(
                Thread.currentThread().getStackTrace(),
                new SourceType.Resource(resourcePath)
        );
    }

    public static ImageSource bufferedImage(BufferedImage image) {
        return new ImageSource(
                Thread.currentThread().getStackTrace(),
                new SourceType.BufferedAWTImage(image)
        );
    }

    public static ImageSource async(Supplier<ImageSource> getter) {
        return new ImageSource(
                Thread.currentThread().getStackTrace(),
                new SourceType.Async(getter)
        );
    }

    public static ImageSource existing(Texture2D texture2D) {
        return new ImageSource(
                Thread.currentThread().getStackTrace(),
                new SourceType.Existing(texture2D)
        );
    }

    void start(
            IOTexture2D.Settings settings,
            Consumer<IOTexture2D.TextureMemory> setter
    ) {
        if (this.type instanceof SourceType.Existing(var tex)) {
            setter.accept(() -> tex);
        } else {
            Thread.startVirtualThread(() -> asyncLoading(settings, setter));
        }
    }

    private void asyncLoading(
            IOTexture2D.Settings settings,
            Consumer<IOTexture2D.TextureMemory> setter
    ) {
        try {
            switch (this.type) {
                case SourceType.Async(var getter) -> {
                    getter.get().start(settings, setter);
                }
                case SourceType.BufferedAWTImage(var img) -> {
                    var mem = bufferedImage(settings, img);
                    setter.accept(mem);
                }
                case SourceType.File(var path) -> {
                    var buffer = getBufferFromFile(path);
                    var mem = stb(path.toString(), buffer, settings);
                    setter.accept(mem);
                }
                case SourceType.Resource(var resource) -> {
                    var buffer = getBufferFromResource(resource);
                    var mem = stb(resource, buffer, settings);
                    setter.accept(mem);
                }
                case SourceType.Existing ignored -> {
                    throw new IllegalStateException(
                            "Existing texture should have been handled in the main thread"
                    );
                }
            }
        } catch(Exception e) {
            if (this.creationStack != null) {
                var creationException = new Exception("Creation stack trace (image source created here)");
                creationException.setStackTrace(this.creationStack);
                e.addSuppressed(creationException);
            }
            throw e;
        }
    }

    @SneakyThrows
    private static ByteBuffer getBufferFromResource(String resourcePath) {
        return ByteBufferUtils.ioResourceToByteBuffer(resourcePath, 2048);
    }
    @SneakyThrows
    private static ByteBuffer getBufferFromFile(Path path) {
        return ByteBufferUtils.inputSteamToByteBuffer(path);
    }

    private static IOTexture2D.ByteTextureMemory bufferedImage(
            IOTexture2D.Settings settings,
            BufferedImage image
    ) {
        var data = Texture2D.getImageData(image, settings.type.hasAlpha());
        int width = image.getWidth();
        int height = image.getHeight();
        return new IOTexture2D.ByteTextureMemory(settings, width, height, false, data);
    }


    @SneakyThrows
    private static IOTexture2D.ByteTextureMemory stb(
            String source,
            ByteBuffer buffer,
            IOTexture2D.Settings settings
    ) {
        var width = new int[1];
        var height = new int[1];
        var components = new int[1];

        var expectedHDR = settings.type.isHDR();
        var actualHDR = STBImage.stbi_is_hdr_from_memory(buffer);
        if (expectedHDR != actualHDR) {
            var expectedStr = expectedHDR ? "HDR" : "SDR";
            var actualStr = actualHDR ? "HDR" : "SDR";
            throw new IOException(
                    source + ": Image format mismatch: expected "
                    + expectedStr
                    + " but got "
                    + actualStr
            );
        }

        Buffer data;
        if (expectedHDR) {
            FloatBuffer fB = STBImage.stbi_loadf_from_memory(
                buffer,
                width,
                height,
                components,
                settings.type.channels
            );
            data = fB;
        } else {
            ByteBuffer bB = STBImage.stbi_load_from_memory(
                    buffer,
                    width,
                    height,
                    components,
                    settings.type.channels
            );
            data = bB;
        }

        if (data == null) {
            throw new RuntimeException(source + ": Failed to load image: " + stbi_failure_reason());
        }

        return new IOTexture2D.ByteTextureMemory(
                settings,
                width[0],
                height[0],
                true,
                data
        );
    }




    private sealed interface SourceType {
        // just return the existing texture immediately
        record Existing(Texture2D texture2D) implements SourceType {}

        record Resource(String resourcePath) implements SourceType {}
        record BufferedAWTImage(BufferedImage image) implements SourceType {}
        record File(Path path) implements SourceType {}
        record Async(Supplier<ImageSource> getter) implements SourceType {}
    }
}

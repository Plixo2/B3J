package io.github.plixo2.framework;

import io.github.plixo2.abstraction.MemorySide;
import io.github.plixo2.abstraction.Mesh;
import io.github.plixo2.abstraction.Shader;
import io.github.plixo2.abstraction.ShaderBuffer;
import io.github.plixo2.abstraction.texture.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class TextAtlas {

    private final DisplayableTexture2D texture;
    private final Metric[] metrics;

    public TextAtlas() {
        var metrics = new Metric[256];
        for (var i = 0; i < 256; i++) {
            metrics[i] = new Metric(0, 0, new Vector4f());
        }

        DisplayableTexture2D texture = new IOTexture2D(
                ImageSource.resource("font/atlas.png"),
                IOTexture2D.settings(IOTextureType.RGBA)
                    .setFilter(Texture.Filter.LINEAR)
                    .setWrap(Texture.Wrap.CLAMP_TO_EDGE)
        );

        Thread.startVirtualThread(() -> {
            try {
                var metricsBin = TextAtlas.class.getResourceAsStream("/font/metrics.bin");
                if (metricsBin == null) {
                    throw new IOException("Metrics file not found");
                }

                try (var stream = new DataInputStream(metricsBin)) {
                    for (var i = 0; i < metrics.length; i++) {
                        metrics[i] = new Metric(
                                stream.readFloat(),
                                stream.readFloat(),
                                new Vector4f(
                                        stream.readFloat(),
                                        stream.readFloat(),
                                        stream.readFloat(),
                                        stream.readFloat()
                                )
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load font atlas texture: " + e.getMessage());
            }
        });


        this.texture = texture;
        this.metrics = metrics;
    }

    public static void write() {
        var metrics = atlas();

        try (var out = new DataOutputStream(Files.newOutputStream(Path.of("metrics.bin")))) {
            for (var metric : metrics) {
                out.writeFloat(metric.width);
                out.writeFloat(metric.height);
                out.writeFloat(metric.uv.x());
                out.writeFloat(metric.uv.y());
                out.writeFloat(metric.uv.z());
                out.writeFloat(metric.uv.w());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Metric[] atlas() {
        int size = 512;
        Metric[] metrics;
        do {
            metrics = tryBuildAtlas(size);
            size *= 2;
        } while (metrics == null);
        return metrics;
    }

    private static Metric[] tryBuildAtlas(int size) {
        var font = new Font("JetBrains Mono SemiBold", Font.PLAIN, 28);
        var color = java.awt.Color.WHITE;
        var padding = 4;
        var bg = new java.awt.Color(255, 255, 255, 0);


        var image = image(size);
        var gfx = graphics(image, font, bg, color);

        try {
            var fm = gfx.getFontMetrics();

            var glyphHeight = fm.getHeight();
            var glyphAscent = fm.getAscent();

            int positionX = 0;
            int positionY = 0;

            var metrics = new Metric[256];

            for (int i = 0; i < 256; i++) {
                char ch = (char) i;

                var glyphWidth = Math.max(fm.charWidth(ch), 1);
                var canDisplay = ch != '\t' && canDisplayLatin1(ch);
                if (!canDisplay) {
                    glyphWidth = Math.max(fm.charWidth(' '), 1);
                }

                if (positionX + glyphWidth >= size) {
                    positionX = 0;
                    positionY += glyphHeight + padding;

                    if (positionY + glyphHeight >= size) {
                        return null;
                    }
                }

                if (canDisplay) {
                    gfx.drawString(String.valueOf(ch), positionX, positionY + glyphAscent);
                }

                metrics[i] = Metric.create(positionX, positionY, glyphWidth, glyphHeight, size);
                positionX += glyphWidth + padding;
            }


            ImageIO.write(image, "png", new java.io.File("atlas.png"));

            return metrics;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            dispose(gfx, image);
        }

    }


    public record Metric(
            float width,
            float height,
            Vector4f uv
    ) {
        static Metric create(
                int x,
                int y,
                float width,
                float height,
                float textureSize
        ) {
            var uv = new Vector4f(
                    x / textureSize,
                    y / textureSize,
                    (x + width) / textureSize,
                    (y + height) / textureSize
            );
            return new Metric(width, height, uv);
        }
    }

    private static boolean canDisplayLatin1(char c) {
        return c >= 32 && c <= 255 && c != 127 && c != 129 && c != 141 && c != 143 && c != 144 && c != 157 &&
                c != 160 && c != 173;
    }
    private static void dispose(Graphics2D g, BufferedImage image) {
        g.dispose();
        image.flush();
    }
    private static BufferedImage image(int size) {
        return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    }
    private static Graphics2D graphics(
            BufferedImage image,
            @Nullable Font font,
            java.awt.Color backgroundFill,
            java.awt.Color color
    ) {
        Graphics2D gfx = image.createGraphics();
        setRenderingHints(gfx, font);
        gfx.setColor(backgroundFill);
        gfx.fillRect(0, 0, image.getWidth(), image.getHeight());
        gfx.setColor(color);
        return gfx;
    }

    private static void setRenderingHints(
            Graphics2D g,
            Font font
    ) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(font);
    }


}

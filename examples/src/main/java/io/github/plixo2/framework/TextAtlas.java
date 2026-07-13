package io.github.plixo2.framework;

import io.github.plixo2.framework.abstractions.texture.*;
import lombok.Getter;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.*;
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

        // will load the atlas async by default
        DisplayableTexture2D texture = new IOTexture2D(
                ImageSource.resource("font/atlas.png"),
                IOTexture2D.settings(IOTextureType.RGBA)
                    .disableMipmapGeneration()
                    .setFilter(Texture.Filter.LINEAR)
                    .setWrap(Texture.Wrap.CLAMP_TO_EDGE)
        );

        // init empty
        for (var i = 0; i < 256; i++) {
            metrics[i] = new Metric(0, 0, new Vector4f());
        }

        // fill metrics async
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
                System.err.println("Failed to load font metrics: " + e.getMessage());
            }
        });


        this.texture = texture;
        this.metrics = metrics;
    }


    private record Atlas(
            BufferedImage img,
            Graphics2D gfx,
            Metric[] metrics,
            boolean success
    ) implements AutoCloseable {
        @Override
        public void close() {
            this.gfx.dispose();
            this.img.flush();
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

    // generator for atlas & metrics
    public static class Writer {

        void main() {
            var font = new Font("JetBrains Mono SemiBold", Font.PLAIN, 28);
            var dir = Path.of("fontGen/");
            var metricsPath = dir.resolve("metrics.bin");
            var atlasPath = dir.resolve("atlas.png");

            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try (
                    var atlas = atlas(font);
                    var out = new DataOutputStream(Files.newOutputStream(metricsPath))
            ) {
                ImageIO.write(atlas.img, "png", atlasPath.toFile());

                for (var metric : atlas.metrics) {
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

            try {
                Desktop.getDesktop().open(dir.toFile());
            } catch (Exception e) {
                // ignore
            }

        }

        private static Atlas atlas(Font font) {
            int size = 128;

            while (true) {

                var atlas = tryBuildAtlas(font, size);
                if (atlas.success) {
                    return atlas;
                }
                atlas.close();
                size *= 2;

            }
        }

        private static Atlas tryBuildAtlas(Font font, int size) {

            var color = java.awt.Color.WHITE;
            var padding = 4;
            var bg = new java.awt.Color(255, 255, 255, 0);

            var image = image(size);
            var gfx = graphics(image, font, bg, color);

            var fm = gfx.getFontMetrics();
            var glyphHeight = fm.getHeight();
            var glyphAscent = fm.getAscent();

            int positionX = 0;
            int positionY = 0;

            var metrics = new Metric[256];
            var success = true;

            for (int i = 0; i < 256; i++) {
                char ch = (char) i;

                var glyphWidth = Math.max(fm.charWidth(ch), 1);
                var canDisplay = canDisplayLatin1(ch);
                if (!canDisplay) {
                    glyphWidth = Math.max(fm.charWidth(' '), 1);
                }

                if (positionX + glyphWidth >= size) {
                    positionX = 0;
                    positionY += glyphHeight + padding;

                    if (positionY + glyphHeight >= size) {
                        success = false;
                        break;
                    }
                }

                if (canDisplay) {
                    gfx.drawString(String.valueOf(ch), positionX, positionY + glyphAscent);
                }

                metrics[i] = Metric.create(positionX, positionY, glyphWidth, glyphHeight, size);
                positionX += glyphWidth + padding;
            }

            return new Atlas(image, gfx, metrics, success);
        }

        private static boolean canDisplayLatin1(char c) {
            var inAscii = c >= 32 && c <= 126;
            var inLatin1 = c >= 161 && c <= 255;
            var softHyphen = c == 173;
            return (inAscii || inLatin1) && !softHyphen;
        }

        private static BufferedImage image(int size) {
            return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }

        private static Graphics2D graphics(
                BufferedImage image,
                Font font,
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

}

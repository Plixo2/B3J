package io.github.plixo2.framework.abstractions;


import io.github.plixo2.framework.abstractions.texture.Texture2D;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.lwjgl.opengl.GL20.glDrawBuffers;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RenderTarget {


    @Getter
    @Accessors(fluent = true)
    private final @NotNull Framebuffer framebuffer;

    private final @NotNull Map<Integer, RenderAttachment> attachments;

    public static RenderTarget generate(int width, int height) {
        var framebuffer = Framebuffer.generate(width, height);
        return new RenderTarget(framebuffer, new HashMap<>());
    }

    public static RenderTarget generate() {
        var framebuffer = Framebuffer.generate();
        return new RenderTarget(framebuffer, new HashMap<>());
    }


    public void bind(@Nullable Color color) {
        this.framebuffer.bind();
        if (color != null) {
            this.framebuffer.clear(color);
        }
    }

    public void unbind() {
        Framebuffer.unbind();
    }

    public Texture2D new_texture(int target, int format, int type, int exFormat, Texture2D.Filter filter, Texture2D.Wrap wrap) {
        framebuffer.bind();
        val attachment =
                Texture2D.createAttachment(framebuffer.width(), framebuffer.height(), exFormat, format, type, filter, wrap);
        attach_texture(attachment, target);
        Framebuffer.unbind();
        return attachment;
    }

    public RenderBuffer new_buffer(int attachment, int storage) {
        framebuffer.bind();
        val attachment_ = RenderBuffer.generate(framebuffer.width(), framebuffer.height());
        attachment_.bind();
        attachment_.store(storage);
        attach_buffer(attachment_, attachment);
        attachment_.unbind();
        Framebuffer.unbind();
        return attachment_;
    }

    public void attach_buffer(@NotNull RenderBuffer buffer, int attachment) {
        framebuffer.attach_buffer(buffer, attachment);
        attachments.put(attachment, new RenderAttachment.Buffer(buffer));
    }

    public void attach_texture(@NotNull Texture2D texture, int target) {
        framebuffer.attach_texture(texture, target);
        attachments.put(target, new RenderAttachment.Textured(texture));
    }

    public @NotNull RenderAttachment get(int target) {
        return Objects.requireNonNull(this.attachments.get(target));
    }

    public void assertState() {
        framebuffer.assertState();
    }

    public void setBufferUsage(int[] attachments) {
        framebuffer.bind();
        glDrawBuffers(attachments);
    }

    public int width() {
        return framebuffer.width();
    }

    public int height() {
        return framebuffer.height();
    }

    public List<Texture2D> getAttachedTextures() {
        var list = new ArrayList<Texture2D>();
        for (var attachment : this.attachments.values()) {
            if (attachment instanceof RenderAttachment.Textured(var texture)) {
                list.add(texture);
            }
        }
        return list;
    }

    public sealed interface RenderAttachment {

        record Buffer(RenderBuffer buffer) implements RenderAttachment {}
        record Textured(Texture2D texture) implements RenderAttachment {}

        default boolean isTexture() {
            return this instanceof Textured;
        }

        default boolean isBuffer() {
            return this instanceof Buffer;
        }

        default Texture2D asTexture() {
            if (this instanceof Textured(var texture)) {
                return texture;
            }
            throw new IllegalStateException("Attachment is not a texture");
        }
        default RenderBuffer asBuffer() {
            if (this instanceof Buffer(var buffer)) {
                return buffer;
            }
            throw new IllegalStateException("Attachment is not a buffer");
        }

    }
}

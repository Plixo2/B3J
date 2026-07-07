package io.github.plixo2.abstraction;

import io.github.plixo2.abstraction.texture.Texture2D;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30C.*;


public class Framebuffer extends GPUResource {

    @Getter
    @Accessors(fluent = true)
    private final int width;

    @Getter
    @Accessors(fluent = true)
    private final int height;

    private final FramebufferHandleData handleData;

    private final boolean ignoreSize;

    private Framebuffer(int id, int width, int height, boolean ignoreSize) {
        this.handleData = new FramebufferHandleData(id);
        this.width = width;
        this.height = height;
        this.ignoreSize = ignoreSize;
        this.remover = GLResourceManagement.add(this, this.handleData);
    }


    public static Framebuffer generate(int width, int height) {
        return new Framebuffer(glGenFramebuffers(), width, height, false);
    }

    public static Framebuffer generate() {
        return new Framebuffer(glGenFramebuffers(), 0, 0, true);
    }

    public void attach_texture(@NotNull Texture2D texture, int target) {
        assert texture.width() == this.width && texture.height() == this.height || this.ignoreSize;
        bind();
        if (Texture2D.DSA) {
            GL45.glNamedFramebufferTexture(this.handleData.id, target, texture.getTemporaryIdentifier(), 0);
        } else {
            glFramebufferTexture2D(GL_FRAMEBUFFER, target, GL_TEXTURE_2D, texture.getTemporaryIdentifier(), 0);
        }
        unbind();
    }

    public void attach_buffer(@NotNull RenderBuffer buffer, int attachment) {
        assert buffer.width() == this.width && buffer.height() == this.height || this.ignoreSize;
        bind();
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, buffer.getTemporaryIdentifier());
        unbind();
    }

    public void assertState() {
        bind();
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE;
        unbind();
    }


    public static int boundBuffer = -1;
    public void bind() {
        ensureAllocated();
        var id = this.handleData.id;
        if (boundBuffer != id) {
            glBindFramebuffer(GL_FRAMEBUFFER, id);
            boundBuffer = id;
        }
    }

    public static void unbind() {
        if (boundBuffer != 0) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            boundBuffer = 0;
        }
    }

    /// dont store, since the underlying buffer may be deleted when this object no longer exists
    public int getTemporaryIdentifier() {
        return this.handleData.id;
    }

    public void clear(Color color) {
        glClearColor(color.redFloat(), color.greenFloat(), color.blueFloat(), color.alphaFloat());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    private record FramebufferHandleData(int id) implements GCResource {
        @Override
        public void freeResource() {
            Framebuffer.boundBuffer = -1;
            glDeleteFramebuffers(this.id);
        }
    }
}

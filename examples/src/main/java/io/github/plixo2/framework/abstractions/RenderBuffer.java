package io.github.plixo2.framework.abstractions;

import lombok.Getter;
import lombok.experimental.Accessors;

import static org.lwjgl.opengl.GL30C.*;

public class RenderBuffer extends GPUResource  {

    @Getter
    @Accessors(fluent = true)
    private final int width;

    @Getter
    @Accessors(fluent = true)
    private final int height;

    private final RenderBufferHandleData handleData;

    private RenderBuffer(int id, int width, int height) {
        this.handleData = new RenderBufferHandleData(id);
        this.remover = GLResourceManagement.add(this, this.handleData);
        this.width = width;
        this.height = height;
    }
    public static RenderBuffer generate(int width, int height) {
        return new RenderBuffer(glGenRenderbuffers(), width, height);
    }

    public void store(int format) {
        glRenderbufferStorage(GL_RENDERBUFFER, format, this.width, this.height);
    }

    public static int boundRenderBuffer = 0;
    public void bind() {
        ensureAllocated();
        var id = this.handleData.id;
        if (boundRenderBuffer != id) {
            glBindRenderbuffer(GL_RENDERBUFFER, id);
            boundRenderBuffer = id;
        }
    }

    public void unbind() {
        if (boundRenderBuffer != 0) {
            glBindRenderbuffer(GL_RENDERBUFFER, 0);
            boundRenderBuffer = 0;
        }
    }

    /// dont store, since the underlying buffer may be deleted when this object no longer exists
    public int getTemporaryIdentifier() {
        return this.handleData.id;
    }

    private record RenderBufferHandleData(int id) implements GCResource {

        @Override
        public void freeResource() {
            RenderBuffer.boundRenderBuffer = -1;
            glDeleteRenderbuffers(this.id);
        }
    }

    public static void resetAllSlots() {
        boundRenderBuffer = -1;
    }
}

package io.github.plixo2.abstraction;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;

public abstract sealed class CommandBuffer extends GPUResource permits ArrayCommandBuffer, ElementCommandBuffer {

    protected static final int BYTES_PER_PARAM = Integer.BYTES;

    @Getter
    @Accessors(fluent = true)
    private final @Nullable ByteBuffer byteBuffer;

    private final IndirectBufferHandle handleData;

    public final int count;
    private final int bytesPerCommand;

    protected CommandBuffer(
            int count,
            int bytesPerCommand,
            MemorySide generationSide
    ) {
        if (!Capabilities.get().drawIndirect() && !Capabilities.get().multiDrawIndirect()) {
            throw new UnsupportedOperationException("Draw indirect not supported on this system");
        }
        this.count = count;
        this.bytesPerCommand = bytesPerCommand;
        this.handleData = new IndirectBufferHandle(glGenBuffers());
        this.remover = GLResourceManagement.add(this, this.handleData);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, this.handleData.id);
        boundBuffer = this.handleData.id;

        if (generationSide == MemorySide.CPU_AND_GPU_SIDE) {
            this.byteBuffer = BufferUtils.createByteBuffer(
                    bytesPerCommand * count
            );
            this.byteBuffer.position(0);
        } else {
            this.byteBuffer = null;
        }

        glBufferData(GL_DRAW_INDIRECT_BUFFER,  ((long) bytesPerCommand) * count, GL_STREAM_DRAW);
    }


    public void upload() {
        if (this.byteBuffer == null) {
            throw new IllegalStateException("This command buffer is not for CPU generation, cannot upload");
        }
        bind();
        this.byteBuffer.position(0);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, this.byteBuffer);
    }

    public void bind() {
        ensureAllocated();
        var handle = getTemporaryBufferIdentifier();
        if (boundBuffer != handle) {
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, handle);
            boundBuffer = handle;
        }
    }

    protected static void bindAsCommandBuffer(ShaderBuffer buffer) {
        var id = buffer.getTemporarySSBOIdentifier();
        if (boundBuffer != id) {
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, id);
            boundBuffer = id;
        }
    }

    public static void unbind() {
        if (boundBuffer != 0) {
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
            boundBuffer = 0;
        }
    }
    private static int boundBuffer = -1;

    protected void set(int index, int offset, int value) {
        if (this.byteBuffer == null) {
            throw new IllegalStateException("This command buffer is not for CPU generation, cannot upload");
        }
        this.byteBuffer.putInt(index * this.bytesPerCommand + offset, value);
    }


    /// dont store, since the underlying data may be deleted when this object no longer exists
    public int getTemporaryBufferIdentifier() {
        return this.handleData.id;
    }

    private record IndirectBufferHandle(int id) implements GCResource {
        @Override
        public void freeResource() {
            CommandBuffer.boundBuffer = -1;
            glDeleteBuffers(this.id);
        }
    }

}

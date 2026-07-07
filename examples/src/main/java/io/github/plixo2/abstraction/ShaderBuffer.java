package io.github.plixo2.abstraction;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Set;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class ShaderBuffer extends GPUResource implements GPUOutputStream  {
    private static final Set<Integer> VALID_TYPES = Set.of(
            GL_STREAM_DRAW,
            GL_STREAM_READ,
            GL_STREAM_COPY,
            GL_STATIC_DRAW,
            GL_STATIC_READ,
            GL_STATIC_COPY,
            GL_DYNAMIC_DRAW,
            GL_DYNAMIC_READ,
            GL_DYNAMIC_COPY
    );

    private static final int MAX_STORAGE_BUFFER_BINDINGS;
    private static final int[] ACTIVE_BUFFER;
    static {
        var capabilities = Capabilities.get();
        MAX_STORAGE_BUFFER_BINDINGS = capabilities.maxStorageBufferBindings();

        ACTIVE_BUFFER = new int[MAX_STORAGE_BUFFER_BINDINGS];
        for (int i = 0; i < MAX_STORAGE_BUFFER_BINDINGS; ++i) {
            ACTIVE_BUFFER[i] = -1;
        }
    }


    private final BufferDataHandleData handleData;

    @Accessors(fluent = true)
    private @Nullable ByteBuffer byteBuffer;

    /// like GL_STATIC_DRAW or GL_DYNAMIC_DRAW
    private final int type;

    /// binding index in the shader
    private final int shaderIndex;

    private int capacity;

    public ShaderBuffer(
            int initialCapacity,
            int type,
            int shaderIndex,
            MemorySide memorySide
    ) {
        if (!Capabilities.get().storageBuffers()) {
            throw new UnsupportedOperationException("Storage buffers not supported on this system");
        }
        if (!VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid buffer type: " + type);
        }
        if (shaderIndex < 0 || shaderIndex >= MAX_STORAGE_BUFFER_BINDINGS) {
            throw new IllegalArgumentException("Shader index " + shaderIndex + " is out of bounds (0-" + (MAX_STORAGE_BUFFER_BINDINGS - 1) + ")");
        }


        this.shaderIndex = shaderIndex;
        this.type = type;
        this.capacity = initialCapacity;

        if (memorySide == MemorySide.CPU_AND_GPU_SIDE) {
            this.byteBuffer = BufferUtils.createByteBuffer(initialCapacity);
        } else {
            this.byteBuffer = null;
        }

        this.handleData = new BufferDataHandleData(glGenBuffers());
        this.remover = GLResourceManagement.add(this, this.handleData);

        bindNotToShader();
        glBufferData(GL_SHADER_STORAGE_BUFFER, initialCapacity, type);
        ACTIVE_BUFFER[this.shaderIndex] = -1;
        unbindBuffer();
    }

    /// @return the number of bytes uploaded
    public int upload() {
        var bb = ensureCPUAccess();
        bb.flip();
        bindNotToShader();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, bb);
        return bb.limit();
    }

    private void bindNotToShader() {
        ensureAllocated();
        var id = this.handleData.id;
        if (currentlyBoundBuffer != id) {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, this.handleData.id);
            currentlyBoundBuffer = id;
        }
    }

    private static int currentlyBoundBuffer = -1;
    public void bind() {
        ensureAllocated();
        var slot = ACTIVE_BUFFER[this.shaderIndex];
        if (slot != this.handleData.id) {
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, this.shaderIndex, this.handleData.id);
            currentlyBoundBuffer = this.handleData.id;
            ACTIVE_BUFFER[this.shaderIndex] = this.handleData.id;
        } else {
            bindNotToShader();
        }
    }

    public void bindAsCommandBuffer() {
        CommandBuffer.bindAsCommandBuffer(this);
    }

    public static void unbindBuffer() {
        if (currentlyBoundBuffer != 0) {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
            currentlyBoundBuffer = 0;
        }
    }

    /// dont store, since the underlying data may be deleted when this object no longer exists
    public int getTemporarySSBOIdentifier() {
        return this.handleData.id;
    }

    public void seek(int index) {
        if (index < 0) index = 0;
        var bb = ensureCapacity(index);
        bb.position(index);
    }

    public int capacity() {
        return this.capacity;
    }

    public int position() {
        var bb = ensureCPUAccess();
        return bb.position();
    }

    public void clear() {
        var bb = ensureCPUAccess();
        bb.clear();
    }

    public byte getByte(int index) {
        var bb = ensureCPUAccess();
        return bb.get(index);
    }


    private ByteBuffer ensureCapacity(int bytesToAppend) {
        final var bb = ensureCPUAccess();

        int needed = bb.position() + bytesToAppend;
        if (needed <= capacity()) return this.byteBuffer;

        // double until enough
        int newCap = capacity();
        while (newCap < needed) newCap = Math.max(16, newCap * 2);

        this.capacity = newCap;

        // grow cpu buffer (preserve content & position)
        int oldPos = bb.position();
        bb.flip(); // prepare to copy 0..oldPos
        ByteBuffer bigger = BufferUtils.createByteBuffer(newCap);
        bigger.put(bb);
        bigger.position(oldPos);
        bigger.limit(newCap);
        this.byteBuffer = bigger;

        bindNotToShader();
        // grow gpu buffer (needs reupload of data)
        glBufferData(GL_SHADER_STORAGE_BUFFER, newCap, this.type);
        ACTIVE_BUFFER[this.shaderIndex] = -1;
        unbindBuffer();

        return this.byteBuffer;
    }
    @Override
    public void putMatrix(Matrix4f matrix4f) {
        var bb = ensureCapacity(64);
        matrix4f.get(bb);
        bb.position(bb.position() + 64);
    }
    @Override
    public void putFloat(float f) {
        var bb = ensureCapacity(4);
        bb.putFloat(f);
    }
    @Override
    public void putInt(int i) {
        var bb = ensureCapacity(4);
        bb.putInt(i);
    }
    @Override
    public void putShort(short s) {
        var bb = ensureCapacity(2);
        bb.putShort(s);
    }
    @Override
    public void putVector3f(float x, float y, float z) {
        var bb = ensureCapacity(12);
        bb.putFloat(x);
        bb.putFloat(y);
        bb.putFloat(z);
    }
    @Override
    public void putVector3f(Vector3f vector) {
        putVector3f(vector.x, vector.y, vector.z);
    }
    @Override
    public void putVector4f(float x, float y, float z, float w) {
        var bb = ensureCapacity(16);
        bb.putFloat(x);
        bb.putFloat(y);
        bb.putFloat(z);
        bb.putFloat(w);
    }
    @Override
    public void putVector4f(Vector4f vector) {
        putVector4f(vector.x, vector.y, vector.z, vector.w);
    }
    @Override
    public void putVector2f(float x, float y) {
        var bb = ensureCapacity(8);
        bb.putFloat(x);
        bb.putFloat(y);
    }
    @Override
    public void putVector2f(Vector2f vector) {
        putVector2f(vector.x, vector.y);
    }
    @Override
    public void putByte(byte b) {
        var bb = ensureCapacity(1);
        bb.put(b);
    }
    @Override
    public void putColor(Color color) {
        var bb = ensureCapacity(16);
        bb.putFloat(color.redFloat());
        bb.putFloat(color.greenFloat());
        bb.putFloat(color.blueFloat());
        bb.putFloat(color.alphaFloat());
    }
    @Override
    public void putLong(long l) {
        var bb = ensureCapacity(8);
        bb.putLong(l);
    }
    @Override
    public void put(ByteBuffer buffer) {
        var bb = ensureCapacity(buffer.remaining());
        bb.put(buffer);
    }

    private ByteBuffer ensureCPUAccess() {
        if (this.byteBuffer == null) {
            throw new IllegalStateException("This buffer was created with GPU-only memory, so no data can be put from the CPU");
        }
        return this.byteBuffer;
    }

    private record BufferDataHandleData(int id) implements GCResource {
        @Override
        public void freeResource() {
            ShaderBuffer.unbindBuffer();
            CommandBuffer.unbind();
            glDeleteBuffers(this.id);
            resetSlots(this.id);
        }
    }

    private static void resetSlots(int id) {
        for (int i = 0; i < ACTIVE_BUFFER.length; ++i) {
            if (ACTIVE_BUFFER[i] == id) {
                ACTIVE_BUFFER[i] = -1;
            }
        }

    }

}

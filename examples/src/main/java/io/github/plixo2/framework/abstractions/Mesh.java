package io.github.plixo2.framework.abstractions;

import lombok.val;
import org.lwjgl.opengl.ARBDrawIndirect;
import org.lwjgl.opengl.ARBMultiDrawIndirect;
import org.lwjgl.opengl.GL31;

import java.lang.foreign.MemorySegment;
import java.nio.*;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL32C.glDrawElementsBaseVertex;

public class Mesh extends GPUResource  {
    private static final boolean drawIndirectSupported = Capabilities.get().drawIndirect();
    private static final boolean multidrawIndirectSupported = Capabilities.get().multiDrawIndirect();

    private static final int USE_ARRAYS = -1;
    private static final int NO_BUFFER = -1;


    private final MeshHandleData handleData;
    public int elementCount;

    public final Shader.Attribute[] layout;
    public final int type;
    public final int indicesType;   // USE_ARRAYS means no indices, so use glDrawArrays*

    public Mesh(
            Shader.Attribute[] layout,
            int vertexArrayObject,
            int vertexBufferObject,
            int vertexElementObject,
            int elementCount,
            int type,
            int indicesType
    ) {
        this.handleData = new MeshHandleData(vertexArrayObject, vertexBufferObject, vertexElementObject);
        this.elementCount = elementCount;
        this.type = type;
        this.indicesType = indicesType;
        this.layout = layout;
        this.remover = GLResourceManagement.add(this, this.handleData);
    }

    public Mesh(
            Shader.Attribute[] layout,
            int vertexArrayObject,
            int vertexBufferObject,
            int elementCount,
            int type
    ) {
        this(layout, vertexArrayObject, vertexBufferObject, NO_BUFFER, elementCount, type, USE_ARRAYS);
    }

    public void overwriteElementCount(int newCount) {
        this.elementCount = newCount;
    }


    /// Creates a quad mesh with no information
    /// Useful for shaders that generate their own vertex data (using gl_VertexID)
    public static Mesh shaderCreatedQuad() {
        var layout = new Shader.Attribute[]{};
        val vao = glGenVertexArrays();
        bindVAO(vao);
        return new Mesh(layout, vao, NO_BUFFER, 4, GL_TRIANGLE_STRIP);
    }

    public static Mesh triangleStripOfBuffer(ShaderBuffer vertexBuffer, Shader.Attribute[] layout) {
        var vao = glGenVertexArrays();
        bindVAO(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getTemporarySSBOIdentifier());
        setLayout(layout);
        bindVAO(0);
        return new Mesh(layout, vao, NO_BUFFER, 0, GL_TRIANGLE_STRIP);
    }

    public static Mesh fromBuffers(Shader.Attribute[] layout, Buffer indices, Buffer... buffers) {
        return fromBuffers(GL_TRIANGLES, layout, indices, buffers);
    }
    public static Mesh fromBuffers(int primitive, Shader.Attribute[] layout, Buffer indices, Buffer... buffers) {
        if (layout.length != buffers.length) {
            throw new IllegalArgumentException("Layout and buffers must have the same length");
        }
        var elementCount = indices.remaining();

        if (primitive == GL_TRIANGLES) {
            if (elementCount % 3 != 0) {
                throw new IllegalArgumentException("Element count must be a multiple of 3 for triangles");
            }
        } else if (primitive == GL_LINES) {
            if (elementCount % 2 != 0) {
                throw new IllegalArgumentException("Element count must be a multiple of 2 for lines");
            }
        }
//        ensureBufferType(layout, buffers);

        val vao = glGenVertexArrays();
        bindVAO(vao);

        val ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        var indicesType = switch (indices) {
            case ByteBuffer buffer -> {
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
                yield GL_UNSIGNED_BYTE;
            }
            case IntBuffer buffer -> {
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
                yield GL_UNSIGNED_INT;
            }
            case ShortBuffer buffer -> {
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
                yield GL_UNSIGNED_SHORT;
            }
            default -> throw new IllegalArgumentException(
                    "Indices must be either a ByteBuffer, ShortBuffer or IntBuffer"
            );
        };

        val vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        int totalSize = 0;
        int[] offsets = new int[buffers.length];

        for (var i = 0; i < buffers.length; i++) {
            var buffer = buffers[i];
            var attrib = layout[i];
            var size = buffer.remaining() * attrib.typeSize();
            offsets[i] = totalSize;
            totalSize += size;
        }

        glBufferData(GL_ARRAY_BUFFER, totalSize, GL_STATIC_DRAW);
        for (var i = 0; i < buffers.length; i++) {
            var offset = offsets[i];
            var buffer = buffers[i];

            switch (buffer) {
                case ByteBuffer byteBuffer -> {
                    glBufferSubData(GL_ARRAY_BUFFER, offset, byteBuffer);
                }
                case FloatBuffer floatBuffer -> {
                    glBufferSubData(GL_ARRAY_BUFFER, offset, floatBuffer);
                }
                case IntBuffer intBuffer -> {
                    glBufferSubData(GL_ARRAY_BUFFER, offset, intBuffer);
                }
                case ShortBuffer shortBuffer -> {
                    glBufferSubData(GL_ARRAY_BUFFER, offset, shortBuffer);
                }
                default -> {
                    throw new IllegalArgumentException(
                            "Buffer must be either a ByteBuffer, ShortBuffer, FloatBuffer or IntBuffer");
                }
            }


        }
        for (int i = 0; i < layout.length; i++) {
            var attrib = layout[i];
            glEnableVertexAttribArray(i);
            if (attrib.useAttribInteger()) {
                glVertexAttribIPointer(i, attrib.size(), attrib.type(), attrib.byte_size(), offsets[i]);
            } else {
                glVertexAttribPointer(i, attrib.size(), attrib.type(), false, attrib.byte_size(), offsets[i]);
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        bindVAO(0);

        return new Mesh(layout, vao, vbo, ebo, elementCount, primitive, indicesType);
    }

    private static void ensureBufferType(Shader.Attribute[] layout, Buffer[] buffers) {
        assert layout.length == buffers.length;

        BiConsumer<Integer, String> throwException = (i, type) -> {
            throw new IllegalArgumentException("Buffer type must be " + type + " at index " + i);
        };

        for (var i = 0; i < buffers.length; i++) {
            var buffer = buffers[i];
            var attrib = layout[i];

            switch (buffer) {
                case ByteBuffer byteBuffer -> {
                    if (!attrib.isByte()) {
                        throwException.accept(i, "GL_BYTE");
                    }
                }
                case FloatBuffer floatBuffer -> {
                    if (!attrib.isFloat()) {
                        throwException.accept(i, "GL_FLOAT");
                    }
                }
                case IntBuffer intBuffer -> {
                    if (!attrib.isInt()) {
                        throwException.accept(i, "GL_INT");
                    }
                }
                case ShortBuffer shortBuffer -> {
                    if (!attrib.isShort()) {
                        throwException.accept(i, "GL_SHORT");
                    }
                }
                case null, default ->
                        throw new IllegalArgumentException("Buffer type not supported");
            }
        }
    }

    public static Mesh fromRaw(float[] vertices, int[] indices, Shader.Attribute[] layout) {
        return fromRaw(vertices, indices, layout, GL_TRIANGLES);
    }

    public static Mesh fromRaw(float[] vertices, int[] indices, Shader.Attribute[] layout, int elementType) {
        val vao = glGenVertexArrays();
        val vbo = glGenBuffers();
        val ebo = glGenBuffers();
        bindVAO(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        setLayout(layout);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        bindVAO(0);

        return new Mesh(layout, vao, vbo, ebo, indices.length, elementType, GL_UNSIGNED_INT);
    }

    public static Mesh empty() {
        var layout = new Shader.Attribute[]{};
        val vao = glGenVertexArrays();
        bindVAO(vao);
        return new Mesh(layout, vao, NO_BUFFER, 0, GL_TRIANGLES);
    }

    public static Mesh fromRaw(MemorySegment vertices, MemorySegment indices, int indicesType, Shader.Attribute[] layout) {
        return fromRaw(vertices, indices, indicesType, layout, GL_TRIANGLES);
    }

    public static Mesh fromRaw(
            MemorySegment vertices,
            MemorySegment indices,
            int indicesType,
            Shader.Attribute[] layout,
            int elementType
    ) {
        val vao = glGenVertexArrays();
        val vbo = glGenBuffers();
        val ebo = glGenBuffers();
        bindVAO(vao);


        var bytesPerElement = switch (indicesType) {
            case GL_UNSIGNED_INT -> Integer.BYTES;
            case GL_UNSIGNED_SHORT -> Short.BYTES;
            case GL_UNSIGNED_BYTE -> Byte.BYTES;
            default -> {
                throw new IllegalArgumentException("Indices type must be GL_UNSIGNED_INT, GL_UNSIGNED_SHORT or GL_UNSIGNED_BYTE");
            }
        };
        var elementCount = Math.toIntExact(indices.byteSize() / bytesPerElement);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        nglBufferData(GL_ARRAY_BUFFER, vertices.byteSize(), vertices.address(), GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        nglBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.byteSize(), vertices.address(), GL_STATIC_DRAW);

        setLayout(layout);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        bindVAO(0);

        return new Mesh(layout, vao, vbo, ebo, elementCount, elementType, indicesType);
    }

    public static Mesh fromSized(
            int vertices,
            int indices,
            int indicesType,
            Shader.Attribute[] layout,
            int elementType
    ) {
        val vao = glGenVertexArrays();
        val vbo = glGenBuffers();
        val ebo = glGenBuffers();
        bindVAO(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);

        setLayout(layout);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        bindVAO(0);

        return new Mesh(layout, vao, vbo, ebo, 0, elementType, indicesType);
    }

    private static void setLayout(Shader.Attribute[] layout) {
        var vertexSize = 0;
        for (Shader.Attribute attribute : layout) {
            vertexSize += attribute.byte_size();
        }
        int offset = 0;
        for (int i = 0; i < layout.length; i++) {
            var attrib = layout[i];
            glVertexAttribPointer(i, attrib.size(), GL_FLOAT, false, vertexSize, offset);

            offset += attrib.byte_size();
            glEnableVertexAttribArray(i);
        }
    }
    public void drawBaseVertex(
            int count,
            long indexOffset,
            int baseVertex
    ) {
        bind();

        if (count == 0) {
            return;
        }
        if (this.indicesType == USE_ARRAYS) {
            System.err.println("Mesh.drawBaseVertex called on a mesh without an index buffer");
            return;
        }

        glDrawElementsBaseVertex(
                this.type,
                count,
                this.indicesType,
                indexOffset,
                baseVertex
        );



    }


    public void draw() {
        bind();
        if (this.elementCount == 0) {
            return;
        }
        if (this.indicesType == USE_ARRAYS) {
            glDrawArrays(this.type, 0, this.elementCount);
            return;
        }
        glDrawElements(this.type, this.elementCount, this.indicesType, 0);
    }

    public void drawInstanced(int count) {
        if (count > 0) {
            bind();
            if (this.indicesType == USE_ARRAYS) {
                GL31.glDrawArraysInstanced(this.type, 0, this.elementCount, count);
                return;
            }
            glDrawElementsInstanced(this.type, this.elementCount, this.indicesType, 0, count);
        }
    }
    public void drawIndirect(int indirectBuffer) {
        if (!drawIndirectSupported) {
            throw new UnsupportedOperationException("Draw indirect is not supported on this system");
        }

        bind();
        if (this.indicesType == USE_ARRAYS) {
            ARBDrawIndirect.glDrawArraysIndirect(this.type, indirectBuffer);
            return;
        }
        ARBDrawIndirect.glDrawElementsIndirect(this.type, this.indicesType, indirectBuffer);
    }

    public void multiDrawInstancedIndirect(
            int startInstance,
            int count
    ) {
        if (!multidrawIndirectSupported) {
            throw new UnsupportedOperationException("Multi draw indirect is not supported on this system");
        }

        if (count > 0) {
            bind();
            if (this.indicesType == USE_ARRAYS) {
                var stride = startInstance != 0 ? ArrayCommandBuffer.BYTES_PER_COMMAND : 0;
                var offset = startInstance * stride;
                ARBMultiDrawIndirect.glMultiDrawArraysIndirect(this.type, offset, count, stride);
                return;
            }
            var stride = startInstance != 0 ? ElementCommandBuffer.BYTES_PER_COMMAND : 0;
            var offset = startInstance * stride;
            ARBMultiDrawIndirect.glMultiDrawElementsIndirect(this.type, this.indicesType, offset, count, stride);
        }
    }

    public static int boundVAO = -1;
    public void bind() {
        ensureAllocated();
        bindVAO(this.handleData.vertexArrayObject);
    }

    public static void bindVAO(int vao) {
        if (boundVAO != vao) {
            boundVAO = vao;
            glBindVertexArray(vao);
        }
    }

    public void uploadVerticies(
            long offset,
            float[] data
    ) {
        ensureAllocated();
        glBindBuffer(GL_ARRAY_BUFFER, this.handleData.vertexBufferObject);
        glBufferSubData(GL_ARRAY_BUFFER, offset, data);
    }
    public void uploadIndicies(
            long offset,
            int[] data
    ) {
        ensureAllocated();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.handleData.vertexElementObject);
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, data);
    }

    /// dont store, since the underlying data may be deleted when this object no longer exists
    public int getTemporaryVertexArrayObject() {
        return this.handleData.vertexArrayObject;
    }

    /// dont store, since the underlying data may be deleted when this object no longer exists
    /// @return -1 if no vertex buffer object is used
    public int getTemporaryVertexBufferObject() {
        return this.handleData.vertexBufferObject;
    }

    /// dont store, since the underlying data may be deleted when this object no longer exists
    /// @return -1 if no element buffer object is used
    public int getTemporaryVertexElementObject() {
        return this.handleData.vertexElementObject;
    }

    public static Mesh normalizedQuad() {
        var layout = new Shader.Attribute[]{Shader.Attribute.Float(3), Shader.Attribute.Float(2), Shader.Attribute.Float(3)};
        float[] vertices = {
                //position          uv           normals
                1f, 1f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top right
                1f, -1f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,// bottom right
                -1f, -1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, // bottom left
                -1f, 1f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,// top left
        };
        int[] indices = {  // note that we start from 0!
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };
        return Mesh.fromRaw(vertices, indices, layout);
    }

    private record MeshHandleData(
            int vertexArrayObject,
            int vertexBufferObject,
            int vertexElementObject
    ) implements GCResource {

        @Override
        public void freeResource() {
            Mesh.boundVAO = -1;
            glDeleteVertexArrays(this.vertexArrayObject);
            if (this.vertexBufferObject != NO_BUFFER) {
                glDeleteBuffers(this.vertexBufferObject);
            }
            if (this.vertexElementObject != NO_BUFFER) {
                glDeleteBuffers(this.vertexElementObject);
            }
        }
    }

    public static void resetAllSlots() {
        Mesh.boundVAO = -1;
    }
}

package io.github.plixo2.framework;

import io.github.plixo2.abstraction.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glUniform1i;
import static org.lwjgl.opengl.GL32C.glDrawElementsBaseVertex;

public class MultiMesh {
    private static final int FLOATS_PER_VERTEX = 6;
    private static final int LEGACY_COMMAND_INTS = 3;
    private static final int LEGACY_INDEX_OFFSET = 0;
    private static final int LEGACY_VERTEX_OFFSET = 1;
    private static final int LEGACY_INDEX_COUNT = 2;
    private static final int VERTEX_BYTE_CAPACITY = 1024 * 1024 * 1; // 1 mb
    private static final int INDEX_BYTE_CAPACITY = 1024 * 1024 * 1; // 1 mb

    private static final Shader.Attribute[] layout = new Shader.Attribute[]{
            Shader.Attribute.Float(3),
            Shader.Attribute.Float(3)
    };

    private final int vertexByteCapacity;
    private final int indexByteCapacity;

    private int vertexFloatCount;
    private int indexCount;

    final Mesh mesh;
    final boolean useLegacy;

    private @Nullable ElementCommandBuffer commandBuffer;
    private @Nullable int[] legacyCommands;
    private int commandCapacity = 0;
    private int count = 0;

    private final ShaderBuffer perInstanceData;

    public MultiMesh(boolean useLegacy, int minVertexCapacity, int minIndexCapacity) {
        this.vertexByteCapacity = Math.max(minVertexCapacity * Float.BYTES, VERTEX_BYTE_CAPACITY);
        this.indexByteCapacity = Math.max(minIndexCapacity * Integer.BYTES, INDEX_BYTE_CAPACITY);
        this.useLegacy = useLegacy;

        this.mesh = Mesh.fromSized(
                this.vertexByteCapacity,
                this.indexByteCapacity,
                GL_UNSIGNED_INT,
                layout,
                GL_TRIANGLES
        );
        if (!useLegacy) {
            ensureCommandBuffer();
        }

        this.perInstanceData = new ShaderBuffer(64, GL_STREAM_DRAW, 0, MemorySide.CPU_AND_GPU_SIDE);
    }
    private void ensureCommandBuffer() {
        if (this.useLegacy) {

            if (this.legacyCommands == null) {
                this.legacyCommands = new int[64 * LEGACY_COMMAND_INTS];
                this.commandCapacity = 64;
                return;
            }
            var newCapacity = this.commandCapacity * 2;
            var newCommands = new int[newCapacity * LEGACY_COMMAND_INTS];
            System.arraycopy(this.legacyCommands, 0, newCommands, 0, this.commandCapacity * LEGACY_COMMAND_INTS);
            this.legacyCommands = newCommands;
            this.commandCapacity = newCapacity;
            return;
        }

        if (this.commandBuffer == null) {
            this.commandBuffer = new ElementCommandBuffer(64, MemorySide.CPU_AND_GPU_SIDE);
            this.commandCapacity = 64;
            return;
        }

        var prev = this.commandBuffer;
        var data = prev.byteBuffer();

        this.commandBuffer = new ElementCommandBuffer(this.commandCapacity * 2, MemorySide.CPU_AND_GPU_SIDE);
        assert this.commandBuffer.byteBuffer() != null;
        this.commandBuffer.byteBuffer().put(data);
        this.commandCapacity *= 2;

        prev.free();
    }


    boolean canPlace(MeshCreator.MeshArgs mesh) {
        return (this.vertexFloatCount + mesh.verticies().length) * Float.BYTES <= this.vertexByteCapacity &&
                (this.indexCount + mesh.indices().length) * Integer.BYTES <= this.indexByteCapacity;
    }


    MeshRecord place(MeshCreator.MeshArgs mesh, @Nullable Color customColor) {
        if (!canPlace(mesh)) {
            throw new RuntimeException("Cannot place mesh, capacity exceeded");
        }
        var vertexFloatCount = mesh.verticies().length;
        var vertexCount = vertexFloatCount / FLOATS_PER_VERTEX;
        var indexCount = mesh.indices().length;

        var vbo = this.mesh.getTemporaryVertexBufferObject();
        var ebo = this.mesh.getTemporaryVertexElementObject();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, (long) this.vertexFloatCount * Float.BYTES, mesh.verticies());

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, (long) this.indexCount * Integer.BYTES, mesh.indices());

        var record = new MeshRecord(
                customColor,
                this,
                this.vertexFloatCount / FLOATS_PER_VERTEX,
                this.indexCount,
                vertexCount,
                indexCount
        );


        this.vertexFloatCount += vertexFloatCount;
        this.indexCount += indexCount;

        return record;

    }

    void free() {
        this.mesh.free();
        if (!this.useLegacy) {
            assert this.commandBuffer != null;
            this.commandBuffer.free();
        }
        this.perInstanceData.free();
    }

    void draw(Shader.Uniform<Integer> u_legacy_instance) {


        if (this.useLegacy) {
            assert this.legacyCommands != null;

            this.perInstanceData.upload();
            this.perInstanceData.bind();

            this.mesh.bind();
            var location = u_legacy_instance.location();
            u_legacy_instance.loadCached(-1);
            for (var i = 0; i < this.count; i++) {
                var offset = i * LEGACY_COMMAND_INTS;
                var indexOffset = this.legacyCommands[offset + LEGACY_INDEX_OFFSET];
                var vertexOffset = this.legacyCommands[offset + LEGACY_VERTEX_OFFSET];
                var indexCount = this.legacyCommands[offset + LEGACY_INDEX_COUNT];

                glUniform1i(location, i);
                glDrawElementsBaseVertex(
                        GL_TRIANGLES,
                        indexCount,
                        GL_UNSIGNED_INT,
                        (long) indexOffset * Integer.BYTES,
                        vertexOffset
                );
            }
        } else {
            assert this.commandBuffer != null;

            this.commandBuffer.upload();
            this.commandBuffer.bind();

            this.perInstanceData.upload();
            this.perInstanceData.bind();

            this.mesh.multiDrawInstancedIndirect(0, this.count);
        }

        this.count = 0;
        this.perInstanceData.clear();
    }

    private void addDraw(
            int vertexOffset,
            int indexOffset,
            int indexCount,
            Matrix4f transform,
            Matrix4f normal,
            int color
    ) {
        if (this.count >= this.commandCapacity) {
            ensureCommandBuffer();
        }
        var commandIndex = this.count;

        this.perInstanceData.putMatrix(transform);
        this.perInstanceData.putMatrix(normal);

        var r = ((color >> 16) & 0xff) / 255.0f;
        var g = ((color >> 8) & 0xff) / 255.0f;
        var b = ((color) & 0xff) / 255.0f;
        var a = ((color >> 24) & 0xff) / 255.0f;
        this.perInstanceData.putVector4f(r, g, b, a);

        this.count += 1;
        if (this.useLegacy) {
            assert this.legacyCommands != null;
            var offset = commandIndex * LEGACY_COMMAND_INTS;
            this.legacyCommands[offset + LEGACY_INDEX_OFFSET] = indexOffset;
            this.legacyCommands[offset + LEGACY_VERTEX_OFFSET] = vertexOffset;
            this.legacyCommands[offset + LEGACY_INDEX_COUNT] = indexCount;
        } else {
            assert this.commandBuffer != null;
            this.commandBuffer.setCompleteDraw(
                    commandIndex,
                    indexCount,
                    1,
                    indexOffset,
                    vertexOffset,
                    commandIndex
            );

        }


    }


    public record MeshRecord(
            @Nullable Color customColor,
            MultiMesh mesh,
            int vertexOffset,
            int indexOffset,
            int vertexCount,
            int indexCount
    ) {
        void addDraw(
                Matrix4f transform,
                Matrix4f normal,
                int color
        ) {
            this.mesh.addDraw(
                    this.vertexOffset,
                    this.indexOffset,
                    this.indexCount,
                    transform,
                    normal,
                    color
            );
        }
    }


}

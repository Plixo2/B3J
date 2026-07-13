package io.github.plixo2.framework.abstractions;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.*;

public class Capabilities {
    private static Capabilities instance;

    @Getter
    @Accessors(fluent = true)
    private final boolean bindlessTexture;

    @Getter
    @Accessors(fluent = true)
    private final int maxFragmentTextureUnits;

    @Getter
    @Accessors(fluent = true)
    private final boolean textureUnits;

    @Getter
    @Accessors(fluent = true)
    private final int maxTotalTextureUnits;

    @Getter
    @Accessors(fluent = true)
    private final boolean directStateAccess;

    @Getter
    @Accessors(fluent = true)
    private final boolean computeShaders;

    @Getter
    @Accessors(fluent = true)
    private final boolean drawIndirect;

    @Getter
    @Accessors(fluent = true)
    private final boolean multiDrawIndirect;

    @Getter
    @Accessors(fluent = true)
    private final boolean storageBuffers;

    @Getter
    @Accessors(fluent = true)
    private final int maxStorageBufferBindings;

    @Getter
    @Accessors(fluent = true)
    private final boolean debug;

    @Getter
    @Accessors(fluent = true)
    private final boolean pipelineStatistics;

    @Getter
    @Accessors(fluent = true)
    private final boolean clearTexture;

    @Getter
    @Accessors(fluent = true)
    private final int maxTextureSize;

    @Getter
    @Accessors(fluent = true)
    private final boolean nvxMemInfo;

    @Getter
    @Accessors(fluent = true)
    private final boolean amdMemInfo;

    @Getter
    @Accessors(fluent = true)
    private final boolean glDrawID;

    @Getter
    @Accessors(fluent = true)
    private final boolean shaderDrawParameters;

    public Capabilities(GLCapabilities capabilities) {
        this.bindlessTexture = capabilities.GL_ARB_bindless_texture;
        this.maxFragmentTextureUnits = GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS);
        this.maxTotalTextureUnits = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
        this.textureUnits = (capabilities.OpenGL45 || capabilities.GL_ARB_multi_bind);
        this.directStateAccess = (capabilities.OpenGL45 || capabilities.GL_ARB_direct_state_access);
        this.computeShaders = capabilities.OpenGL43 || capabilities.GL_ARB_compute_shader;
        this.drawIndirect = capabilities.OpenGL40 || capabilities.GL_ARB_draw_indirect;
        this.multiDrawIndirect = capabilities.OpenGL43 || capabilities.GL_ARB_multi_draw_indirect;
        this.storageBuffers = capabilities.OpenGL43 || capabilities.GL_ARB_shader_storage_buffer_object;
        this.maxStorageBufferBindings = this.storageBuffers ?
                 GL11.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS) : 0;

        this.debug = capabilities.OpenGL43 || capabilities.GL_KHR_debug;
        this.pipelineStatistics = capabilities.GL_ARB_pipeline_statistics_query;
        this.clearTexture = capabilities.OpenGL44 || capabilities.GL_ARB_clear_texture;
        this.maxTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);

        this.glDrawID = capabilities.OpenGL46;
        this.shaderDrawParameters = capabilities.OpenGL43 && capabilities.GL_ARB_shader_draw_parameters;

        this.nvxMemInfo = capabilities.GL_NVX_gpu_memory_info;
        this.amdMemInfo = capabilities.GL_ATI_meminfo;

    }

    public void print() {
        System.out.println("OpenGL Capabilities:");
        System.out.println("  Bindless Texture: " + this.bindlessTexture);
        System.out.println("  Max Fragment Texture Units: " + this.maxFragmentTextureUnits);
        System.out.println("  Max Total Texture Units: " + this.maxTotalTextureUnits);
        System.out.println("  Texture Units: " + this.textureUnits);
        System.out.println("  Direct State Access: " + this.directStateAccess);
        System.out.println("  Compute Shaders: " + this.computeShaders);
        System.out.println("  Draw Indirect: " + this.drawIndirect);
        System.out.println("  Multi Draw Indirect: " + this.multiDrawIndirect);
        System.out.println("  Storage Buffers: " + this.storageBuffers);
        System.out.println("  Max Storage Buffer Bindings: " + this.maxStorageBufferBindings);
        System.out.println("  OpenGL Debugging: " + this.debug);
        System.out.println("  Pipeline Statistics Query: " + this.pipelineStatistics);
        System.out.println("  Clear Texture: " + this.clearTexture);
        System.out.println("  Max Texture Size: " + this.maxTextureSize);
    }

    public static Capabilities get() {
        if (instance == null) {
            instance = new Capabilities(GL.getCapabilities());
        }
        return instance;
    }
}

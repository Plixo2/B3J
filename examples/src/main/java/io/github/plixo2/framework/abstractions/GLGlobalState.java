package io.github.plixo2.framework.abstractions;

public class GLGlobalState {

    public static void reset() {
        // clear previous run
        GLResourceManagement.startNewGeneration();

        // reset momoization slots
        GLTextureState.resetAll();
        ShaderBuffer.resetAllSlots();
        Shader.resetAllSlots();
        Framebuffer.resetAllSlots();
        RenderBuffer.resetAllSlots();
        Mesh.resetAllSlots();
        CommandBuffer.resetAllSlots();
    }

}

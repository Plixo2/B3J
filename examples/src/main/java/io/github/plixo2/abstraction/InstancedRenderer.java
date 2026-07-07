package io.github.plixo2.abstraction;

public class InstancedRenderer {

    public void render(Shader shader, Mesh mesh, ShaderBuffer data, ElementCommandBuffer commandBuffer) {
            shader.bind();
            data.bind();

            commandBuffer.bind();
            mesh.multiDrawInstancedIndirect(0, commandBuffer.count);

//            Shader.unbind();
    }

}

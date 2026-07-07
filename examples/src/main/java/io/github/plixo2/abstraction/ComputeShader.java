package io.github.plixo2.abstraction;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;

import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.*;

public class ComputeShader extends Shader {

    public ComputeShader(String src) {
        super(glCreateProgram());
        if (!Capabilities.get().computeShaders()) {
            throw new UnsupportedOperationException("Compute shaders are not supported on this system");
        }

        var program = this.id();
        int compute = 0;
        try {
            compute = compileShader(src, GL_COMPUTE_SHADER);
            glAttachShader(program, compute);
            glLinkProgram(program);
            glDeleteShader(compute);
        } finally {
            if (compute != 0) {
                glDeleteShader(compute);
            }
        }
        check(program);
    }

    @SneakyThrows
    public static ComputeShader fromResource(String folder) {
        var compute_ = IOUtils.resourceToString(folder, Charset.defaultCharset());
        return new ComputeShader(compute_);
    }

    public void dispatch(int x, int y, int z) {
        this.bind();
        glDispatchCompute(x, y, z);
    }

    public void memoryBarrier(AccessBarrier barrier) {
        glMemoryBarrier(barrier.bit);
    }
    public void memoryBarrier(AccessBarrier a, AccessBarrier b) {
        glMemoryBarrier(a.bit | b.bit);
    }
    public void memoryBarrier(AccessBarrier a, AccessBarrier b, AccessBarrier c) {
        glMemoryBarrier(a.bit | b.bit | c.bit);
    }
    public void memoryBarrier(AccessBarrier a, AccessBarrier b, AccessBarrier c, AccessBarrier d) {
        glMemoryBarrier(a.bit | b.bit | c.bit | d.bit);
    }

    public void memoryBarrier(AccessBarrier... barriers) {
        int bits = 0;
        for (var barrier : barriers) {
            bits |= barrier.bit;
        }
        glMemoryBarrier(bits);
    }

    @RequiredArgsConstructor
    public enum AccessBarrier {
        SHADER_IMAGE_ACCESS(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT),
        SHADER_STORAGE(GL_SHADER_STORAGE_BARRIER_BIT),
        VERTEX_ATTRIB_ARRAY(GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT),
        ELEMENT_ARRAY(GL_ELEMENT_ARRAY_BARRIER_BIT),
        UNIFORM(GL_UNIFORM_BARRIER_BIT),
        TEXTURE_FETCH(GL_TEXTURE_FETCH_BARRIER_BIT),
        COMMAND(GL_COMMAND_BARRIER_BIT);

        private final int bit;
    }

}

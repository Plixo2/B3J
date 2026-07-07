package io.github.plixo2.abstraction;

import io.github.plixo2.abstraction.texture.Texture;
import lombok.Getter;
import lombok.experimental.Accessors;

/// Manages a fixed-size array of texture units for a `sampler2D` array uniform:
/// ```glsl
/// uniform sampler2D textures[NUM_TEXTURES];
/// ```
///
/// Use {@link #put} to add a texture unit and get the index to use in the shader. The array will
/// automatically reuse texture units that have already been added, so you can call {@link #put}
/// multiple times with the same texture unit without worrying about duplicates.
/// If the array is full, this method will return -1
///
///
/// Use {@link #bind} to bind all the texture units and
/// use {@link #reset} to clear the array for the next frame.
///
///
/// See {@link SetupElement} to link the sampler array to the shader uniform
/// (typically done once during initialization)
public class SamplerArray {

    @Getter
    @Accessors(fluent = true)
    private final int size;

    @Getter
    @Accessors(fluent = true)
    private final TextureType type;

    private final int[] samplers;

    private int count = 0;

    public SamplerArray(TextureType type) {
        this(type, Capabilities.get().maxFragmentTextureUnits());
    }

    public SamplerArray(TextureType type, int size) {
        var maxTextureUnits = Capabilities.get().maxFragmentTextureUnits();
        if (size > maxTextureUnits) {
            throw new IllegalArgumentException(
                    "Size cannot be greater than the maximum texture units supported by the hardware (" + maxTextureUnits + ")"
            );
        }
        this.type = type;
        this.size = size;
        this.samplers = new int[size];
        for (int i = 0; i < size; i++) {
            this.samplers[i] = i;
        }
    }

    /// @return the texture unit index, or -1 if the array is full
    /// @throws IllegalArgumentException if the texture type does not match the sampler array type
    public int put(Texture texture) {
        if (texture.type() != this.type) {
            throw new IllegalArgumentException("Texture type does not match sampler array type");
        }
        var id = texture.getTemporaryIdentifier();
        var index = findSampler(id);
        if (index != -1) {
            return index;
        }
        if (this.count >= this.size) {
            return -1;
        }
        var indexToUse = this.count;
        this.samplers[indexToUse] = id;
        this.count++;
        return indexToUse;
    }

    private int findSampler(int id) {
        // iterate backwards for locality
        for (var i = this.count - 1; i >= 0; i--) {
            var sampler = this.samplers[i];
            if (sampler == id) {
                return i;
            }
        }
        return -1;
    }

    public void bind() {
        for (var i = 0; i < this.count; i++) {
            GLTextureState.bind(this.type, i, this.samplers[i]);
        }
    }

    public int fillCount() {
        return this.count;
    }

    public void reset() {
        this.count = 0;
    }

    /// links the sampler array on the shader side to the texture units
    /// used in {@link Shader.Uniform#loadCached}
    public static class SetupElement {
        public final int size;

        private SetupElement(int size) {
            this.size = size;
        }

        public static SetupElement of(int size) {
            return new SetupElement(size);
        }
    }
}

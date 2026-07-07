package io.github.plixo2.abstraction.texture;

import io.github.plixo2.abstraction.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.ARBBindlessTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL44.GL_MIRROR_CLAMP_TO_EDGE;


public abstract class Texture extends GPUResource {
    public static final boolean DSA = Capabilities.get().directStateAccess();
    public static final boolean COMPUTE = Capabilities.get().computeShaders();

    @Getter
    @Accessors(fluent = true)
    protected final int width;
    @Getter
    @Accessors(fluent = true)
    protected final int height;

    @Getter
    @Accessors(fluent = true)
    protected final TextureType type;

    private final TextureHandleData handleData;


    protected Texture(
            TextureType type,
            int width,
            int height
    ) {
        this.width = width;
        this.height = height;
        this.type = type;

        var id = DSA ? GL45.glCreateTextures(this.type.target()) : glGenTextures();
        this.handleData = new TextureHandleData(this.type, id);
        // ensure the texture is initialized with the valid target, used when
        // `directStateAccess` is disabled, but `textureUnits` is enabled
        GLTextureState.bindNew(this.type, this.handleData.id);
        this.remover = GLResourceManagement.add(this, this.handleData);
    }


    public abstract void setStorage(
            int levels,
            int internalFormat,
            int format,
            int type
    );


    public void bindImage(int unit, int type, int access) {
        if (!COMPUTE) {
            throw new UnsupportedOperationException("Compute shaders are not supported on this system");
        }
        this.bindTextureUnit(unit);
        glBindImageTexture(unit, id(), 0, false, 0,  access, type);
    }

    public void bindTextureUnit(int unit) {
        ensureAllocated();
        GLTextureState.bind(this.type, unit, id());
    }

    public void enableClamp() {
        setWrap(Wrap.CLAMP_TO_EDGE);
    }

    public void pixelate() {
        setFilter(Filter.NEAREST);
    }

    public void setFilter(Filter filter) {
        setMinFilter(filter);
        setMagFilter(filter);
    }

    public void setMagFilter(Filter filter) {
        setTextureParameter(GL_TEXTURE_MAG_FILTER, filter.value());
    }

    public void setMinFilter(Filter filter) {
        setTextureParameter(GL_TEXTURE_MIN_FILTER, filter.value());
    }

    public void setWrap(Wrap wrap) {
        setTextureParameter(GL_TEXTURE_WRAP_S, wrap.value());
        setTextureParameter(GL_TEXTURE_WRAP_T, wrap.value());
        if (this.type.samplesAcrossZ()) {
            setTextureParameter(GL_TEXTURE_WRAP_R, wrap.value());
        }
    }

    public void setTextureParameter(int type, int filter) {
        ensureNotBindless();
        if (DSA) {
            GL45.glTextureParameteri(this.id(), type, filter);
        } else {
            this.bindTextureUnit(0);
            glTexParameteri(this.type.target(), type, filter);
        }
    }

    public void generateMipmaps() {
        ensureNotBindless();
        if (DSA) {
            GL45.glGenerateTextureMipmap(this.id());
        } else {
            this.bindTextureUnit(0);
            glGenerateMipmap(this.type.target());
        }
    }

    public boolean isBindless() {
        return this.handleData.handle != -1;
    }

    protected void ensureNotBindless() {
        if (isBindless()) {
            makeNonResident();
        }
    }

    public void makeResident() {
        if (isBindless()) {
            return;
        }
        if (!Capabilities.get().bindlessTexture()) {
            throw new UnsupportedOperationException("Bindless textures are not supported on this system");
        }
        this.bindTextureUnit(0);
        if (!GL11.glIsTexture(this.id())) {
            System.err.println("Invalid texture ID while making resident: " + this.id());
        }
        this.handleData.handle = ARBBindlessTexture.glGetTextureHandleARB(this.id());
        ARBBindlessTexture.glMakeTextureHandleResidentARB(this.handleData.handle);
        if (!ARBBindlessTexture.glIsTextureHandleResidentARB(this.handleData.handle)
                || this.handleData.handle == -1
                || this.handleData.handle == 0
        ) {
            System.err.println("Could not make texture resident: " + this.id());
        }
    }

    public void makeNonResident() {
        ARBBindlessTexture.glMakeTextureHandleNonResidentARB(this.handleData.handle);
        this.handleData.handle = -1;
    }


    /// dont store, since the underlying data may be deleted when this object no longer exists
    public int getTemporaryIdentifier() {
        return this.handleData.id;
    }

    /// dont store, since the underlying data may be deleted when this object no longer exists
    public long getTemporaryHandle() {
        return this.handleData.handle;
    }

    /// protected short version
    protected int id() {
        return getTemporaryIdentifier();
    }
    /// protected short version
    protected int handle() {
        return getTemporaryIdentifier();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Texture texture)) {
            return false;
        }
        return this.handleData.equals(texture.handleData);
    }

    @Override
    public int hashCode() {
        return this.handleData.hashCode();
    }

    @Getter
    public enum Wrap {
        REPEAT(GL_REPEAT),
        CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
        MIRRORED_REPEAT(GL_MIRRORED_REPEAT),
        CLAMP_TO_BORDER(GL_CLAMP_TO_BORDER),
        MIRROR_CLAMP_TO_EDGE(GL_MIRROR_CLAMP_TO_EDGE);


        private final int value;

        Wrap(int value) {
            this.value = value;
        }
    }

    @Getter
    public enum Filter {
        NEAREST(GL_NEAREST),
        LINEAR(GL_LINEAR);

        private final int value;

        Filter(int value) {
            this.value = value;
        }
    }

    @EqualsAndHashCode
    private static class TextureHandleData implements GCResource {
        private final int id;
        private final TextureType type;
        private long handle;

        TextureHandleData(
                TextureType type,
                int id
        ) {
            this.type = type;
            this.id = id;
            this.handle = -1;
        }

        @Override
        public void freeResource() {
            if (this.handle != -1) {
                ARBBindlessTexture.glMakeTextureHandleNonResidentARB(this.handle);
            }
            glDeleteTextures(this.id);
            GLTextureState.resetSlots(this.type, this.id);
        }
    }


}

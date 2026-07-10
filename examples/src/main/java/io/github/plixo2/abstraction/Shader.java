package io.github.plixo2.abstraction;

import io.github.plixo2.abstraction.texture.DisplayableTexture2D;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.ARBBindlessTexture;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import static org.lwjgl.opengl.GL20C.*;

public class Shader extends GPUResource  {

    private final ShaderHandleData handleData;

    protected Shader(int program) {
        this.handleData = new ShaderHandleData(program);
        this.remover = GLResourceManagement.add(this, this.handleData);
    }

    public Shader(String vertex, String fragment) {
        this(glCreateProgram());
        var program = this.id();
        int vert = 0;
        int frag = 0;
        try {
            vert = compileShader(vertex, GL_VERTEX_SHADER);
            frag = compileShader(fragment, GL_FRAGMENT_SHADER);
            glAttachShader(program, vert);
            glAttachShader(program, frag);
            glLinkProgram(program);
        } finally {
            if (vert != 0) {
                glDeleteShader(vert);
            }
            if (frag != 0) {
                glDeleteShader(frag);
            }
        }
        check(program);
    }
    protected void check(int id) {
        final int linked = glGetProgrami(id, GL_LINK_STATUS);
        final String programLog = glGetProgramInfoLog(id);
        if (!programLog.trim().isEmpty()) {
            System.err.println(programLog);
            throw new RuntimeException(programLog);
        }
        if (linked == 0) throw new AssertionError("Could not link program");
    }


    @SneakyThrows
    public static Shader fromResource(String folder) {
        var vertex_ = IOUtils.resourceToString(folder + "/vertex.glsl", Charset.defaultCharset());
        var fragment_ = IOUtils.resourceToString(folder + "/fragment.glsl", Charset.defaultCharset());
        return new Shader(vertex_, fragment_);
    }


    public static int compileShader(String src, int type) {
        final int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        final int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
        final String log = glGetShaderInfoLog(shader);
        if (!log.trim().isEmpty()) {
            System.err.println(log);
            throw new RuntimeException(log);
        }
        if (compiled == 0) throw new AssertionError("Could not compile shader: " + src);

        return shader;
    }

    public <T> Uniform<T> uniform(String str, Class<T> cls) {
        bind();
        var location = glGetUniformLocation(this.handleData.id, str);
        return new Uniform<>(str, cls, location);
    }

    static int boundProgram = -1;

    public void bind() {
        ensureAllocated();
        var id = this.handleData.id;
        if (boundProgram != id) {
            boundProgram = id;
            glUseProgram(id);
        }
    }

    public static void unbind() {
        if (boundProgram != 0) {
            boundProgram = 0;
            glUseProgram(0);
        }
    }
    protected int id() {
        return this.handleData.id;
    }

    /// dont store, since the underlying data may be deleted when this object no longer exists
    public int getTemporaryProgramIdentifier() {
        return this.handleData.id;
    }


    private interface ShaderValueLoader<T> {
        /// Loads the given object and returns the next object to cache
        Object load(T object, Object cache, int location);
    }


    public class Uniform<T> {
        private final String name;
        private final Class<T> cls;
        @Getter
        private final int location;
        private final ShaderValueLoader<T> loader;
        private @Nullable Object cachedValue = null;

        public Uniform(
                String name,
                Class<T> cls,
                int location
        ) {
            this.name = name;
            this.cls = cls;
            this.location = location;
            this.loader = Shader.findShaderValueLoader(cls);
        }

        public void loadCached(T obj) {
            if (this.location == -1) {
                this.cachedValue = null;
                return;
            }
            Shader.this.bind();
            this.cachedValue = this.loader.load(obj, this.cachedValue, this.location);
        }

        public Uniform<T> validate() {
            if (this.location == -1) {
                throw new RuntimeException(
                        "Uniform '" + this.name + "'"
                        + " of type '" + this.cls.getSimpleName()
                        + "' not found in shader (Might exist, but is unused)"
                );
            }
            return this;
        }
    }




    public record Attribute(int size, int byte_size, int type) {
        public static Attribute Float(int size) { return new Attribute(size, 4 * size, GL_FLOAT); };
        public static Attribute UInt(int size) { return new Attribute(size, 4 * size, GL_UNSIGNED_INT); }
        public static Attribute Int(int size) { return new Attribute(size, 4 * size, GL_INT); }
        public static Attribute UByte(int size) { return new Attribute(size, size, GL_UNSIGNED_BYTE); }
        public static Attribute Byte(int size) { return new Attribute(size, size, GL_BYTE); }
        public static Attribute Short(int size) { return new Attribute(size, 2 * size, GL_SHORT); }
        public static Attribute UShort(int size) { return new Attribute(size, 2 * size, GL_UNSIGNED_SHORT); }



        public boolean isByte() {
            return this.type == GL_BYTE || this.type == GL_UNSIGNED_BYTE;
        }
        public boolean isFloat() {
            return this.type == GL_FLOAT;
        }
        public boolean isInt() {
            return this.type == GL_INT || this.type == GL_UNSIGNED_INT;
        }
        public boolean isShort() {
            return this.type == GL_SHORT || this.type == GL_UNSIGNED_SHORT;
        }
        public boolean useAttribInteger() {
            return isShort() || isInt() || isByte();
        }

        public int typeSize() {
            return switch (this.type) {
                case GL_FLOAT, GL_UNSIGNED_INT, GL_INT -> 4;
                case GL_UNSIGNED_SHORT, GL_SHORT -> 2;
                case GL_UNSIGNED_BYTE, GL_BYTE -> 1;
                default -> throw new IllegalStateException("Unexpected value: " + this.type);
            };
        }

    }

    private record ShaderHandleData(int id) implements GCResource {

        @Override
        public void freeResource() {
            Shader.boundProgram = -1;
            glDeleteProgram(this.id);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ShaderValueLoader<T> findShaderValueLoader(Class<T> cls) {
        ShaderValueLoader<?> loader = SHADER_VALUE_LOADERS.get(cls);
        if (loader == null) {
            for (Map.Entry<Class<?>, ShaderValueLoader<?>> entry : SHADER_VALUE_LOADERS.entrySet()) {
                if (entry.getKey().isAssignableFrom(cls)) {
                    loader = entry.getValue();
                    break;
                }
            }
        }
        if (loader == null) {
            throw new UnsupportedOperationException("Unsupported type: " + cls);
        }
        return (ShaderValueLoader<T>) loader;
    }

    private static final Map<Class<?>, ShaderValueLoader<?>> SHADER_VALUE_LOADERS = createShaderValueLoaders();

    static FloatBuffer matrix4fBuffer = MemoryUtil.memAllocFloat(16);
    static FloatBuffer matrix2fBuffer = MemoryUtil.memAllocFloat(4);
    static FloatBuffer matrix3fBuffer = MemoryUtil.memAllocFloat(9);
    static float[] colorBuffer = new float[4];

    private static Map<Class<?>, ShaderValueLoader<?>> createShaderValueLoaders() {
        var loaders = new HashMap<Class<?>, ShaderValueLoader<?>>() {
            private <T> void put(Class<T> cls, ShaderValueLoader<T> loader) {
                super.put(cls, loader);
            }
        };

        loaders.put(DisplayableTexture2D.class, (object, cache, location) -> {
            var realTex2D = object.get();
            realTex2D.makeResident();
            var handle = realTex2D.getTemporaryHandle();
            if (!Objects.equals(cache, handle)) {
                ARBBindlessTexture.glUniformHandleui64ARB(location, handle);
            }
            return handle;
        });

        loaders.put(Matrix4f[].class, (object, cache, location) -> {
            for (int i = 0; i < object.length; i++) {
                object[i].get(matrix4fBuffer);
                glUniformMatrix4fv(location + i, false, matrix4fBuffer);
            }
            return null;
        });

        loaders.put(SamplerArray.SetupElement.class, (object, cache, location) -> {
            if (cache instanceof Integer size && size == object.size) {
                return object.size;
            }
            int[] units = new int[object.size];
            for (int i = 0; i < units.length; i++) {
                units[i] = i;
            }
            glUniform1iv(location, units);
            return object.size;
        });

        loaders.put(float[].class, (object, cache, location) -> {
            if (cache instanceof float[] cachedFloats && cachedFloats.length == object.length) {
                for (int i = 0; i < cachedFloats.length; i++) {
                    if (cachedFloats[i] != object[i]) {
                        // update cache
                        for (; i < cachedFloats.length; i++) {
                            cachedFloats[i] = object[i];
                        }
                        glUniform1fv(location, object);
                        return cachedFloats;
                    }
                }
                return cachedFloats;
            }
            glUniform1fv(location, object);
            float[] newCache = new float[object.length];
            System.arraycopy(object, 0, newCache, 0, object.length);
            return newCache;
        });

        loaders.put(int[].class, (object, cache, location) -> {
            if (cache instanceof int[] cachedInts && cachedInts.length == object.length) {
                for (int i = 0; i < cachedInts.length; i++) {
                    if (cachedInts[i] != object[i]) {
                        // update cache
                        for (; i < cachedInts.length; i++) {
                            cachedInts[i] = object[i];
                        }
                        glUniform1iv(location, object);
                        return cachedInts;
                    }
                }
                return cachedInts;
            }
            glUniform1iv(location, object);
            int[] newCache = new int[object.length];
            System.arraycopy(object, 0, newCache, 0, object.length);
            return newCache;
        });

        loaders.put(Boolean.class, (object, cache, location) -> {
            if (!Objects.equals(cache, object)) {
                glUniform1i(location, object ? 1 : 0);
            }
            return object;
        });

        loaders.put(Float.class, (object, cache, location) -> {
            if (!Objects.equals(cache, object)) {
                glUniform1f(location, object);
            }
            return object;
        });

        loaders.put(Integer.class, (object, cache, location) -> {
            if (!Objects.equals(cache, object)) {
                glUniform1i(location, object);
            }
            return object;
        });

        loaders.put(HDRColor.class, (object, cache, location) -> {
            if (!Objects.equals(cache, object)) {
                glUniform4fv(location, object.putRGBA(colorBuffer));
            }
            return object;
        });

        loaders.put(Color.class, (object, cache, location) -> {
            if (!Objects.equals(cache, object)) {
                glUniform4fv(location, object.putRGBA(colorBuffer));
            }
            return object;
        });

        loaders.put(Matrix4f.class, (object, cache, location) -> {
            if (cache instanceof Matrix4f cachedMatrix) {
                if (!cachedMatrix.equals(object)) {
                    cachedMatrix.set(object);
                    glUniformMatrix4fv(location, false, object.get(matrix4fBuffer));
                }
                return cache;
            }
            glUniformMatrix4fv(location, false, object.get(matrix4fBuffer));
            return new Matrix4f(object);
        });

        loaders.put(Matrix3f.class, (object, cache, location) -> {
            if (cache instanceof Matrix3f cachedMatrix) {
                if (!cachedMatrix.equals(object)) {
                    cachedMatrix.set(object);
                    glUniformMatrix3fv(location, false, object.get(matrix3fBuffer));
                }
                return cache;
            }
            glUniformMatrix3fv(location, false, object.get(matrix3fBuffer));
            return new Matrix3f(object);
        });

        loaders.put(Matrix2f.class, (object, cache, location) -> {
            if (cache instanceof Matrix2f cachedMatrix) {
                if (!cachedMatrix.equals(object)) {
                    cachedMatrix.set(object);
                    glUniformMatrix2fv(location, false, object.get(matrix2fBuffer));
                }
                return cache;
            }
            glUniformMatrix2fv(location, false, object.get(matrix2fBuffer));
            return new Matrix2f(object);
        });

        loaders.put(Vector3f.class, (object, cache, location) -> {
            if (cache instanceof Vector3f cachedVector) {
                if (!cachedVector.equals(object)) {
                    cachedVector.set(object);
                    glUniform3f(location, object.x, object.y, object.z);
                }
                return cache;
            }
            glUniform3f(location, object.x, object.y, object.z);
            return new Vector3f(object);
        });

        loaders.put(Vector2f.class, (object, cache, location) -> {
            if (cache instanceof Vector2f cachedVector) {
                if (!cachedVector.equals(object)) {
                    cachedVector.set(object);
                    glUniform2f(location, object.x, object.y);
                }
                return cache;
            }
            glUniform2f(location, object.x, object.y);
            return new Vector2f(object);
        });

        loaders.put(Vector4f.class, (object, cache, location) -> {
            if (cache instanceof Vector4f cachedVector) {
                if (!cachedVector.equals(object)) {
                    cachedVector.set(object);
                    glUniform4f(location, object.x, object.y, object.z, object.w);
                }
                return cache;
            }
            glUniform4f(location, object.x, object.y, object.z, object.w);
            return new Vector4f(object);
        });

        loaders.put(Vector2i.class, (object, cache, location) -> {
            if (cache instanceof Vector2i cachedVector) {
                if (!cachedVector.equals(object)) {
                    cachedVector.set(object);
                    glUniform2i(location, object.x, object.y);
                }
                return cache;
            }
            glUniform2i(location, object.x, object.y);
            return new Vector2i(object);
        });

        loaders.put(Vector3i.class, (object, cache, location) -> {
            if (cache instanceof Vector3i cachedVector) {
                if (!cachedVector.equals(object)) {
                    cachedVector.set(object);
                    glUniform3i(location, object.x, object.y, object.z);
                }
                return cache;
            }
            glUniform3i(location, object.x, object.y, object.z);
            return new Vector3i(object);
        });

        loaders.put(Vector4i.class, (object, cache, location) -> {
            if (cache instanceof Vector4i cachedVector) {
                if (!cachedVector.equals(object)) {
                    cachedVector.set(object);
                    glUniform4i(location, object.x, object.y, object.z, object.w);
                }
                return cache;
            }
            glUniform4i(location, object.x, object.y, object.z, object.w);
            return new Vector4i(object);
        });

        return loaders;
    }


}

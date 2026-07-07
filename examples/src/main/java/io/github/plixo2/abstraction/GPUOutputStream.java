package io.github.plixo2.abstraction;

import io.github.plixo2.abstraction.texture.IOTexture2D;
import io.github.plixo2.abstraction.texture.Texture2D;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public interface GPUOutputStream {

    void putByte(byte b);
    void putShort(short s);
    void putFloat(float f);
    void putInt(int i);
    void putLong(long l);
    void putMatrix(Matrix4f matrix4f);
    void putVector3f(float x, float y, float z);
    void putVector3f(Vector3f vector);
    void putVector4f(Vector4f vector);
    void putVector4f(float x, float y, float z, float w);
    void putVector2f(float x, float y);
    void putVector2f(Vector2f vector);
    void putColor(Color color);
    void put(ByteBuffer buffer);

    default void putTexture(Texture2D texture) {
        texture.makeResident();
        putLong(texture.getTemporaryHandle());
    }
    default void putTexture(IOTexture2D texture) {
        putTexture(texture.get());
    }

    interface Backed extends GPUOutputStream {
        GPUOutputStream stream();

        @Override
        default void putByte(byte b) {
            stream().putByte(b);
        }

        @Override
        default void putShort(short s) {
            stream().putShort(s);
        }

        @Override
        default void putFloat(float f) {
            stream().putFloat(f);
        }

        @Override
        default void putInt(int i) {
            stream().putInt(i);
        }

        @Override
        default void putLong(long l) {
            stream().putLong(l);
        }

        @Override
        default void putMatrix(Matrix4f matrix4f) {
            stream().putMatrix(matrix4f);
        }

        @Override
        default void putVector3f(float x, float y, float z) {
            stream().putVector3f(x, y, z);
        }

        @Override
        default void putVector3f(Vector3f vector) {
            stream().putVector3f(vector);
        }

        @Override
        default void putVector4f(Vector4f vector) {
            stream().putVector4f(vector);
        }

        @Override
        default void putVector4f(float x, float y, float z, float w) {
            stream().putVector4f(x, y, z, w);
        }

        @Override
        default void putVector2f(float x, float y) {
            stream().putVector2f(x, y);
        }

        @Override
        default void putVector2f(Vector2f vector) {
            stream().putVector2f(vector);
        }

        @Override
        default void putColor(Color color) {
            stream().putColor(color);
        }

        @Override
        default void put(ByteBuffer buffer) {
            stream().put(buffer);
        }
    }
}

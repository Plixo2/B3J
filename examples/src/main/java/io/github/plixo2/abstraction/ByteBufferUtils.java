/*  
 * Copyright LWJGL. All rights reserved.    
 * License terms: https://www.lwjgl.org/license 
 */
package io.github.plixo2.abstraction;

import org.lwjgl.BufferUtils;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * From LWJGL3 examples
 */
public class ByteBufferUtils {

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new IOException("Classpath resource not found: " + resource);
        }

        var file = new File(url.getFile());
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            var buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            fc.close();
            fis.close();
            return buffer;
        }

        var buffer = BufferUtils.createByteBuffer(bufferSize);

        var source = url.openStream();
        if (source == null) {
            throw new FileNotFoundException(resource);
        }

        try (source) {
            byte[] buf = new byte[8192];
            while (true) {
                int bytes = source.read(buf, 0, buf.length);

                if (bytes == -1) {
                    break;
                }

                if (buffer.remaining() < bytes) {
                    var newCapacity = Math.max(
                            buffer.capacity() * 2,
                            buffer.capacity() - buffer.remaining() + bytes
                    );
                    buffer = resizeBuffer(buffer, newCapacity);
                }

                buffer.put(buf, 0, bytes);
            }
            buffer.flip();
        }

        return buffer;
    }

    public static ByteBuffer inputSteamToByteBuffer(Path path) throws IOException {
        try (var fis = new FileInputStream(path.toFile())) {
            try (var fc = fis.getChannel()) {
                return fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size());
            }
        }
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }


    public static class ByteBufferBackedInputStream extends InputStream {
        private final ByteBuffer byteBuffer;

        public ByteBufferBackedInputStream(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        @Override
        public int read() {
            if (!this.byteBuffer.hasRemaining()) {
                return -1; // End of stream
            }
            return this.byteBuffer.get() & 0xFF;
        }

        @Override
        public int read(byte[] bytes, int off, int len) {
            if (!this.byteBuffer.hasRemaining()) {
                return -1; // End of stream
            }
            len = Math.min(len, this.byteBuffer.remaining());
            this.byteBuffer.get(bytes, off, len);
            return len;
        }
    }
}

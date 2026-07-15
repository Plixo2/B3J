package io.github.plixo2.box3d.internal;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Objects;

public final class B3JUtil {

    private static @Nullable PrintStream logStream = null;

    private B3JUtil() {}

    /// @throws IllegalArgumentException if the value is not a valid unsigned 32-bit integer
    public static int assertU32(long value, String name) {
        checkUnsigned(name, value, U32.MAX, 32);
        return (int) value;
    }

    public static boolean isU32(long value) {
        return value >= 0 && value <= U32.MAX;
    }

    /// @throws IllegalArgumentException if the value is not a valid unsigned 16-bit integer
    public static byte assertU8(int value, String name) {
        checkUnsigned(name, value, U8.MAX, 8);
        return (byte) value;
    }


    public static boolean isU8(int value) {
        return isU8((long) value);
    }

    public static boolean isU8(short value) {
        return isU8((long) value);
    }

    public static boolean isU8(long value) {
        return value >= 0 && value <= U8.MAX;
    }

    /// @throws IllegalArgumentException if the value is not a valid unsigned 16-bit integer
    public static short assertU16(int value, String name) {
        checkUnsigned(name, value, U16.MAX, 16);
        return (short) value;
    }

    public static boolean isU16(int value) {
        return isU16((long) value);
    }
    public static boolean isU16(long value) {
        return value >= 0 && value <= U16.MAX;
    }

    public static int toUnsignedInt(byte value) {
        return Byte.toUnsignedInt(value);
    }
    public static int toUnsignedInt(short value) {
        return Short.toUnsignedInt(value);
    }

    public static long toUnsignedLong(byte value) {
        return Byte.toUnsignedInt(value);
    }
    public static long toUnsignedLong(short value) {
        return Short.toUnsignedInt(value);
    }
    public static long toUnsignedLong(int value) {
        return Integer.toUnsignedLong(value);
    }



    /// @throws IllegalArgumentException if the value is out of range for the enum
    public static <T extends Enum<T>> T enumValue(Class<T> enumClass, int ordinal) {
        var values = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException(
                    "Invalid ordinal for enum " + enumClass.getSimpleName() + ": " + ordinal
            );
        }
        return values[ordinal];
    }

    /// @return the enum value corresponding to the ordinal, or the default value if the ordinal is out of range
    @Contract("_, _, !null -> !null")
    public static <T extends Enum<T>> T enumValueOrElse(Class<T> enumClass, int ordinal, @Nullable T defaultValue) {
        var values = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            return defaultValue;
        }
        return values[ordinal];
    }

    public static <T extends Enum<T>> long mask(T a) {
        return 1L << ensureBit(a);
    }
    public static <T extends Enum<T>> long mask(T a, T b) {
        return 1L << ensureBit(a)
                | 1L << ensureBit(b);
    }
    public static <T extends Enum<T>> long mask(T a, T b, T c) {
        return 1L << ensureBit(a)
                | 1L << ensureBit(b)
                | 1L << ensureBit(c);
    }
    public static <T extends Enum<T>> long mask(T a, T b, T c, T d) {
        return 1L << ensureBit(a)
                | 1L << ensureBit(b)
                | 1L << ensureBit(c)
                | 1L << ensureBit(d);
    }
    public static <T extends Enum<T>> long mask(T a, T b, T c, T d, T e) {
        return 1L << ensureBit(a)
                | 1L << ensureBit(b)
                | 1L << ensureBit(c)
                | 1L << ensureBit(d)
                | 1L << ensureBit(e);
    }
    public static <T extends Enum<T>> long mask(T a, T b, T c, T d, T e, T f) {
        return 1L << ensureBit(a)
                | 1L << ensureBit(b)
                | 1L << ensureBit(c)
                | 1L << ensureBit(d)
                | 1L << ensureBit(e)
                | 1L << ensureBit(f);
    }

    @SafeVarargs
    public static <T extends Enum<T>> long mask(T... values) {
        long mask = 0;
        for (var value : values) {
            mask |= 1L << ensureBit(value);
        }
        return mask;
    }

    public static <T extends Enum<T>> long mask(EnumSet<T> values) {
        long mask = 0;
        for (var value : values) {
            mask |= 1L << ensureBit(value);
        }
        return mask;
    }

    public static long mask(BitSet bits) {
        if (bits.length() > Long.SIZE) {
            throw new ArithmeticException("BitSet exceeds 64 bits");
        }

        long mask = 0L;

        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            mask |= 1L << i;
        }

        return mask;
    }

    private static int ensureBit(Enum<?> e) {
        var bit = e.ordinal();
        if (bit >= Long.SIZE) {
            throw new ArithmeticException("Enum exceeds 64 values");
        }
        return bit;
    }



    public static MemorySegment allocNullString(SegmentAllocator arena, @Nullable String str) {
        if (str == null) {
            return MemorySegment.NULL;
        } else {
            return arena.allocateFrom(str);
        }
    }

    public static @Nullable String getNullString(MemorySegment segment) {
        if (segment.address() == 0) {
            return null;
        } else {
            return segment.getString(0);
        }
    }
    public static String getNullString(MemorySegment segment, String defaultValue) {
        if (segment.address() == 0) {
            return defaultValue;
        } else {
            return segment.getString(0);
        }
    }

    public static MemorySegment ensureOffHeap(SegmentAllocator arena, MemorySegment segment) {
        if (segment.isNative()) {
            return segment;
        }

        var newSegment = arena.allocate(segment.byteSize());
        newSegment.copyFrom(segment);
        return newSegment;
    }


    private static void checkUnsigned(String name, long value, long max, int bit) {
        if (value >= 0 && value <= max) {
            return;
        }

        var exceptionMessage =
                "'" + name + "'"
                + " does not fit into a "
                + "unsigned " + bit + "-bit integer: ";

        if (value < 0) {
            exceptionMessage += value + " < 0";
        } else {
            exceptionMessage += value + " > " + max;
        }
        throw new IllegalArgumentException(exceptionMessage);
    }

    public static void setLogStream(@Nullable PrintStream stream) {
        logStream = stream;
    }

    public static void unhandledCallbackException(Exception e) {
        var stream = logStream;
        if (stream == null) {
            return;
        }

        try {
            stream.println("[B3J]: Unhandled exception in callback:");
            e.printStackTrace(stream);
        } catch(Exception _) {
            // ignore bad stream
        }
    }

    public static void log(String message) {
        var stream = logStream;
        if (stream == null) {
            return;
        }

        try {
            stream.println("[B3J]: " + message);
        } catch(Exception _) {
            // ignore bad stream
        }
    }


}

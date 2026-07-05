package io.github.plixo2.box3d.internal;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

public final class Internal {
    public static final long U64_MAX = -1L;
    public static final long U32_MAX = 0xFFFFFFFFL;
    public static final long U16_MAX = 0xFFFF;
    public static final long U8_MAX = 0xFF;


    private Internal() {}


    /// @throws IllegalArgumentException if the value is not a valid unsigned 32-bit integer
    public static int assertU32(long value, String name) {
        checkUnsigned(name, value, U32_MAX, 32);
        return (int) value;
    }

    public static boolean isU32(long value) {
        return value >= 0 && value <= U32_MAX;
    }

    /// @throws IllegalArgumentException if the value is not a valid unsigned 16-bit integer
    public static byte assertU8(int value, String name) {
        checkUnsigned(name, value, U8_MAX, 8);
        return (byte) value;
    }

    public static boolean isU8(int value) {
        return value >= 0 && value <= U8_MAX;
    }

    /// @throws IllegalArgumentException if the value is not a valid unsigned 16-bit integer
    public static short assertU16(int value, String name) {
        checkUnsigned(name, value, U16_MAX, 16);
        return (short) value;
    }

    public static boolean isU16(int value) {
        return value >= 0 && value <= U16_MAX;
    }


    @Contract("_, _, !null -> !null")
    public static <T extends Enum<T>> T enumFromCode(Class<T> enumClass, int ordinal, @Nullable T defaultValue) {
        var values = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            return defaultValue;
        }
        return values[ordinal];
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


}

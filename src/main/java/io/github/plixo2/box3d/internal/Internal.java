package io.github.plixo2.box3d.internal;


import io.github.plixo2.box3d.AssertFcn;
import lombok.Lombok;
import lombok.SneakyThrows;
import org.box2d.box3d.b3AssertFcn;
import org.box2d.box3d.box3d_h;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.Objects;

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


    public static void setAssertFcn() {
        setAssertFcn((condition, fileName, lineNumber) -> {
            throw new AssertionError(assertionError(condition, fileName, lineNumber));
        });
    }

    public static void setAssertFcn(AssertFcn fcn) {
        class Holder {
            private final static Object lock = new Object();

            private static @Nullable b3AssertFcn.Function fcn;
            private static @Nullable MemorySegment assertFcnSegment;
        }
        Objects.requireNonNull(fcn, "fcn");
        synchronized (Holder.lock) {
            Holder.fcn = (condition, fileName, lineNumber) -> {
                var conditionString = condition.getString(0);
                var fileString = fileName.getString(0);
                try {
                    fcn.onFailure(conditionString, fileString, lineNumber);
                } catch (Exception e) {
                    unhandledCallbackException(e);
                }
                throw new AssertionError(assertionError(conditionString, fileString, lineNumber));
            };
            Holder.assertFcnSegment = b3AssertFcn.allocate(Holder.fcn, Arena.ofAuto());
            box3d_h.b3SetAssertFcn(Holder.assertFcnSegment);
        }
    }

    private static String assertionError(String condition, String fileName, int lineNumber) {
        return "[B3J] Assertion failed: " + condition + " at " + fileName + ":" + lineNumber;
    }

    public static void unhandledCallbackException(Exception e) {
        System.err.println("[B3J] Unhandled exception in callback:");
        e.printStackTrace(System.err);
    }

}

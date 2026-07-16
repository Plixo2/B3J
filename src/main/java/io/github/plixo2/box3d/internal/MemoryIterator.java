package io.github.plixo2.box3d.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

/// Be careful what conversions you pick for primitives
///
/// This iterator will always return the same object, but with different values.
/// See [#get(int)] to get the object with the values at the given index.
/// See [#get(Function, int)] to get a unique object with the values at the given index.
/// See [#collect] to create a list of unique objects.
public final class MemoryIterator<T> extends MemoryIteratorBase<T> {

    private final T element;

    private @Nullable final BiConsumer<T, MemorySegment> set;   // pass a slice
    private @Nullable final OffsetConsumer<T> setWithOffset;    // pass segment and offset

    public MemoryIterator(
            T element,
            MemorySegment segment,
            long bytesPerElement,
            BiConsumer<T, MemorySegment> set
    ) {
        super(segment, bytesPerElement);
        this.element = element;
        this.set = Objects.requireNonNull(set);
        this.setWithOffset = null;
    }

    public MemoryIterator(
            T element,
            MemorySegment segment,
            long count,
            long bytesPerElement,
            BiConsumer<T, MemorySegment> set
    ) {
        super(segment, count, bytesPerElement);
        this.element = element;
        this.set = Objects.requireNonNull(set);
        this.setWithOffset = null;
    }

    public MemoryIterator(
            T element,
            MemorySegment segment,
            long bytesPerElement,
            OffsetConsumer<T> set
    ) {
        super(segment, bytesPerElement);
        this.element = element;
        this.set = null;
        this.setWithOffset = Objects.requireNonNull(set);
    }

    public MemoryIterator(
            T element,
            MemorySegment segment,
            long count,
            long bytesPerElement,
            OffsetConsumer<T> set
    ) {
        super(segment, count, bytesPerElement);
        this.element = element;
        this.set = null;
        this.setWithOffset = Objects.requireNonNull(set);
    }

    /// Creates a list of unique objects
    public List<T> collect(Function<T, T> clone) {
        var list = new ArrayList<T>(this.length);

        for (int i = 0; i < this.length; i++) {
            list.add(clone.apply(getUnchecked(i)));
        }

        return list;
    }

    public T get(int index) {
        checkIndex(index);
        return getUnchecked(index);
    }

    public T get(Function<T, T> clone, int index) {
        checkIndex(index);
        return clone.apply(getUnchecked(index));
    }

    private T getUnchecked(int index) {
        if (this.set != null) {
            var seg = this.segment.asSlice(index * this.bytesPerElement, this.bytesPerElement);
            this.set.accept(this.element, seg);
        } else {
            assert this.setWithOffset != null;

            var offset = index * this.bytesPerElement;
            this.setWithOffset.accept(this.element, this.segment, offset);
        }
        return this.element;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < this.length; i++) {
            action.accept(getUnchecked(i));
        }
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<>() {
            private final int length = MemoryIterator.this.length;

            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < this.length;
            }

            @Override
            public T next() {
                return MemoryIterator.this.get(this.index++);
            }
        };
    }

    public static abstract sealed class OfPrimitive<T> extends MemoryIteratorBase<T> {

        OfPrimitive(
                MemorySegment segment,
                long bytesPerElement
        ) {
            super(segment, bytesPerElement);
        }

        OfPrimitive(
                MemorySegment segment,
                long count,
                long bytesPerElement
        ) {
            super(segment, bytesPerElement, count);
        }

        abstract T getUnchecked(int index);

        @Override
        public void forEach(Consumer<? super T> action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(getUnchecked(i));
            }
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return new Iterator<>() {
                private final int length = OfPrimitive.this.length;
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return this.index < this.length;
                }

                @Override
                public T next() {
                    checkIndex(this.index);
                    return OfPrimitive.this.getUnchecked(this.index++);
                }
            };
        }

    }


    public static final class OfLong extends OfPrimitive<Long> {

        public OfLong(MemorySegment segment) {
            super(segment, Long.BYTES);
        }
        public OfLong(MemorySegment segment, long count) {
            super(segment, count, Long.BYTES);
        }

        public long getLong(int index) {
            checkIndex(index);
            return this.segment.getAtIndex(ValueLayout.JAVA_LONG, index);
        }

        public long[] collect() {
            return this.segment.toArray(ValueLayout.JAVA_LONG);
        }

        public void forEach(LongConsumer action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(getLong(i));
            }
        }

        @Override
        protected Long getUnchecked(int index) {
            return this.segment.getAtIndex(ValueLayout.JAVA_LONG, index);
        }
    }

    public static final class OfU64 extends OfPrimitive<Long> {
        public OfU64(MemorySegment segment) {
            super(segment, Long.BYTES);
        }
        public OfU64(MemorySegment segment, long count) {
            super(segment, count, Long.BYTES);
        }

        public @Unsigned long getLong(int index) {
            checkIndex(index);
            return this.segment.getAtIndex(ValueLayout.JAVA_LONG, index);
        }

        public long getLongExact(int index) {
            var v = getLong(index);
            if (v < 0) {
                throw new ArithmeticException("long overflow");
            }
            return v;
        }

        public @Unsigned long[] collect() {
            return this.segment.toArray(ValueLayout.JAVA_LONG);
        }

        public void forEach(LongConsumer action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(getLong(i));
            }
        }

        @Override
        protected Long getUnchecked(int index) {
            return this.segment.getAtIndex(ValueLayout.JAVA_LONG, index);
        }

    }

    public static final class OfInt extends OfPrimitive<Integer> {

        public OfInt(MemorySegment segment) {
            super(segment, Integer.BYTES);
        }
        public OfInt(MemorySegment segment, long count) {
            super(segment, count, Integer.BYTES);
        }

        public int getInt(int index) {
            checkIndex(index);
            return this.segment.getAtIndex(ValueLayout.JAVA_INT, index);
        }

        public int[] collect() {
            return this.segment.toArray(ValueLayout.JAVA_INT);
        }

        public void forEach(IntConsumer action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(getInt(i));
            }
        }

        @Override
        protected Integer getUnchecked(int index) {
            return this.segment.getAtIndex(ValueLayout.JAVA_INT, index);
        }

    }

    public static final class OfU32 extends OfPrimitive<Integer> {
        public OfU32(MemorySegment segment) {
            super(segment, Integer.BYTES);
        }
        public OfU32(MemorySegment segment, long count) {
            super(segment, count, Integer.BYTES);
        }

        /// performs [Integer#toUnsignedLong]
        public @Unsigned long getToUnsignedLong(int index) {
            checkIndex(index);
            return Integer.toUnsignedLong(this.segment.getAtIndex(ValueLayout.JAVA_INT, index));
        }
        public @Unsigned int getAsInt(int index) {
            checkIndex(index);
            return this.segment.getAtIndex(ValueLayout.JAVA_INT, index);
        }
        public int getAsIntExact(int index) {
            return Math.toIntExact(getToUnsignedLong(index));
        }

        /// performs [Integer#toUnsignedLong]
        public @Unsigned long[] collectToUnsignedLong() {
            var array = new long[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = Integer.toUnsignedLong(this.segment.getAtIndex(ValueLayout.JAVA_INT, i));
            }
            return array;
        }

        public @Unsigned int[] collectAsInt() {
            var array = new int[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = this.segment.getAtIndex(ValueLayout.JAVA_INT, i);
            }
            return array;
        }

        public void forEach(IntConsumer action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(getAsInt(i));
            }
        }

        @Override
        protected Integer getUnchecked(int index) {
            return this.segment.getAtIndex(ValueLayout.JAVA_INT, index);
        }

    }

    public static final class OfU16 extends OfPrimitive<Short> {
        public OfU16(MemorySegment segment) {
            super(segment, Short.BYTES);
        }
        public OfU16(MemorySegment segment, long count) {
            super(segment, count, Short.BYTES);
        }

        // performs [Short#toUnsignedInt]
        public @Unsigned int getToUnsignedInt(int index) {
            checkIndex(index);
            return Short.toUnsignedInt(this.segment.getAtIndex(ValueLayout.JAVA_SHORT, index));
        }
        public @Unsigned short getAsShort(int index) {
            checkIndex(index);
            return this.segment.getAtIndex(ValueLayout.JAVA_SHORT, index);
        }
        public short getAsShortExact(int index) {
            var v = getToUnsignedInt(index);
            if ((short)v != v) {
                throw new ArithmeticException("short overflow");
            }
            return (short)v;
        }

        /// performs [Short#toUnsignedInt]
        public @Unsigned int[] collectToUnsignedInt() {
            var array = new int[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = Short.toUnsignedInt(this.segment.getAtIndex(ValueLayout.JAVA_SHORT, i));
            }
            return array;
        }

        public @Unsigned short[] collectAsShort() {
            var array = new short[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = this.segment.getAtIndex(ValueLayout.JAVA_SHORT, i);
            }
            return array;
        }

        public void forEach(ShortConsumer action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(this.segment.getAtIndex(ValueLayout.JAVA_SHORT, i));
            }
        }

        @Override
        protected Short getUnchecked(int index) {
            return this.segment.getAtIndex(ValueLayout.JAVA_SHORT, index);
        }

    }

    public static final class OfU8 extends OfPrimitive<Byte> {
        public OfU8(MemorySegment segment) {
            super(segment, Byte.BYTES);
        }
        public OfU8(MemorySegment segment, long count) {
            super(segment, count, Byte.BYTES);
        }

        /// performs [Byte#toUnsignedInt]
        public @Unsigned int getToUnsignedInt(int index) {
            checkIndex(index);
            return Byte.toUnsignedInt(this.segment.getAtIndex(ValueLayout.JAVA_BYTE, index));
        }
        /// performs [Byte#toUnsignedInt]
        public @Unsigned short getToUnsignedShort(int index) {
            return (short) getToUnsignedInt(index);
        }
        public @Unsigned byte getAsByte(int index) {
            checkIndex(index);
            return this.segment.getAtIndex(ValueLayout.JAVA_BYTE, index);
        }
        public byte getAsByteExact(int index) {
            var v = getToUnsignedInt(index);
            if ((byte)v != v) {
                throw new ArithmeticException("byte overflow");
            }
            return (byte)v;
        }

        /// performs [Byte#toUnsignedInt]
        public @Unsigned int[] collectToUnsignedInt() {
            var array = new int[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = Byte.toUnsignedInt(this.segment.getAtIndex(ValueLayout.JAVA_BYTE, i));
            }
            return array;
        }

        /// performs [Byte#toUnsignedInt]
        public @Unsigned short[] collectToUnsignedShort() {
            var array = new short[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = (short) Byte.toUnsignedInt(this.segment.getAtIndex(ValueLayout.JAVA_BYTE, i));
            }
            return array;
        }

        public @Unsigned byte[] collectAsByte() {
            var array = new byte[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = this.segment.getAtIndex(ValueLayout.JAVA_BYTE, i);
            }
            return array;
        }

        public void forEach(ByteConsumer action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(this.segment.getAtIndex(ValueLayout.JAVA_BYTE, i));
            }
        }

        @Override
        protected Byte getUnchecked(int index) {
            return this.segment.getAtIndex(ValueLayout.JAVA_BYTE, index);
        }

    }


    @FunctionalInterface
    public interface ShortConsumer {

        void accept(short value);

    }

    @FunctionalInterface
    public interface ByteConsumer {

        void accept(byte value);

    }

    @FunctionalInterface
    public interface OffsetConsumer<T> {

        void accept(T t, MemorySegment segment, long offset);

    }
}

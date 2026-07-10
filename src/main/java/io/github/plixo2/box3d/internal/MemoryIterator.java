package io.github.plixo2.box3d.internal;

import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class MemoryIterator<T> implements Iterable<T> {
    protected final T element;
    protected final long elementBytes;
    protected final MemorySegment segment;
    protected int length;
    protected final BiConsumer<T, MemorySegment> set;

    public MemoryIterator(
            T element,
            MemorySegment segment,
            long elementBytes,
            BiConsumer<T, MemorySegment> set
    ) {
        this.element = element;
        this.segment = segment;
        this.elementBytes = elementBytes;

        if (segment.address() == 0) {
            this.length = 0;
        } else {
            this.length = assertSize(segment, elementBytes);
        }

        this.set = set;
    }

    public int length() {
        return this.length;
    }

    public List<T> collect(Function<T, T> clone) {
        var list = new ArrayList<T>(this.length);

        for (int i = 0; i < this.length; i++) {
            var seg = this.segment.asSlice(i * this.elementBytes, this.elementBytes);
            var el = clone.apply(this.element);
            this.set.accept(el, seg);
            list.add(el);
        }
        return list;
    }

    public T get(int index) {
        if (index < 0 || index >= this.length) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for length " + this.length);
        }
        return getUnchecked(index);
    }

    private T getUnchecked(int index) {
        var seg = this.segment.asSlice(index * this.elementBytes, this.elementBytes);
        this.set.accept(this.element, seg);
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

                if (this.index >= this.length) {
                    throw new IndexOutOfBoundsException(
                            "Index "
                            + this.index
                            + " is out of bounds for length "
                            + this.length
                    );
                }

                return MemoryIterator.this.getUnchecked(this.index++);
            }
        };
    }



    static abstract class OfPrimitive<T> extends MemoryIterator<T> {

        OfPrimitive(
                MemorySegment segment,
                long elementBytes
        ) {
            super(null, segment, elementBytes, null);
        }

        protected abstract T getAtIndex(int index);

        @Override
        public T get(int index) {
            if (index < 0 || index >= this.length) {
                throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for length " + this.length);
            }
            return getAtIndex(index);
        }

        @Override
        public List<T> collect(Function<T, T> clone) {
            var list = new ArrayList<T>(this.length);
            for (int i = 0; i < this.length; i++) {
                list.add(getAtIndex(i));
            }
            return list;
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            for (int i = 0; i < this.length; i++) {
                action.accept(getAtIndex(i));
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
                    if (this.index >= this.length) {
                        throw new IndexOutOfBoundsException(
                                "Index "
                                + this.index
                                + " is out of bounds for length "
                                + this.length
                        );
                    }
                    return OfPrimitive.this.getAtIndex(this.index++);
                }
            };
        }

    }

    public static class OfInt extends OfPrimitive<Integer> {
        public OfInt(MemorySegment segment) {
            super(segment, Integer.BYTES);
        }

        @Override
        protected Integer getAtIndex(int index) {
            return this.segment.get(ValueLayout.JAVA_INT, index * 4L);
        }

        public int[] collect() {
            return this.segment.toArray(ValueLayout.JAVA_INT);
        }

    }
    public static class OfLong extends OfPrimitive<Long> {

        public OfLong(MemorySegment segment) {
            super(segment, Long.BYTES);
        }

        @Override
        protected Long getAtIndex(int index) {
            return this.segment.get(ValueLayout.JAVA_LONG, index * 8L);
        }

        public long[] collect() {
            return this.segment.toArray(ValueLayout.JAVA_LONG);
        }


    }

    public static class OfU32 extends OfPrimitive<Long> {
        public OfU32(MemorySegment segment) {
            super(segment, Integer.BYTES);
        }

        @Override
        protected Long getAtIndex(int index) {
            return Integer.toUnsignedLong(this.segment.get(ValueLayout.JAVA_INT, index * 4L));
        }

        public long[] collect() {
            var array = new long[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = getAtIndex(i);
            }
            return array;
        }

    }
    public static class OfU64 extends OfLong {
        public OfU64(MemorySegment segment) {
            super(segment);
        }
    }

    public static class OfU16 extends OfPrimitive<Integer> {
        public OfU16(MemorySegment segment) {
            super(segment, Short.BYTES);
        }

        @Override
        protected Integer getAtIndex(int index) {
            return Short.toUnsignedInt(this.segment.get(ValueLayout.JAVA_SHORT, index * 2L));
        }

        public int[] collect() {
            var array = new int[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = getAtIndex(i);
            }
            return array;
        }

    }

    public static class OfU8 extends OfPrimitive<Integer> {
        public OfU8(MemorySegment segment) {
            super(segment, Byte.BYTES);
        }

        @Override
        protected Integer getAtIndex(int index) {
            return Byte.toUnsignedInt(this.segment.get(ValueLayout.JAVA_BYTE, index));
        }

        public int[] collect() {
            var array = new int[this.length];
            for (int i = 0; i < this.length; i++) {
                array[i] = getAtIndex(i);
            }
            return array;
        }
    }


    private static int assertSize(MemorySegment segment, long elementBytes) {
        long size = segment.byteSize();
        if (size % elementBytes != 0) {
            throw new IllegalArgumentException("Segment size " + size + " is not a multiple of element size " + elementBytes);
        }
        return Math.toIntExact(size / elementBytes);
    }

}

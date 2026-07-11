package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.MemoryIterator;
import org.box2d.box3d.b3Simplex;
import org.box2d.box3d.b3SimplexVertex;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Iterator;

/// Parameter for shapeDistance (`simplexes` and `simplexCapacity`)
public class Simplexes implements Iterable<Simplexes.Simplex> {

    final MemorySegment segment;
    final int capacity;

    private final SimplexVertex reference = new SimplexVertex();

    public Simplexes(int capacity) {
        this.capacity = capacity;

        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }

        if (capacity == 0) {
            this.segment = MemorySegment.NULL;
        } else {
            this.segment = b3Simplex.allocateArray(capacity, Arena.ofAuto());
        }
    }


    @Override
    public @NotNull Iterator<Simplex> iterator() {
        var capacity = Simplexes.this.capacity;

        return new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < capacity;
            }

            @Override
            public Simplex next() {
                return new Simplex(this.index++);
            }
        };

    }

    public class Simplex {

        private final int index;
        private Simplex(int index) {
            this.index = index;
        }

        public MemoryIterator<SimplexVertex> vertices() {
            var simplex = b3Simplex.asSlice(Simplexes.this.segment, this.index);
            var size = b3SimplexVertex.sizeof();
            var vertices = b3Simplex.vertices(simplex);
            var count = b3Simplex.count(simplex);

            return new MemoryIterator<>(
                    Simplexes.this.reference,
                    vertices,
                    count * size,
                    size,
                    SimplexVertex::set
            );
        }

    }

}

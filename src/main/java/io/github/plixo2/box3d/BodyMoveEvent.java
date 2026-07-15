package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import org.box2d.box3d.b3BodyEvents;
import org.box2d.box3d.b3BodyMoveEvent;
import org.joml.Matrix4f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.NoSuchElementException;


public class BodyMoveEvent {

    @Getter
    private BodyID bodyID;

    @Getter
    private boolean fellAsleep;

    @Getter
    private final Matrix4f transform = new Matrix4f();


    private BodyMoveEvent() {}

    public BodyMoveEvent(BodyMoveEvent other) {
        this.bodyID = other.bodyID;
        this.fellAsleep = other.fellAsleep;
        this.transform.set(other.transform);
    }


    private BodyMoveEvent set(MemorySegment segment, long offset) {

        var transformOffset  = offset + b3BodyMoveEvent.transform$offset();
        var bodyIDOffset     = offset + b3BodyMoveEvent.bodyId$offset();
        var fellAsleepOffset = offset + b3BodyMoveEvent.fellAsleep$offset();

        PrimitiveMemOps.setTransform(this.transform, segment,  transformOffset);
        this.bodyID = BodyID.of(segment, bodyIDOffset);
        this.fellAsleep = segment.get(ValueLayout.JAVA_BOOLEAN, fellAsleepOffset);
        return this;
    }


    static class Iterator implements java.util.Iterator<BodyMoveEvent> {
        private static final long SIZE = b3BodyEvents.sizeof();

        private final BodyMoveEvent instance = new BodyMoveEvent();
        private final MemorySegment segment;
        private final long count;

        private long index = 0;

        Iterator(
                MemorySegment moveEvents,
                long moveCount
        ) {
            this.segment =  moveEvents.asSlice(SIZE * moveCount);
            this.count = moveCount;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.count;
        }

        @Override
        public BodyMoveEvent next() {
            if (this.index >= this.count) {
                throw new NoSuchElementException();
            }

            var offset = SIZE * this.index++;

            return this.instance.set(this.segment, offset);
        }
    }

}

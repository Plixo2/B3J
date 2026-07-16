package io.github.plixo2.box3d;

import lombok.Getter;
import org.box2d.box3d.b3SensorBeginTouchEvent;

import java.lang.foreign.MemorySegment;

public class SensorBeginTouchEvent {

    @Getter
    private ShapeID sensorShapeID = ShapeID.NULL_ID;

    @Getter
    private ShapeID visitorShapeID = ShapeID.NULL_ID;

    SensorBeginTouchEvent() {}

    public SensorBeginTouchEvent(SensorBeginTouchEvent other) {
        this.sensorShapeID = other.sensorShapeID;
        this.visitorShapeID = other.visitorShapeID;
    }

    SensorBeginTouchEvent set(MemorySegment segment, long offset) {
        this.sensorShapeID = ShapeID.of(segment, offset + b3SensorBeginTouchEvent.sensorShapeId$offset());
        this.visitorShapeID = ShapeID.of(segment, offset + b3SensorBeginTouchEvent.visitorShapeId$offset());
        return this;
    }

}

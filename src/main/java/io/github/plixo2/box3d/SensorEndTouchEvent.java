package io.github.plixo2.box3d;

import lombok.Getter;
import org.box2d.box3d.b3SensorEndTouchEvent;

import java.lang.foreign.MemorySegment;

public class SensorEndTouchEvent {

    @Getter
    private ShapeID sensorShapeID = ShapeID.NULL_ID;

    @Getter
    private ShapeID visitorShapeID = ShapeID.NULL_ID;

    SensorEndTouchEvent() {}

    public SensorEndTouchEvent(SensorEndTouchEvent other) {
        this.sensorShapeID = other.sensorShapeID;
        this.visitorShapeID = other.visitorShapeID;
    }

    SensorEndTouchEvent set(MemorySegment segment, long offset) {
        this.sensorShapeID = ShapeID.of(segment, offset + b3SensorEndTouchEvent.sensorShapeId$offset());
        this.visitorShapeID = ShapeID.of(segment, offset + b3SensorEndTouchEvent.visitorShapeId$offset());
        return this;
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.MemoryIterator;
import lombok.Getter;

public class SensorEvents {

    @Getter
    private final MemoryIterator<SensorBeginTouchEvent> beginEvents;

    @Getter
    private final MemoryIterator<SensorEndTouchEvent> endEvents;

    SensorEvents(
            MemoryIterator<SensorBeginTouchEvent> beginEvents,
            MemoryIterator<SensorEndTouchEvent> endEvents
    ) {
        this.beginEvents = beginEvents;
        this.endEvents = endEvents;
    }

}

package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.MemoryIterator;
import lombok.Getter;

public class ContactEvents {

    @Getter
    private final MemoryIterator<ContactBeginTouchEvent> beginEvents;

    @Getter
    private final MemoryIterator<ContactEndTouchEvent> endEvents;

    @Getter
    private final MemoryIterator<ContactHitEvent> hitEvents;

    ContactEvents(
            MemoryIterator<ContactBeginTouchEvent> beginEvents,
            MemoryIterator<ContactEndTouchEvent> endEvents,
            MemoryIterator<ContactHitEvent> hitEvents
    ) {
        this.beginEvents = beginEvents;
        this.endEvents = endEvents;
        this.hitEvents = hitEvents;
    }

}

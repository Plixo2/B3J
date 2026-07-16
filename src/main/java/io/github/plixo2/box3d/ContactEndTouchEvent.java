package io.github.plixo2.box3d;

import lombok.Getter;
import org.box2d.box3d.b3ContactEndTouchEvent;

import java.lang.foreign.MemorySegment;

public class ContactEndTouchEvent {

    @Getter
    private ShapeID shapeIDA = ShapeID.NULL_ID;

    @Getter
    private ShapeID shapeIDB = ShapeID.NULL_ID;

    @Getter
    private ContactID contactID = ContactID.NULL_ID;

    ContactEndTouchEvent() {}

    public ContactEndTouchEvent(ContactEndTouchEvent other) {
        this.shapeIDA = other.shapeIDA;
        this.shapeIDB = other.shapeIDB;
        this.contactID = other.contactID;
    }

    ContactEndTouchEvent set(MemorySegment segment, long offset) {
        this.shapeIDA = ShapeID.of(segment, offset + b3ContactEndTouchEvent.shapeIdA$offset());
        this.shapeIDB = ShapeID.of(segment, offset + b3ContactEndTouchEvent.shapeIdB$offset());
        this.contactID = ContactID.of(segment, offset + b3ContactEndTouchEvent.contactId$offset());
        return this;
    }

}

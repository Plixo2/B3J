package io.github.plixo2.box3d;

import lombok.Getter;
import org.box2d.box3d.b3ContactBeginTouchEvent;

import java.lang.foreign.MemorySegment;

public class ContactBeginTouchEvent {

    @Getter
    private ShapeID shapeIDA = ShapeID.NULL_ID;

    @Getter
    private ShapeID shapeIDB = ShapeID.NULL_ID;

    @Getter
    private ContactID contactID = ContactID.NULL_ID;

    ContactBeginTouchEvent() {}

    public ContactBeginTouchEvent(ContactBeginTouchEvent other) {
        this.shapeIDA = other.shapeIDA;
        this.shapeIDB = other.shapeIDB;
        this.contactID = other.contactID;
    }

    ContactBeginTouchEvent set(MemorySegment segment, long offset) {
        this.shapeIDA = ShapeID.of(segment, offset + b3ContactBeginTouchEvent.shapeIdA$offset());
        this.shapeIDB = ShapeID.of(segment, offset + b3ContactBeginTouchEvent.shapeIdB$offset());
        this.contactID = ContactID.of(segment, offset + b3ContactBeginTouchEvent.contactId$offset());
        return this;
    }

}

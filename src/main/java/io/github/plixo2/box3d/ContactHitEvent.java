package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.Unsigned;
import lombok.Getter;
import org.box2d.box3d.b3ContactHitEvent;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ContactHitEvent {

    @Getter
    private ShapeID shapeIDA = ShapeID.NULL_ID;

    @Getter
    private ShapeID shapeIDB = ShapeID.NULL_ID;

    @Getter
    private ContactID contactID = ContactID.NULL_ID;

    @Getter
    private final Vector3f point = new Vector3f();

    @Getter
    private final Vector3f normal = new Vector3f();

    @Getter
    private float approachSpeed;

    @Getter
    private @Unsigned long userMaterialIDA;

    @Getter
    private @Unsigned long userMaterialIDB;

    ContactHitEvent() {}

    public ContactHitEvent(ContactHitEvent other) {
        this.shapeIDA = other.shapeIDA;
        this.shapeIDB = other.shapeIDB;
        this.contactID = other.contactID;
        this.point.set(other.point);
        this.normal.set(other.normal);
        this.approachSpeed = other.approachSpeed;
        this.userMaterialIDA = other.userMaterialIDA;
        this.userMaterialIDB = other.userMaterialIDB;
    }

    ContactHitEvent set(MemorySegment segment, long offset) {
        this.shapeIDA = ShapeID.of(segment, offset + b3ContactHitEvent.shapeIdA$offset());
        this.shapeIDB = ShapeID.of(segment, offset + b3ContactHitEvent.shapeIdB$offset());
        this.contactID = ContactID.of(segment, offset + b3ContactHitEvent.contactId$offset());
        PrimitiveMemOps.setVec3(this.point, segment, offset + b3ContactHitEvent.point$offset());
        PrimitiveMemOps.setVec3(this.normal, segment, offset + b3ContactHitEvent.normal$offset());
        this.approachSpeed = segment.get(ValueLayout.JAVA_FLOAT, offset + b3ContactHitEvent.approachSpeed$offset());
        this.userMaterialIDA = segment.get(ValueLayout.JAVA_LONG, offset + b3ContactHitEvent.userMaterialIdA$offset());
        this.userMaterialIDB = segment.get(ValueLayout.JAVA_LONG, offset + b3ContactHitEvent.userMaterialIdB$offset());
        return this;
    }

}

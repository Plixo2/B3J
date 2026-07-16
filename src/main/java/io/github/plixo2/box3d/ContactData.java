package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.MemoryIterator;
import lombok.Getter;
import org.box2d.box3d.b3ContactData;
import org.box2d.box3d.b3Manifold;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ContactData {

    @Getter
    private ContactID contactID = ContactID.NULL_ID;

    @Getter
    private ShapeID shapeIDA = ShapeID.NULL_ID;

    @Getter
    private ShapeID shapeIDB = ShapeID.NULL_ID;

    @Getter
    private MemoryIterator<Manifold> manifolds = emptyManifolds();

    ContactData() {}

    public ContactData(ContactData other) {
        this.contactID = other.contactID;
        this.shapeIDA = other.shapeIDA;
        this.shapeIDB = other.shapeIDB;
        this.manifolds = other.manifolds;
    }

    ContactData set(MemorySegment segment, long offset) {
        this.contactID = ContactID.of(segment, offset + b3ContactData.contactId$offset());
        this.shapeIDA = ShapeID.of(segment, offset + b3ContactData.shapeIdA$offset());
        this.shapeIDB = ShapeID.of(segment, offset + b3ContactData.shapeIdB$offset());

        var manifoldSegment = segment.get(ValueLayout.ADDRESS, offset + b3ContactData.manifolds$offset());
        var manifoldCount = segment.get(ValueLayout.JAVA_INT, offset + b3ContactData.manifoldCount$offset());
        this.manifolds = new MemoryIterator<>(
                new Manifold(),
                manifoldSegment,
                manifoldCount,
                b3Manifold.sizeof(),
                Manifold::set
        );

        return this;
    }

    private static MemoryIterator<Manifold> emptyManifolds() {
        return new MemoryIterator<>(
                new Manifold(),
                MemorySegment.NULL,
                0,
                b3Manifold.sizeof(),
                Manifold::set
        );
    }

}

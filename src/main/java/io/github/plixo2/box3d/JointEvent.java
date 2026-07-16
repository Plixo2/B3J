package io.github.plixo2.box3d;

import lombok.Getter;
import org.box2d.box3d.b3JointEvent;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class JointEvent {

    @Getter
    private JointID<?> jointID = JointID.NULL_ID;

    @Getter
    private long userData;

    JointEvent() {}

    public JointEvent(JointEvent other) {
        this.jointID = other.jointID;
        this.userData = other.userData;
    }

    JointEvent set(MemorySegment segment, long offset) {
        this.jointID = JointID.of(segment, offset + b3JointEvent.jointId$offset());
        this.userData = segment.get(ValueLayout.JAVA_LONG, offset + b3JointEvent.userData$offset());
        return this;
    }

}

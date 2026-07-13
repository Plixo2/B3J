package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.B3JUtil;
import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import lombok.Getter;
import lombok.Setter;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

@Getter
@Setter
public class BodyDef {

    private BodyType type;
    private Vector3f position = new Vector3f();
    private Quaternionf rotation = new Quaternionf();
    private Vector3f linearVelocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();
    private float linearDamping;
    private float angularDamping;
    private float gravityScale;
    private float sleepThreshold;
    private @Nullable String name;
    private MotionLocks motionLocks = new MotionLocks();
    private boolean enableSleep;
    private boolean isAwake;
    private boolean isBullet;
    private boolean isEnabled;
    private boolean allowFastRotation;
    private boolean enableContactRecycling;

    /// @api b3DefaultBodyDef
    public BodyDef() {
        this.type = BodyType.STATIC;

        this.sleepThreshold = 0.05f * B3.getLengthUnitsPerMeter();
        this.gravityScale = 1.0f;
        this.enableSleep = true;
        this.isAwake = true;
        this.isEnabled = true;
        this.enableContactRecycling = true;
    }

    public BodyDef(BodyDef other) {
        this.type = other.type;
        this.position.set(other.position);
        this.rotation.set(other.rotation);
        this.linearVelocity.set(other.linearVelocity);
        this.angularVelocity.set(other.angularVelocity);
        this.linearDamping = other.linearDamping;
        this.angularDamping = other.angularDamping;
        this.gravityScale = other.gravityScale;
        this.sleepThreshold = other.sleepThreshold;
        this.name = other.name;
        this.motionLocks.set(other.motionLocks);
        this.enableSleep = other.enableSleep;
        this.isAwake = other.isAwake;
        this.isBullet = other.isBullet;
        this.isEnabled = other.isEnabled;
        this.allowFastRotation = other.allowFastRotation;
        this.enableContactRecycling = other.enableContactRecycling;
    }

    MemorySegment create(SegmentAllocator arena) {
        var segment = b3BodyDef.allocate(arena);
        b3BodyDef.type(segment, this.type.code());
        PrimitiveMemOps.putVec3(b3BodyDef.position(segment), this.position);
        PrimitiveMemOps.putQuat(b3BodyDef.rotation(segment), this.rotation);
        PrimitiveMemOps.putVec3(b3BodyDef.linearVelocity(segment), this.linearVelocity);
        PrimitiveMemOps.putVec3(b3BodyDef.angularVelocity(segment), this.angularVelocity);
        b3BodyDef.linearDamping(segment, this.linearDamping);
        b3BodyDef.angularDamping(segment, this.angularDamping);
        b3BodyDef.gravityScale(segment, this.gravityScale);
        b3BodyDef.sleepThreshold(segment, this.sleepThreshold);
        b3BodyDef.name(segment, B3JUtil.allocNullString(arena, this.name));
        this.motionLocks.put(b3BodyDef.motionLocks(segment));
        b3BodyDef.enableSleep(segment, this.enableSleep);
        b3BodyDef.isAwake(segment, this.isAwake);
        b3BodyDef.isBullet(segment, this.isBullet);
        b3BodyDef.isEnabled(segment, this.isEnabled);
        b3BodyDef.allowFastRotation(segment, this.allowFastRotation);
        b3BodyDef.enableContactRecycling(segment, this.enableContactRecycling);
        b3BodyDef.internalValue(segment, B3.SECRET_COOKIE);

        return segment;
    }

    private MemorySegment nls(Object object) {
        if (object != null) {
            throw new RuntimeException("Not implemented");
        }
        return MemorySegment.NULL;
    }

}

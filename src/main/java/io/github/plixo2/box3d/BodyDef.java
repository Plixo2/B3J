package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.Internal;
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

    BodyType type;
    Vector3f position = new Vector3f();
    Quaternionf rotation = new Quaternionf();
    Vector3f linearVelocity = new Vector3f();
    Vector3f angularVelocity = new Vector3f();
    float linearDamping;
    float angularDamping;
    float gravityScale;
    float sleepThreshold;
    @Nullable String name;
    @Nullable Object userData;
    MotionLocks motionLocks = new MotionLocks();
    boolean enableSleep;
    boolean isAwake;
    boolean isBullet;
    boolean isEnabled;
    boolean allowFastRotation;
    boolean enableContactRecycling;

    /// @api b3DefaultBodyDef
    public BodyDef() {
        this.type = BodyType.STATIC;

        this.sleepThreshold = 0.05f * B3.lengthUnitsPerMeter();
        this.gravityScale = 1.0f;
        this.enableSleep = true;
        this.isAwake = true;
        this.isEnabled = true;
        this.enableContactRecycling = true;
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
        b3BodyDef.name(segment, Internal.allocNullString(arena, this.name));
        b3BodyDef.userData(segment, nls(this.userData));
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

package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.ConstArena;
import io.github.plixo2.box3d.internal.StackArena;
import org.box2d.box3d.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.plixo2.box3d.internal.Internal.*;
import static org.box2d.box3d.box3d_h.*;

public class B3 {
    private B3() {}

    // took your cookie
    static final int SECRET_COOKIE = 1152023;

    public static long DEFAULT_CATEGORY_BITS = U64_MAX;
    public static long DEFAULT_MASK_BITS = U64_MAX;


    private static final ConcurrentHashMap<WorldID, TaskPool.Allocated> allocatedPools = new ConcurrentHashMap<>();
    private static final ThreadLocal<B3> instances = ThreadLocal.withInitial(B3::new);

    public static B3 get() {
        return instances.get();
    }


    private final Arena tlArena = Arena.ofAuto();
    private final MemorySegment worldIDSegment = b3WorldId.allocate(this.tlArena);
    private final MemorySegment bodyIDSegment = b3BodyId.allocate(this.tlArena);
    private final MemorySegment contactIDSegment = b3ContactId.allocate(this.tlArena);
    private final MemorySegment shapeIDSegment = b3ShapeId.allocate(this.tlArena);
    private final MemorySegment jointIDSegment = b3JointId.allocate(this.tlArena);
    private final ConstArena returnArena = new ConstArena(this.tlArena, 1024);      // 1 KB
    private final StackArena argArena = new StackArena(this.tlArena, 32 * 1024);    // 32 KB

    private MemorySegment worldID(WorldID worldID) {
        b3WorldId.index1(this.worldIDSegment, assertU16(worldID.index1(), "index1"));
        b3WorldId.generation(this.worldIDSegment, assertU16(worldID.generation(), "generation"));
        return this.worldIDSegment;
    }
    private MemorySegment bodyID(BodyID bodyID) {
        b3BodyId.index1(this.bodyIDSegment, bodyID.index1());
        b3BodyId.world0(this.bodyIDSegment, assertU16(bodyID.world0(), "world0"));
        b3BodyId.generation(this.bodyIDSegment, assertU16(bodyID.generation(), "generation"));
        return this.bodyIDSegment;
    }
    private MemorySegment contactID(ContactID contactID) {
        b3ContactId.index1(this.contactIDSegment, contactID.index1());
        b3ContactId.world0(this.contactIDSegment, assertU16(contactID.world0(), "world0"));
        b3ContactId.generation(this.contactIDSegment, assertU32(contactID.generation(), "generation"));
        return this.contactIDSegment;
    }
    private MemorySegment shapeID(ShapeID shapeID) {
        b3ShapeId.index1(this.shapeIDSegment, shapeID.index1());
        b3ShapeId.world0(this.shapeIDSegment, assertU16(shapeID.world0(), "world0"));
        b3ShapeId.generation(this.shapeIDSegment, assertU16(shapeID.generation(), "generation"));
        return this.shapeIDSegment;
    }
    private MemorySegment jointID(JointID jointID) {
        b3JointId.index1(this.jointIDSegment, jointID.index1());
        b3JointId.world0(this.jointIDSegment, assertU16(jointID.world0(), "world0"));
        b3JointId.generation(this.jointIDSegment, assertU16(jointID.generation(), "generation"));
        return this.jointIDSegment;
    }


    /// @api b3DefaultWorldDef
    public WorldDef defaultWorldDef() {
        return new WorldDef();
    }

    /// @api b3CreateWorld
    public WorldID createWorld(WorldDef worldDef) {
        try (this.argArena) {
            var result = worldDef.create(this.argArena);
            var key = WorldID.of(b3CreateWorld(this.returnArena, result.segment()));
            var taskPool = result.taskPool();

            allocatedPools.compute(key, (_, previous) -> {
                if (previous != null) {
                    previous.close();
                }
                return taskPool;
            });

            return key;
        }
    }

    /// @api b3DefaultBodyDef
    public BodyDef defaultBodyDef() {
        return new BodyDef();
    }

    /// @api b3CreateBody
    public BodyID createBody(WorldID worldID, BodyDef bodyDef) {
        try (this.argArena) {
            var bodyID = b3CreateBody(this.returnArena, worldID(worldID), bodyDef.create(this.argArena));
            return BodyID.of(bodyID);
        }
    }

    /// @api b3MakeBoxHull
    public BoxHull makeBoxHull(float hx, float hy, float hz) {
        var hull = b3MakeBoxHull(Arena.ofAuto(), hx, hy, hz);
        return new BoxHull(hull);
    }

    /// @api b3DefaultShapeDef
    public ShapeDef defaultShapeDef() {
        return new ShapeDef();
    }

    /// @api b3CreateHullShape
    public ShapeID createHullShape(BodyID bodyID, ShapeDef def, HullData hull) {
        try (this.argArena) {
            var shapeID = b3CreateHullShape(this.returnArena, bodyID(bodyID), def.create(this.argArena), hull.segment);
            return ShapeID.of(shapeID);
        }
    }

    /// @api b3MakeCubeHull
    public BoxHull makeCubeHull(float halfWidth) {
        var hull = b3MakeCubeHull(Arena.ofAuto(), halfWidth);
        return new BoxHull(hull);
    }

    /// @api b3Body_GetPosition
    public Vec3 bodyGetPosition(Vec3 in, BodyID bodyId) {
        var vec = b3Body_GetPosition(this.returnArena, bodyID(bodyId));
        in.set(vec);
        return in;
    }

    /// @api b3Body_GetRotation
    public Quat bodyGetRotation(Quat in, BodyID bodyId) {
        var quat = b3Body_GetRotation(this.returnArena, bodyID(bodyId));
        in.set(quat);
        return in;
    }

    /// @api b3World_Step
    public void worldStep(WorldID worldID, float timeStep, int subStepCount) {
        b3World_Step(worldID(worldID), timeStep, subStepCount);
    }

    /// @api b3GetAxisAngle
    public float getAxisAngle(Vec3 axisOut, Quat rotation) {

        float length = (float) Math.sqrt(rotation.x * rotation.x + rotation.y * rotation.y + rotation.z * rotation.z);
        float radians = 2.0f * (float) Math.atan2(length, rotation.w);
        if (length > 0.0f) {
            float invLength = 1.0f / length;
            axisOut.set(invLength * rotation.x, invLength * rotation.y, invLength * rotation.z);
        }

        return radians;
    }


    /// @api b3DestroyBody
    public void destory(BodyID bodyID) {
        b3DestroyBody(bodyID(bodyID));
    }


    /// @api b3GetVersion
    public Version getVersion() {
        return Version.of(b3GetVersion(this.returnArena));
    }


    /// @api b3DestroyWorld
    public void destroyWorld(WorldID worldID) {
        var allocatedPool = allocatedPools.remove(worldID);
        if (allocatedPool != null) {
            allocatedPool.close();
        }
        b3DestroyWorld(worldID(worldID));
    }

    /// @api b3DefaultFilter
    public Filter defaultFiler() {
        return new Filter();
    }

    /// @api b3DefaultSurfaceMaterial
    public SurfaceMaterial defaultSurfaceMaterial() {
        return new SurfaceMaterial();
    }

    /// @api b3GetLengthUnitsPerMeter
    public float getLengthUnitsPerMeter() {
        return b3GetLengthUnitsPerMeter();
    }

}

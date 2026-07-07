package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.*;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.FloatBuffer;

import static io.github.plixo2.box3d.internal.Internal.*;
import static org.box2d.box3d.box3d_h.*;

public class B3 {
    private B3() {}

    // haha, took your cookie
    static final int SECRET_COOKIE = 1152023;

    public static final long DEFAULT_CATEGORY_BITS = U64_MAX;
    public static final long DEFAULT_MASK_BITS = U64_MAX;
    public static final @U8 int HEIGHT_FIELD_HOLE = B3_HEIGHT_FIELD_HOLE;

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

    private final MemorySegment vec3Segment = b3Vec3.allocate(this.tlArena);
    private final MemorySegment quatSegment = b3Quat.allocate(this.tlArena);
    private final MemorySegment vec3Segment2 = b3Vec3.allocate(this.tlArena);
    private final MemorySegment transformSegment = b3Transform.allocate(this.tlArena);
    private final Quaternionf tempQuat = new Quaternionf();

    private final ConstArena returnArena = new ConstArena(this.tlArena, 1024);      // 1 KB
    private final StackArena argArena = new StackArena(this.tlArena, 32 * 1024);    // 32 KB

    private final AllocatedCastResultFcn castResultFcn = new AllocatedCastResultFcn(this.tlArena, ShapeID::of);

    private MemorySegment worldID(WorldID worldID) {
        worldID.ensureAccess();
        return worldID(worldID.index1, worldID.generation);
    }
    private MemorySegment worldID(int index1, int generation) {
        b3WorldId.index1(this.worldIDSegment, assertU16(index1, "index1"));
        b3WorldId.generation(this.worldIDSegment, assertU16(generation, "generation"));
        return this.worldIDSegment;
    }
    private MemorySegment bodyID(BodyID bodyID) {
        bodyID.ensureAccess();
        return bodyID(bodyID.index1, bodyID.world0, bodyID.generation);
    }
    private MemorySegment bodyID(int index1, int world0, int generation) {
        b3BodyId.index1(this.bodyIDSegment, index1);
        b3BodyId.world0(this.bodyIDSegment, assertU16(world0, "world0"));
        b3BodyId.generation(this.bodyIDSegment, assertU16(generation, "generation"));
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
    private MemorySegment transform(Matrix4f transform) {
        PrimitveMemOps.putTransform(this.transformSegment, this.tempQuat, transform);
        return this.transformSegment;
    }
    private MemorySegment vec3(Vector3f vec) {
        PrimitveMemOps.putVec3(this.vec3Segment, vec);
        return this.vec3Segment;
    }
    private MemorySegment vec3_2(Vector3f vec) {
        PrimitveMemOps.putVec3(this.vec3Segment2, vec);
        return this.vec3Segment2;
    }
    private MemorySegment quat(Quaternionf quat) {
        PrimitveMemOps.putQuat(this.quatSegment, quat);
        return this.quatSegment;
    }


    /// @api b3CreateWorld
    public WorldID createWorld(
            Region region,
            WorldDef worldDef
    ) {
        try (this.argArena) {
            var result = worldDef.create(this.argArena);
            var taskPool = result.taskPool();
            var shapes = result.shapes();

            return WorldID.of(
                    this,
                    region,
                    taskPool,
                    shapes,
                    b3CreateWorld(this.returnArena, result.segment())
            );
        }
    }

    /// @api b3CreateBody
    public BodyID createBody(
            Region region,
            WorldID worldID,
            BodyDef bodyDef
    ) {
        try (this.argArena) {
            var bodyID = b3CreateBody(this.returnArena, worldID(worldID), bodyDef.create(this.argArena));
            return BodyID.of(this, region, bodyID);
        }
    }

    /// @api b3MakeBoxHull
    public BoxHull makeBoxHull(float hx, float hy, float hz) {
        var hull = b3MakeBoxHull(Arena.ofAuto(), hx, hy, hz);
        return new BoxHull(hull);
    }

    /// @api b3MakeCubeHull
    public BoxHull makeCubeHull(float halfWidth) {
        var hull = b3MakeCubeHull(Arena.ofAuto(), halfWidth);
        return new BoxHull(hull);
    }

    /// @api b3CreateHullShape
    public ShapeID createHullShape(BodyID bodyID, ShapeDef def, HullData hull) {
        try (this.argArena) {
            var shapeID = b3CreateHullShape(this.returnArena, bodyID(bodyID), def.create(this.argArena), hull.segment);
            return ShapeID.of(shapeID);
        }
    }

    /// @api b3CreateSphereShape
    public ShapeID createSphereShape(BodyID bodyID, ShapeDef def, Sphere sphere) {
        try (this.argArena) {
            var shapeID = b3CreateSphereShape(
                    this.returnArena,
                    bodyID(bodyID),
                    def.create(this.argArena),
                    sphere.create(this.argArena)
            );
            return ShapeID.of(shapeID);
        }
    }

    /// @api b3CreateCapsuleShape
    public ShapeID createCapsuleShape(BodyID bodyID, ShapeDef def, Capsule capsule) {
        try (this.argArena) {
            var shapeID = b3CreateCapsuleShape(
                    this.returnArena,
                    bodyID(bodyID),
                    def.create(this.argArena),
                    capsule.create(this.argArena)
            );
            return ShapeID.of(shapeID);
        }
    }


    /// @api b3MakeTransformedBoxHull
    public BoxHull makeTransformedBoxHull(float hx, float hy, float hz, Matrix4f transform) {
        var hull = b3MakeTransformedBoxHull(Arena.ofAuto(), hx, hy, hz, transform(transform));
        return new BoxHull(hull);
    }


    /// @api b3CreateHull
    public @Nullable HullData createHull(
            MemorySegment points,
            int maxVertexCount
    ) {
        var byteLength = points.byteSize();
        if (byteLength % (3 * Float.BYTES) != 0) {
            throw new IllegalArgumentException("points must be a multiple of 3 floats");
        }
        var vertexCount = Math.toIntExact((byteLength / (3 * Float.BYTES)));

        var hull = b3CreateHull(points, vertexCount, maxVertexCount);
        if (hull.address() == 0) {
            return null;
        }
        hull = hull.reinterpret(
                Arena.ofAuto(),
                this::destroyHull
        );
        return new HullData(hull);
    }


    /// @api b3CreateHull
    public @Nullable HullData createHull(
            FloatBuffer points,
            int maxVertexCount
    ) {
        return createHull(MemorySegment.ofBuffer(points), maxVertexCount);
    }

    /// @api b3CreateHull
    public @Nullable HullData createHull(
            float[] points,
            int maxVertexCount
    ) {
        return createHull(MemorySegment.ofArray(points), maxVertexCount);
    }

    /// @api b3CreateCylinder
    public HullData createCylinder(
            float height,
            float radius,
            float yOffset,
            int sides
    ) {
        var hull = b3CreateCylinder(height, radius, yOffset, sides);
        hull = hull.reinterpret(
                Arena.ofAuto(),
                this::destroyHull
        );
        return new HullData(hull);
    }

    /// @api b3CreateCone
    public HullData createCone(
            float height,
            float radius1,
            float radius2,
            int slices
    ) {
        var hull = b3CreateCone(height, radius1, radius2, slices);
        hull = hull.reinterpret(
                Arena.ofAuto(),
                this::destroyHull
        );
        return new HullData(hull);
    }

    /// @api b3CreateMesh
    public @Nullable MeshData createMesh(
            Region region,
            MeshDef meshDef,
            int @Nullable [] degenerateTriangleIndices
    ) {
        try (this.argArena) {
            var def = meshDef.create(this.argArena);

            MemorySegment mesh;

            if (degenerateTriangleIndices == null) {
                mesh = b3CreateMesh(def, MemorySegment.NULL, 0);
            } else {
                var cap = degenerateTriangleIndices.length;
                try (var degenerateArena = Arena.ofConfined()) {
                    // cannot use MemorySegment.ofArray, has to be off-heap
                    var data = degenerateArena.allocate(ValueLayout.JAVA_INT.byteSize() * cap);

                    mesh = b3CreateMesh(def, data, cap);

                    // copy the data back to the array
                    MemorySegment.copy(data, ValueLayout.JAVA_INT, 0, degenerateTriangleIndices, 0, cap);
                }
            }

            if (mesh.address() == 0) {
                return null;
            }
            return new MeshData(this, region, mesh);
        }
    }

    /// @api b3CreateMesh
    public @Nullable MeshData createMesh(
            Region region,
            MeshDef meshDef
    ) {
        return createMesh(region, meshDef, null);
    }

    /// @api b3CreateMeshShape
    public ShapeID createMeshShape(
            BodyID bodyID,
            ShapeDef def,
            MeshData mesh,
            Vector3f scale
    ) {
        try (this.argArena) {
            var shapeID = b3CreateMeshShape(
                    this.returnArena,
                    bodyID(bodyID),
                    def.create(this.argArena),
                    mesh.segment(),
                    vec3(scale)
            );
            return ShapeID.of(shapeID);
        }
    }

    /// @api b3CreateHeightField
    public HeightFieldData createHeightField(
            Region region,
            HeightFieldDef def
    ) {
        try (this.argArena) {
            var segment = def.create(this.argArena);
            var heightField = b3CreateHeightField(segment);
            return new HeightFieldData(this, region, heightField);
        }
    }


    /// @api b3CreateHeightFieldShape
    public ShapeID createHeightFieldShape(
            BodyID bodyID,
            ShapeDef def,
            HeightFieldData heightField
    ) {
        try (this.argArena) {
            var shapeID = b3CreateHeightFieldShape(
                    this.returnArena,
                    bodyID(bodyID),
                    def.create(this.argArena),
                    heightField.segment()
            );
            return ShapeID.of(shapeID);
        }
    }


    /// @api b3World_CastRay
    @Contract("null, _, _, _, _, _ -> null; !null, _, _, _, _, _ -> !null")
    public @Nullable TreeStats worldCastRay(
            @Nullable TreeStats statsIn,
            WorldID worldID,
            Vector3f origin,
            Vector3f translation,
            QueryFilter filter,
            CastResult fcn
    ) {
        var callback = this.castResultFcn.set(fcn);

        try (this.castResultFcn) {
            var stats = b3World_CastRay(
                    this.returnArena,
                    worldID(worldID),
                    vec3(origin),
                    vec3_2(translation),
                    filter.segment,
                    callback,
                    MemorySegment.NULL
            );
            if (statsIn != null) {
                statsIn.set(stats);
                return statsIn;
            } else {
                return null;
            }
        }
    }

    /// @api b3World_CastRayClosest
    public @Nullable RayResult worldCastRayClosest(
            WorldID worldID,
            Vector3f origin,
            Vector3f translation,
            QueryFilter filter
    ) {
        var result = b3World_CastRayClosest(
                this.returnArena,
                worldID(worldID),
                vec3(origin),
                vec3_2(translation),
                filter.segment
        );
        if (!RayResult.hit(result)) {
            return null;
        }

        return new RayResult(result);
    }


    /// @api b3Body_GetPosition
    public Vector3f bodyGetPosition(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetPosition(this.returnArena, bodyID(bodyId));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetRotation
    public Quaternionf bodyGetRotation(Quaternionf in, BodyID bodyId) {
        var quat = b3Body_GetRotation(this.returnArena, bodyID(bodyId));
        return PrimitveMemOps.setQuat(in, quat);
    }

    /// @api b3Body_GetTransform
    public Matrix4f bodyGetTransform(Matrix4f in, BodyID bodyId) {
        var transform = b3Body_GetTransform(this.returnArena, bodyID(bodyId));
        return PrimitveMemOps.setTransform(in, transform);
    }

    /// @api b3Body_SetTransform
    public void bodySetTransform(BodyID bodyId, Matrix4f transform) {
        var segment = transform(transform);
        var quaternion = b3Transform.q(segment);
        var position = b3Transform.p(segment);
        b3Body_SetTransform(bodyID(bodyId), position, quaternion);
    }

    /// @api b3Body_SetTransform
    public void bodySetTransform(BodyID bodyId, Vector3f position, Quaternionf rotation) {
        b3Body_SetTransform(bodyID(bodyId), vec3(position), quat(rotation));
    }

    /// @api b3Body_GetLinearVelocity
    public Vector3f bodyGetLinearVelocity(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetLinearVelocity(this.returnArena, bodyID(bodyId));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetAngularVelocity
    public Vector3f bodyGetAngularVelocity(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetAngularVelocity(this.returnArena, bodyID(bodyId));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_SetTargetTransform
    public void bodySetTargetTransform(
            BodyID bodyId,
            Matrix4f transform,
            float timeStep,
            boolean wake
    ) {
        var segment = transform(transform);
        b3Body_SetTargetTransform(bodyID(bodyId), segment, timeStep, wake);
    }

    /// @api b3Body_SetLinearVelocity
    public void bodySetLinearVelocity(BodyID bodyId, Vector3f linearVelocity) {
        b3Body_SetLinearVelocity(bodyID(bodyId), vec3(linearVelocity));
    }

    /// @api b3Body_SetAngularVelocity
    public void bodySetAngularVelocity(BodyID bodyId, Vector3f angularVelocity) {
        b3Body_SetAngularVelocity(bodyID(bodyId), vec3(angularVelocity));
    }

    /// @api b3Body_GetWorldCenterOfMass
    public Vector3f bodyGetWorldCenterOfMass(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetWorldCenterOfMass(this.returnArena, bodyID(bodyId));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetLocalCenterOfMass
    public Vector3f bodyGetLocalCenterOfMass(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetLocalCenterOfMass(this.returnArena, bodyID(bodyId));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_ApplyForce
    public void bodyApplyForce(BodyID bodyId, Vector3f force, Vector3f point, boolean wake) {
        b3Body_ApplyForce(bodyID(bodyId), vec3(force), vec3_2(point), wake);
    }

    /// @api b3Body_ApplyTorque
    public void bodyApplyTorque(BodyID bodyId, Vector3f torque, boolean wake) {
        b3Body_ApplyTorque(bodyID(bodyId), vec3(torque), wake);
    }

    /// @api b3Body_ApplyLinearImpulse
    public void bodyApplyLinearImpulse(BodyID bodyId, Vector3f impulse, Vector3f point, boolean wake) {
        b3Body_ApplyLinearImpulse(bodyID(bodyId), vec3(impulse), vec3_2(point), wake);
    }

    /// @api b3Body_ApplyAngularImpulse
    public void bodyApplyAngularImpulse(BodyID bodyId, Vector3f impulse, boolean wake) {
        b3Body_ApplyAngularImpulse(bodyID(bodyId), vec3(impulse), wake);
    }

    /// @api b3Body_ApplyForceToCenter
    public void bodyApplyForceToCenter(BodyID bodyId, Vector3f force, boolean wake) {
        b3Body_ApplyForceToCenter(bodyID(bodyId), vec3(force), wake);
    }

    /// @api b3Body_ApplyLinearImpulseToCenter
    public void bodyApplyLinearImpulseToCenter(BodyID bodyId, Vector3f impulse, boolean wake) {
        b3Body_ApplyLinearImpulseToCenter(bodyID(bodyId), vec3(impulse), wake);
    }


    /// @api b3Body_GetWorldPoint
    public Vector3f bodyGetWorldPoint(Vector3f in, BodyID bodyId, Vector3f localPoint) {
        var vec = b3Body_GetWorldPoint(this.returnArena, bodyID(bodyId), vec3(localPoint));
        return PrimitveMemOps.setVec3(in, vec);
    }


    /// @api b3Body_GetWorldVector
    public Vector3f bodyGetWorldVector(Vector3f in, BodyID bodyId, Vector3f localVector) {
        var vec = b3Body_GetWorldVector(this.returnArena, bodyID(bodyId), vec3(localVector));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetLocalPoint
    public Vector3f bodyGetLocalPoint(Vector3f in, BodyID bodyId, Vector3f worldPoint) {
        var vec = b3Body_GetLocalPoint(this.returnArena, bodyID(bodyId), vec3(worldPoint));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetLocalVector
    public Vector3f bodyGetLocalVector(Vector3f in, BodyID bodyId, Vector3f worldVector) {
        var vec = b3Body_GetLocalVector(this.returnArena, bodyID(bodyId), vec3(worldVector));
        return PrimitveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetType
    public BodyType bodyGetType(BodyID bodyId) {
        var type = b3Body_GetType(bodyID(bodyId));
        return BodyType.fromCode(type);
    }

    /// @api b3Shape_GetBody
    public BodyID shapeGetBody(ShapeID shapeId) {
        var body = b3Shape_GetBody(this.returnArena, shapeID(shapeId));
        return BodyID.of(null, null, body);
    }

    /// @api b3Body_GetMass
    public float bodyGetMass(BodyID bodyId) {
        return b3Body_GetMass(bodyID(bodyId));
    }

    /// @api b3World_GetBodyEvents
    public MemoryIterator<BodyMoveEvent> worldGetBodyEvents() {
        var eventSegment = b3World_GetBodyEvents(this.returnArena, this.worldIDSegment);

        var moveCount = b3BodyEvents.moveCount(eventSegment);
        var moveEvents = b3BodyEvents.moveEvents(eventSegment);

        var moveEventByteSize = b3BodyMoveEvent.layout().byteSize();
        moveEvents = moveEvents.asSlice(moveEventByteSize * moveCount);

        return new MemoryIterator<>(
                new BodyMoveEvent(),
                moveEvents,
                moveEventByteSize,
                BodyMoveEvent::set
        );
    }

    /// @api b3World_Step
    public void worldStep(WorldID worldID, float timeStep, int subStepCount) {
        b3World_Step(worldID(worldID), timeStep, subStepCount);
    }

    /// @api b3World_Draw
    public void worldDraw(
            WorldID worldID,
            DebugDraw debugDraw,
            @U64 long maskBits
    ) {
        b3World_Draw(worldID(worldID), debugDraw.segment(), maskBits);
    }


    /// @api b3GetVersion
    public Version getVersion() {
        return Version.of(b3GetVersion(this.returnArena));
    }

    /// @api b3GetLengthUnitsPerMeter
    public float getLengthUnitsPerMeter() {
        return b3GetLengthUnitsPerMeter();
    }

    /// @api b3DestroyWorld
    void destroyWorld(int index1, int generation) {
        b3DestroyWorld(worldID(index1, generation));
    }

    /// @api b3DestroyHull
    void destroyHull(MemorySegment segment) {
        b3DestroyHull(segment);
    }

    /// @api b3DestroyMesh
    void destroyMesh(MemorySegment segment) {
        b3DestroyMesh(segment);
    }

    /// @api b3DestroyHeightField
    void destroyHeightField(MemorySegment segment) {
        b3DestroyHeightField(segment);
    }

    /// @api b3DestroyBody
    void destoryBody(int index1, int world0, int generation) {
        b3DestroyBody(bodyID(index1, world0, generation));
    }

    /// @api b3GetLengthUnitsPerMeter
    static float lengthUnitsPerMeter() {
        return b3GetLengthUnitsPerMeter();
    }
}

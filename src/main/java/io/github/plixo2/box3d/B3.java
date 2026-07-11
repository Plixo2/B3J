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

public final class B3 {

    public static final long DEFAULT_CATEGORY_BITS = U64_MAX;
    public static final long DEFAULT_MASK_BITS = U64_MAX;
    public static final @U8 int HEIGHT_FIELD_HOLE = B3_HEIGHT_FIELD_HOLE;

    private static final ThreadLocal<B3> tls = ThreadLocal.withInitial(B3::new);

    private B3() {}

    public static B3 get() {
        return tls.get();
    }

    static final int SECRET_COOKIE = 1152023; // took your cookie

    private final Arena scratchArena = Arena.ofAuto();
    private final ConstArena returnArena = new ConstArena(this.scratchArena, 1024);      // 1 KB
    private final StackArena argArena    = new StackArena(this.scratchArena, 32 * 1024); // 32 KB

    private final MemorySegment worldIDSegment = b3WorldId.allocate(this.scratchArena);
    private final MemorySegment bodyIDSegment = b3BodyId.allocate(this.scratchArena);
    private final MemorySegment contactIDSegment = b3ContactId.allocate(this.scratchArena);
    private final MemorySegment shapeIDSegment = b3ShapeId.allocate(this.scratchArena);
    private final MemorySegment jointIDSegment = b3JointId.allocate(this.scratchArena);
    private final MemorySegment vec3Segment = b3Vec3.allocate(this.scratchArena);
    private final MemorySegment quatSegment = b3Quat.allocate(this.scratchArena);
    private final MemorySegment vec3Segment2 = b3Vec3.allocate(this.scratchArena);
    private final MemorySegment transformSegment = b3Transform.allocate(this.scratchArena);
    private final MemorySegment aabbSegment = b3AABB.allocate(this.scratchArena);

    private final Quaternionf scratchQuat = new Quaternionf();
    private final ScratchCastResultFcn scratchCastFn = new ScratchCastResultFcn(this.scratchArena);
    private final ScratchOverlapAABB scratchOverlapAABB = new ScratchOverlapAABB(this.scratchArena);

    private final SimplexCache emptyDistanceCache = new SimplexCache();

    /// @api b3CreateWorld
    public WorldID createWorld(
            Region region,
            WorldDef worldDef
    ) {
        try (this.argArena) {
            var result = worldDef.create(this.argArena);
            var taskPool = result.taskPool();
            var shapes = result.shapes();

            var segment = b3CreateWorld(this.returnArena, result.segment());

            return WorldID.of(
                    this,
                    region,
                    taskPool,
                    shapes,
                    segment
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
            var bodyID = b3CreateBody(
                    this.returnArena,
                    worldID(worldID),
                    bodyDef.create(this.argArena)
            );
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
            var shapeID = b3CreateHullShape(
                    this.returnArena,
                    bodyID(bodyID),
                    def.create(this.argArena),
                    hull.segment
            );
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

        MemorySegment hull;

        try (var arena = Arena.ofConfined()) {
             hull = b3CreateHull(
                     ensureOffHeap(arena, points),
                     vertexCount,
                     maxVertexCount
             );
        }

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
        try (var arena = Arena.ofConfined()) {
            var def = meshDef.create(arena);

            MemorySegment mesh;

            if (degenerateTriangleIndices == null) {
                mesh = b3CreateMesh(def, MemorySegment.NULL, 0);
            } else {
                var cap = degenerateTriangleIndices.length;

                // cannot use MemorySegment.ofArray, has to be off-heap
                try (var degenerateArena = Arena.ofConfined()) {
                    var data = degenerateArena.allocate(ValueLayout.JAVA_INT.byteSize() * cap);

                    mesh = b3CreateMesh(def, data, cap);

                    // copy back
                    MemorySegment.copy(
                            data, ValueLayout.JAVA_INT, 0, // src
                            degenerateTriangleIndices, 0,  // dst
                            cap                            // length
                    );
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
        try (var arena = Arena.ofConfined()) {
            var segment = def.create(arena);
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

    /// @api b3World_OverlapAABB
    @Contract("null, _, _, _, _ -> null; !null, _, _, _, _ -> !null")
    public @Nullable TreeStats worldOverlapAABB(
            @Nullable TreeStats statsIn,
            WorldID worldID,
            AABB aabb,
            QueryFilter filter,
            OverlapResult fcn
    ) {

        var stats = this.scratchOverlapAABB.invoke(
                this.returnArena,
                worldID(worldID),
                aabb(aabb),
                filter.segment,
                fcn
        );

        if (statsIn != null) {
            statsIn.set(stats);
        }
        return statsIn;

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

        var stats = this.scratchCastFn.invoke(
                this.returnArena,
                worldID(worldID),
                vec3(origin),
                vec3_2(translation),
                filter.segment,
                fcn
        );

        if (statsIn != null) {
            statsIn.set(stats);
        }
        return statsIn;

    }

    /// @return true if `hit`, false otherwise
    /// @api b3World_CastRayClosest
    public boolean worldCastRayClosest(
            @Nullable RayResult in,
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
            return false;
        }

        if (in != null) {
            in.set(result);
        }

        return true;

    }

    /// @api b3RayCastSphere
    public boolean rayCastSphere(
            CastOutput in,
            Sphere sphere,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastSphere(
                    this.returnArena,
                    sphere.create(this.argArena),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3RayCastHollowSphere
    public boolean rayCastHollowSphere(
            CastOutput in,
            Sphere sphere,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastHollowSphere(
                    this.returnArena,
                    sphere.create(this.argArena),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3RayCastCapsule
    public boolean rayCastCapsule(
            CastOutput in,
            Capsule capsule,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastCapsule(
                    this.returnArena,
                    capsule.create(this.argArena),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3RayCastHull
    public boolean rayCastHull(
            CastOutput in,
            HullData hull,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastHull(
                    this.returnArena,
                    hull.segment,
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }


    /// @api b3RayCastMesh
    public boolean rayCastMesh(
            CastOutput in,
            Mesh mesh,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastMesh(
                    this.returnArena,
                    mesh.create(this.argArena),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3RayCastHeightField
    public boolean rayCastHeightField(
            CastOutput in,
            HeightFieldData heightField,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastHeightField(
                    this.returnArena,
                    heightField.segment(),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }


    /// @api b3ShapeCastSphere
    public boolean shapeCastSphere(
            CastOutput in,
            Sphere sphere,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastSphere(
                    this.returnArena,
                    sphere.create(this.argArena),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3ShapeCastCapsule
    public boolean shapeCastCapsule(
            CastOutput in,
            Capsule capsule,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastCapsule(
                    this.returnArena,
                    capsule.create(this.argArena),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3ShapeCastHull
    public boolean shapeCastHull(
            CastOutput in,
            HullData hull,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastHull(
                    this.returnArena,
                    hull.segment,
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3ShapeCastMesh
    public boolean shapeCastMesh(
            CastOutput in,
            Mesh mesh,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastMesh(
                    this.returnArena,
                    mesh.create(this.argArena),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }

    /// @api b3ShapeCastHeightField
    public boolean shapeCastHeightField(
            CastOutput in,
            HeightFieldData heightField,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastHeightField(
                    this.returnArena,
                    heightField.segment(),
                    input.create(this.argArena)
            );

            if (!CastOutput.hit(result)) {
                return false;
            }
            in.set(result);

            return true;
        }

    }


    /// @api b3OverlapHull
    public boolean overlapHull(HullData hullData, Matrix4f transform, ShapeProxy proxy) {
        try (this.argArena) {
            return b3OverlapHull(
                    hullData.segment,
                    transform(transform),
                    proxy.create(this.argArena)
            );
        }
    }

    /// @api b3OverlapMesh
    public boolean overlapMesh(Mesh mesh, Matrix4f transform, ShapeProxy proxy) {
        try (this.argArena) {
            return b3OverlapMesh(
                    mesh.create(this.argArena),
                    transform(transform),
                    proxy.create(this.argArena)
            );
        }
    }

    /// @api b3OverlapHeightField
    public boolean overlapHeightField(HeightFieldData heightFieldData, Matrix4f transform, ShapeProxy proxy) {
        try (this.argArena) {
            return b3OverlapHeightField(
                    heightFieldData.segment(),
                    transform(transform),
                    proxy.create(this.argArena)
            );
        }
    }

    /// @api b3OverlapCapsule
    public boolean overlapCapsule(Capsule capsule, Matrix4f transform, ShapeProxy proxy) {
        try (this.argArena) {
            return b3OverlapCapsule(
                    capsule.create(this.argArena),
                    transform(transform),
                    proxy.create(this.argArena)
            );
        }
    }

    /// @api b3OverlapSphere
    public boolean overlapSphere(Sphere sphere, Matrix4f transform, ShapeProxy proxy) {
        try (this.argArena) {
            return b3OverlapSphere(
                    sphere.create(this.argArena),
                    transform(transform),
                    proxy.create(this.argArena)
            );
        }
    }

    /// @api b3ShapeDistance
    @Contract("null, _, _, _ -> null; !null, _, _, _ -> !null")
    public @Nullable DistanceOutput shapeDistance(
            @Nullable DistanceOutput in,
            DistanceInput input,
            @Nullable SimplexCache cache,
            @Nullable Simplexes simplexes
    ) {
        var simplexesSegment = simplexes != null
                ? simplexes.segment
                : MemorySegment.NULL;

        var simplexCapacity = simplexes != null
                ? simplexes.capacity
                : 0;

        SimplexCache simplexCache;
        if (cache != null) {
            simplexCache = cache;
        } else {
            this.emptyDistanceCache.clear();
            simplexCache = this.emptyDistanceCache;
        }
        try (this.argArena) {
            var distanceOutput = b3ShapeDistance(
                    this.returnArena,
                    input.create(this.scratchQuat, this.argArena),
                    simplexCache.segment,
                    simplexesSegment,
                    simplexCapacity
            );

            if (in != null) {
                in.set(distanceOutput);
            }

            return in;
        }
    }


    /// convenience method
    ///
    /// @api b3CreateDistanceJoint
    /// @api b3CreateFilterJoint
    /// @api b3CreateMotorJoint
    /// @api b3CreateParallelJoint
    /// @api b3CreatePrismaticJoint
    /// @api b3CreateRevoluteJoint
    /// @api b3CreateSphericalJoint
    /// @api b3CreateWeldJoint
    /// @api b3CreateWheelJoint
    public <T extends JointType> JointID<T> createJoint(
            WorldID worldID,
            AbstractJointDef<T> def
    ) {
        //noinspection unchecked
        return (JointID<T>) switch (def) {
            case DistanceJointDef distance -> createDistanceJoint(worldID, distance);
            case FilterJointDef filter -> createFilterJoint(worldID, filter);
            case MotorJointDef motor -> createMotorJoint(worldID, motor);
            case ParallelJointDef parallel -> createParallelJoint(worldID, parallel);
            case PrismaticJointDef prismatic -> createPrismaticJoint(worldID, prismatic);
            case RevoluteJointDef revolute -> createRevoluteJoint(worldID, revolute);
            case SphericalJointDef spherical -> createSphericalJoint(worldID, spherical);
            case WeldJointDef weld -> createWeldJoint(worldID, weld);
            case WheelJointDef wheel -> createWheelJoint(worldID, wheel);
        };
    }


    /// @api b3CreateRevoluteJoint
    public JointID<JointType.Revolute> createRevoluteJoint(
            WorldID worldID,
            RevoluteJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateRevoluteJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreatePrismaticJoint
    public JointID<JointType.Prismatic> createPrismaticJoint(
            WorldID worldID,
            PrismaticJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreatePrismaticJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreateWeldJoint
    public JointID<JointType.Weld> createWeldJoint(
            WorldID worldID,
            WeldJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateWeldJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreateWheelJoint
    public JointID<JointType.Wheel> createWheelJoint(
            WorldID worldID,
            WheelJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateWheelJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreateSphericalJoint
    public JointID<JointType.Spherical> createSphericalJoint(
            WorldID worldID,
            SphericalJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateSphericalJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreateParallelJoint
    public JointID<JointType.Parallel> createParallelJoint(
            WorldID worldID,
            ParallelJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateParallelJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreateDistanceJoint
    public JointID<JointType.Distance> createDistanceJoint(
            WorldID worldID,
            DistanceJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateDistanceJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreateMotorJoint
    public JointID<JointType.Motor> createMotorJoint(
            WorldID worldID,
            MotorJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateMotorJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3CreateFilterJoint
    public JointID<JointType.Filter> createFilterJoint(
            WorldID worldID,
            FilterJointDef def
    ) {
        try (this.argArena) {
            var jointID = b3CreateFilterJoint(
                    this.returnArena,
                    worldID(worldID),
                    def.create(this.scratchQuat, this.argArena)
            );
            return JointID.of(jointID);
        }
    }

    /// @api b3Body_GetPosition
    public Vector3f bodyGetPosition(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetPosition(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetRotation
    public Quaternionf bodyGetRotation(Quaternionf in, BodyID bodyId) {
        var quat = b3Body_GetRotation(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setQuat(in, quat);
    }

    /// @api b3Body_GetTransform
    public Matrix4f bodyGetTransform(Matrix4f in, BodyID bodyId) {
        var transform = b3Body_GetTransform(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setTransform(in, transform);
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
        return PrimitiveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetAngularVelocity
    public Vector3f bodyGetAngularVelocity(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetAngularVelocity(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(in, vec);
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
        return PrimitiveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetLocalCenterOfMass
    public Vector3f bodyGetLocalCenterOfMass(Vector3f in, BodyID bodyId) {
        var vec = b3Body_GetLocalCenterOfMass(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(in, vec);
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
        return PrimitiveMemOps.setVec3(in, vec);
    }


    /// @api b3Body_GetWorldVector
    public Vector3f bodyGetWorldVector(Vector3f in, BodyID bodyId, Vector3f localVector) {
        var vec = b3Body_GetWorldVector(this.returnArena, bodyID(bodyId), vec3(localVector));
        return PrimitiveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetLocalPoint
    public Vector3f bodyGetLocalPoint(Vector3f in, BodyID bodyId, Vector3f worldPoint) {
        var vec = b3Body_GetLocalPoint(this.returnArena, bodyID(bodyId), vec3(worldPoint));
        return PrimitiveMemOps.setVec3(in, vec);
    }

    /// @api b3Body_GetLocalVector
    public Vector3f bodyGetLocalVector(Vector3f in, BodyID bodyId, Vector3f worldVector) {
        var vec = b3Body_GetLocalVector(this.returnArena, bodyID(bodyId), vec3(worldVector));
        return PrimitiveMemOps.setVec3(in, vec);
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

    /// @api b3Body_GetName
    public @Nullable String bodyGetName(BodyID bodyId) {
        var name = b3Body_GetName(bodyID(bodyId));
        return Internal.getNullString(name);
    }

    /// @api b3Body_SetName
    public void bodySetName(BodyID bodyId, @Nullable String name) {
        try (this.argArena) {
            var segment = allocNullString(this.argArena, name);
            b3Body_SetName(bodyID(bodyId), segment);
        }
    }

    /// @api b3Body_EnableSleep
    public void bodyEnableSleep(BodyID bodyId, boolean enable) {
        b3Body_EnableSleep(bodyID(bodyId), enable);
    }

    /// @api b3Shape_IsValid
    public boolean shapeIsValid(ShapeID shapeId) {
        return b3Shape_IsValid(shapeID(shapeId));
    }

    /// @api b3World_IsValid
    public boolean worldIsValid(WorldID worldId) {
        return b3World_IsValid(worldID(worldId));
    }

    /// @api b3Body_IsValid
    public boolean bodyIsValid(BodyID bodyId) {
        return b3Body_IsValid(bodyID(bodyId));
    }

    /// @api b3Joint_IsValid
    public boolean jointIsValid(JointID<?> jointId) {
        return b3Joint_IsValid(jointID(jointId));
    }

    /// @api b3World_GetBodyEvents
    public Iterable<BodyMoveEvent> worldGetBodyEvents() {
        var eventSegment = b3World_GetBodyEvents(this.returnArena, this.worldIDSegment);

        var moveEvents = b3BodyEvents.moveEvents(eventSegment);
        var moveCount = b3BodyEvents.moveCount(eventSegment);

        return () -> new BodyMoveEvent.Iterator(moveEvents, moveCount);
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
        b3jshimWorld_Draw(worldID(worldID), debugDraw.segment(), maskBits);
        debugDraw.invoke();
    }


    /// @api b3GetVersion
    public Version getVersion() {
        return Version.of(b3GetVersion(this.returnArena));
    }

    /// @api b3GetLengthUnitsPerMeter
    public float getLengthUnitsPerMeter() {
        return b3GetLengthUnitsPerMeter();
    }

    /// @api b3DestroyBody
    public void destroyBody(BodyID bodyId) {
        if (!bodyIsValid(bodyId)) {
            throw new IllegalStateException("Body " + bodyId + " is not valid anymore");
        }
        var segment = bodyID(bodyId);
        bodyId.state.once();
        b3DestroyBody(segment);
    }


    /// @api b3DestroyJoint
    public void destroyJoint(JointID<?> jointID, boolean wakeAttached) {
        if (!jointIsValid(jointID)) {
            throw new IllegalStateException("Joint " + jointID + " is not valid anymore");
        }
        b3DestroyJoint(jointID(jointID), wakeAttached);
    }


    /// @api b3DestroyShape
    public void destroyShape(ShapeID shapeID, boolean updateBodyMass) {
        if (!shapeIsValid(shapeID)) {
            throw new IllegalStateException("Shape " +  shapeID + " is not valid anymore");
        }
        b3DestroyShape(shapeID(shapeID), updateBodyMass);
    }

    /// @api b3DestroyWorld
    public void destroyWorld(WorldID worldID) {
        try {
            if (!worldIsValid(worldID)) {
                throw new IllegalStateException("World " +  worldID + " is not valid anymore");
            }

            var segment = worldID(worldID);
            worldID.state.once();
            b3DestroyWorld(segment);

        } finally {
            if (worldID.taskPool != null) {
                worldID.taskPool.close();
            }
            if (worldID.shapes != null) {
                worldID.shapes.close();
            }
        }
    }

    /// @api b3DestroyMesh
    public void destroyMesh(MeshData meshData) {
        var segment = meshData.segment();
        meshData.state.once();
        b3DestroyMesh(segment);
    }

    /// @api b3DestroyHeightField
    public void destroyHeightField(HeightFieldData data) {
        var segment = data.segment();
        data.state.once();
        b3DestroyHeightField(segment);
    }






    /// @api b3DestroyWorld
    void destroyWorld(int index1, int generation) {
        var segment = worldID(index1, generation);
        if (!b3World_IsValid(segment)) {
            throw new IllegalStateException(
                    "World " +  WorldID.toString(index1, generation) + " is not valid anymore"
            );
        }

        b3DestroyWorld(segment);
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
    void destroyBody(long packedID) {
        var segment = bodyID(packedID);
        if (!b3Body_IsValid(segment)) {
            throw new IllegalStateException(
                    "Body " + BodyID.toString(packedID) + " is not valid anymore"
            );
        }
        b3DestroyBody(segment);
    }

    /// @api b3GetLengthUnitsPerMeter
    static float lengthUnitsPerMeter() {
        return b3GetLengthUnitsPerMeter();
    }




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
        return bodyID(bodyID.packedID());
    }
    private MemorySegment bodyID(long packedID) {
        PrimitiveMemOps.putBodyID(this.bodyIDSegment, packedID);
        return this.bodyIDSegment;
    }
    private MemorySegment contactID(ContactID contactID) {
        b3ContactId.index1(this.contactIDSegment, contactID.index1());
        b3ContactId.world0(this.contactIDSegment, assertU16(contactID.world0(), "world0"));
        b3ContactId.generation(this.contactIDSegment, assertU32(contactID.generation(), "generation"));
        return this.contactIDSegment;
    }
    private MemorySegment shapeID(ShapeID shapeID) {
        PrimitiveMemOps.putShapeID(this.shapeIDSegment, shapeID.packedID());
        return this.shapeIDSegment;
    }
    private MemorySegment jointID(JointID<?> jointID) {
        return jointID(jointID.packedID());
    }
    private MemorySegment jointID(long packedID) {
        PrimitiveMemOps.putJointID(this.jointIDSegment, packedID);
        return this.jointIDSegment;
    }
    private MemorySegment transform(Matrix4f transform) {
        PrimitiveMemOps.putTransform(this.transformSegment, this.scratchQuat, transform);
        return this.transformSegment;
    }
    private MemorySegment vec3(Vector3f vec) {
        PrimitiveMemOps.putVec3(this.vec3Segment, vec);
        return this.vec3Segment;
    }
    private MemorySegment vec3_2(Vector3f vec) {
        PrimitiveMemOps.putVec3(this.vec3Segment2, vec);
        return this.vec3Segment2;
    }
    private MemorySegment quat(Quaternionf quat) {
        PrimitiveMemOps.putQuat(this.quatSegment, quat);
        return this.quatSegment;
    }
    private MemorySegment aabb(AABB aabb) {
        aabb.put(this.aabbSegment);
        return this.aabbSegment;
    }

}

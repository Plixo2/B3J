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
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3RayCastHollowSphere
    public boolean rayCastHollowSphere(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3RayCastCapsule
    public boolean rayCastCapsule(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3RayCastHull
    public boolean rayCastHull(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }


    /// @api b3RayCastMesh
    public boolean rayCastMesh(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3RayCastHeightField
    public boolean rayCastHeightField(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }


    /// @api b3ShapeCastSphere
    public boolean shapeCastSphere(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3ShapeCastCapsule
    public boolean shapeCastCapsule(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3ShapeCastHull
    public boolean shapeCastHull(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3ShapeCastMesh
    public boolean shapeCastMesh(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

            return true;
        }

    }

    /// @api b3ShapeCastHeightField
    public boolean shapeCastHeightField(
            @Nullable CastOutput in,
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

            if (in != null) {
                in.set(result);
            }

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


    // #HERE ParallelJoint

    /// @api b3ParallelJoint_SetSpringHertz
    public void parallelJointSetSpringHertz(JointID<JointType.Parallel> jointID) {

    }
    /// @api b3ParallelJoint_SetSpringDampingRatio
    public void parallelJointSetSpringDampingRatio(JointID<JointType.Parallel> jointID) {

    }
    /// @api b3ParallelJoint_GetSpringHertz
    public void parallelJointGetSpringHertz(JointID<JointType.Parallel> jointID) {

    }
    /// @api b3ParallelJoint_GetSpringDampingRatio
    public void parallelJointGetSpringDampingRatio(JointID<JointType.Parallel> jointID) {

    }
    /// @api b3ParallelJoint_SetMaxTorque
    public void parallelJointSetMaxTorque(JointID<JointType.Parallel> jointID) {

    }
    /// @api b3ParallelJoint_GetMaxTorque
    public void parallelJointGetMaxTorque(JointID<JointType.Parallel> jointID) {

    }

    // #HERE DistanceJoint

    /// @api b3DistanceJoint_SetLength
    public void distanceJointSetLength(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetLength
    public void distanceJointGetLength(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_EnableSpring
    public void distanceJointEnableSpring(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_IsSpringEnabled
    public void distanceJointIsSpringEnabled(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_SetSpringForceRange
    public void distanceJointSetSpringForceRange(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetSpringForceRange
    public void distanceJointGetSpringForceRange(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_SetSpringHertz
    public void distanceJointSetSpringHertz(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_SetSpringDampingRatio
    public void distanceJointSetSpringDampingRatio(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetSpringHertz
    public void distanceJointGetSpringHertz(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetSpringDampingRatio
    public void distanceJointGetSpringDampingRatio(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_EnableLimit
    public void distanceJointEnableLimit(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_IsLimitEnabled
    public void distanceJointIsLimitEnabled(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_SetLengthRange
    public void distanceJointSetLengthRange(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetMinLength
    public void distanceJointGetMinLength(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetMaxLength
    public void distanceJointGetMaxLength(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetCurrentLength
    public void distanceJointGetCurrentLength(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_EnableMotor
    public void distanceJointEnableMotor(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_IsMotorEnabled
    public void distanceJointIsMotorEnabled(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_SetMotorSpeed
    public void distanceJointSetMotorSpeed(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetMotorSpeed
    public void distanceJointGetMotorSpeed(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_SetMaxMotorForce
    public void distanceJointSetMaxMotorForce(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetMaxMotorForce
    public void distanceJointGetMaxMotorForce(JointID<JointType.Distance> jointID) {

    }
    /// @api b3DistanceJoint_GetMotorForce
    public void distanceJointGetMotorForce(JointID<JointType.Distance> jointID) {

    }

    // #HERE MotorJoint

    /// @api b3MotorJoint_SetLinearVelocity
    public void motorJointSetLinearVelocity(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetLinearVelocity
    public void motorJointGetLinearVelocity(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetAngularVelocity
    public void motorJointSetAngularVelocity(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetAngularVelocity
    public void motorJointGetAngularVelocity(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetMaxVelocityForce
    public void motorJointSetMaxVelocityForce(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetMaxVelocityForce
    public void motorJointGetMaxVelocityForce(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetMaxVelocityTorque
    public void motorJointSetMaxVelocityTorque(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetMaxVelocityTorque
    public void motorJointGetMaxVelocityTorque(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetLinearHertz
    public void motorJointSetLinearHertz(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetLinearHertz
    public void motorJointGetLinearHertz(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetLinearDampingRatio
    public void motorJointSetLinearDampingRatio(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetLinearDampingRatio
    public void motorJointGetLinearDampingRatio(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetAngularHertz
    public void motorJointSetAngularHertz(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetAngularHertz
    public void motorJointGetAngularHertz(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetAngularDampingRatio
    public void motorJointSetAngularDampingRatio(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetAngularDampingRatio
    public void motorJointGetAngularDampingRatio(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetMaxSpringForce
    public void motorJointSetMaxSpringForce(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetMaxSpringForce
    public void motorJointGetMaxSpringForce(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_SetMaxSpringTorque
    public void motorJointSetMaxSpringTorque(JointID<JointType.Motor> jointID) {

    }
    /// @api b3MotorJoint_GetMaxSpringTorque
    public void motorJointGetMaxSpringTorque(JointID<JointType.Motor> jointID) {

    }

    // #HERE PrismaticJoint

    /// @api b3PrismaticJoint_EnableSpring
    public void prismaticJointEnableSpring(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_IsSpringEnabled
    public void prismaticJointIsSpringEnabled(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_SetSpringHertz
    public void prismaticJointSetSpringHertz(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetSpringHertz
    public void prismaticJointGetSpringHertz(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_SetSpringDampingRatio
    public void prismaticJointSetSpringDampingRatio(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetSpringDampingRatio
    public void prismaticJointGetSpringDampingRatio(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_SetTargetTranslation
    public void prismaticJointSetTargetTranslation(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetTargetTranslation
    public void prismaticJointGetTargetTranslation(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_EnableLimit
    public void prismaticJointEnableLimit(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_IsLimitEnabled
    public void prismaticJointIsLimitEnabled(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetLowerLimit
    public void prismaticJointGetLowerLimit(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetUpperLimit
    public void prismaticJointGetUpperLimit(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_SetLimits
    public void prismaticJointSetLimits(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_EnableMotor
    public void prismaticJointEnableMotor(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_IsMotorEnabled
    public void prismaticJointIsMotorEnabled(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_SetMotorSpeed
    public void prismaticJointSetMotorSpeed(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetMotorSpeed
    public void prismaticJointGetMotorSpeed(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_SetMaxMotorForce
    public void prismaticJointSetMaxMotorForce(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetMaxMotorForce
    public void prismaticJointGetMaxMotorForce(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetMotorForce
    public void prismaticJointGetMotorForce(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetTranslation
    public void prismaticJointGetTranslation(JointID<JointType.Prismatic> jointID) {

    }
    /// @api b3PrismaticJoint_GetSpeed
    public void prismaticJointGetSpeed(JointID<JointType.Prismatic> jointID) {

    }

    // #HERE RevoluteJoint

    /// @api b3RevoluteJoint_EnableSpring
    public void revoluteJointEnableSpring(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_IsSpringEnabled
    public void revoluteJointIsSpringEnabled(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_SetSpringHertz
    public void revoluteJointSetSpringHertz(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetSpringHertz
    public void revoluteJointGetSpringHertz(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_SetSpringDampingRatio
    public void revoluteJointSetSpringDampingRatio(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetSpringDampingRatio
    public void revoluteJointGetSpringDampingRatio(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_SetTargetAngle
    public void revoluteJointSetTargetAngle(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetTargetAngle
    public void revoluteJointGetTargetAngle(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetAngle
    public void revoluteJointGetAngle(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_EnableLimit
    public void revoluteJointEnableLimit(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_IsLimitEnabled
    public void revoluteJointIsLimitEnabled(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetLowerLimit
    public void revoluteJointGetLowerLimit(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetUpperLimit
    public void revoluteJointGetUpperLimit(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_SetLimits
    public void revoluteJointSetLimits(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_EnableMotor
    public void revoluteJointEnableMotor(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_IsMotorEnabled
    public void revoluteJointIsMotorEnabled(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_SetMotorSpeed
    public void revoluteJointSetMotorSpeed(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetMotorSpeed
    public void revoluteJointGetMotorSpeed(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetMotorTorque
    public void revoluteJointGetMotorTorque(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_SetMaxMotorTorque
    public void revoluteJointSetMaxMotorTorque(JointID<JointType.Revolute> jointID) {

    }
    /// @api b3RevoluteJoint_GetMaxMotorTorque
    public void revoluteJointGetMaxMotorTorque(JointID<JointType.Revolute> jointID) {

    }


    // #HERE SphericalJoints

    /// @api b3SphericalJoint_EnableConeLimit
    public void sphericalJoint_EnableConeLimit(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_IsConeLimitEnabled
    public void sphericalJoint_IsConeLimitEnabled(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetConeLimit
    public void sphericalJoint_GetConeLimit(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_SetConeLimit
    public void sphericalJoint_SetConeLimit(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetConeAngle
    public void sphericalJoint_GetConeAngle(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_EnableTwistLimit
    public void sphericalJoint_EnableTwistLimit(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_IsTwistLimitEnabled
    public void sphericalJoint_IsTwistLimitEnabled(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetLowerTwistLimit
    public void sphericalJoint_GetLowerTwistLimit(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetUpperTwistLimit
    public void sphericalJoint_GetUpperTwistLimit(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_SetTwistLimits
    public void sphericalJoint_SetTwistLimits(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetTwistAngle
    public void sphericalJoint_GetTwistAngle(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_EnableSpring
    public void sphericalJoint_EnableSpring(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_IsSpringEnabled
    public void sphericalJoint_IsSpringEnabled(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_SetSpringHertz
    public void sphericalJoint_SetSpringHertz(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetSpringHertz
    public void sphericalJoint_GetSpringHertz(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_SetSpringDampingRatio
    public void sphericalJoint_SetSpringDampingRatio(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetSpringDampingRatio
    public void sphericalJoint_GetSpringDampingRatio(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_SetTargetRotation
    public void sphericalJoint_SetTargetRotation(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetTargetRotation
    public void sphericalJoint_GetTargetRotation(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_EnableMotor
    public void sphericalJoint_EnableMotor(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_IsMotorEnabled
    public void sphericalJoint_IsMotorEnabled(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_SetMotorVelocity
    public void sphericalJoint_SetMotorVelocity(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetMotorVelocity
    public void sphericalJoint_GetMotorVelocity(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetMotorTorque
    public void sphericalJoint_GetMotorTorque(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_SetMaxMotorTorque
    public void sphericalJoint_SetMaxMotorTorque(JointID<JointType.Spherical> jointID) {

    }
    /// @api b3SphericalJoint_GetMaxMotorTorque
    public void sphericalJoint_GetMaxMotorTorque(JointID<JointType.Spherical> jointID) {

    }

    // #HERE WeldJoint

    /// @api b3WeldJoint_SetLinearHertz
    public void weldJointSetLinearHertz(JointID<JointType.Weld> jointID) {

    }
    /// @api b3WeldJoint_GetLinearHertz
    public void weldJointGetLinearHertz(JointID<JointType.Weld> jointID) {

    }
    /// @api b3WeldJoint_SetLinearDampingRatio
    public void weldJointSetLinearDampingRatio(JointID<JointType.Weld> jointID) {

    }
    /// @api b3WeldJoint_GetLinearDampingRatio
    public void weldJointGetLinearDampingRatio(JointID<JointType.Weld> jointID) {

    }
    /// @api b3WeldJoint_SetAngularHertz
    public void weldJointSetAngularHertz(JointID<JointType.Weld> jointID) {

    }
    /// @api b3WeldJoint_GetAngularHertz
    public void weldJointGetAngularHertz(JointID<JointType.Weld> jointID) {

    }
    /// @api b3WeldJoint_SetAngularDampingRatio
    public void weldJointSetAngularDampingRatio(JointID<JointType.Weld> jointID) {

    }
    /// @api b3WeldJoint_GetAngularDampingRatio
    public void weldJointGetAngularDampingRatio(JointID<JointType.Weld> jointID) {

    }

    // #HERE WheelJoint

    /// @api b3WheelJoint_EnableSuspension
    public void wheelJointEnableSuspension(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_IsSuspensionEnabled
    public void wheelJointIsSuspensionEnabled(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetSuspensionHertz
    public void wheelJointSetSuspensionHertz(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSuspensionHertz
    public void wheelJointGetSuspensionHertz(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetSuspensionDampingRatio
    public void wheelJointSetSuspensionDampingRatio(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSuspensionDampingRatio
    public void wheelJointGetSuspensionDampingRatio(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_EnableSuspensionLimit
    public void wheelJointEnableSuspensionLimit(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_IsSuspensionLimitEnabled
    public void wheelJointIsSuspensionLimitEnabled(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetLowerSuspensionLimit
    public void wheelJointGetLowerSuspensionLimit(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetUpperSuspensionLimit
    public void wheelJointGetUpperSuspensionLimit(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetSuspensionLimits
    public void wheelJointSetSuspensionLimits(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_EnableSpinMotor
    public void wheelJointEnableSpinMotor(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_IsSpinMotorEnabled
    public void wheelJointIsSpinMotorEnabled(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetSpinMotorSpeed
    public void wheelJointSetSpinMotorSpeed(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSpinMotorSpeed
    public void wheelJointGetSpinMotorSpeed(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetMaxSpinTorque
    public void wheelJointSetMaxSpinTorque(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetMaxSpinTorque
    public void wheelJointGetMaxSpinTorque(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSpinSpeed
    public void wheelJointGetSpinSpeed(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSpinTorque
    public void wheelJointGetSpinTorque(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_EnableSteering
    public void wheelJointEnableSteering(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_IsSteeringEnabled
    public void wheelJointIsSteeringEnabled(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetSteeringHertz
    public void wheelJointSetSteeringHertz(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSteeringHertz
    public void wheelJointGetSteeringHertz(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetSteeringDampingRatio
    public void wheelJointSetSteeringDampingRatio(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSteeringDampingRatio
    public void wheelJointGetSteeringDampingRatio(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetMaxSteeringTorque
    public void wheelJointSetMaxSteeringTorque(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetMaxSteeringTorque
    public void wheelJointGetMaxSteeringTorque(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_EnableSteeringLimit
    public void wheelJointEnableSteeringLimit(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_IsSteeringLimitEnabled
    public void wheelJointIsSteeringLimitEnabled(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetLowerSteeringLimit
    public void wheelJointGetLowerSteeringLimit(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetUpperSteeringLimit
    public void wheelJointGetUpperSteeringLimit(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetSteeringLimits
    public void wheelJointSetSteeringLimits(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_SetTargetSteeringAngle
    public void wheelJointSetTargetSteeringAngle(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetTargetSteeringAngle
    public void wheelJointGetTargetSteeringAngle(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSteeringAngle
    public void wheelJointGetSteeringAngle(JointID<JointType.Wheel> jointID) {

    }
    /// @api b3WheelJoint_GetSteeringTorque
    public void wheelJointGetSteeringTorque(JointID<JointType.Wheel> jointID) {

    }








    /// @api b3DestroyWorld
    void destroyWorld(long packedID) {
        var segment = worldID(packedID);
        if (!b3World_IsValid(segment)) {
            throw new IllegalStateException(
                    "World " +  WorldID.toString(packedID) + " is not valid anymore"
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
        return worldID(worldID.packedID());
    }
    private MemorySegment worldID(long packedID) {
        PrimitiveMemOps.putPackedWorldID(this.worldIDSegment, packedID);
        return this.worldIDSegment;
    }
    private MemorySegment bodyID(BodyID bodyID) {
        return bodyID(bodyID.packedID());
    }
    private MemorySegment bodyID(long packedID) {
        PrimitiveMemOps.putPackedID(this.bodyIDSegment, packedID);
        return this.bodyIDSegment;
    }
    private MemorySegment contactID(ContactID contactID) {
        b3ContactId.index1(this.contactIDSegment, contactID.index1());
        b3ContactId.world0(this.contactIDSegment, assertU16(contactID.world0(), "world0"));
        b3ContactId.generation(this.contactIDSegment, assertU32(contactID.generation(), "generation"));
        return this.contactIDSegment;
    }
    private MemorySegment shapeID(ShapeID shapeID) {
        PrimitiveMemOps.putPackedID(this.shapeIDSegment, shapeID.packedID());
        return this.shapeIDSegment;
    }
    private MemorySegment jointID(JointID<?> jointID) {
        return jointID(jointID.packedID());
    }
    private MemorySegment jointID(long packedID) {
        PrimitiveMemOps.putPackedID(this.jointIDSegment, packedID);
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

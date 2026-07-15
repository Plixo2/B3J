package io.github.plixo2.box3d;


import io.github.plixo2.box3d.internal.*;
import io.github.plixo2.box3d.region.Region;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.FloatBuffer;
import java.util.Objects;

import static io.github.plixo2.box3d.internal.B3JUtil.*;
import static org.box2d.box3d.box3d_h.*;

public final class B3 {

    static {
        B3JUtil.setLogStream(System.err);
        StaticCallbacks.installDefault();
    }

    private static final ThreadLocal<B3> tls = ThreadLocal.withInitial(B3::new);

    private B3() {}

    public static B3 get() {
        return tls.get();
    }


    //<editor-fold desc="Static" default-state="collapsed">


    /// @api B3_DEFAULT_CATEGORY_BITS
    public static final long DEFAULT_CATEGORY_BITS = U64.MAX;

    /// @api B3_DEFAULT_MASK_BITS
    public static final long DEFAULT_MASK_BITS = U64.MAX;

    /// @api B3_HEIGHT_FIELD_HOLE
    public static final @Unsigned byte HEIGHT_FIELD_HOLE = (byte) B3_HEIGHT_FIELD_HOLE;

    /// @api B3_MAX_WORKERS
    public static final @Unsigned int MAX_WORKERS = B3_MAX_WORKERS;

    /// @api B3_MAX_TASKS
    public static final @Unsigned int MAX_TASKS = B3_MAX_TASKS;

    /// @api B3_GRAPH_COLOR_COUNT
    public static final @Unsigned int GRAPH_COLOR_COUNT = B3_GRAPH_COLOR_COUNT;

    /// @api B3_CONTACT_MANIFOLD_COUNT_BUCKETS
    public static final @Unsigned int CONTACT_MANIFOLD_COUNT_BUCKETS = B3_CONTACT_MANIFOLD_COUNT_BUCKETS;

    /// @api B3_MAX_WORLDS
    public static final @Unsigned int MAX_WORLDS = B3_MAX_WORLDS;

    /// @api B3_MAX_ROTATION
    public static final float MAX_ROTATION = B3_MAX_ROTATION;

    /// @api B3_CONTACT_RECYCLE_ANGULAR_DISTANCE
    public static final float CONTACT_RECYCLE_ANGULAR_DISTANCE = B3_CONTACT_RECYCLE_ANGULAR_DISTANCE;

    /// @api B3_AABB_MARGIN_FRACTION
    public static final float AABB_MARGIN_FRACTION = B3_AABB_MARGIN_FRACTION;

    /// @api B3_TIME_TO_SLEEP
    public static final float TIME_TO_SLEEP = B3_TIME_TO_SLEEP;

    /// @api B3_BODY_NAME_LENGTH
    public static final @Unsigned int BODY_NAME_LENGTH = B3_BODY_NAME_LENGTH;

    /// @api B3_SHAPE_NAME_LENGTH
    public static final @Unsigned int SHAPE_NAME_LENGTH = B3_SHAPE_NAME_LENGTH;

    /// @api B3_MAX_MANIFOLD_POINTS
    public static final @Unsigned int MAX_MANIFOLD_POINTS = B3_MAX_MANIFOLD_POINTS;

    /// @api B3_MAX_SHAPE_CAST_POINTS
    public static final @Unsigned int MAX_SHAPE_CAST_POINTS = B3_MAX_SHAPE_CAST_POINTS;

    /// @api B3_SHAPE_POWER
    public static final @Unsigned int SHAPE_POWER = B3_SHAPE_POWER;

    /// @api B3_CHILD_POWER
    public static final @Unsigned int CHILD_POWER = B3_CHILD_POWER;

    /// @api B3_MAX_SHAPES
    public static final @Unsigned int MAX_SHAPES = B3_MAX_SHAPES;

    /// @api B3_MAX_CHILD_SHAPES
    public static final @Unsigned int MAX_CHILD_SHAPES = B3_MAX_CHILD_SHAPES;

    /// @api B3_MIN_SCALE
    public static final float MIN_SCALE = B3_MIN_SCALE;

    /// @api B3_HUGE
    public static float HUGE() {
        return 1.0e5f * b3GetLengthUnitsPerMeter();
    }
    /// @api B3_LINEAR_SLOP
    public static float LINEAR_SLOP() {
        return 0.005f * b3GetLengthUnitsPerMeter();
    }
    /// @api B3_MIN_CAPSULE_LENGTH
    public static float MIN_CAPSULE_LENGTH() {
        return LINEAR_SLOP();
    }
    /// @api B3_OVERLAP_SLOP
    public static float OVERLAP_SLOP() {
        return 0.1f * LINEAR_SLOP();
    }
    /// @api B3_SPECULATIVE_DISTANCE
    public static float SPECULATIVE_DISTANCE() {
        return 4.0f * LINEAR_SLOP();
    }
    /// @api B3_MESH_REST_OFFSET
    public static float B3_MESH_REST_OFFSET() {
        return 1.0f * LINEAR_SLOP();
    }
    /// @api B3_CONTACT_RECYCLE_DISTANCE
    public static float B3_CONTACT_RECYCLE_DISTANCE() {
        return 10.0f * LINEAR_SLOP();
    }
    /// @api B3_CONTACT_RECYCLE_ANGULAR_DISTANCE
    public static float B3_CONTACT_RECYCLE_ANGULAR_DISTANCE() {
        return 0.99240388f;
    }
    /// @api B3_MAX_AABB_MARGIN
    public static float B3_MAX_AABB_MARGIN() {
        return 0.05f * b3GetLengthUnitsPerMeter();
    }


    /// @api b3GetVersion
    public static Version getVersion() {
        try (var arena = Arena.ofConfined()) {
            return Version.of(b3GetVersion(arena));
        }
    }

    /// @api b3SetAssertFcn
    public static void setAssertFcn(@Nullable AssertFcn assertFcn) {
        StaticCallbacks.installAssersionHandler(assertFcn);
    }

    /// @api b3SetAllocator
    public static void setAllocator(@Nullable AllocNFreeFcn allocNFreeFcn) {
        StaticCallbacks.installAllocNFreeHandler(allocNFreeFcn);
    }

    /// @api b3SetLogFcn
    public static void setLogFcn(@Nullable LogFcn logFcn) {
        StaticCallbacks.installLogHandler(logFcn);
    }

    /// @api b3SetStallThreshold
    public static void setStallThreshold(float seconds) {
        b3SetStallThreshold(seconds);
    }

    /// @api b3GetStallThreshold
    public static float getStallThreshold() {
        return b3GetStallThreshold();
    }

    /// @api b3GetLengthUnitsPerMeter
    public static float getLengthUnitsPerMeter() {
        return b3GetLengthUnitsPerMeter();
    }
    /// @api b3SetLengthUnitsPerMeter
    public static void setLengthUnitsPerMeter(float lengthUnits) {
        b3SetLengthUnitsPerMeter(lengthUnits);
    }

    /// @api b3GetTicks
    public static @Unsigned long getTicks() {
        return b3GetTicks();
    }

    /// @api b3GetMilliseconds
    public static float getMilliseconds(long ticks) {
        return b3GetMilliseconds(ticks);
    }

    /// @api b3Hash
    public static @Unsigned int hash(@Unsigned int hash, MemorySegment segment) {
        return b3Hash(hash, segment, assertU32(segment.byteSize(), "segment.byteSize()"));
    }

    /// @api b3Hash
    public static @Unsigned int hash(@Unsigned int hash, MemorySegment segment, int count) {
        return b3Hash(hash, segment, count);
    }

    /// @api b3GetByteCount
    public static int getByteCount() {
        return b3GetByteCount();
    }
    //</editor-fold>

    //<editor-fold desc="Math" default-state="collapsed">


    /// @api b3ComputeCosSin
    public void computeCosSin(CosSin dest, float radians) {
        var segment = b3ComputeCosSin(this.returnArena, radians);
        dest.set(segment);
    }

    /// @api b3IsValidFloat
    public boolean isValidFloat(float a) {
        return Float.isFinite(a);
    }

    /// @api b3Atan2
    public float atan2(float y, float x) {
        if (x == 0.0f && y == 0.0f) {
            return 0.0f;
        }

        float ax = x < 0.0f ? -x : x;
        float ay = y < 0.0f ? -y : y;
        //noinspection ManualMinMaxCalculation
        float mx = ay > ax ? ay : ax;
        //noinspection ManualMinMaxCalculation
        float mn = ay < ax ? ay : ax;
        float a = mn / mx;

        float s = a * a;
        float c = s * a;
        float q = s * s;
        float r = 0.024840285f * q + 0.18681418f;
        float t = -0.094097948f * q - 0.33213072f;
        r = r * s + t;
        r = r * c + a;

        if (ay > ax) {
            r = 1.57079637f - r;
        }
        if (x < 0) {
            r = 3.14159274f - r;
        }
        if (y < 0) {
            r = -r;
        }

        return r;
    }

    /// @api b3IsValidVec3
    public boolean isValidVec3(Vector3f a) {
        return a.isFinite();
    }

    /// @api b3IsValidQuat
    public boolean isValidQuat(Quaternionf q) {
        var qq = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
        var tolerance = 20.0f * Math.ulp(1.0f);
        return q.isFinite()
                && 1.0f - tolerance < qq && qq < 1.0f + tolerance;
    }

    /// @api b3IsValidTransform
    public boolean isValidTransform(Matrix4f a) {
        if (!a.isFinite() || !PrimitiveMemOps.isRigidMatrix(a)) {
            return false;
        }
        return isValidQuat(this.scratchQuat.setFromNormalized(a));
    }

    /// @api b3IsValidMatrix3
    public boolean isValidMatrix3(Matrix3f a) {
        return a.isFinite();
    }

    /// @api b3IsValidAABB
    public boolean isValidAABB(AABB a) {
        return isValidVec3(a.lowerBound) && isValidVec3(a.upperBound)
                && a.lowerBound.x <= a.upperBound.x
                && a.lowerBound.y <= a.upperBound.y
                && a.lowerBound.z <= a.upperBound.z;
    }

    /// @api b3Clamp
    public Vector3f clamp(Vector3f dest, Vector3f a, Vector3f lower, Vector3f upper) {
        return dest.set(a).max(lower).min(upper);
    }

    /// @api b3IsBoundedAABB
    public boolean isBoundedAABB(AABB a) {
        var lower = a.lowerBound;
        var upper = a.upperBound;
        var huge = HUGE();

        return
                lower.x >= -huge && lower.y >= -huge && lower.z >= -huge
                && upper.x <= huge && upper.y <= huge && upper.z <= huge;
    }

    /// @api b3IsSaneAABB
    public boolean isSaneAABB(AABB a) {
        return isValidAABB(a) && isBoundedAABB(a);
    }

    /// @api b3IsValidPlane
    public boolean isValidPlane(Plane a) {
        var normalLengthSquared = a.normalX * a.normalX + a.normalY * a.normalY + a.normalZ * a.normalZ;
        return Float.isFinite(a.normalX) && Float.isFinite(a.normalY) && Float.isFinite(a.normalZ)
                && Math.abs(1.0f - normalLengthSquared) < 100.0f * Math.ulp(1.0f)
                && isValidFloat(a.offset);
    }

    /// @api b3IsValidPosition
    public boolean isValidPosition(Vector3f p) {
        return isValidVec3(p);
    }

    /// @api b3IsValidWorldTransform
    public boolean isValidWorldTransform(Matrix4f t) {
        return isValidTransform(t);
    }

    /// @api b3PointToSegmentDistance
    public Vector3f pointToSegmentDistance(Vector3f dest, Vector3f a, Vector3f b, Vector3f q) {
        var abx = b.x - a.x;
        var aby = b.y - a.y;
        var abz = b.z - a.z;
        var aqx = q.x - a.x;
        var aqy = q.y - a.y;
        var aqz = q.z - a.z;
        var alpha = abx * aqx + aby * aqy + abz * aqz;

        if (alpha <= 0.0f) {
            return dest.set(a);
        }

        var denominator = abx * abx + aby * aby + abz * abz;
        if (alpha > denominator) {
            return dest.set(b);
        }

        alpha /= denominator;
        return dest.set(a.x + alpha * abx, a.y + alpha * aby, a.z + alpha * abz);
    }

    /// @api b3LineDistance
    public SegmentDistanceResult lineDistance(
            SegmentDistanceResult dest,
            Vector3f p1,
            Vector3f d1,
            Vector3f p2,
            Vector3f d2
    ) {
        try (this.argArena) {
            var result = b3LineDistance(
                    this.returnArena,
                    vec3(this.argArena, p1),
                    vec3(this.argArena, d1),
                    vec3(this.argArena, p2),
                    vec3(this.argArena, d2)
            );
            dest.set(result);
            return dest;
        }
    }

    /// @api b3SegmentDistance
    public SegmentDistanceResult segmentDistance(
            SegmentDistanceResult dest,
            Vector3f p1,
            Vector3f q1,
            Vector3f p2,
            Vector3f q2
    ) {
        try (this.argArena) {
            var result = b3SegmentDistance(
                    this.returnArena,
                    vec3(this.argArena, p1),
                    vec3(this.argArena, q1),
                    vec3(this.argArena, p2),
                    vec3(this.argArena, q2)
            );
            dest.set(result);
            return dest;
        }
    }

    /// @api b3ClosestPointToAABB
    public Vector3f closestPointToAABB(Vector3f dest, Vector3f point, AABB a) {
        return clamp(dest, point, a.lowerBound, a.upperBound);
    }

    /// @api b3AABB_Area
    public float aabbArea(AABB a) {
        var dx = a.upperBound.x - a.lowerBound.x;
        var dy = a.upperBound.y - a.lowerBound.y;
        var dz = a.upperBound.z - a.lowerBound.z;
        return 2.0f * (dx * dy + dy * dz + dz * dx);
    }

    /// @api b3AABB_Center
    public Vector3f aabbCenter(Vector3f dest, AABB a) {
        return dest.set(a.lowerBound).add(a.upperBound).mul(0.5f);
    }

    /// @api b3AABB_Extents
    public Vector3f aabbExtents(Vector3f dest, AABB a) {
        return dest.set(a.upperBound).sub(a.lowerBound).mul(0.5f);
    }

    /// @api b3AABB_Union
    public AABB aabbUnion(AABB dest, AABB a, AABB b) {
        dest.lowerBound.set(a.lowerBound).min(b.lowerBound);
        dest.upperBound.set(a.upperBound).max(b.upperBound);
        return dest;
    }

    /// @api b3AABB_Inflate
    public AABB aabbInflate(AABB dest, AABB a, float extension) {
        dest.lowerBound.set(a.lowerBound).sub(extension, extension, extension);
        dest.upperBound.set(a.upperBound).add(extension, extension, extension);
        return dest;
    }

    /// @api b3AABB_Contains
    public boolean aabbContains(AABB a, AABB b) {

        return     a.lowerBound.x <= b.lowerBound.x && b.upperBound.x <= a.upperBound.x
                && a.lowerBound.y <= b.lowerBound.y && b.upperBound.y <= a.upperBound.y
                && a.lowerBound.z <= b.lowerBound.z && b.upperBound.z <= a.upperBound.z;

    }

    /// @api b3AABB_Overlaps
    public boolean aabbOverlaps(AABB a, AABB b) {

        return
                   a.upperBound.x >= b.lowerBound.x && a.lowerBound.x <= b.upperBound.x
                && a.upperBound.y >= b.lowerBound.y && a.lowerBound.y <= b.upperBound.y
                && a.upperBound.z >= b.lowerBound.z && a.lowerBound.z <= b.upperBound.z;

    }

    /// @api b3AABB_Transform
    public AABB aabbTransform(AABB dest, Matrix4f transformMatrix, AABB a) {
        PrimitiveMemOps.validateRigidMatrix(transformMatrix);
        transformMatrix.transformAab(a.lowerBound, a.upperBound, dest.lowerBound, dest.upperBound);
        return dest;
    }

    /// @api b3MakeAABB
    public AABB makeAABB(AABB dest, MemorySegment points, int count, float radius) {
        if (count <= 0) {
            throw new IllegalArgumentException("points must not be empty");
        }
        if (points.byteSize() % (3L * Float.BYTES) != 0) {
            throw new IllegalArgumentException("points must be a multiple of 3 floats");
        }
        if (points.byteSize() < count * 3L * Float.BYTES) {
            throw new IllegalArgumentException("points does not contain count vectors");
        }

        var stride = 3L * Float.BYTES;
        PrimitiveMemOps.setVec3(dest.lowerBound, points);
        PrimitiveMemOps.setVec3(dest.upperBound, points);
        for (var i = 1; i < count; ++i) {
            var offset = i * stride;
            var x = points.get(ValueLayout.JAVA_FLOAT, offset);
            var y = points.get(ValueLayout.JAVA_FLOAT, offset + Float.BYTES);
            var z = points.get(ValueLayout.JAVA_FLOAT, offset + 2L * Float.BYTES);
            dest.lowerBound.set(Math.min(dest.lowerBound.x, x), Math.min(dest.lowerBound.y, y), Math.min(dest.lowerBound.z, z));
            dest.upperBound.set(Math.max(dest.upperBound.x, x), Math.max(dest.upperBound.y, y), Math.max(dest.upperBound.z, z));
        }
        dest.lowerBound.sub(radius, radius, radius);
        dest.upperBound.add(radius, radius, radius);
        return dest;
    }

    /// @api b3MakeAABB
    public AABB makeAABB(AABB dest, FloatBuffer points, float radius) {
        if (points.remaining() % 3 != 0) {
            throw new IllegalArgumentException("points must be a multiple of 3 floats");
        }
        return makeAABB(dest, MemorySegment.ofBuffer(points), points.remaining() / 3, radius);
    }

    /// @api b3MakeAABB
    public AABB makeAABB(AABB dest, float[] points, float radius) {
        if (points.length % 3 != 0) {
            throw new IllegalArgumentException("points must be a multiple of 3 floats");
        }
        return makeAABB(dest, MemorySegment.ofArray(points), points.length / 3, radius);
    }

    /// @api b3MakeAABB
    public AABB makeAABB(AABB dest, Iterable<Vector3f> points, float radius) {
        var iterator = points.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("points must not be empty");
        }

        var point = iterator.next();
        dest.lowerBound.set(point);
        dest.upperBound.set(point);
        while (iterator.hasNext()) {
            point = iterator.next();
            dest.lowerBound.min(point);
            dest.upperBound.max(point);
        }

        dest.lowerBound.sub(radius, radius, radius);
        dest.upperBound.add(radius, radius, radius);
        return dest;
    }

    /// @api b3Steiner
    public Matrix3f steiner(Matrix3f dest, float mass, Vector3f origin) {
        var matrix = b3Steiner(this.returnArena, mass, vec3(origin));
        return PrimitiveMemOps.setMat3(dest, matrix);
    }

    /// @api b3OffsetAABB
    public AABB offsetAABB(AABB dest, AABB localBox, Vector3f origin) {
        dest.lowerBound.set(localBox.lowerBound).add(origin);
        dest.upperBound.set(localBox.upperBound).add(origin);
        return dest;
    }

    /// @api b3GetSwingAngle
    public float getSwingAngle(Quaternionf q) {
        var x = (float) Math.sqrt(q.z * q.z + q.w * q.w);
        var y = (float) Math.sqrt(q.x * q.x + q.y * q.y);
        return 2.0f * atan2(y, x);
    }

    /// @api b3GetTwistAngle
    public float getTwistAngle(Quaternionf q) {
        var twist = q.w < 0.0f ? atan2(-q.z, -q.w) : atan2(q.z, q.w);
        return 2.0f * twist;
    }

    /// @api b3Sign
    public Vector3f sign(Vector3f dest, Vector3f a) {
        return dest.set(
                a.x >= 0.0f ? 1.0f : -1.0f,
                a.y >= 0.0f ? 1.0f : -1.0f,
                a.z >= 0.0f ? 1.0f : -1.0f
        );
    }

    /// @api b3SafeScale
    public Vector3f safeScale(Vector3f dest, Vector3f a) {
        var x = Math.max(Math.abs(a.x), MIN_SCALE);
        var y = Math.max(Math.abs(a.y), MIN_SCALE);
        var z = Math.max(Math.abs(a.z), MIN_SCALE);
        return sign(dest, a).mul(x, y, z);
    }
    //</editor-fold>

    //<editor-fold desc="World" default-state="collapsed">


    /// @api b3CreateWorld
    public WorldID createWorld(
            Region region,
            WorldDef worldDef
    ) {
        try (this.argArena) {
            var result = worldDef.create(this.argArena);
            var segment = b3CreateWorld(this.returnArena, result.segment());

            return WorldID.of(
                    this,
                    region,
                    result.worldStateValues(),
                    segment
            );
        }
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
            DebugDraw draw,
            @Unsigned long maskBits
    ) {
        b3jshimWorld_Draw(worldID(worldID), draw.segment(), maskBits);
        draw.invoke();
    }

    /// @api b3World_Explode
    public void worldExplode(
            WorldID worldID,
            ExplosionDef def
    ) {
        try (this.argArena) {
            b3World_Explode(
                    worldID(worldID),
                    def.create(this.argArena)
            );
        }
    }

    //</editor-fold>

    //<editor-fold desc="Hull" default-state="collapsed">


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

        try (this.argArena) {
             hull = b3CreateHull(
                     B3JUtil.ensureOffHeap(this.argArena, points),
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
    //</editor-fold>

    //<editor-fold desc="Height Field" default-state="collapsed">


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
    //</editor-fold>

    //<editor-fold desc="Mesh" default-state="collapsed">


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

                // cannot use MemorySegment.ofArray, has to be off-heap
                var data = this.argArena.allocate(ValueLayout.JAVA_INT.byteSize() * cap);

                mesh = b3CreateMesh(def, data, cap);

                // copy back
                MemorySegment.copy(
                        data, ValueLayout.JAVA_INT, 0, // src
                        degenerateTriangleIndices, 0,  // dst
                        cap                            // length
                );
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
    //</editor-fold>

    //<editor-fold desc="Shape" default-state="collapsed">


    /// @api b3Shape_GetBody
    public BodyID shapeGetBody(ShapeID shapeId) {
        var body = b3Shape_GetBody(this.returnArena, shapeID(shapeId));
        return BodyID.of(body);
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
    //</editor-fold>

    //<editor-fold desc="Spatial Queries" default-state="collapsed">


    /// @api b3World_OverlapAABB
    @Contract("null, _, _, _, _ -> null; !null, _, _, _, _ -> !null")
    public @Nullable TreeStats worldOverlapAABB(
            @Nullable TreeStats dest,
            WorldID worldID,
            AABB aabb,
            QueryFilter filter,
            OverlapResultFcn fcn
    ) {
        try (this.argArena) {

            var stats = this.scratchOverlapAABB.invoke(
                    this.returnArena,
                    worldID(worldID),
                    aabb(aabb),
                    filter.create(this.argArena),
                    fcn
            );

            if (dest != null) {
                dest.set(stats);
            }

            return dest;

        }
    }

    /// @api b3World_CastRay
    @Contract("null, _, _, _, _, _ -> null; !null, _, _, _, _, _ -> !null")
    public @Nullable TreeStats worldCastRay(
            @Nullable TreeStats dest,
            WorldID worldID,
            Vector3f origin,
            Vector3f translation,
            QueryFilter filter,
            CastResultFcn fcn
    ) {
        try (this.argArena) {

            var stats = this.scratchCastFn.invoke(
                    this.returnArena,
                    worldID(worldID),
                    vec3(origin),
                    vec3_2(translation),
                    filter.create(this.argArena),
                    fcn
            );

            if (dest != null) {
                dest.set(stats);
            }

            return dest;

        }
    }

    /// @return true if `hit`, false otherwise
    /// @api b3World_CastRayClosest
    public boolean worldCastRayClosest(
            @Nullable RayResult dest,
            WorldID worldID,
            Vector3f origin,
            Vector3f translation,
            QueryFilter filter
    ) {
        try (this.argArena) {

            var result = b3World_CastRayClosest(
                    this.returnArena,
                    worldID(worldID),
                    vec3(origin),
                    vec3_2(translation),
                    filter.create(this.argArena)
            );

            var hit = RayResult.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;

        }
    }

    /// @return true if `hit`, false otherwise
    /// @api b3Body_CastRay
    public boolean bodyCastRay(
            @Nullable BodyCastResult dest,
            BodyID bodyID,
            Vector3f origin,
            Vector3f translation,
            QueryFilter filter,
            float maxFraction,
            Matrix4f bodyTransform
    ) {

        try (this.argArena) {

            var result = b3Body_CastRay(
                    this.returnArena,
                    bodyID(bodyID),
                    vec3(origin),
                    vec3_2(translation),
                    filter.create(this.argArena),
                    maxFraction,
                    transform(bodyTransform)
            );

            var hit = BodyCastResult.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;

        }
    }

    /// @return true if `hit`, false otherwise
    /// @api b3Body_CastShape
    public boolean bodyCastShape(
            @Nullable BodyCastResult dest,
            BodyID bodyID,
            Vector3f origin,
            ShapeProxy proxy,
            Vector3f translation,
            QueryFilter filter,
            float maxFraction,
            boolean canEncroach,
            Matrix4f bodyTransform
    ) {
        try (this.argArena) {

            var result = b3Body_CastShape(
                    this.returnArena,
                    bodyID(bodyID),
                    vec3(origin),
                    proxy.create(this.argArena),
                    vec3_2(translation),
                    filter.create(this.argArena),
                    maxFraction,
                    canEncroach,
                    transform(bodyTransform)
            );

            var hit = BodyCastResult.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }


    /// @return true if `hit`, false otherwise
    /// @api b3RayCastSphere
    public boolean rayCastSphere(
            @Nullable CastOutput dest,
            Sphere sphere,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastSphere(
                    this.returnArena,
                    sphere.create(this.argArena),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3RayCastHollowSphere
    public boolean rayCastHollowSphere(
            @Nullable CastOutput dest,
            Sphere sphere,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastHollowSphere(
                    this.returnArena,
                    sphere.create(this.argArena),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3RayCastCapsule
    public boolean rayCastCapsule(
            @Nullable CastOutput dest,
            Capsule capsule,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastCapsule(
                    this.returnArena,
                    capsule.create(this.argArena),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3RayCastHull
    public boolean rayCastHull(
            @Nullable CastOutput dest,
            HullData hull,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastHull(
                    this.returnArena,
                    hull.segment,
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3RayCastMesh
    public boolean rayCastMesh(
            @Nullable CastOutput dest,
            Mesh mesh,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastMesh(
                    this.returnArena,
                    mesh.create(this.argArena),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3RayCastHeightField
    public boolean rayCastHeightField(
            @Nullable CastOutput dest,
            HeightFieldData heightField,
            RayCastInput input
    ) {

        try (this.argArena) {
            var result = b3RayCastHeightField(
                    this.returnArena,
                    heightField.segment(),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3ShapeCastSphere
    public boolean shapeCastSphere(
            @Nullable CastOutput dest,
            Sphere sphere,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastSphere(
                    this.returnArena,
                    sphere.create(this.argArena),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3ShapeCastCapsule
    public boolean shapeCastCapsule(
            @Nullable CastOutput dest,
            Capsule capsule,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastCapsule(
                    this.returnArena,
                    capsule.create(this.argArena),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3ShapeCastHull
    public boolean shapeCastHull(
            @Nullable CastOutput dest,
            HullData hull,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastHull(
                    this.returnArena,
                    hull.segment,
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3ShapeCastMesh
    public boolean shapeCastMesh(
            @Nullable CastOutput dest,
            Mesh mesh,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastMesh(
                    this.returnArena,
                    mesh.create(this.argArena),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
        }

    }

    /// @return true if `hit`, false otherwise
    /// @api b3ShapeCastHeightField
    public boolean shapeCastHeightField(
            @Nullable CastOutput dest,
            HeightFieldData heightField,
            ShapeCastInput input
    ) {

        try (this.argArena) {
            var result = b3ShapeCastHeightField(
                    this.returnArena,
                    heightField.segment(),
                    input.create(this.argArena)
            );

            var hit = CastOutput.hit(result);
            if (dest != null) {
                if (hit) {
                    dest.setOnHit(result);
                } else {
                    dest.setMiss();
                }
            }
            return hit;
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
            @Nullable DistanceOutput dest,
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

            if (dest != null) {
                dest.set(distanceOutput);
            }

            return dest;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Character Mover" default-state="collapsed">


    /// @api b3World_CastMover
    public float worldCastMover(
            WorldID world,
            Vector3f origin,
            Capsule mover,
            Vector3f translation,
            QueryFilter filter,
            @Nullable MoverFilterFcn fcn
    ) {

        try (this.argArena) {
            return this.scratchMoverFilter.invoke(
                    worldID(world),
                    vec3(origin),
                    mover.create(this.argArena),
                    vec3_2(translation),
                    filter.create(this.argArena),
                    fcn
            );
        }

    }

    /// @api b3World_CollideMover
    public void worldCollideMover(
            WorldID world,
            Vector3f origin,
            Capsule mover,
            QueryFilter filter,
            PlaneResultFcn fcn
    ) {

        try (this.argArena) {
            this.scratchPlaneResult.invoke(
                    worldID(world),
                    vec3(origin),
                    mover.create(this.argArena),
                    filter.create(this.argArena),
                    fcn
            );
        }

    }

    /// @return iterationCount
    /// @api b3SolvePlanes
    public int solvePlanes(
            Vector3f dest,
            Vector3f targetDelta,
            CollisionPlane[] planes,
            int count
    ) {

        if (count > planes.length) {
            throw new IllegalArgumentException("count > planes.length");
        }

        try (this.argArena) {
            var planesSegment = CollisionPlane.putPlanes(planes, count, this.argArena);

            var result = b3SolvePlanes(
                    this.returnArena,
                    vec3(targetDelta),
                    planesSegment,
                    count
            );

            // write back `push` parameter
            CollisionPlane.setPlanes(planes, count, planesSegment);

            PrimitiveMemOps.setVec3(dest, result, b3PlaneSolverResult.delta$offset());
            return result.get(ValueLayout.JAVA_INT, b3PlaneSolverResult.iterationCount$offset());

        }

    }

    /// @api b3ClipVector
    public Vector3f clipVector(
            Vector3f dest,
            Vector3f vector,
            CollisionPlane[] planes,
            int count
    ) {

        if (count > planes.length) {
            throw new IllegalArgumentException("count > planes.length");
        }

        try (this.argArena) {
            var planesSegment = CollisionPlane.putPlanes(planes, count, this.argArena);

            var result = b3ClipVector(
                    this.returnArena,
                    vec3(vector),
                    planesSegment,
                    count
            );
            PrimitiveMemOps.setVec3(dest, result);

        }

        return dest;

    }
    //</editor-fold>

    //<editor-fold desc="Body" default-state="collapsed">


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

    /// @api b3Body_GetPosition
    public Vector3f bodyGetPosition(Vector3f dest, BodyID bodyId) {
        var vec = b3Body_GetPosition(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(dest, vec);
    }

    /// @api b3Body_GetRotation
    public Quaternionf bodyGetRotation(Quaternionf dest, BodyID bodyId) {
        var quat = b3Body_GetRotation(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setQuat(dest, quat);
    }

    /// @api b3Body_GetTransform
    public Matrix4f bodyGetTransform(Matrix4f dest, BodyID bodyId) {
        var transform = b3Body_GetTransform(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setTransform(dest, transform);
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
    public Vector3f bodyGetLinearVelocity(Vector3f dest, BodyID bodyId) {
        var vec = b3Body_GetLinearVelocity(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(dest, vec);
    }

    /// @api b3Body_GetAngularVelocity
    public Vector3f bodyGetAngularVelocity(Vector3f dest, BodyID bodyId) {
        var vec = b3Body_GetAngularVelocity(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(dest, vec);
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
    public Vector3f bodyGetWorldCenterOfMass(Vector3f dest, BodyID bodyId) {
        var vec = b3Body_GetWorldCenterOfMass(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(dest, vec);
    }

    /// @api b3Body_GetLocalCenterOfMass
    public Vector3f bodyGetLocalCenterOfMass(Vector3f dest, BodyID bodyId) {
        var vec = b3Body_GetLocalCenterOfMass(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setVec3(dest, vec);
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
    public Vector3f bodyGetWorldPoint(Vector3f dest, BodyID bodyId, Vector3f localPoint) {
        var vec = b3Body_GetWorldPoint(this.returnArena, bodyID(bodyId), vec3(localPoint));
        return PrimitiveMemOps.setVec3(dest, vec);
    }

    /// @api b3Body_GetWorldVector
    public Vector3f bodyGetWorldVector(Vector3f dest, BodyID bodyId, Vector3f localVector) {
        var vec = b3Body_GetWorldVector(this.returnArena, bodyID(bodyId), vec3(localVector));
        return PrimitiveMemOps.setVec3(dest, vec);
    }

    /// @api b3Body_GetLocalPoint
    public Vector3f bodyGetLocalPoint(Vector3f dest, BodyID bodyId, Vector3f worldPoint) {
        var vec = b3Body_GetLocalPoint(this.returnArena, bodyID(bodyId), vec3(worldPoint));
        return PrimitiveMemOps.setVec3(dest, vec);
    }

    /// @api b3Body_GetLocalVector
    public Vector3f bodyGetLocalVector(Vector3f dest, BodyID bodyId, Vector3f worldVector) {
        var vec = b3Body_GetLocalVector(this.returnArena, bodyID(bodyId), vec3(worldVector));
        return PrimitiveMemOps.setVec3(dest, vec);
    }

    /// @api b3Body_GetType
    public BodyType bodyGetType(BodyID bodyId) {
        var type = b3Body_GetType(bodyID(bodyId));
        return BodyType.fromCode(type);
    }

    /// @api b3Body_GetMass
    public float bodyGetMass(BodyID bodyId) {
        return b3Body_GetMass(bodyID(bodyId));
    }

    /// @api b3Body_GetInverseMass
    public float bodyGetInverseMass(BodyID bodyId) {
        return b3Body_GetInverseMass(bodyID(bodyId));
    }

    /// @api b3Body_GetWorldInverseRotationalInertia
    public Matrix3f bodyGetWorldInverseRotationalInertia(Matrix3f dest, BodyID bodyId) {
        var matrix = b3Body_GetWorldInverseRotationalInertia(this.returnArena, bodyID(bodyId));
        return PrimitiveMemOps.setMat3(dest, matrix);
    }

    /// @api b3Body_GetName
    public @Nullable String bodyGetName(BodyID bodyId) {
        var name = b3Body_GetName(bodyID(bodyId));
        return B3JUtil.getNullString(name);
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
    //</editor-fold>

    //<editor-fold desc="ID Tests" default-state="collapsed">


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

    public boolean isNull(BodyID bodyID) {
        return PrimitiveMemOps.isPackedIDNull(bodyID.packedID());
    }
    public boolean isNull(ShapeID shapeID) {
        return PrimitiveMemOps.isPackedIDNull(shapeID.packedID());
    }
    public boolean isNull(JointID<?> jointID) {
        return PrimitiveMemOps.isPackedIDNull(jointID.packedID());
    }
    public boolean isNull(ContactID constraintID) {
        return PrimitiveMemOps.isPackedIDNull(constraintID.packedID());
    }
    public boolean isNull(WorldID worldID) {
        return PrimitiveMemOps.isPackedWorldIDNull(worldID.packedID());
    }

    public boolean isNotNull(BodyID bodyID) {
        return !isNull(bodyID);
    }
    public boolean isNotNull(ShapeID shapeID) {
        return !isNull(shapeID);
    }
    public boolean isNotNull(JointID<?> jointID) {
        return !isNull(jointID);
    }
    public boolean isNotNull(ContactID constraintID) {
        return !isNull(constraintID);
    }
    public boolean isNotNull(WorldID worldID) {
        return !isNull(worldID);
    }

    public boolean equals(BodyID id1, BodyID id2) {
        return Objects.equals(id1, id2);
    }
    public boolean equals(ShapeID id1, ShapeID id2) {
        return Objects.equals(id1, id2);
    }
    public boolean equals(JointID<?> id1, JointID<?> id2) {
        return Objects.equals(id1, id2);
    }
    public boolean equals(ContactID id1, ContactID id2) {
        return Objects.equals(id1, id2);
    }
    public boolean equals(WorldID id1, WorldID id2) {
        return Objects.equals(id1, id2);
    }


    //</editor-fold>

    //<editor-fold desc="Destructors" default-state="collapsed">


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
            worldID.stateValues.closeReferences();
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
    //</editor-fold>

    //<editor-fold desc="Joints" default-state="collapsed">


    /// Convenience method
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

    /// @api b3Joint_GetType
    public JointType jointGetType(JointID<?> jointID) {
        return jointType(b3Joint_GetType(jointID(jointID)));
    }
    /// @api b3Joint_GetBodyA
    public BodyID jointGetBodyA(JointID<?> jointID) {
        var bodyID = b3Joint_GetBodyA(this.returnArena, jointID(jointID));
        return BodyID.of(bodyID);
    }
    /// @api b3Joint_GetBodyB
    public BodyID jointGetBodyB(JointID<?> jointID) {
        var bodyID = b3Joint_GetBodyB(this.returnArena, jointID(jointID));
        return BodyID.of(bodyID);
    }
    /// @api b3Joint_GetWorld
    public WorldID jointGetWorld(JointID<?> jointID) {
        var worldID = b3Joint_GetWorld(this.returnArena, jointID(jointID));
        return WorldID.of(worldID);
    }
    /// @api b3Joint_SetLocalFrameA
    public void jointSetLocalFrameA(JointID<?> jointID, Matrix4f localFrame) {
        b3Joint_SetLocalFrameA(jointID(jointID), transform(localFrame));
    }
    /// @api b3Joint_GetLocalFrameA
    public Matrix4f jointGetLocalFrameA(Matrix4f dest, JointID<?> jointID) {
        var localFrame = b3Joint_GetLocalFrameA(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setTransform(dest, localFrame);
    }
    /// @api b3Joint_SetLocalFrameB
    public void jointSetLocalFrameB(JointID<?> jointID, Matrix4f localFrame) {
        b3Joint_SetLocalFrameB(jointID(jointID), transform(localFrame));
    }
    /// @api b3Joint_GetLocalFrameB
    public Matrix4f jointGetLocalFrameB(Matrix4f dest, JointID<?> jointID) {
        var localFrame = b3Joint_GetLocalFrameB(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setTransform(dest, localFrame);
    }
    /// @api b3Joint_SetCollideConnected
    public void jointSetCollideConnected(JointID<?> jointID, boolean shouldCollide) {
        b3Joint_SetCollideConnected(jointID(jointID), shouldCollide);
    }
    /// @api b3Joint_GetCollideConnected
    public boolean jointGetCollideConnected(JointID<?> jointID) {
        return b3Joint_GetCollideConnected(jointID(jointID));
    }
    /// @api b3Joint_WakeBodies
    public void jointWakeBodies(JointID<?> jointID) {
        b3Joint_WakeBodies(jointID(jointID));
    }
    /// @api b3Joint_GetConstraintForce
    public Vector3f jointGetConstraintForce(Vector3f dest, JointID<?> jointID) {
        var force = b3Joint_GetConstraintForce(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setVec3(dest, force);
    }
    /// @api b3Joint_GetConstraintTorque
    public Vector3f jointGetConstraintTorque(Vector3f dest, JointID<?> jointID) {
        var torque = b3Joint_GetConstraintTorque(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setVec3(dest, torque);
    }
    /// @api b3Joint_GetLinearSeparation
    public float jointGetLinearSeparation(JointID<?> jointID) {
        return b3Joint_GetLinearSeparation(jointID(jointID));
    }
    /// @api b3Joint_GetAngularSeparation
    public float jointGetAngularSeparation(JointID<?> jointID) {
        return b3Joint_GetAngularSeparation(jointID(jointID));
    }
    /// @api b3Joint_SetConstraintTuning
    public void jointSetConstraintTuning(JointID<?> jointID, float hertz, float dampingRatio) {
        b3Joint_SetConstraintTuning(jointID(jointID), hertz, dampingRatio);
    }
    /// @return x = hertz, y = dampingRatio
    /// @api b3Joint_GetConstraintTuning
    public Vector2f jointGetConstraintTuning(Vector2f dest, JointID<?> jointID) {
        try (this.argArena) {
            var hertz = this.argArena.allocate(ValueLayout.JAVA_FLOAT);
            var dampingRatio = this.argArena.allocate(ValueLayout.JAVA_FLOAT);
            b3Joint_GetConstraintTuning(jointID(jointID), hertz, dampingRatio);
            dest.x = hertz.get(ValueLayout.JAVA_FLOAT, 0);
            dest.y = dampingRatio.get(ValueLayout.JAVA_FLOAT, 0);
            return dest;
        }
    }
    /// @api b3Joint_SetForceThreshold
    public void jointSetForceThreshold(JointID<?> jointID, float threshold) {
        b3Joint_SetForceThreshold(jointID(jointID), threshold);
    }
    /// @api b3Joint_GetForceThreshold
    public float jointGetForceThreshold(JointID<?> jointID) {
        return b3Joint_GetForceThreshold(jointID(jointID));
    }
    /// @api b3Joint_SetTorqueThreshold
    public void jointSetTorqueThreshold(JointID<?> jointID, float threshold) {
        b3Joint_SetTorqueThreshold(jointID(jointID), threshold);
    }
    /// @api b3Joint_GetTorqueThreshold
    public float jointGetTorqueThreshold(JointID<?> jointID) {
        return b3Joint_GetTorqueThreshold(jointID(jointID));
    }



    //<editor-fold desc="Filter Joints" default-state="collapsed">


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
    //</editor-fold>

    //<editor-fold desc="Parallel Joints" default-state="collapsed">


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


    /// @api b3ParallelJoint_SetSpringHertz
    public void parallelJointSetSpringHertz(JointID<JointType.Parallel> jointID, float hertz) {
        b3ParallelJoint_SetSpringHertz(jointID(jointID), hertz);
    }
    /// @api b3ParallelJoint_SetSpringDampingRatio
    public void parallelJointSetSpringDampingRatio(JointID<JointType.Parallel> jointID, float dampingRatio) {
        b3ParallelJoint_SetSpringDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3ParallelJoint_GetSpringHertz
    public float parallelJointGetSpringHertz(JointID<JointType.Parallel> jointID) {
        return b3ParallelJoint_GetSpringHertz(jointID(jointID));
    }
    /// @api b3ParallelJoint_GetSpringDampingRatio
    public float parallelJointGetSpringDampingRatio(JointID<JointType.Parallel> jointID) {
        return b3ParallelJoint_GetSpringDampingRatio(jointID(jointID));
    }
    /// @api b3ParallelJoint_SetMaxTorque
    public void parallelJointSetMaxTorque(JointID<JointType.Parallel> jointID, float force) {
        b3ParallelJoint_SetMaxTorque(jointID(jointID), force);
    }
    /// @api b3ParallelJoint_GetMaxTorque
    public float parallelJointGetMaxTorque(JointID<JointType.Parallel> jointID) {
        return b3ParallelJoint_GetMaxTorque(jointID(jointID));
    }
    //</editor-fold>

    //<editor-fold desc="Distance Joints" default-state="collapsed">


    /// @api b3DistanceJoint_SetLength
    public void distanceJointSetLength(JointID<JointType.Distance> jointID, float length) {
        b3DistanceJoint_SetLength(jointID(jointID), length);
    }
    /// @api b3DistanceJoint_GetLength
    public float distanceJointGetLength(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetLength(jointID(jointID));
    }
    /// @api b3DistanceJoint_EnableSpring
    public void distanceJointEnableSpring(JointID<JointType.Distance> jointID, boolean enableSpring) {
        b3DistanceJoint_EnableSpring(jointID(jointID), enableSpring);
    }
    /// @api b3DistanceJoint_IsSpringEnabled
    public boolean distanceJointIsSpringEnabled(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_IsSpringEnabled(jointID(jointID));
    }
    /// @api b3DistanceJoint_SetSpringForceRange
    public void distanceJointSetSpringForceRange(JointID<JointType.Distance> jointID, float lowerForce, float upperForce) {
        b3DistanceJoint_SetSpringForceRange(jointID(jointID), lowerForce, upperForce);
    }
    /// @return x = lowerForce, y = upperForce
    /// @api b3DistanceJoint_GetSpringForceRange
    public Vector2f distanceJointGetSpringForceRange(Vector2f dest, JointID<JointType.Distance> jointID) {
        try (this.argArena) {
            var lowerForce = this.argArena.allocate(ValueLayout.JAVA_FLOAT);
            var upperForce = this.argArena.allocate(ValueLayout.JAVA_FLOAT);
            b3DistanceJoint_GetSpringForceRange(jointID(jointID), lowerForce, upperForce);
            dest.x = lowerForce.get(ValueLayout.JAVA_FLOAT, 0);
            dest.y = upperForce.get(ValueLayout.JAVA_FLOAT, 0);
            return dest;
        }
    }
    /// @api b3DistanceJoint_SetSpringHertz
    public void distanceJointSetSpringHertz(JointID<JointType.Distance> jointID, float hertz) {
        b3DistanceJoint_SetSpringHertz(jointID(jointID), hertz);
    }
    /// @api b3DistanceJoint_SetSpringDampingRatio
    public void distanceJointSetSpringDampingRatio(JointID<JointType.Distance> jointID, float dampingRatio) {
        b3DistanceJoint_SetSpringDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3DistanceJoint_GetSpringHertz
    public float distanceJointGetSpringHertz(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetSpringHertz(jointID(jointID));
    }
    /// @api b3DistanceJoint_GetSpringDampingRatio
    public float distanceJointGetSpringDampingRatio(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetSpringDampingRatio(jointID(jointID));
    }
    /// @api b3DistanceJoint_EnableLimit
    public void distanceJointEnableLimit(JointID<JointType.Distance> jointID, boolean enableLimit) {
        b3DistanceJoint_EnableLimit(jointID(jointID), enableLimit);
    }
    /// @api b3DistanceJoint_IsLimitEnabled
    public boolean distanceJointIsLimitEnabled(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_IsLimitEnabled(jointID(jointID));
    }
    /// @api b3DistanceJoint_SetLengthRange
    public void distanceJointSetLengthRange(JointID<JointType.Distance> jointID, float minLength, float maxLength) {
        b3DistanceJoint_SetLengthRange(jointID(jointID), minLength, maxLength);
    }
    /// @api b3DistanceJoint_GetMinLength
    public float distanceJointGetMinLength(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetMinLength(jointID(jointID));
    }
    /// @api b3DistanceJoint_GetMaxLength
    public float distanceJointGetMaxLength(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetMaxLength(jointID(jointID));
    }
    /// @api b3DistanceJoint_GetCurrentLength
    public float distanceJointGetCurrentLength(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetCurrentLength(jointID(jointID));
    }
    /// @api b3DistanceJoint_EnableMotor
    public void distanceJointEnableMotor(JointID<JointType.Distance> jointID, boolean enableMotor) {
        b3DistanceJoint_EnableMotor(jointID(jointID), enableMotor);
    }
    /// @api b3DistanceJoint_IsMotorEnabled
    public boolean distanceJointIsMotorEnabled(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_IsMotorEnabled(jointID(jointID));
    }
    /// @api b3DistanceJoint_SetMotorSpeed
    public void distanceJointSetMotorSpeed(JointID<JointType.Distance> jointID, float motorSpeed) {
        b3DistanceJoint_SetMotorSpeed(jointID(jointID), motorSpeed);
    }
    /// @api b3DistanceJoint_GetMotorSpeed
    public float distanceJointGetMotorSpeed(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetMotorSpeed(jointID(jointID));
    }
    /// @api b3DistanceJoint_SetMaxMotorForce
    public void distanceJointSetMaxMotorForce(JointID<JointType.Distance> jointID, float force) {
        b3DistanceJoint_SetMaxMotorForce(jointID(jointID), force);
    }
    /// @api b3DistanceJoint_GetMaxMotorForce
    public float distanceJointGetMaxMotorForce(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetMaxMotorForce(jointID(jointID));
    }
    /// @api b3DistanceJoint_GetMotorForce
    public float distanceJointGetMotorForce(JointID<JointType.Distance> jointID) {
        return b3DistanceJoint_GetMotorForce(jointID(jointID));
    }
    //</editor-fold>

    //<editor-fold desc="Motor Joints" default-state="collapsed">


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


    /// @api b3MotorJoint_SetLinearVelocity
    public void motorJointSetLinearVelocity(JointID<JointType.Motor> jointID, Vector3f velocity) {
        b3MotorJoint_SetLinearVelocity(jointID(jointID), vec3(velocity));
    }
    /// @api b3MotorJoint_GetLinearVelocity
    public Vector3f motorJointGetLinearVelocity(Vector3f dest, JointID<JointType.Motor> jointID) {
        var velocity = b3MotorJoint_GetLinearVelocity(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setVec3(dest, velocity);
    }
    /// @api b3MotorJoint_SetAngularVelocity
    public void motorJointSetAngularVelocity(JointID<JointType.Motor> jointID, Vector3f velocity) {
        b3MotorJoint_SetAngularVelocity(jointID(jointID), vec3(velocity));
    }
    /// @api b3MotorJoint_GetAngularVelocity
    public Vector3f motorJointGetAngularVelocity(Vector3f dest, JointID<JointType.Motor> jointID) {
        var velocity = b3MotorJoint_GetAngularVelocity(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setVec3(dest, velocity);
    }
    /// @api b3MotorJoint_SetMaxVelocityForce
    public void motorJointSetMaxVelocityForce(JointID<JointType.Motor> jointID, float maxForce) {
        b3MotorJoint_SetMaxVelocityForce(jointID(jointID), maxForce);
    }
    /// @api b3MotorJoint_GetMaxVelocityForce
    public float motorJointGetMaxVelocityForce(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetMaxVelocityForce(jointID(jointID));
    }
    /// @api b3MotorJoint_SetMaxVelocityTorque
    public void motorJointSetMaxVelocityTorque(JointID<JointType.Motor> jointID, float maxTorque) {
        b3MotorJoint_SetMaxVelocityTorque(jointID(jointID), maxTorque);
    }
    /// @api b3MotorJoint_GetMaxVelocityTorque
    public float motorJointGetMaxVelocityTorque(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetMaxVelocityTorque(jointID(jointID));
    }
    /// @api b3MotorJoint_SetLinearHertz
    public void motorJointSetLinearHertz(JointID<JointType.Motor> jointID, float hertz) {
        b3MotorJoint_SetLinearHertz(jointID(jointID), hertz);
    }
    /// @api b3MotorJoint_GetLinearHertz
    public float motorJointGetLinearHertz(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetLinearHertz(jointID(jointID));
    }
    /// @api b3MotorJoint_SetLinearDampingRatio
    public void motorJointSetLinearDampingRatio(JointID<JointType.Motor> jointID, float damping) {
        b3MotorJoint_SetLinearDampingRatio(jointID(jointID), damping);
    }
    /// @api b3MotorJoint_GetLinearDampingRatio
    public float motorJointGetLinearDampingRatio(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetLinearDampingRatio(jointID(jointID));
    }
    /// @api b3MotorJoint_SetAngularHertz
    public void motorJointSetAngularHertz(JointID<JointType.Motor> jointID, float hertz) {
        b3MotorJoint_SetAngularHertz(jointID(jointID), hertz);
    }
    /// @api b3MotorJoint_GetAngularHertz
    public float motorJointGetAngularHertz(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetAngularHertz(jointID(jointID));
    }
    /// @api b3MotorJoint_SetAngularDampingRatio
    public void motorJointSetAngularDampingRatio(JointID<JointType.Motor> jointID, float damping) {
        b3MotorJoint_SetAngularDampingRatio(jointID(jointID), damping);
    }
    /// @api b3MotorJoint_GetAngularDampingRatio
    public float motorJointGetAngularDampingRatio(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetAngularDampingRatio(jointID(jointID));
    }
    /// @api b3MotorJoint_SetMaxSpringForce
    public void motorJointSetMaxSpringForce(JointID<JointType.Motor> jointID, float maxForce) {
        b3MotorJoint_SetMaxSpringForce(jointID(jointID), maxForce);
    }
    /// @api b3MotorJoint_GetMaxSpringForce
    public float motorJointGetMaxSpringForce(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetMaxSpringForce(jointID(jointID));
    }
    /// @api b3MotorJoint_SetMaxSpringTorque
    public void motorJointSetMaxSpringTorque(JointID<JointType.Motor> jointID, float maxTorque) {
        b3MotorJoint_SetMaxSpringTorque(jointID(jointID), maxTorque);
    }
    /// @api b3MotorJoint_GetMaxSpringTorque
    public float motorJointGetMaxSpringTorque(JointID<JointType.Motor> jointID) {
        return b3MotorJoint_GetMaxSpringTorque(jointID(jointID));
    }
    //</editor-fold>

    //<editor-fold desc="Prismatic Joints" default-state="collapsed">


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


    /// @api b3PrismaticJoint_EnableSpring
    public void prismaticJointEnableSpring(JointID<JointType.Prismatic> jointID, boolean enableSpring) {
        b3PrismaticJoint_EnableSpring(jointID(jointID), enableSpring);
    }
    /// @api b3PrismaticJoint_IsSpringEnabled
    public boolean prismaticJointIsSpringEnabled(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_IsSpringEnabled(jointID(jointID));
    }
    /// @api b3PrismaticJoint_SetSpringHertz
    public void prismaticJointSetSpringHertz(JointID<JointType.Prismatic> jointID, float hertz) {
        b3PrismaticJoint_SetSpringHertz(jointID(jointID), hertz);
    }
    /// @api b3PrismaticJoint_GetSpringHertz
    public float prismaticJointGetSpringHertz(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetSpringHertz(jointID(jointID));
    }
    /// @api b3PrismaticJoint_SetSpringDampingRatio
    public void prismaticJointSetSpringDampingRatio(JointID<JointType.Prismatic> jointID, float dampingRatio) {
        b3PrismaticJoint_SetSpringDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3PrismaticJoint_GetSpringDampingRatio
    public float prismaticJointGetSpringDampingRatio(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetSpringDampingRatio(jointID(jointID));
    }
    /// @api b3PrismaticJoint_SetTargetTranslation
    public void prismaticJointSetTargetTranslation(JointID<JointType.Prismatic> jointID, float targetTranslation) {
        b3PrismaticJoint_SetTargetTranslation(jointID(jointID), targetTranslation);
    }
    /// @api b3PrismaticJoint_GetTargetTranslation
    public float prismaticJointGetTargetTranslation(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetTargetTranslation(jointID(jointID));
    }
    /// @api b3PrismaticJoint_EnableLimit
    public void prismaticJointEnableLimit(JointID<JointType.Prismatic> jointID, boolean enableLimit) {
        b3PrismaticJoint_EnableLimit(jointID(jointID), enableLimit);
    }
    /// @api b3PrismaticJoint_IsLimitEnabled
    public boolean prismaticJointIsLimitEnabled(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_IsLimitEnabled(jointID(jointID));
    }
    /// @api b3PrismaticJoint_GetLowerLimit
    public float prismaticJointGetLowerLimit(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetLowerLimit(jointID(jointID));
    }
    /// @api b3PrismaticJoint_GetUpperLimit
    public float prismaticJointGetUpperLimit(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetUpperLimit(jointID(jointID));
    }
    /// @api b3PrismaticJoint_SetLimits
    public void prismaticJointSetLimits(JointID<JointType.Prismatic> jointID, float lower, float upper) {
        b3PrismaticJoint_SetLimits(jointID(jointID), lower, upper);
    }
    /// @api b3PrismaticJoint_EnableMotor
    public void prismaticJointEnableMotor(JointID<JointType.Prismatic> jointID, boolean enableMotor) {
        b3PrismaticJoint_EnableMotor(jointID(jointID), enableMotor);
    }
    /// @api b3PrismaticJoint_IsMotorEnabled
    public boolean prismaticJointIsMotorEnabled(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_IsMotorEnabled(jointID(jointID));
    }
    /// @api b3PrismaticJoint_SetMotorSpeed
    public void prismaticJointSetMotorSpeed(JointID<JointType.Prismatic> jointID, float motorSpeed) {
        b3PrismaticJoint_SetMotorSpeed(jointID(jointID), motorSpeed);
    }
    /// @api b3PrismaticJoint_GetMotorSpeed
    public float prismaticJointGetMotorSpeed(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetMotorSpeed(jointID(jointID));
    }
    /// @api b3PrismaticJoint_SetMaxMotorForce
    public void prismaticJointSetMaxMotorForce(JointID<JointType.Prismatic> jointID, float force) {
        b3PrismaticJoint_SetMaxMotorForce(jointID(jointID), force);
    }
    /// @api b3PrismaticJoint_GetMaxMotorForce
    public float prismaticJointGetMaxMotorForce(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetMaxMotorForce(jointID(jointID));
    }
    /// @api b3PrismaticJoint_GetMotorForce
    public float prismaticJointGetMotorForce(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetMotorForce(jointID(jointID));
    }
    /// @api b3PrismaticJoint_GetTranslation
    public float prismaticJointGetTranslation(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetTranslation(jointID(jointID));
    }
    /// @api b3PrismaticJoint_GetSpeed
    public float prismaticJointGetSpeed(JointID<JointType.Prismatic> jointID) {
        return b3PrismaticJoint_GetSpeed(jointID(jointID));
    }
    //</editor-fold>

    //<editor-fold desc="Revolute Joints" default-state="collapsed">


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


    /// @api b3RevoluteJoint_EnableSpring
    public void revoluteJointEnableSpring(JointID<JointType.Revolute> jointID, boolean enableSpring) {
        b3RevoluteJoint_EnableSpring(jointID(jointID), enableSpring);
    }
    /// @api b3RevoluteJoint_IsSpringEnabled
    public boolean revoluteJointIsSpringEnabled(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_IsSpringEnabled(jointID(jointID));
    }
    /// @api b3RevoluteJoint_SetSpringHertz
    public void revoluteJointSetSpringHertz(JointID<JointType.Revolute> jointID, float hertz) {
        b3RevoluteJoint_SetSpringHertz(jointID(jointID), hertz);
    }
    /// @api b3RevoluteJoint_GetSpringHertz
    public float revoluteJointGetSpringHertz(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetSpringHertz(jointID(jointID));
    }
    /// @api b3RevoluteJoint_SetSpringDampingRatio
    public void revoluteJointSetSpringDampingRatio(JointID<JointType.Revolute> jointID, float dampingRatio) {
        b3RevoluteJoint_SetSpringDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3RevoluteJoint_GetSpringDampingRatio
    public float revoluteJointGetSpringDampingRatio(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetSpringDampingRatio(jointID(jointID));
    }
    /// @api b3RevoluteJoint_SetTargetAngle
    public void revoluteJointSetTargetAngle(JointID<JointType.Revolute> jointID, float targetRadians) {
        b3RevoluteJoint_SetTargetAngle(jointID(jointID), targetRadians);
    }
    /// @api b3RevoluteJoint_GetTargetAngle
    public float revoluteJointGetTargetAngle(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetTargetAngle(jointID(jointID));
    }
    /// @api b3RevoluteJoint_GetAngle
    public float revoluteJointGetAngle(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetAngle(jointID(jointID));
    }
    /// @api b3RevoluteJoint_EnableLimit
    public void revoluteJointEnableLimit(JointID<JointType.Revolute> jointID, boolean enableLimit) {
        b3RevoluteJoint_EnableLimit(jointID(jointID), enableLimit);
    }
    /// @api b3RevoluteJoint_IsLimitEnabled
    public boolean revoluteJointIsLimitEnabled(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_IsLimitEnabled(jointID(jointID));
    }
    /// @api b3RevoluteJoint_GetLowerLimit
    public float revoluteJointGetLowerLimit(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetLowerLimit(jointID(jointID));
    }
    /// @api b3RevoluteJoint_GetUpperLimit
    public float revoluteJointGetUpperLimit(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetUpperLimit(jointID(jointID));
    }
    /// @api b3RevoluteJoint_SetLimits
    public void revoluteJointSetLimits(JointID<JointType.Revolute> jointID, float lowerLimitRadians, float upperLimitRadians) {
        b3RevoluteJoint_SetLimits(jointID(jointID), lowerLimitRadians, upperLimitRadians);
    }
    /// @api b3RevoluteJoint_EnableMotor
    public void revoluteJointEnableMotor(JointID<JointType.Revolute> jointID, boolean enableMotor) {
        b3RevoluteJoint_EnableMotor(jointID(jointID), enableMotor);
    }
    /// @api b3RevoluteJoint_IsMotorEnabled
    public boolean revoluteJointIsMotorEnabled(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_IsMotorEnabled(jointID(jointID));
    }
    /// @api b3RevoluteJoint_SetMotorSpeed
    public void revoluteJointSetMotorSpeed(JointID<JointType.Revolute> jointID, float motorSpeed) {
        b3RevoluteJoint_SetMotorSpeed(jointID(jointID), motorSpeed);
    }
    /// @api b3RevoluteJoint_GetMotorSpeed
    public float revoluteJointGetMotorSpeed(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetMotorSpeed(jointID(jointID));
    }
    /// @api b3RevoluteJoint_GetMotorTorque
    public float revoluteJointGetMotorTorque(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetMotorTorque(jointID(jointID));
    }
    /// @api b3RevoluteJoint_SetMaxMotorTorque
    public void revoluteJointSetMaxMotorTorque(JointID<JointType.Revolute> jointID, float torque) {
        b3RevoluteJoint_SetMaxMotorTorque(jointID(jointID), torque);
    }
    /// @api b3RevoluteJoint_GetMaxMotorTorque
    public float revoluteJointGetMaxMotorTorque(JointID<JointType.Revolute> jointID) {
        return b3RevoluteJoint_GetMaxMotorTorque(jointID(jointID));
    }
    //</editor-fold>

    //<editor-fold desc="Spherical Joints" default-state="collapsed">


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


    /// @api b3SphericalJoint_EnableConeLimit
    public void sphericalJoint_EnableConeLimit(JointID<JointType.Spherical> jointID, boolean enableLimit) {
        b3SphericalJoint_EnableConeLimit(jointID(jointID), enableLimit);
    }
    /// @api b3SphericalJoint_IsConeLimitEnabled
    public boolean sphericalJoint_IsConeLimitEnabled(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_IsConeLimitEnabled(jointID(jointID));
    }
    /// @api b3SphericalJoint_GetConeLimit
    public float sphericalJoint_GetConeLimit(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetConeLimit(jointID(jointID));
    }
    /// @api b3SphericalJoint_SetConeLimit
    public void sphericalJoint_SetConeLimit(JointID<JointType.Spherical> jointID, float angleRadians) {
        b3SphericalJoint_SetConeLimit(jointID(jointID), angleRadians);
    }
    /// @api b3SphericalJoint_GetConeAngle
    public float sphericalJoint_GetConeAngle(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetConeAngle(jointID(jointID));
    }
    /// @api b3SphericalJoint_EnableTwistLimit
    public void sphericalJoint_EnableTwistLimit(JointID<JointType.Spherical> jointID, boolean enableLimit) {
        b3SphericalJoint_EnableTwistLimit(jointID(jointID), enableLimit);
    }
    /// @api b3SphericalJoint_IsTwistLimitEnabled
    public boolean sphericalJoint_IsTwistLimitEnabled(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_IsTwistLimitEnabled(jointID(jointID));
    }
    /// @api b3SphericalJoint_GetLowerTwistLimit
    public float sphericalJoint_GetLowerTwistLimit(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetLowerTwistLimit(jointID(jointID));
    }
    /// @api b3SphericalJoint_GetUpperTwistLimit
    public float sphericalJoint_GetUpperTwistLimit(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetUpperTwistLimit(jointID(jointID));
    }
    /// @api b3SphericalJoint_SetTwistLimits
    public void sphericalJoint_SetTwistLimits(JointID<JointType.Spherical> jointID, float lowerLimitRadians, float upperLimitRadians) {
        b3SphericalJoint_SetTwistLimits(jointID(jointID), lowerLimitRadians, upperLimitRadians);
    }
    /// @api b3SphericalJoint_GetTwistAngle
    public float sphericalJoint_GetTwistAngle(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetTwistAngle(jointID(jointID));
    }
    /// @api b3SphericalJoint_EnableSpring
    public void sphericalJoint_EnableSpring(JointID<JointType.Spherical> jointID, boolean enableSpring) {
        b3SphericalJoint_EnableSpring(jointID(jointID), enableSpring);
    }
    /// @api b3SphericalJoint_IsSpringEnabled
    public boolean sphericalJoint_IsSpringEnabled(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_IsSpringEnabled(jointID(jointID));
    }
    /// @api b3SphericalJoint_SetSpringHertz
    public void sphericalJoint_SetSpringHertz(JointID<JointType.Spherical> jointID, float hertz) {
        b3SphericalJoint_SetSpringHertz(jointID(jointID), hertz);
    }
    /// @api b3SphericalJoint_GetSpringHertz
    public float sphericalJoint_GetSpringHertz(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetSpringHertz(jointID(jointID));
    }
    /// @api b3SphericalJoint_SetSpringDampingRatio
    public void sphericalJoint_SetSpringDampingRatio(JointID<JointType.Spherical> jointID, float dampingRatio) {
        b3SphericalJoint_SetSpringDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3SphericalJoint_GetSpringDampingRatio
    public float sphericalJoint_GetSpringDampingRatio(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetSpringDampingRatio(jointID(jointID));
    }
    /// @api b3SphericalJoint_SetTargetRotation
    public void sphericalJoint_SetTargetRotation(JointID<JointType.Spherical> jointID, Quaternionf targetRotation) {
        b3SphericalJoint_SetTargetRotation(jointID(jointID), quat(targetRotation));
    }
    /// @api b3SphericalJoint_GetTargetRotation
    public Quaternionf sphericalJoint_GetTargetRotation(Quaternionf dest, JointID<JointType.Spherical> jointID) {
        var rotation = b3SphericalJoint_GetTargetRotation(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setQuat(dest, rotation);
    }
    /// @api b3SphericalJoint_EnableMotor
    public void sphericalJoint_EnableMotor(JointID<JointType.Spherical> jointID, boolean enableMotor) {
        b3SphericalJoint_EnableMotor(jointID(jointID), enableMotor);
    }
    /// @api b3SphericalJoint_IsMotorEnabled
    public boolean sphericalJoint_IsMotorEnabled(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_IsMotorEnabled(jointID(jointID));
    }
    /// @api b3SphericalJoint_SetMotorVelocity
    public void sphericalJoint_SetMotorVelocity(JointID<JointType.Spherical> jointID, Vector3f motorVelocity) {
        b3SphericalJoint_SetMotorVelocity(jointID(jointID), vec3(motorVelocity));
    }
    /// @api b3SphericalJoint_GetMotorVelocity
    public Vector3f sphericalJoint_GetMotorVelocity(Vector3f dest, JointID<JointType.Spherical> jointID) {
        var velocity = b3SphericalJoint_GetMotorVelocity(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setVec3(dest, velocity);
    }
    /// @api b3SphericalJoint_GetMotorTorque
    public Vector3f sphericalJoint_GetMotorTorque(Vector3f dest, JointID<JointType.Spherical> jointID) {
        var torque = b3SphericalJoint_GetMotorTorque(this.returnArena, jointID(jointID));
        return PrimitiveMemOps.setVec3(dest, torque);
    }
    /// @api b3SphericalJoint_SetMaxMotorTorque
    public void sphericalJoint_SetMaxMotorTorque(JointID<JointType.Spherical> jointID, float torque) {
        b3SphericalJoint_SetMaxMotorTorque(jointID(jointID), torque);
    }
    /// @api b3SphericalJoint_GetMaxMotorTorque
    public float sphericalJoint_GetMaxMotorTorque(JointID<JointType.Spherical> jointID) {
        return b3SphericalJoint_GetMaxMotorTorque(jointID(jointID));
    }
    //</editor-fold>

    //<editor-fold desc="Weld Joints" default-state="collapsed">


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


    /// @api b3WeldJoint_SetLinearHertz
    public void weldJointSetLinearHertz(JointID<JointType.Weld> jointID, float hertz) {
        b3WeldJoint_SetLinearHertz(jointID(jointID), hertz);
    }
    /// @api b3WeldJoint_GetLinearHertz
    public float weldJointGetLinearHertz(JointID<JointType.Weld> jointID) {
        return b3WeldJoint_GetLinearHertz(jointID(jointID));
    }
    /// @api b3WeldJoint_SetLinearDampingRatio
    public void weldJointSetLinearDampingRatio(JointID<JointType.Weld> jointID, float dampingRatio) {
        b3WeldJoint_SetLinearDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3WeldJoint_GetLinearDampingRatio
    public float weldJointGetLinearDampingRatio(JointID<JointType.Weld> jointID) {
        return b3WeldJoint_GetLinearDampingRatio(jointID(jointID));
    }
    /// @api b3WeldJoint_SetAngularHertz
    public void weldJointSetAngularHertz(JointID<JointType.Weld> jointID, float hertz) {
        b3WeldJoint_SetAngularHertz(jointID(jointID), hertz);
    }
    /// @api b3WeldJoint_GetAngularHertz
    public float weldJointGetAngularHertz(JointID<JointType.Weld> jointID) {
        return b3WeldJoint_GetAngularHertz(jointID(jointID));
    }
    /// @api b3WeldJoint_SetAngularDampingRatio
    public void weldJointSetAngularDampingRatio(JointID<JointType.Weld> jointID, float dampingRatio) {
        b3WeldJoint_SetAngularDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3WeldJoint_GetAngularDampingRatio
    public float weldJointGetAngularDampingRatio(JointID<JointType.Weld> jointID) {
        return b3WeldJoint_GetAngularDampingRatio(jointID(jointID));
    }
    //</editor-fold>

    //<editor-fold desc="Wheel Joints" default-state="collapsed">


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


    /// @api b3WheelJoint_EnableSuspension
    public void wheelJointEnableSuspension(JointID<JointType.Wheel> jointID, boolean flag) {
        b3WheelJoint_EnableSuspension(jointID(jointID), flag);
    }
    /// @api b3WheelJoint_IsSuspensionEnabled
    public boolean wheelJointIsSuspensionEnabled(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_IsSuspensionEnabled(jointID(jointID));
    }
    /// @api b3WheelJoint_SetSuspensionHertz
    public void wheelJointSetSuspensionHertz(JointID<JointType.Wheel> jointID, float hertz) {
        b3WheelJoint_SetSuspensionHertz(jointID(jointID), hertz);
    }
    /// @api b3WheelJoint_GetSuspensionHertz
    public float wheelJointGetSuspensionHertz(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSuspensionHertz(jointID(jointID));
    }
    /// @api b3WheelJoint_SetSuspensionDampingRatio
    public void wheelJointSetSuspensionDampingRatio(JointID<JointType.Wheel> jointID, float dampingRatio) {
        b3WheelJoint_SetSuspensionDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3WheelJoint_GetSuspensionDampingRatio
    public float wheelJointGetSuspensionDampingRatio(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSuspensionDampingRatio(jointID(jointID));
    }
    /// @api b3WheelJoint_EnableSuspensionLimit
    public void wheelJointEnableSuspensionLimit(JointID<JointType.Wheel> jointID, boolean flag) {
        b3WheelJoint_EnableSuspensionLimit(jointID(jointID), flag);
    }
    /// @api b3WheelJoint_IsSuspensionLimitEnabled
    public boolean wheelJointIsSuspensionLimitEnabled(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_IsSuspensionLimitEnabled(jointID(jointID));
    }
    /// @api b3WheelJoint_GetLowerSuspensionLimit
    public float wheelJointGetLowerSuspensionLimit(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetLowerSuspensionLimit(jointID(jointID));
    }
    /// @api b3WheelJoint_GetUpperSuspensionLimit
    public float wheelJointGetUpperSuspensionLimit(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetUpperSuspensionLimit(jointID(jointID));
    }
    /// @api b3WheelJoint_SetSuspensionLimits
    public void wheelJointSetSuspensionLimits(JointID<JointType.Wheel> jointID, float lower, float upper) {
        b3WheelJoint_SetSuspensionLimits(jointID(jointID), lower, upper);
    }
    /// @api b3WheelJoint_EnableSpinMotor
    public void wheelJointEnableSpinMotor(JointID<JointType.Wheel> jointID, boolean flag) {
        b3WheelJoint_EnableSpinMotor(jointID(jointID), flag);
    }
    /// @api b3WheelJoint_IsSpinMotorEnabled
    public boolean wheelJointIsSpinMotorEnabled(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_IsSpinMotorEnabled(jointID(jointID));
    }
    /// @api b3WheelJoint_SetSpinMotorSpeed
    public void wheelJointSetSpinMotorSpeed(JointID<JointType.Wheel> jointID, float speed) {
        b3WheelJoint_SetSpinMotorSpeed(jointID(jointID), speed);
    }
    /// @api b3WheelJoint_GetSpinMotorSpeed
    public float wheelJointGetSpinMotorSpeed(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSpinMotorSpeed(jointID(jointID));
    }
    /// @api b3WheelJoint_SetMaxSpinTorque
    public void wheelJointSetMaxSpinTorque(JointID<JointType.Wheel> jointID, float torque) {
        b3WheelJoint_SetMaxSpinTorque(jointID(jointID), torque);
    }
    /// @api b3WheelJoint_GetMaxSpinTorque
    public float wheelJointGetMaxSpinTorque(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetMaxSpinTorque(jointID(jointID));
    }
    /// @api b3WheelJoint_GetSpinSpeed
    public float wheelJointGetSpinSpeed(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSpinSpeed(jointID(jointID));
    }
    /// @api b3WheelJoint_GetSpinTorque
    public float wheelJointGetSpinTorque(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSpinTorque(jointID(jointID));
    }
    /// @api b3WheelJoint_EnableSteering
    public void wheelJointEnableSteering(JointID<JointType.Wheel> jointID, boolean flag) {
        b3WheelJoint_EnableSteering(jointID(jointID), flag);
    }
    /// @api b3WheelJoint_IsSteeringEnabled
    public boolean wheelJointIsSteeringEnabled(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_IsSteeringEnabled(jointID(jointID));
    }
    /// @api b3WheelJoint_SetSteeringHertz
    public void wheelJointSetSteeringHertz(JointID<JointType.Wheel> jointID, float hertz) {
        b3WheelJoint_SetSteeringHertz(jointID(jointID), hertz);
    }
    /// @api b3WheelJoint_GetSteeringHertz
    public float wheelJointGetSteeringHertz(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSteeringHertz(jointID(jointID));
    }
    /// @api b3WheelJoint_SetSteeringDampingRatio
    public void wheelJointSetSteeringDampingRatio(JointID<JointType.Wheel> jointID, float dampingRatio) {
        b3WheelJoint_SetSteeringDampingRatio(jointID(jointID), dampingRatio);
    }
    /// @api b3WheelJoint_GetSteeringDampingRatio
    public float wheelJointGetSteeringDampingRatio(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSteeringDampingRatio(jointID(jointID));
    }
    /// @api b3WheelJoint_SetMaxSteeringTorque
    public void wheelJointSetMaxSteeringTorque(JointID<JointType.Wheel> jointID, float torque) {
        b3WheelJoint_SetMaxSteeringTorque(jointID(jointID), torque);
    }
    /// @api b3WheelJoint_GetMaxSteeringTorque
    public float wheelJointGetMaxSteeringTorque(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetMaxSteeringTorque(jointID(jointID));
    }
    /// @api b3WheelJoint_EnableSteeringLimit
    public void wheelJointEnableSteeringLimit(JointID<JointType.Wheel> jointID, boolean flag) {
        b3WheelJoint_EnableSteeringLimit(jointID(jointID), flag);
    }
    /// @api b3WheelJoint_IsSteeringLimitEnabled
    public boolean wheelJointIsSteeringLimitEnabled(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_IsSteeringLimitEnabled(jointID(jointID));
    }
    /// @api b3WheelJoint_GetLowerSteeringLimit
    public float wheelJointGetLowerSteeringLimit(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetLowerSteeringLimit(jointID(jointID));
    }
    /// @api b3WheelJoint_GetUpperSteeringLimit
    public float wheelJointGetUpperSteeringLimit(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetUpperSteeringLimit(jointID(jointID));
    }
    /// @api b3WheelJoint_SetSteeringLimits
    public void wheelJointSetSteeringLimits(JointID<JointType.Wheel> jointID, float lowerRadians, float upperRadians) {
        b3WheelJoint_SetSteeringLimits(jointID(jointID), lowerRadians, upperRadians);
    }
    /// @api b3WheelJoint_SetTargetSteeringAngle
    public void wheelJointSetTargetSteeringAngle(JointID<JointType.Wheel> jointID, float radians) {
        b3WheelJoint_SetTargetSteeringAngle(jointID(jointID), radians);
    }
    /// @api b3WheelJoint_GetTargetSteeringAngle
    public float wheelJointGetTargetSteeringAngle(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetTargetSteeringAngle(jointID(jointID));
    }
    /// @api b3WheelJoint_GetSteeringAngle
    public float wheelJointGetSteeringAngle(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSteeringAngle(jointID(jointID));
    }
    /// @api b3WheelJoint_GetSteeringTorque
    public float wheelJointGetSteeringTorque(JointID<JointType.Wheel> jointID) {
        return b3WheelJoint_GetSteeringTorque(jointID(jointID));
    }
    //</editor-fold>

    //</editor-fold>




    //<editor-fold desc="Internal" default-state="collapsed">


    static final int SECRET_COOKIE = 1152023; // sorry, i had to take it

    private final Arena scratchArena = Arena.ofAuto();

    /// 1 KB for return values. Should definitely be enough for all return types
    private final ReturnAllocator returnArena = new ReturnAllocator(this.scratchArena, 1 * 1024);

    /// 8 KB for arguments. Will create a confined arena when it runs out of space.
    /// Warning: The allocator can return segments with byte sizes larger than the requested size.
    private final StackAllocator argArena    = new StackAllocator(this.scratchArena, 8 * 1024);

    private final MemorySegment worldIDSegment    = b3WorldId    .allocate(this.scratchArena);
    private final MemorySegment bodyIDSegment     = b3BodyId     .allocate(this.scratchArena);
    private final MemorySegment contactIDSegment  = b3ContactId  .allocate(this.scratchArena);
    private final MemorySegment shapeIDSegment    = b3ShapeId    .allocate(this.scratchArena);
    private final MemorySegment jointIDSegment    = b3JointId    .allocate(this.scratchArena);
    private final MemorySegment vec3Segment       = b3Vec3       .allocate(this.scratchArena);
    private final MemorySegment vec3Segment2      = b3Vec3       .allocate(this.scratchArena);
    private final MemorySegment mat3Segment       = b3Matrix3    .allocate(this.scratchArena);
    private final MemorySegment quatSegment       = b3Quat       .allocate(this.scratchArena);
    private final MemorySegment transformSegment  = b3Transform  .allocate(this.scratchArena);
    private final MemorySegment aabbSegment       = b3AABB       .allocate(this.scratchArena);

    private final Quaternionf           scratchQuat        = new Quaternionf();
    private final SimplexCache          emptyDistanceCache = new SimplexCache();
    private final ScratchCastResultFcn  scratchCastFn      = new ScratchCastResultFcn(this.scratchArena);
    private final ScratchMoverFilterFcn scratchMoverFilter = new ScratchMoverFilterFcn(this.scratchArena);
    private final ScratchOverlapAABB    scratchOverlapAABB = new ScratchOverlapAABB(this.scratchArena);
    private final ScratchPlaneResultFcn scratchPlaneResult = new ScratchPlaneResultFcn(this.scratchArena);


    //<editor-fold desc="Destructors without reference" default-state="collapsed">


    void destroyWorld(long packedID) {
        var segment = worldID(packedID);
        if (!b3World_IsValid(segment)) {
            throw new IllegalStateException(
                    "World " +  WorldID.toString(packedID) + " is not valid anymore"
            );
        }

        b3DestroyWorld(segment);
    }

    void destroyHull(MemorySegment segment) {
        b3DestroyHull(segment);
    }

    void destroyMesh(MemorySegment segment) {
        b3DestroyMesh(segment);
    }

    void destroyHeightField(MemorySegment segment) {
        b3DestroyHeightField(segment);
    }

    void destroyBody(long packedID) {
        var segment = bodyID(packedID);
        if (!b3Body_IsValid(segment)) {
            throw new IllegalStateException(
                    "Body " + BodyID.toString(packedID) + " is not valid anymore"
            );
        }
        b3DestroyBody(segment);
    }
    //</editor-fold>

    //<editor-fold desc="Scratch Writes" default-state="collapsed">


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
        b3ContactId.world0(this.contactIDSegment, contactID.world0());
        b3ContactId.generation(this.contactIDSegment, contactID.generation());
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
    private static JointType jointType(int type) {
        return switch (type) {
            case b3_parallelJoint -> JointType.PARALLEL;
            case b3_distanceJoint -> JointType.DISTANCE;
            case b3_filterJoint -> JointType.FILTER;
            case b3_motorJoint -> JointType.MOTOR;
            case b3_prismaticJoint -> JointType.PRISMATIC;
            case b3_revoluteJoint -> JointType.REVOLUTE;
            case b3_sphericalJoint -> JointType.SPHERICAL;
            case b3_weldJoint -> JointType.WELD;
            case b3_wheelJoint -> JointType.WHEEL;
            default -> throw new IllegalArgumentException("Unknown joint type: " + type);
        };
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
    private static MemorySegment vec3(SegmentAllocator arena, Vector3f vec) {
        var segment = b3Vec3.allocate(arena);
        PrimitiveMemOps.putVec3(segment, vec);
        return segment;
    }
    private MemorySegment mat3(Matrix3f matrix3f) {
        PrimitiveMemOps.putMat3(this.mat3Segment, matrix3f);
        return this.mat3Segment;
    }
    private MemorySegment quat(Quaternionf quat) {
        PrimitiveMemOps.putQuat(this.quatSegment, quat);
        return this.quatSegment;
    }
    private MemorySegment aabb(AABB aabb) {
        aabb.put(this.aabbSegment);
        return this.aabbSegment;
    }
    //</editor-fold>

    //</editor-fold>

}

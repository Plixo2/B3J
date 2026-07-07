package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitveMemOps;
import org.box2d.box3d.b3DebugDraw;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class DebugDraw {

    public AABB drawingBounds;
    public float forceScale;
    public float jointScale;

    public boolean drawShapes;
    public boolean drawJoints;
    public boolean drawJointExtras;
    public boolean drawBounds;
    public boolean drawMass;
    public boolean drawBodyNames;
    public boolean drawContacts;
    public int drawAnchorA;
    public boolean drawGraphColors;
    public boolean drawContactFeatures;
    public boolean drawContactNormals;
    public boolean drawContactForces;
    public boolean drawFrictionForces;
    public boolean drawIslands;

    private final Callbacks<?> callbacks;

    private final MemorySegment shimDebugDrawSegment;

    /// @api b3DefaultDebugDraw
    public <T> DebugDraw(
            DebugShapeCollection<T> shapes,
            DebugDrawCallbacks<T> functions
    ) {
        this.callbacks = new Callbacks<>(functions, shapes);

        float h = 100.0f * B3.lengthUnitsPerMeter();

        this.drawingBounds = new AABB();
        this.drawingBounds.lowerBound.set(-h);
        this.drawingBounds.upperBound.set(h);

        this.forceScale = 1f;
        this.jointScale = 1f;

        this.shimDebugDrawSegment = alloc(Arena.ofAuto());
    }


    MemorySegment segment() {
        setArgs(this.shimDebugDrawSegment);
        return this.shimDebugDrawSegment;
    }


    private MemorySegment alloc(Arena arena) {

        var drawShape = b3DebugDraw.DrawShapeFcn.allocate(this.callbacks.drawShape, arena);
        var drawSegment = b3DebugDraw.DrawSegmentFcn.allocate(this.callbacks.drawSegment, arena);
        var drawTransform = b3DebugDraw.DrawTransformFcn.allocate(this.callbacks.drawTransform, arena);
        var drawPoint = b3DebugDraw.DrawPointFcn.allocate(this.callbacks.drawPoint, arena);
        var drawSphere = b3DebugDraw.DrawSphereFcn.allocate(this.callbacks.drawSphere, arena);
        var drawCapsule = b3DebugDraw.DrawCapsuleFcn.allocate(this.callbacks.drawCapsule, arena);
        var drawBounds = b3DebugDraw.DrawBoundsFcn.allocate(this.callbacks.drawBounds, arena);
        var drawBox = b3DebugDraw.DrawBoxFcn.allocate(this.callbacks.drawBox, arena);
        var drawString = b3DebugDraw.DrawStringFcn.allocate(this.callbacks.drawString, arena);

        var segment = b3DebugDraw.allocate(arena);

        b3DebugDraw.DrawShapeFcn(segment, drawShape);
        b3DebugDraw.DrawSegmentFcn(segment, drawSegment);
        b3DebugDraw.DrawTransformFcn(segment, drawTransform);
        b3DebugDraw.DrawPointFcn(segment, drawPoint);
        b3DebugDraw.DrawSphereFcn(segment, drawSphere);
        b3DebugDraw.DrawCapsuleFcn(segment, drawCapsule);
        b3DebugDraw.DrawBoundsFcn(segment, drawBounds);
        b3DebugDraw.DrawBoxFcn(segment, drawBox);
        b3DebugDraw.DrawStringFcn(segment, drawString);

        setArgs(segment);

        return segment;
    }

    private void setArgs(MemorySegment segment) {
        this.drawingBounds.put(b3DebugDraw.drawingBounds(segment));
        b3DebugDraw.forceScale(segment, this.forceScale);
        b3DebugDraw.jointScale(segment, this.jointScale);
        b3DebugDraw.drawShapes(segment, this.drawShapes);
        b3DebugDraw.drawJoints(segment, this.drawJoints);
        b3DebugDraw.drawJointExtras(segment, this.drawJointExtras);
        b3DebugDraw.drawBounds(segment, this.drawBounds);
        b3DebugDraw.drawMass(segment, this.drawMass);
        b3DebugDraw.drawBodyNames(segment, this.drawBodyNames);
        b3DebugDraw.drawContacts(segment, this.drawContacts);
        b3DebugDraw.drawAnchorA(segment, this.drawAnchorA);
        b3DebugDraw.drawGraphColors(segment, this.drawGraphColors);
        b3DebugDraw.drawContactFeatures(segment, this.drawContactFeatures);
        b3DebugDraw.drawContactNormals(segment, this.drawContactNormals);
        b3DebugDraw.drawContactForces(segment, this.drawContactForces);
        b3DebugDraw.drawFrictionForces(segment, this.drawFrictionForces);
        b3DebugDraw.drawIslands(segment, this.drawIslands);
    }

    private static final class Callbacks<T> {
        private DebugDrawCallbacks<T> functions;
        private DebugShapeCollection<T> shapes;

        private final Matrix4f t1 = new Matrix4f();
        private final Vector3f v1 = new Vector3f();
        private final Vector3f v2 = new Vector3f();
        private final AABB bounds = new AABB();

        public Callbacks(DebugDrawCallbacks<T> functions, DebugShapeCollection<T> shapes) {
            this.functions = functions;
            this.shapes = shapes;
        }

        final b3DebugDraw.DrawShapeFcn.Function drawShape = (MemorySegment ptr, MemorySegment transform, int color, MemorySegment _) -> {
            var shape = this.shapes.get(ptr.address());
            if (shape == null) {
                System.err.println("DebugDraw: Shape not found for index " + ptr.address());
                return false;
            }
            PrimitveMemOps.setTransform(this.t1, transform);

            return this.functions.drawShape(shape, this.t1, color);
        };

        final b3DebugDraw.DrawSegmentFcn.Function drawSegment = (MemorySegment p1, MemorySegment p2, int color, MemorySegment _) -> {
            PrimitveMemOps.setVec3(this.v1, p1);
            PrimitveMemOps.setVec3(this.v2, p2);
            this.functions.drawSegment(this.v1, this.v2, color);
        };

        final b3DebugDraw.DrawTransformFcn.Function drawTransform = (MemorySegment transform, MemorySegment _) -> {
            PrimitveMemOps.setTransform(this.t1, transform);
            this.functions.drawTransform(this.t1);
        };

        final b3DebugDraw.DrawPointFcn.Function drawPoint = (MemorySegment position, float size, int color, MemorySegment _) -> {
            PrimitveMemOps.setVec3(this.v1, position);
            this.functions.drawPoint(this.v1, size, color);
        };
        final b3DebugDraw.DrawSphereFcn.Function drawSphere = (MemorySegment position, float radius, int color, float alpha, MemorySegment _) -> {
            PrimitveMemOps.setVec3(this.v1, position);
            this.functions.drawSphere(this.v1, radius, color, alpha);
        };

        final b3DebugDraw.DrawCapsuleFcn.Function drawCapsule = (MemorySegment positionA, MemorySegment positionB, float radius, int color, float alpha, MemorySegment _) -> {
            PrimitveMemOps.setVec3(this.v1, positionA);
            PrimitveMemOps.setVec3(this.v2, positionB);
            this.functions.drawCapsule(this.v1, this.v2, radius, color, alpha);
        };

        final b3DebugDraw.DrawBoundsFcn.Function drawBounds = (MemorySegment aabb, int color, MemorySegment _) -> {
            this.bounds.set(aabb);
            this.functions.drawBounds(this.bounds, color);
        };
        final b3DebugDraw.DrawBoxFcn.Function drawBox = (MemorySegment extend, MemorySegment transform, int color, MemorySegment _) -> {
            PrimitveMemOps.setVec3(this.v1, extend);
            PrimitveMemOps.setTransform(this.t1, transform);
            this.functions.drawBox(this.v1, this.t1, color);
        };
        final b3DebugDraw.DrawStringFcn.Function drawString = (MemorySegment position, MemorySegment text, int color, MemorySegment _) -> {
            PrimitveMemOps.setVec3(this.v1, position);
            if (text.address() == 0) {
                return;
            }

            this.functions.drawString(this.v1, text.getString(0), color);
        };

    }


}

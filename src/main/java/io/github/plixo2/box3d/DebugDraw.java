package io.github.plixo2.box3d;

import io.github.plixo2.box3d.internal.PrimitiveMemOps;
import io.github.plixo2.box3d.internal.ShimArgBuffer;
import org.box2d.box3d.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.box2d.box3d.box3d_h.*;


import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

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

    private final MemorySegment shimDebugDrawSegment;

    private final Callbacks<?> callbacks;

    /// @api b3DefaultDebugDraw
    public <T> DebugDraw(
            DebugShapeCollection<T> shapes,
            DebugDrawCallbacks<T> functions
    ) {
        float h = 100.0f * B3.lengthUnitsPerMeter();

        this.drawingBounds = new AABB();
        this.drawingBounds.lowerBound.set(-h);
        this.drawingBounds.upperBound.set(h);

        this.forceScale = 1f;
        this.jointScale = 1f;

        this.shimDebugDrawSegment = alloc(Arena.ofAuto());
        this.callbacks = new Callbacks<>(this.shimDebugDrawSegment, functions, shapes);
    }

    MemorySegment segment() {
        setArgs(this.shimDebugDrawSegment);
        return this.shimDebugDrawSegment;
    }

    void invoke() {
        this.callbacks.invoke();
    }

    private MemorySegment alloc(Arena arena) {

        var shimDebugDrawSegment = b3jshimDebugDraw_Create();
        shimDebugDrawSegment = shimDebugDrawSegment.reinterpret(
                arena,
                box3d_h::b3jshimDebugDraw_Destroy
        );
        setArgs(shimDebugDrawSegment);

        return shimDebugDrawSegment;
    }

    private void setArgs(MemorySegment segment) {
        this.drawingBounds.put(b3jshimDebugDraw.drawingBounds(segment));
        b3jshimDebugDraw.forceScale(segment, this.forceScale);
        b3jshimDebugDraw.jointScale(segment, this.jointScale);
        b3jshimDebugDraw.drawShapes(segment, this.drawShapes);
        b3jshimDebugDraw.drawJoints(segment, this.drawJoints);
        b3jshimDebugDraw.drawJointExtras(segment, this.drawJointExtras);
        b3jshimDebugDraw.drawBounds(segment, this.drawBounds);
        b3jshimDebugDraw.drawMass(segment, this.drawMass);
        b3jshimDebugDraw.drawBodyNames(segment, this.drawBodyNames);
        b3jshimDebugDraw.drawContacts(segment, this.drawContacts);
        b3jshimDebugDraw.drawAnchorA(segment, this.drawAnchorA);
        b3jshimDebugDraw.drawGraphColors(segment, this.drawGraphColors);
        b3jshimDebugDraw.drawContactFeatures(segment, this.drawContactFeatures);
        b3jshimDebugDraw.drawContactNormals(segment, this.drawContactNormals);
        b3jshimDebugDraw.drawContactForces(segment, this.drawContactForces);
        b3jshimDebugDraw.drawFrictionForces(segment, this.drawFrictionForces);
        b3jshimDebugDraw.drawIslands(segment, this.drawIslands);
    }

    private static final class Callbacks<T> {

        private final DebugDrawCallbacks<T> functions;
        private final DebugShapeCollection<T> shapes;

        private final ShimArgBuffer drawShapeBuffer;
        private final ShimArgBuffer drawSegmentBuffer;
        private final ShimArgBuffer drawTransformBuffer;
        private final ShimArgBuffer drawPointBuffer;
        private final ShimArgBuffer drawSphereBuffer;
        private final ShimArgBuffer drawCapsuleBuffer;
        private final ShimArgBuffer drawBoundsBuffer;
        private final ShimArgBuffer drawBoxBuffer;
        private final ShimArgBuffer drawStringBuffer;
        private final ShimArgBuffer textBuffer;


        private final Matrix4f t1 = new Matrix4f();
        private final Vector3f v1 = new Vector3f();
        private final Vector3f v2 = new Vector3f();
        private final AABB bounds = new AABB();

        Callbacks(
                MemorySegment segment,
                DebugDrawCallbacks<T> functions,
                DebugShapeCollection<T> shapes
        ) {
            this.functions = functions;
            this.shapes = shapes;
            this.drawShapeBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawShapeBuffer(segment));
            this.drawSegmentBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawSegmentBuffer(segment));
            this.drawTransformBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawTransformBuffer(segment));
            this.drawPointBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawPointBuffer(segment));
            this.drawSphereBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawSphereBuffer(segment));
            this.drawCapsuleBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawCapsuleBuffer(segment));
            this.drawBoundsBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawBoundsBuffer(segment));
            this.drawBoxBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawBoxBuffer(segment));
            this.drawStringBuffer = new ShimArgBuffer(b3jshimDebugDraw.DrawStringBuffer(segment));
            this.textBuffer = new ShimArgBuffer(b3jshimDebugDraw.TextBuffer(segment));

        }

        void invoke() {

            drawShapes();
            drawSegments();
            drawTransforms();
            drawPoints();
            drawSpheres();
            drawCapsules();
            drawBounds();
            drawBoxes();
            drawStrings();

        }

        private void drawShapes() {
            var count = this.drawShapeBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var ptr_offset = b3jshimDrawShapeArgs.userShape$offset();
            var transform_offset = b3jshimDrawShapeArgs.transform$offset();
            var color_offset = b3jshimDrawShapeArgs.color$offset();
            var total_size = b3jshimDrawShapeArgs.sizeof();

            var data = this.drawShapeBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                var shapePtr = data.get(ValueLayout.JAVA_LONG, byteOffset + ptr_offset);

                var shape = this.shapes.get(shapePtr);
                if (shape == null) {
                    System.err.println("DebugDraw: Shape not found for index " + shapePtr);
                    continue;
                }

                PrimitiveMemOps.setTransform(this.t1, data, byteOffset + transform_offset);
                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);

                if (!this.functions.drawShape(shape, this.t1, color)) {
                    break;
                }
            }
        }

        private void drawSegments() {
            var count = this.drawSegmentBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var p1_offset = b3jshimDrawSegmentArgs.p1$offset();
            var p2_offset = b3jshimDrawSegmentArgs.p2$offset();
            var color_offset = b3jshimDrawSegmentArgs.color$offset();
            var total_size = b3jshimDrawSegmentArgs.sizeof();

            var data = this.drawSegmentBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                PrimitiveMemOps.setVec3(this.v1, data, byteOffset + p1_offset);
                PrimitiveMemOps.setVec3(this.v2, data, byteOffset + p2_offset);

                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);

                this.functions.drawSegment(this.v1, this.v2, color);
            }

        }

        private void drawTransforms() {
            var count = this.drawTransformBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var transform_offset = b3jshimDrawTransformArgs.transform$offset();
            var total_size = b3jshimDrawTransformArgs.sizeof();

            var data = this.drawTransformBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                PrimitiveMemOps.setTransform(this.t1, data, byteOffset + transform_offset);

                this.functions.drawTransform(this.t1);
            }

        }

        private void drawPoints() {
            var count = this.drawPointBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var p_offset = b3jshimDrawPointArgs.p$offset();
            var size_offset = b3jshimDrawPointArgs.size$offset();
            var color_offset = b3jshimDrawPointArgs.color$offset();
            var total_size = b3jshimDrawPointArgs.sizeof();

            var data = this.drawPointBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                PrimitiveMemOps.setVec3(this.v1, data, byteOffset + p_offset);
                var size = data.get(ValueLayout.JAVA_FLOAT, byteOffset + size_offset);
                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);

                this.functions.drawPoint(this.v1, size, color);
            }

        }
        private void drawSpheres() {
            var count = this.drawSphereBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var p_offset = b3jshimDrawSphereArgs.p$offset();
            var radius_offset = b3jshimDrawSphereArgs.radius$offset();
            var color_offset = b3jshimDrawSphereArgs.color$offset();
            var alpha_offset = b3jshimDrawSphereArgs.alpha$offset();
            var total_size = b3jshimDrawSphereArgs.sizeof();

            var data = this.drawSphereBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                PrimitiveMemOps.setVec3(this.v1, data, byteOffset + p_offset);
                var radius = data.get(ValueLayout.JAVA_FLOAT, byteOffset + radius_offset);
                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);
                var alpha = data.get(ValueLayout.JAVA_FLOAT, byteOffset + alpha_offset);

                this.functions.drawSphere(this.v1, radius, color, alpha);
            }

        }
        private void drawCapsules() {
            var count = this.drawCapsuleBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var p1_offset = b3jshimDrawCapsuleArgs.p1$offset();
            var p2_offset = b3jshimDrawCapsuleArgs.p2$offset();
            var radius_offset = b3jshimDrawCapsuleArgs.radius$offset();
            var color_offset = b3jshimDrawCapsuleArgs.color$offset();
            var alpha_offset = b3jshimDrawCapsuleArgs.alpha$offset();
            var total_size = b3jshimDrawCapsuleArgs.sizeof();

            var data = this.drawCapsuleBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                PrimitiveMemOps.setVec3(this.v1, data, byteOffset + p1_offset);
                PrimitiveMemOps.setVec3(this.v2, data, byteOffset + p2_offset);
                var radius = data.get(ValueLayout.JAVA_FLOAT, byteOffset + radius_offset);
                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);
                var alpha = data.get(ValueLayout.JAVA_FLOAT, byteOffset + alpha_offset);

                this.functions.drawCapsule(this.v1, this.v2, radius, color, alpha);
            }

        }

        private void drawBounds() {
            var count = this.drawBoundsBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var aabb_offset = b3jshimDrawBoundsArgs.aabb$offset();
            var color_offset = b3jshimDrawBoundsArgs.color$offset();
            var total_size = b3jshimDrawBoundsArgs.sizeof();

            var data = this.drawBoundsBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                this.bounds.set(data, byteOffset + aabb_offset);
                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);

                this.functions.drawBounds(this.bounds, color);
            }

        }

        private void drawBoxes() {
            var count = this.drawBoxBuffer.elementCount();
            if (count == 0) {
                return;
            }

            var extents_offset = b3jshimDrawBoxArgs.extents$offset();
            var transform_offset = b3jshimDrawBoxArgs.transform$offset();
            var color_offset = b3jshimDrawBoxArgs.color$offset();
            var total_size = b3jshimDrawBoxArgs.sizeof();

            var data = this.drawBoxBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                PrimitiveMemOps.setVec3(this.v1, data, byteOffset + extents_offset);
                PrimitiveMemOps.setTransform(this.t1, data, byteOffset + transform_offset);
                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);

                this.functions.drawBox(this.v1, this.t1, color);
            }

        }

        private void drawStrings() {
            var count = this.drawStringBuffer.elementCount();
            if (count == 0) {
                return;
            }
            var text = this.textBuffer.data();

            var p_offset = b3jshimDrawStringArgs.p$offset();
            var s_offset = b3jshimDrawStringArgs.s$offset();
            var color_offset = b3jshimDrawStringArgs.color$offset();
            var total_size = b3jshimDrawStringArgs.sizeof();

            var data = this.drawStringBuffer.data();

            for (var i = 0; i < count; i++) {
                var byteOffset = i * total_size;
                PrimitiveMemOps.setVec3(this.v1, data, byteOffset + p_offset);
                var offset = data.get(ValueLayout.JAVA_LONG, byteOffset + s_offset);
                var color = data.get(ValueLayout.JAVA_INT, byteOffset + color_offset);

                this.functions.drawString(this.v1, text, offset, color);
            }

        }


    }


}

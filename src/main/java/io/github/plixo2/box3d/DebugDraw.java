package io.github.plixo2.box3d;

import org.box2d.box3d.b3DebugDraw;

import java.lang.foreign.MemorySegment;
import java.util.List;

public abstract class DebugDraw {

    protected DebugDraw() {

    }

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

    public abstract boolean drawShape(Transform transform, int color);

    public abstract void drawSegment(Vec3 p1, Vec3 p2, int color);

    public abstract void drawTransform(Transform transform);

    public abstract void drawPoint(Vec3 position, float size, int color);

    public abstract void drawSphere(Vec3 center, float radius, int color, float alpha);

    public abstract void drawCapsule(Vec3 p1, Vec3 p2, float radius, int color, float alpha);

    public abstract void drawBounds(AABB aabb, int color);

    public abstract void drawBox(Vec3 extend, Transform transform, int color);

    public abstract void drawString(Vec3 pos, String text, int color);



    class Allocated {


        public Allocated() {

            new b3DebugDraw.DrawShapeFcn.Function() {

                @Override
                public boolean apply(
                        MemorySegment _x0,
                        MemorySegment _x1,
                        int _x2,
                        MemorySegment _x3
                ) {

                    return false;
                }
            };

        }

    }


}

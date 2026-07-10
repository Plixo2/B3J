package io.github.plixo2.box3d;

import lombok.Getter;

@Getter
public sealed abstract class AbstractJointDef<T extends JointType>
    permits
        DistanceJointDef,
        FilterJointDef,
        MotorJointDef,
        ParallelJointDef,
        PrismaticJointDef,
        RevoluteJointDef,
        SphericalJointDef,
        WeldJointDef,
        WheelJointDef
{

    protected JointDef base;
    private final T type;

    public AbstractJointDef(
            T type,
            BodyID bodyIdA,
            BodyID bodyIdB
    ) {
        this.type = type;
        this.base = new JointDef(bodyIdA, bodyIdB);
    }


}

package io.github.plixo2.box3d;

public sealed interface JointType {

    JointType.Parallel   PARALLEL  = new Parallel();
    JointType.Distance   DISTANCE  = new Distance();
    JointType.Filter     FILTER    = new Filter();
    JointType.Motor      MOTOR     = new Motor();
    JointType.Prismatic  PRISMATIC = new Prismatic();
    JointType.Revolute   REVOLUTE  = new Revolute();
    JointType.Spherical  SPHERICAL = new Spherical();
    JointType.Weld       WELD      = new Weld();
    JointType.Wheel      WHEEL     = new Wheel();


    final class Parallel  implements JointType { private Parallel()  {} }
    final class Distance  implements JointType { private Distance()  {} }
    final class Filter    implements JointType { private Filter()    {} }
    final class Motor     implements JointType { private Motor()     {} }
    final class Prismatic implements JointType { private Prismatic() {} }
    final class Revolute  implements JointType { private Revolute()  {} }
    final class Spherical implements JointType { private Spherical() {} }
    final class Weld      implements JointType { private Weld()      {} }
    final class Wheel     implements JointType { private Wheel()     {} }

}

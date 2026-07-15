package io.github.plixo2.box3d;

import lombok.ToString;
import org.box2d.box3d.b3Profile;

import java.lang.foreign.MemorySegment;

@ToString
public class Profile {
    public float step;
    public float pairs;
    public float collide;
    public float solve;
    public float solverSetup;
    public float constraints;
    public float prepareConstraints;
    public float integrateVelocities;
    public float warmStart;
    public float solveImpulses;
    public float integratePositions;
    public float relaxImpulses;
    public float applyRestitution;
    public float storeImpulses;
    public float splitIslands;
    public float transforms;
    public float sensorHits;
    public float jointEvents;
    public float hitEvents;
    public float refit;
    public float bullets;
    public float sleepIslands;
    public float sensors;

    public Profile() {

    }

    public Profile(Profile other) {
        this.step = other.step;
        this.pairs = other.pairs;
        this.collide = other.collide;
        this.solve = other.solve;
        this.solverSetup = other.solverSetup;
        this.constraints = other.constraints;
        this.prepareConstraints = other.prepareConstraints;
        this.integrateVelocities = other.integrateVelocities;
        this.warmStart = other.warmStart;
        this.solveImpulses = other.solveImpulses;
        this.integratePositions = other.integratePositions;
        this.relaxImpulses = other.relaxImpulses;
        this.applyRestitution = other.applyRestitution;
        this.storeImpulses = other.storeImpulses;
        this.splitIslands = other.splitIslands;
        this.transforms = other.transforms;
        this.sensorHits = other.sensorHits;
        this.jointEvents = other.jointEvents;
        this.hitEvents = other.hitEvents;
        this.refit = other.refit;
        this.bullets = other.bullets;
        this.sleepIslands = other.sleepIslands;
        this.sensors = other.sensors;
    }

    Profile set(MemorySegment segment) {
        this.step = b3Profile.step(segment);
        this.pairs = b3Profile.pairs(segment);
        this.collide = b3Profile.collide(segment);
        this.solve = b3Profile.solve(segment);
        this.solverSetup = b3Profile.solverSetup(segment);
        this.constraints = b3Profile.constraints(segment);
        this.prepareConstraints = b3Profile.prepareConstraints(segment);
        this.integrateVelocities = b3Profile.integrateVelocities(segment);
        this.warmStart = b3Profile.warmStart(segment);
        this.solveImpulses = b3Profile.solveImpulses(segment);
        this.integratePositions = b3Profile.integratePositions(segment);
        this.relaxImpulses = b3Profile.relaxImpulses(segment);
        this.applyRestitution = b3Profile.applyRestitution(segment);
        this.storeImpulses = b3Profile.storeImpulses(segment);
        this.splitIslands = b3Profile.splitIslands(segment);
        this.transforms = b3Profile.transforms(segment);
        this.sensorHits = b3Profile.sensorHits(segment);
        this.jointEvents = b3Profile.jointEvents(segment);
        this.hitEvents = b3Profile.hitEvents(segment);
        this.refit = b3Profile.refit(segment);
        this.bullets = b3Profile.bullets(segment);
        this.sleepIslands = b3Profile.sleepIslands(segment);
        this.sensors = b3Profile.sensors(segment);
        return this;
    }
}

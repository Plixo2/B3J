package io.github.plixo2.box3d;

import lombok.ToString;
import org.box2d.box3d.b3Counters;

import java.lang.foreign.MemorySegment;

@ToString
public class Counters {
    public int bodyCount;
    public int shapeCount;
    public int contactCount;
    public int jointCount;
    public int islandCount;
    public int stackUsed;
    public int arenaCapacity;
    public int staticTreeHeight;
    public int treeHeight;
    public int satCallCount;
    public int satCacheHitCount;
    public int byteCount;
    public int taskCount;
    public final int[] colorCounts = new int[24];
    public final int[] manifoldCounts = new int[B3.CONTACT_MANIFOLD_COUNT_BUCKETS];
    public int awakeContactCount;
    public int recycledContactCount;
    public int distanceIterations;
    public int pushBackIterations;
    public int rootIterations;

    public Counters() {

    }
    public Counters(Counters other) {
        this.bodyCount = other.bodyCount;
        this.shapeCount = other.shapeCount;
        this.contactCount = other.contactCount;
        this.jointCount = other.jointCount;
        this.islandCount = other.islandCount;
        this.stackUsed = other.stackUsed;
        this.arenaCapacity = other.arenaCapacity;
        this.staticTreeHeight = other.staticTreeHeight;
        this.treeHeight = other.treeHeight;
        this.satCallCount = other.satCallCount;
        this.satCacheHitCount = other.satCacheHitCount;
        this.byteCount = other.byteCount;
        this.taskCount = other.taskCount;
        System.arraycopy(other.colorCounts, 0, this.colorCounts, 0, this.colorCounts.length);
        System.arraycopy(other.manifoldCounts, 0, this.manifoldCounts, 0, this.manifoldCounts.length);
        this.awakeContactCount = other.awakeContactCount;
        this.recycledContactCount = other.recycledContactCount;
        this.distanceIterations = other.distanceIterations;
        this.pushBackIterations = other.pushBackIterations;
        this.rootIterations = other.rootIterations;
    }

    Counters set(MemorySegment segment) {
        this.bodyCount = b3Counters.bodyCount(segment);
        this.shapeCount = b3Counters.shapeCount(segment);
        this.contactCount = b3Counters.contactCount(segment);
        this.jointCount = b3Counters.jointCount(segment);
        this.islandCount = b3Counters.islandCount(segment);
        this.stackUsed = b3Counters.stackUsed(segment);
        this.arenaCapacity = b3Counters.arenaCapacity(segment);
        this.staticTreeHeight = b3Counters.staticTreeHeight(segment);
        this.treeHeight = b3Counters.treeHeight(segment);
        this.satCallCount = b3Counters.satCallCount(segment);
        this.satCacheHitCount = b3Counters.satCacheHitCount(segment);
        this.byteCount = b3Counters.byteCount(segment);
        this.taskCount = b3Counters.taskCount(segment);
        for (var i = 0; i < this.colorCounts.length; i++) {
            this.colorCounts[i] = b3Counters.colorCounts(segment, i);
        }
        for (var i = 0; i < this.manifoldCounts.length; i++) {
            this.manifoldCounts[i] = b3Counters.manifoldCounts(segment, i);
        }
        this.awakeContactCount = b3Counters.awakeContactCount(segment);
        this.recycledContactCount = b3Counters.recycledContactCount(segment);
        this.distanceIterations = b3Counters.distanceIterations(segment);
        this.pushBackIterations = b3Counters.pushBackIterations(segment);
        this.rootIterations = b3Counters.rootIterations(segment);
        return this;
    }
}

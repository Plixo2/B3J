package io.github.plixo2.box3d;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.box2d.box3d.b3TreeStats;

import java.lang.foreign.MemorySegment;

@ToString
@EqualsAndHashCode
public class TreeStats {
    public int leafVisits;
    public int nodeVisits;

    public TreeStats() {

    }

    public TreeStats(TreeStats other) {
        this.leafVisits = other.leafVisits;
        this.nodeVisits = other.nodeVisits;
    }

    void set(MemorySegment segment) {
        this.leafVisits = b3TreeStats.leafVisits(segment);
        this.nodeVisits = b3TreeStats.nodeVisits(segment);
    }

}

package io.github.plixo2.box3d;

import org.box2d.box3d.b3Version;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.MemorySegment;

public record Version(
        int major,
        int minor,
        int revision
) {

    @Override
    public @NotNull String toString() {
        return this.major + "." + this.minor + "." + this.revision;
    }

    static Version of(MemorySegment segment) {
        var major = b3Version.major(segment);
        var minor = b3Version.minor(segment);
        var revision = b3Version.revision(segment);
        return new Version(major, minor, revision);
    }
}

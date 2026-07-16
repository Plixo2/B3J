package io.github.plixo2.box3d.region;

import io.github.plixo2.box3d.BodyID;
import io.github.plixo2.box3d.WorldID;
import io.github.plixo2.box3d.MeshData;
import io.github.plixo2.box3d.HeightFieldData;
import java.lang.foreign.Arena;

///
/// Regions are used to keep track of resources like
/// [BodyID], [WorldID], [MeshData] and [HeightFieldData].
///
/// If you are familiar with [Arena], this is similar.
///
/// You can destroy single resources independently,
/// so feel free to opt-out completly by just passing [#global()] as the region.
///
/// ## Confined regions
///
/// Resources registered to confined regions will be released when the region is closed.
/// They are released in the reverse order of registration.
///
/// If you need to share a confined region between
/// threads, you need to take care of synchronization yourself.
///
/// ### [#ofConfined()]
///
/// Creates a confined region, similar to [Arena#ofConfined()]
///
/// ```java
/// BodyID body;
/// try (var region = Region.ofConfined()) {
///     body = b3.createBody(region, worldID, new BodyDef())
/// } // b3DestroyBody is called here
///
/// b3.bodyGetPosition(new Vector3f(), body); // throws IllegalStateException
/// ```
///
/// ### [#ofConfined(Region)]
/// Creates a confined region with a parent region
///
/// The parent region will close the child, but the child region can be closed independently.
///
/// This is useful when you want to close a region manually, without a try-with-resources block,
/// but want to avoid extra cleanup calls when a outer object is closed.
///
/// Example: A game world may have multiple dimensions with multiple chunks.
/// Closing a world should close all dimensions, which should close all chunks,
/// but a chunk or a dimension should be abled to be unloaded independently.
///
/// ## [#global()]
/// Returns a global region, which is never closed. Similar to [Arena#global()]
///
/// Box3D will automatically destroy any bodies of a world when the world is destroyed,
/// so this should be used for bodies that are meant to live for the entire lifetime of a world.
///
/// A call to [#global()] returns a singleton.
/// This is a cheap operation so you dont have to store the region.
///
/// The Global region is thread safe.
///
public sealed interface Region
        extends
            AutoCloseable
        permits ConfinedRegion,
            GlobalRegion
{

    static Region global() {
        interface Holder {
            Region INSTANCE = new GlobalRegion();
        }
        return Holder.INSTANCE;
    }

    static Region ofConfined() {
        return new ConfinedRegion();
    }

    static Region ofConfined(Region parent) {
        return new ConfinedRegion(parent);
    }

    void register(Lifetime owner, Runnable cleanup);

    boolean isClosed();

    /// @throws UnsupportedOperationException if the region cannot be closed manually
    @Override
    void close();

}

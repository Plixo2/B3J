package io.github.plixo2.box3d.region;

import io.github.plixo2.box3d.internal.AllocState;

import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

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
/// but want to avoid extra cleanup calls when a outer object is closed:
///
/// A game world may have multiple Dimensions with multiple chunks:
/// Closing a world should close all dimensions, which should close all chunks,
/// but a chunk or a dimension may be unloaded at any time.
///
/// ```
/// class Chunk {
///     Region region;
///     BodyID body;
///
///     public Chunk(Region parent) {
///         this.region = Region.ofConfined(parent);
///         var heightFieldData = b3.createHeightField(this.region, ...);
///         // ...
///         this.body = b3.createBody(this.region, ...)
///     }
///
///     public void update(Region parent) {
///         this.region.close(); // destroys the height field and body
///         this.region = Region.ofConfined(parent);
///         var heightFieldData = b3.createHeightField(this.region, ...);
///         // ...
///         this.body = b3.createBody(this.region, ...)
///     }
///
///     static void main() {
///         try (var outer = Region.ofConfined()) {
///             var chunk = new Chunk(outer);
///             // ...
///             chunk.update(outer);
///             // ...
///             chunk.update(outer);
///
///         } // also destroys the height field and body
///     }
/// }
/// ```
///
/// ## [#global()]
/// Returns a global region, which is never closed. Similar to [Arena#global()]
///
/// Box3D will automatically destroy any bodies of a world when the world is destroyed,
/// so this should be used for bodies that are meant to live for the entire lifetime of a world.
///
/// A call to [#global()] returns a singleton. This is a cheap operation and you dont have to store it.
///
/// ## [#ofAuto(FreeList)]
/// Returns a region, tied to a [FreeList].
///
/// Resources registered to this region will be managed by the garbage collector.
/// These resources may be released in any order.
///
/// This can effectively synchronize your Java side with Box3D,
/// but it is not guaranteed to be immediate.
///
/// Unlike [Arena#ofAuto()], it takes a [FreeList] to be drained in your thread.
/// This is to avoid any race conditions that may occur,
/// since the [java.lang.ref.Cleaner] runs on a separate thread.
///
/// [#ofAuto(FreeList)] is a simple getter that returns the region of a [FreeList],
/// so this does not allocate a new region every time.
///
/// Unlike [Arena#ofAuto()], the region itself can stay reachable.
/// The objects are independently registered to a [java.lang.ref.Cleaner]
///
/// [FreeList#drain()] is a relatively cheap operation, so feel free to
/// call it in your game loop every frame:
///
/// ```
/// private static FreeList freeList = new FreeList();
/// public static Region GC_REGION = Region.ofAuto(freeList);
///
/// void update() {
///     freeList.drain();
///     b3.worldStep(...)
/// }
/// ```
///
///
/// ## Thread safety
///
/// - A [#ofAuto(FreeList)] region can be shared between threads,
/// but be careful when [FreeList#drain()] is called.
///
/// - [#global()] is thread safe.
///
/// - If you need to share a confined region between
/// threads, you need to take care synchronization yourself.
///
///
///
public sealed interface Region
        extends
            AutoCloseable
        permits
            AutoRegion,
            ConfinedRegion,
            GlobalRegion
{

    static Region global() {
        interface Holder {
            Region INSTANCE = new GlobalRegion();
        }
        return Holder.INSTANCE;
    }

    static Region ofAuto(FreeList freeList) {
        return freeList.region;
    }

    static Region ofConfined() {
        return new ConfinedRegion();
    }

    static Region ofConfined(Region parent) {
        return new ConfinedRegion(parent);
    }

    void register(AllocState owner, Runnable cleanup);

    default void register(AllocState owner, MemorySegment segment, Consumer<MemorySegment> cleanup) {
        register(owner, () -> cleanup.accept(segment));
    }

    /// @throws UnsupportedOperationException if the region cannot be closed manually
    @Override
    void close();

}

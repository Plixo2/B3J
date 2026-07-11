package io.github.plixo2.box3d;

import java.util.function.Function;
import org.jetbrains.annotations.Nullable;


/// Should be used for a replacement for `userData` in various Box3D objects.
///
/// <span style="color:red"><strong>Warning:</strong></span>
/// > Make sure to remove the data when the object is deleted, otherwise it will leak memory.
///
/// @see PointerData
public class UserData<T> extends PointerData<T> {

    public static final class OfBody<T> extends UserData<T> {

        /// @return the object associated with the body, or null if none exists
        public @Nullable T get(BodyID body) {
            return super.get(body.packedID());
        }

        /// @return the previous value associated with the body. Can be null
        public @Nullable T put(BodyID body, T object) {
            return super.put(body.packedID(), object);
        }

        /// @return the previous value associated with the body. Can be null
        public @Nullable T remove(BodyID body) {
            return super.remove(body.packedID());
        }

        public T putIfAbsent(BodyID body, T object) {
            return super.putIfAbsent(body.packedID(), object);
        }

        public T computeIfAbsent(BodyID body, Function<BodyID, ? extends T> supplier) {
            return super.computeIfAbsent(body.packedID(), (_) -> supplier.apply(body));
        }

    }

    public static final class OfWorld<T> extends UserData<T> {

        /// @return the object associated with the world, or null if none exists
        public @Nullable T get(WorldID world) {
            return super.get(world.packedID());
        }

        /// @return the previous value associated with the world. Can be null
        public @Nullable T put(WorldID world, T object) {
            return super.put(world.packedID(), object);
        }

        /// @return the previous value associated with the world. Can be null
        public @Nullable T remove(WorldID world) {
            return super.remove(world.packedID());
        }

        public T putIfAbsent(WorldID world, T object) {
            return super.putIfAbsent(world.packedID(), object);
        }

        public T computeIfAbsent(WorldID world, Function<WorldID, ? extends T> supplier) {
            return super.computeIfAbsent(world.packedID(), (_) -> supplier.apply(world));
        }

    }

    public static final class OfShape<T> extends UserData<T> {

        /// @return the object associated with the shape, or null if none exists
        public @Nullable T get(ShapeID shape) {
            return super.get(shape.packedID());
        }

        /// @return the previous value associated with the shape. Can be null
        public @Nullable T put(ShapeID shape, T object) {
            return super.put(shape.packedID(), object);
        }

        /// @return the previous value associated with the shape. Can be null
        public @Nullable T remove(ShapeID shape) {
            return super.remove(shape.packedID());
        }

        public T putIfAbsent(ShapeID shape, T object) {
            return super.putIfAbsent(shape.packedID(), object);
        }

        public T computeIfAbsent(ShapeID shape, Function<ShapeID, ? extends T> supplier) {
            return super.computeIfAbsent(shape.packedID(), (_) -> supplier.apply(shape));
        }

    }

    public static final class OfJoint<T> extends UserData<T> {

        /// @return the object associated with the joint, or null if none exists
        public @Nullable T get(JointID<?> joint) {
            return super.get(joint.packedID());
        }

        /// @return the previous value associated with the joint. Can be null
        public @Nullable T put(JointID<?> joint, T object) {
            return super.put(joint.packedID(), object);
        }

        /// @return the previous value associated with the joint. Can be null
        public @Nullable T remove(JointID<?> joint) {
            return super.remove(joint.packedID());
        }

        public T putIfAbsent(JointID<?> joint, T object) {
            return super.putIfAbsent(joint.packedID(), object);
        }

        public T computeIfAbsent(JointID<?> joint, Function<JointID<?>, ? extends T> supplier) {
            return super.computeIfAbsent(joint.packedID(), (_) -> supplier.apply(joint));
        }

    }


}

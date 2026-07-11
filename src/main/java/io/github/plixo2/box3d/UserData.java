package io.github.plixo2.box3d;

import java.util.function.Function;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class UserData<T> {

    private final LongObjectHashMap<T> objects = new LongObjectHashMap<>();

    private @Nullable T get(long id) {
        return this.objects.get(id);
    }
    private @Nullable T put(long id, T object) {
        return this.objects.put(id, object);
    }
    private @Nullable T remove(long id) {
        return this.objects.remove(id);
    }
    private T putIfAbsent(long id, T object) {
        return this.objects.getIfAbsentPut(id, object);
    }

    private <P> T computeIfAbsent(
            long id,
            org.eclipse.collections.api.block.function.Function<? super P, ? extends T> function,
            P parameter
    ) {
        return this.objects.getIfAbsentPutWith(id, function, parameter);
    }

    public final void removeIf(Predicate<? super T> predicate) {
        this.objects.removeIf((_, v) -> predicate.test(v));
    }

    public final void clear() {
        this.objects.clear();
    }

    public final boolean isEmpty() {
        return this.objects.isEmpty();
    }

    public final int size() {
        return this.objects.size();
    }

    public final Collection<T> values() {
        return this.objects.values();
    }

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
            return super.computeIfAbsent(body.packedID(), supplier::apply, body);
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
            return super.computeIfAbsent(world.packedID(), supplier::apply, world);
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
            return super.computeIfAbsent(shape.packedID(), supplier::apply, shape);
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
            return super.computeIfAbsent(joint.packedID(), supplier::apply, joint);
        }

    }


}

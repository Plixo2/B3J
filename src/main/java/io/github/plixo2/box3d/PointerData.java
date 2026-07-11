package io.github.plixo2.box3d;

import lombok.Getter;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.LongFunction;
import java.util.function.Predicate;

/// Should be used to associate any data with a pointer
///
/// <span style="color:red"><strong>Warning:</strong></span>
/// > Make sure to remove the data when the object is deleted, otherwise it will leak memory.
///
/// @see UserData
public class PointerData<T> {

    private final LongObjectHashMap<T> objects = new LongObjectHashMap<>();

    public @Nullable T get(long id) {
        return this.objects.get(id);
    }

    public @Nullable T put(long id, T object) {
        return this.objects.put(id, object);
    }

    public @Nullable T remove(long id) {
        return this.objects.remove(id);
    }

    public T putIfAbsent(long id, T object) {
        return this.objects.getIfAbsentPut(id, object);
    }

    public T computeIfAbsent(
            long id,
            LongFunction<? extends T> function
    ) {
        return this.objects.getIfAbsentPutWithKey(id, function::apply);
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


}

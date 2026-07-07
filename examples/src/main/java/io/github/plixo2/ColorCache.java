package io.github.plixo2;

import io.github.plixo2.abstraction.Color;

import java.util.HashMap;
import java.util.Map;

public class ColorCache {
    private final Map<Integer, Color> cache = new HashMap<>();

    public Color get(int color) {
        return this.cache.computeIfAbsent(color, Color::new);
    }

}

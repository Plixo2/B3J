package io.github.plixo2.abstraction;


import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;


/// Represents a high dynamic range color, where each component can be any non-negative float value.
/// The alpha component is still clamped to \[0, 1\] range. \
/// The RGB components can be greater than 1
///
/// Use {@link #clamped()} to convert to a regular 8-bit color
///
/// @see Color
public class HDRColor {
    public static final HDRColor YELLOW = new HDRColor(1,1,0);
    public final static HDRColor BLACK = new HDRColor(0,0,0);
    public final static HDRColor WHITE = new HDRColor(1,1,1);
    public final static HDRColor RED = new HDRColor(1,0,0);
    public final static HDRColor GREEN = new HDRColor(0, 1, 0);
    public final static HDRColor GRAY = new HDRColor(0.5f, 0.5f, 0.5f);
    public final static HDRColor BLUE = new HDRColor(0,0,1);
    public static final HDRColor PURPLE = new HDRColor(1f,0,1f);
    public static final HDRColor CYAN = new HDRColor(0,1,1);
    public final static HDRColor TRANSPARENT = new HDRColor(0 ,0,0,0);


    @Getter
    @Accessors(fluent = true)
    private final float red, green, blue, alpha;


    public HDRColor(Color color) {
        this.red = color.redFloat();
        this.green = color.greenFloat();
        this.blue = color.blueFloat();
        this.alpha = color.alphaFloat();
    }

    public HDRColor(float red, float green, float blue, float alpha) {
        this.red = Math.max(red, 0);
        this.green = Math.max(green, 0);
        this.blue = Math.max(blue, 0);
        this.alpha =  Math.clamp(alpha, 0f, 1f);
    }

    public HDRColor(float r, float g, float b) {
        this(r, g, b, 1f);
    }

    public float[] putRGBA(float[] dest) {
        dest[0] = red();
        dest[1] = green();
        dest[2] = blue();
        dest[3] = alpha();
        return dest;
    }

    public HDRColor mix(HDRColor color, float fade) {
        fade = Math.clamp(fade, 0f, 1f);
        if (fade == 0.0f) {
            return this;
        } else if (fade == 1.0f) {
            return color;
        }
        float r = red() + (color.red() - red()) * fade;
        float g = green() + (color.green() - green()) * fade;
        float b = blue() + (color.blue() - blue()) * fade;
        float a = alpha() + (color.alpha() - alpha()) * fade;

        return new HDRColor(r, g, b, a);
    }

    public Color clamped() {
        return new Color(
                (int) (this.red * 255),
                (int) (this.green * 255),
                (int) (this.blue * 255),
                (int) (this.alpha * 255)
        );
    }

    public HDRColor darker() {
        return darker(0.2f);
    }

    public HDRColor darker(float fade) {
        return mix(BLACK, fade);
    }

    public HDRColor brighter() {
        return brighter(0.2f);
    }

    public HDRColor mul(float scalar) {
        return new HDRColor(this.red * scalar, this.green * scalar, this.blue * scalar, this.alpha);
    }

    public HDRColor brighter(float fade) {
        return mix(WHITE, fade);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HDRColor hdrColor)) {
            return false;
        }
        return Float.compare(this.red, hdrColor.red) == 0
                && Float.compare(this.green, hdrColor.green) == 0
                && Float.compare(this.blue, hdrColor.blue) == 0
                && Float.compare(this.alpha, hdrColor.alpha) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.red, this.green, this.blue, this.alpha);
    }

    @Override
    public String toString() {
        return "HDRColor(r=" + this.red + ", g=" + this.green + ", b=" + this.blue + ", a=" + this.alpha + ")";
    }
}

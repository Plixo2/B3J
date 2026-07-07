package io.github.plixo2.abstraction;


import lombok.Getter;
import lombok.experimental.Accessors;

/// Represents a 8-bit color in Alpha-RGB format, so alpha is the highest byte:
/// ```
/// (alpha << 24) | (red << 16) | (green << 8) | blue
/// ```
///
/// Use {@link #unclamped()} to convert to a HDRColor
///
/// @see HDRColor
public class Color {
    public static final Color YELLOW = new Color(0xFFFFFF00);
    public static final Color ORANGE = new Color(0xFFFF8800);
    public static final Color PURPLE = new Color(0xFFA832A6);

    public final static Color BLACK = new Color(0xFF000000);
    public final static Color WHITE = new Color(0xFFFFFFFF);
    public final static Color RED = new Color(0xFFFF0000);
    public final static Color GREEN = new Color(0xFF00FF00);
    public final static Color GRAY = new Color(0xFF808080);
    public final static Color MAGENTA = new Color(0xFFFF00FF);
    public final static Color BLUE = new Color(0xFF0000FF);
    public static final Color CYAN = new Color(0xFF00FFFF);
    public final static Color TRANSPARENT = new Color(0);


    @Getter
    @Accessors(fluent = true)
    private final int argb;

    public Color(int argb) {
        this.argb = argb;
    }
    public Color(java.awt.Color awtColor) {
        this.argb = awtColor.getRGB();
    }

    public Color(int r, int g, int b, int a) {
        r = clamp_0_255(r);
        g = clamp_0_255(g);
        b = clamp_0_255(b);
        a = clamp_0_255(a);

        this.argb = a << 24 | r << 16 | g << 8 | b;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }


    public java.awt.Color toAWTColor() {
        return new java.awt.Color(red(), green(), blue(), alpha());
    }

    public float[] putRGBA(float[] dest) {
        dest[0] = redFloat();
        dest[1] = greenFloat();
        dest[2] = blueFloat();
        dest[3] = alphaFloat();
        return dest;
    }

    public Color withTransparency(float alpha) {
        var fade = clamp_0_1(alpha);
        return new Color(red(), green(), blue(), (int) (fade * 255));
    }

    public Color darker() {
        return darker(0.2f);
    }

    public Color darker(float fade) {
        return mixNoAlpha(BLACK, fade);
    }

    public Color brighter() {
        return brighter(0.2f);
    }

    public Color brighter(float fade) {
        return mixNoAlpha(WHITE, fade);
    }

    public int alpha() {
        return (this.argb >> 24) & 0xff;
    }

    public int red() {
        return (this.argb >> 16) & 0xff;
    }

    public int green() {
        return (this.argb >> 8) & 0xff;
    }

    public int blue() {
        return this.argb & 0xff;
    }

    public float redFloat() {
        return red() / 255f;
    }
    public float greenFloat() {
        return green() / 255f;
    }
    public float blueFloat() {
        return blue() / 255f;
    }

    public float alphaFloat() {
        return alpha() / 255f;
    }

    /// uses the alpha of `this`
    public Color mixNoAlpha(Color color, float t) {
        return mix(color, this.alpha(), t);
    }

    public Color mixWithAlpha(Color color, float t) {
        var code = mixWithAlpha(this.argb, color.argb, t);
        if (code == this.argb) {
            return this;
        } else if (code == color.argb) {
            return color;
        } else {
            return new Color(code);
        }
    }

    /// @see Color#mixWithAlpha(Color, float)
    /// @see Color#mixNoAlpha(Color, float)
    public Color mix(Color color, int alpha, float t) {
        var code = mix(this.argb, color.argb, alpha, t);
        if (code == this.argb) {
            return this;
        } else if (code == color.argb) {
            return color;
        }
        return new Color(code);
    }

    public HDRColor unclamped() {
        return new HDRColor(this);
    }

    @Override
    public String toString() {
        return "Color(" + toHexString() + ")";
    }

    public String toHexString() {
        return String.format("#%08X", this.argb);
    }

    public float getHue() {
        return getHue(red(), green(), blue());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Color other && other.argb == this.argb;
    }

    @Override
    public int hashCode() {
        return this.argb; // just return the rgba value, no need to hash
    }


    public static Color fromHSB(float hue, float saturation, float brightness) {
        var code = java.awt.Color.HSBtoRGB(hue % 1f, saturation, brightness);
        return new Color(code);
    }

    /// uses alpha of `color1`
    public static int mixNoAlpha(int color1, int color2, float t) {
        return mix(color1, color2, alpha(color1), t);
    }

    public static int mixWithAlpha(int color1, int color2, float t) {
        t = clamp_0_1(t);

        int a1 = alpha (color1);
        int r1 = red   (color1);
        int g1 = green (color1);
        int b1 = blue  (color1);

        int a2 = alpha (color2);
        int r2 = red   (color2);
        int g2 = green (color2);
        int b2 = blue  (color2);

        var a = (int) ((a2 - a1) * t + a1) << 24;
        var r = (int) ((r2 - r1) * t + r1) << 16;
        var g = (int) ((g2 - g1) * t + g1) << 8;
        var b = (int) ((b2 - b1) * t + b1);
        return a | r | g | b;
    }

    /// @see Color#mixWithAlpha(int, int, float)
    /// @see Color#mixNoAlpha(int, int, float)
    public static int mix(int color1, int color2, int alpha, float t) {
        t = clamp_0_1(t);

        int r1 = red   (color1);
        int g1 = green (color1);
        int b1 = blue  (color1);

        int r2 = red   (color2);
        int g2 = green (color2);
        int b2 = blue  (color2);

        var a = clamp_0_255(alpha) << 24;
        var r = (int) ((r2 - r1) * t + r1) << 16;
        var g = (int) ((g2 - g1) * t + g1) << 8;
        var b = (int) ((b2 - b1) * t + b1);
        return a | r | g | b;
    }

    public static int red(int color) {
        return (color >> 16) & 0xff;
    }
    public static int green(int color) {
        return (color >> 8) & 0xff;
    }
    public static int blue(int color) {
        return color & 0xff;
    }
    public static int alpha(int color) {
        return (color >> 24) & 0xff;
    }

    private static int clamp_0_255(int value) {
        return Math.max(0, Math.min(value, 255));
    }

    private static float clamp_0_1(float value) {
        return Math.max(0f, Math.min(value, 1f));
    }


    //<editor-fold desc="Hue, Saturation & Brightness from java.awt.Color.RGBtoHSB" default-state="collapsed">

    /// @see java.awt.Color#RGBtoHSB
    private static float getHue(int r, int g, int b) {
        float hue = 0;

        int cmax = cmax(r, g, b);
        int cmin = cmin(r, g, b);

        boolean hasSaturation = cmax != 0 && cmax != cmin;

        if (hasSaturation) {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        return hue;
    }
    /// @see java.awt.Color#RGBtoHSB
    private static float getSaturation(int r, int g, int b) {
        int cmax = cmax(r, g, b);
        if (cmax == 0) {
            return 0;
        }
        int cmin = cmin(r, g, b);
        return ((float) (cmax - cmin)) / ((float) cmax);
    }
    /// @see java.awt.Color#RGBtoHSB
    private static float getBrightness(int r, int g, int b) {
        return ((float) cmax(r, g, b)) / 255.0f;
    }

    /// @see java.awt.Color#RGBtoHSB
    private static int cmax(int r, int g, int b) {
        int cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        return cmax;
    }
    /// @see java.awt.Color#RGBtoHSB
    private static int cmin(int r, int g, int b) {
        int cmin = Math.min(r, g);
        if (b < cmin) cmin = b;
        return cmin;
    }
    //</editor-fold>

}

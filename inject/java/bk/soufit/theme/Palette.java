package bk.soufit.theme;
import android.graphics.Color;
public final class Palette {
    private Palette() {} public static final float HUE = 20f;
    public static final float MIN_SATURATION = 0.35f;
    public static final float MIN_VALUE = 0.35f;
    public static boolean captures(int colour, int reference) {
        if ((colour & 0xFFFFFF) == (reference & 0xFFFFFF)) return true;
        float[] here = hsv(colour);
        if ((reference & 0xFFFFFF) == 0xFE2C55 && here[1] >= 0.06f && here[2] >= 0.25f && (here[0] >= 300f || here[0] <= 10f)) return true;
        if (here[1] < MIN_SATURATION || here[2] < MIN_VALUE) return false;
        float apart = Math.abs(here[0] - hsv(reference)[0]);
        if (apart > 180f) apart = 360f - apart;
        return apart <= HUE;
    }

    public static int map(int colour, int reference, int accent) {
        if (reference == accent) return colour;
        if ((colour & 0xFFFFFF) == (accent & 0xFFFFFF)) return colour;
        try {
            if (!captures(colour, reference)) return colour;
            float[] here = hsv(colour);
            float[] from = hsv(reference);
            float[] to = hsv(accent);
            float hue = to[0] + (here[0] - from[0]);
            hue = ((hue % 360f) + 360f) % 360f;
            float saturation = from[1] == 0f ? to[1] : to[1] * (here[1] / from[1]);
            float value = from[2] == 0f ? to[2] : to[2] * (here[2] / from[2]);
            return Color.HSVToColor(Color.alpha(colour), new float[] {hue, clamp(saturation), clamp(value)});
        } catch (Throwable ignored) {
            return colour;
        }
    }

    public static boolean secondary(int colour) {
        int r = (colour >> 16) & 0xFF, g = (colour >> 8) & 0xFF, b = colour & 0xFF;
        int max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        if (max < 128 || (max - min) * 100 < 60 * max) return false;
        float d = max - min, hue;
        if (max == r) hue = ((g - b) / d) % 6f;
        else if (max == g) hue = (b - r) / d + 2f;
        else hue = (r - g) / d + 4f;
        hue *= 60f;
        if (hue < 0f) hue += 360f;
        if (hue >= 175f && hue <= 205f) return true;
        float saturation = d / max, value = max / 255f;
        return saturation >= 0.78f && value >= 0.72f && value <= 0.92f && hue >= 136f && hue <= 150f;
    }

    public static int toAccent(int colour, int accent) {
        return (colour & 0xFF000000) | (accent & 0xFFFFFF);
    }

    private static float[] hsv(int colour) {
        float[] out = new float[3];
        Color.colorToHSV(colour, out);
        return out;
    }

    private static float clamp(float value) {
        return value < 0f ? 0f : (value > 1f ? 1f : value);
    }
}

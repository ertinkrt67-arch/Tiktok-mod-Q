package bk.soufit.badges.model;

import java.util.Arrays;

public final class BadgeGradient {
    public enum Type { LINEAR, RADIAL, SWEEP }

    private final int[] colours;
    private final float angle;
    private final Type type;

    public BadgeGradient(int[] colours, float angle, Type type) {
        if (colours == null || colours.length < 2) {
            throw new IllegalArgumentException("a gradient needs at least two colours");
        }
        this.colours = new int[colours.length];
        for (int i = 0; i < colours.length; i++) {
            this.colours[i] = 0xFF000000 | (colours[i] & 0xFFFFFF);
        }
        this.angle = ((angle % 360f) + 360f) % 360f;
        this.type = type == null ? Type.LINEAR : type;
    }

    public int[] colours() { return colours.clone(); }
    public float angle() { return angle; }
    public Type type() { return type; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BadgeGradient)) return false;
        BadgeGradient that = (BadgeGradient) other;
        return Float.compare(angle, that.angle) == 0 && type == that.type && Arrays.equals(colours, that.colours);
    }

    @Override public int hashCode() {
        return 31 * (31 * Arrays.hashCode(colours) + Float.floatToIntBits(angle)) + type.hashCode();
    }
}

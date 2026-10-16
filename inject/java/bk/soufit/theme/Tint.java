package bk.soufit.theme;
public final class Tint {
    private Tint() {} public static final int GLASS_DARK = 0xFF1C1C1E;
    public static final int BLACK = 0xFF000000;
    public static boolean darkUnder(int colour) {
        int r = (colour >> 16) & 0xFF, g = (colour >> 8) & 0xFF, b = colour & 0xFF;
        float bright = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
        return bright < 0.5f;
    }

    public static boolean darkGlass(boolean themeDark, int ground) {
        return themeDark || darkUnder(ground);
    }

    public static int glassBody(boolean fullAmoled) {
        return fullAmoled ? BLACK : GLASS_DARK;
    }

    public static boolean neutralDark(int colour) {
        int red = (colour >> 16) & 0xFF, green = (colour >> 8) & 0xFF, blue = colour & 0xFF;
        int most = Math.max(red, Math.max(green, blue));
        int least = Math.min(red, Math.min(green, blue));
        return most - least <= 24 && darkUnder(colour);
    }

    public static int surface(boolean dark, boolean fullAmoled, int mixed) {
        return dark && fullAmoled ? BLACK : mixed;
    }
}

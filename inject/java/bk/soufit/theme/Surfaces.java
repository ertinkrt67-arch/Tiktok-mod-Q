package bk.soufit.theme;
import bk.soufit.core.soufiT;
import android.content.Context;
public final class Surfaces {
    private Surfaces() {}
    public static final String BAR = "bar", PROFILE = "profile", FEED = "feed", INBOX = "inbox";
    public static final String[] PIECES = {BAR, PROFILE, FEED, INBOX};
    public static final int DEFAULT = 0, MATERIAL = 1, GLASS = 2;

    private static final String LEGACY_PROFILE = "md3_profile", LEGACY_FEED = "md3_feed", LEGACY_INBOX = "md3_inbox";

    public static int choice(String piece) {
        try {
            Context context = soufiT.context();
            if (context != null) {
                int stored = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).getInt(key(piece), - 1);
                if (stored >= 0) return stored;
            }
        } catch (Throwable ignored) {} return fallback(piece);
    }

    private static int fallback(String piece) {
        boolean on;
        if (BAR.equals(piece)) on = NavBar.isEnabled();
        else if (PROFILE.equals(piece)) on = legacy(LEGACY_PROFILE, true);
        else if (FEED.equals(piece)) on = legacy(LEGACY_FEED, false);
        else on = legacy(LEGACY_INBOX, true);
        return on ? MATERIAL : DEFAULT;
    }

    private static boolean legacy(String key, boolean fallback) {
        try {
            Context context = soufiT.context();
            if (context != null) return context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).getBoolean(key, fallback);
        } catch (Throwable ignored) {} return fallback;
    }

    public static boolean mdThree(String piece) {
        return choice(piece) == MATERIAL;
    }

    public static boolean glass(String piece) {
        return choice(piece) == GLASS;
    }

    public static void set(String piece, int value) {
        try {
            Context context = soufiT.context();
            if (context != null) {
                context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putInt(key(piece), value).apply();
            }
        } catch (Throwable ignored) {}
        if (BAR.equals(piece)) NavBar.setEnabled(value != DEFAULT);
    }

    public static int glassCount() {
        int count = 0;
        for (String piece : PIECES) if (choice(piece) != DEFAULT) count ++;
        return count;
    }

    private static String key(String piece) {
        return "glass_choice_" + piece;
    }
}

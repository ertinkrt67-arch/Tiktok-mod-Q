package bk.soufit.core;
import bk.soufit.profile.Badges;
import android.content.Context;
public final class DevMode {
    private DevMode() {} public static final String KEY = "developer";
    private static final int NEEDED = 5;
    private static final long GAP = 1000L;
    private static volatile int state;
    private static int taps;
    private static long lastTap;
    public static boolean isOn() {
        int known = state;
        if (known != 0) return known == 2;
        Context context = soufiT.context();
        if (context == null) return false;
        boolean on = false;
        try {
            on = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false);
        } catch (Throwable ignored) {} state = on ? 2 : 1;
        return on;
    }

    public static void set(boolean on) {
        state = on ? 2 : 1;
        Flags.forget();
        Context context = soufiT.context();
        if (context == null) return;
        try {
            context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, on).apply();
        } catch (Throwable ignored) {} Badges.loadServer(context);
    }

    public static synchronized boolean tapped() {
        long now = android.os.SystemClock.uptimeMillis();
        taps = now - lastTap > GAP ? 1 : taps + 1;
        lastTap = now;
        if (taps < NEEDED) return false;
        taps = 0;
        return true;
    }
}

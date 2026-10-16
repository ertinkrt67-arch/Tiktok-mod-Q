package bk.soufit.feed;
import bk.soufit.core.Diary;
import bk.soufit.core.soufiT;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Display;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;
public final class Rate {
    public static final String KEY = "fps_lock";
    public static final String AUTO = "";
    public static final String[] CHOICES = {AUTO, "60", "90", "120"};
    private static final float CLOSE = 1.5f;
    private static volatile String chosen;
    private Rate() {} public static String name() {
        String known = chosen;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        String value = prefs == null ? AUTO : prefs.getString(KEY, AUTO);
        chosen = value;
        return value;
    }

    public static void choose(String value) {
        if (value == null) value = AUTO;
        chosen = value;
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putString(KEY, value).apply();
    }

    public static int wanted() {
        String value = name();
        if (value == null || value.length() == 0) return 0;
        try {
            return Integer.parseInt(value);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static SharedPreferences prefs() {
        Context context = soufiT.context();
        if (context == null) return null;
        try {
            return context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void apply(Activity activity) {
        int rate = wanted();
        if (rate <= 0 || activity == null) return;
        try {
            Window window = activity.getWindow();
            if (window == null) return;
            WindowManager.LayoutParams params = window.getAttributes();
            if (Build.VERSION.SDK_INT >= 23) {
                Display display = window.getWindowManager().getDefaultDisplay();
                Display.Mode now = display.getMode();
                Display.Mode best = null;
                for (Display.Mode mode : display.getSupportedModes()) {
                    if (mode.getPhysicalWidth() != now.getPhysicalWidth() || mode.getPhysicalHeight() != now.getPhysicalHeight()) {
                        continue;
                    }
                    if (Math.abs(mode.getRefreshRate() - rate) > CLOSE) continue;
                    if (best == null || mode.getRefreshRate() > best.getRefreshRate()) {
                        best = mode;
                    }
                }
                if (best != null) params.preferredDisplayModeId = best.getModeId();
            }
            params.preferredRefreshRate = rate;
            window.setAttributes(params);
        } catch (Throwable error) {
            Diary.note("fps: " + error);
        }
    }

    public static boolean reachable(Activity activity) {
        int rate = wanted();
        if (rate <= 0 || activity == null || Build.VERSION.SDK_INT < 23) return true;
        try {
            Display display = activity.getWindowManager().getDefaultDisplay();
            for (Display.Mode mode : display.getSupportedModes()) {
                if (Math.abs(mode.getRefreshRate() - rate) <= CLOSE) return true;
            }
            return false;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void setFrameRate(Surface surface, float rate, int compatibility) {
        int ours = wanted();
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                surface.setFrameRate(ours > 0 ? ours : rate, compatibility);
            }
        } catch (Throwable ignored) {}
    }

    public static void setFrameRate(Surface surface, float rate, int compatibility, int strategy) {
        int ours = wanted();
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                surface.setFrameRate(ours > 0 ? ours : rate, compatibility, strategy);
            } else if (Build.VERSION.SDK_INT >= 30) {
                surface.setFrameRate(ours > 0 ? ours : rate, compatibility);
            }
        } catch (Throwable ignored) {}
    }
}

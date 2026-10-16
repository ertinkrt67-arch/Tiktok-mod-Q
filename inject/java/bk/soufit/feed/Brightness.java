package bk.soufit.feed;
import bk.soufit.core.Diary;
import bk.soufit.core.soufiT;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
public final class Brightness {
    public static final String KEY = "own_brightness";
    private static volatile Boolean cached;
    private static volatile boolean told;
    private Brightness() {} public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        if (prefs == null) return true;
        boolean on = prefs.getBoolean(KEY, true);
        cached = on;
        return on;
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY, enabled).apply();
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

    public static void setAttributes(Window window, WindowManager.LayoutParams wanted) {
        try {
            if (wanted != null && wanted.screenBrightness >= 0f && isEnabled()) {
                if (!told) {
                    told = true;
                    Diary.detail("brightness: a window asked for " + wanted.screenBrightness + " and was not given it");
                }
                wanted.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            }
        } catch (Throwable ignored) {} window.setAttributes(wanted);
    }

    public static boolean flat() {
        return isEnabled();
    }

    public static void apply(Activity activity) {
        if (activity == null || !isEnabled()) return;
        try {
            Window window = activity.getWindow();
            if (window == null) return;
            WindowManager.LayoutParams now = window.getAttributes();
            if (now != null && now.screenBrightness >= 0f) {
                now.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                window.setAttributes(now);
            }
            if (Build.VERSION.SDK_INT >= 26 && window.getColorMode() == ActivityInfo.COLOR_MODE_HDR) {
                window.setColorMode(ActivityInfo.COLOR_MODE_DEFAULT);
            }
            if (Build.VERSION.SDK_INT >= 35) window.setDesiredHdrHeadroom(1f);
        } catch (Throwable error) {
            Diary.detail("brightness: " + error);
        }
    }
}

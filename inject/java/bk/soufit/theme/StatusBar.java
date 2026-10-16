package bk.soufit.theme;
import bk.soufit.core.Diary;
import bk.soufit.core.Flags;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
public final class StatusBar {
    public static final String KEY = "status_bar_hidden";
    private static final Map<View, Boolean> watched = new WeakHashMap<View, Boolean>();
    private StatusBar() {} public static boolean isOn() {
        return Flags.isOn(KEY);
    }

    public static void setOn(boolean on) {
        Flags.set(KEY, on);
        for (View decor : new ArrayList<View>(watched.keySet())) apply(decor, on, false);
    }

    public static void watch(Activity activity) {
        if (activity == null || activity.getClass().getName().startsWith("bk.soufit.")) return;
        try {
            final View decor = activity.getWindow().getDecorView();
            if (!watched.containsKey(decor)) {
                watched.put(decor, Boolean.TRUE);
                decor.getViewTreeObserver().addOnWindowFocusChangeListener(focus -> {
                    if (focus) apply(decor, isOn(), false);
                });
            }
            apply(decor, isOn(), false);
        } catch (Throwable error) {
            Diary.fail("status bar", error);
        }
    }

    private static int transientSwipeBehavior() {
        try {
            return WindowInsetsController.class.getField("BEHAVIOR_SHOW_TRANSIENT_BY_SWIPE").getInt(null);
        } catch (Throwable missing) {
            return 2;
        }
    }

    private static void apply(final View decor, final boolean on, boolean retried) {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                WindowInsetsController controller = decor.getWindowInsetsController();
                if (controller == null) {
                    if (!retried) decor.post(() -> apply(decor, isOn(), true));
                    return;
                }
                if (on) {
                    controller.setSystemBarsBehavior(transientSwipeBehavior());
                    controller.hide(WindowInsets.Type.statusBars());
                } else {
                    controller.show(WindowInsets.Type.statusBars());
                }
            } else {
                int flags = decor.getSystemUiVisibility();
                decor.setSystemUiVisibility(on ? flags | View.SYSTEM_UI_FLAG_FULLSCREEN : flags & ~View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        } catch (Throwable error) {
            Diary.fail("status bar", error);
        }
    }
}

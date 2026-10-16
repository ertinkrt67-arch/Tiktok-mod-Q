package bk.soufit.feed;
import bk.soufit.core.Diary;
import bk.soufit.core.soufiT;
import android.content.SharedPreferences;
import java.lang.reflect.Method;
public final class Seekbar {
    private Seekbar() {} public static final String KEY = "seekbar_always";
    private static final int ALWAYS = 0;
    private static volatile Boolean cached;
    private static volatile Method original;
    private static final java.util.Map<Object, Integer> requested = new java.util.WeakHashMap<Object, Integer>();
    public static void reloadSettings() {
        cached = null;
        setEnabled(isEnabled());
    }

    public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        if (prefs == null) return false;
        boolean on = false;
        try {
            on = prefs.getBoolean(KEY, false);
        } catch (Throwable ignored) {} cached = on;
        return on;
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY, enabled).apply();
        java.util.List<java.util.Map.Entry<Object, Integer>> views = new java.util.ArrayList<>();
        synchronized (requested) {
            for (java.util.Map.Entry<Object, Integer> entry : requested.entrySet()) views.add(new java.util.AbstractMap.SimpleImmutableEntry<Object,
                    Integer>(entry));
        }
        for (java.util.Map.Entry<Object, Integer> entry : views) {
            Runnable update = () -> invoke(entry.getKey(), isEnabled() ? ALWAYS : entry.getValue());
            if (entry.getKey() instanceof android.view.View)((android.view.View) entry.getKey()).post(update);
            else new android.os.Handler(android.os.Looper.getMainLooper()).post(update);
        }
    }

    private static SharedPreferences prefs() {
        android.content.Context context = soufiT.context();
        if (context == null) return null;
        return context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE);
    }

    public static void setSeekBarShowType(Object view, int type) {
        if (view == null) return;
        synchronized (requested) {
            requested.put(view, type);
        }
        invoke(view, isEnabled() ? ALWAYS : type);
    }

    private static void invoke(Object view, int type) {
        try {
            Method setter = original;
            if (setter == null || !setter.getDeclaringClass().isInstance(view)) {
                setter = view.getClass().getMethod("setSeekBarShowType", int.class);
                setter.setAccessible(true);
                original = setter;
            }
            setter.invoke(view, Integer.valueOf(type));
        } catch (Throwable error) {
            Diary.note("seekbar: " + error);
        }
    }
}

package bk.soufit.core;
import android.content.res.Resources;
import android.util.SparseArray;
import android.view.View;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
public final class Names {
    private Names() {} private static final SparseArray<String> names = new SparseArray<String>();
    private static final ConcurrentHashMap<Class<?>, String> types = new ConcurrentHashMap<Class<?>, String>();
    public static String of(View view) {
        if (view == null) return "";
        int id = view.getId();
        int owner = id >>> 24;
        if (owner != 0x7f && owner != 0x01) return "";
        try {
            return of(view.getResources(), id);
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static String of(Resources resources, int id) {
        int owner = id >>> 24;
        if (resources == null || (owner != 0x7f && owner != 0x01)) return "";
        synchronized (names) {
            String known = names.get(id);
            if (known != null) return known;
        }
        String name;
        try {
            name = resources.getResourceEntryName(id).toLowerCase(Locale.ROOT);
        } catch (Throwable none) {
            name = "";
        }
        synchronized (names) {
            names.put(id, name);
        }
        return name;
    }

    public static String type(View view) {
        Class<?> type = view.getClass();
        String known = types.get(type);
        if (known == null) {
            known = type.getName().toLowerCase(Locale.ROOT);
            types.put(type, known);
        }
        return known;
    }
}

package bk.soufit.core;
import bk.soufit.profile.AccountAppearance;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public final class Unsaved {
    private Unsaved() {} private static Map<String, Object> start;
    private static Map<String, Object> before;
    private static final Set<String> touched = new HashSet<String>();
    private static final Map<String, Object> implied = new HashMap<String, Object>();
    private static boolean elsewhere;
    public static void look(Context context) {
        try {
            Map<String, Object> now = read(context);
            if (start == null) start = now;
            before = now;
        } catch (Throwable error) {
            Diary.note("unsaved: " + error);
        }
    }

    public static boolean after(Context context) {
        try {
            Map<String, Object> now = read(context);
            if (start == null || before == null) {
                if (start == null) start = now;
                before = now;
                elsewhere = true;
                return true;
            }
            boolean moved = false;
            Set<String> keys = new HashSet<String>(now.keySet());
            keys.addAll(before.keySet());
            for (String key : keys) {
                Object is = now.get(key);
                if (same(is, before.get(key))) continue;
                moved = true;
                touched.add(key);
                if (!start.containsKey(key) && !implied.containsKey(key) && is instanceof Boolean) {
                    implied.put(key, Boolean.valueOf(!((Boolean) is).booleanValue()));
                }
            }
            before = now;
            if (!moved) elsewhere = true;
            if (elsewhere) return true;
            for (String key : touched) {
                Object was = start.containsKey(key) ? start.get(key) : implied.get(key);
                if (!same(was, now.get(key))) return true;
            }
            return false;
        } catch (Throwable error) {
            Diary.note("unsaved: " + error);
            return true;
        }
    }

    public static void other() {
        elsewhere = true;
    }

    private static boolean same(Object one, Object two) {
        return one == null ? two == null : one.equals(two);
    }

    private static Map<String, Object> read(Context context) {
        Set<String> files = new HashSet<String>();
        files.add(soufiT.PREFS);
        files.add("soufit_store");
        try {
            String[] names = new File(context.getApplicationInfo().dataDir, "shared_prefs").list();
            if (names != null) {
                for (String name : names) {
                    if (name.startsWith(soufiT.PREFS) && name.endsWith(".xml")) {
                        files.add(name.substring(0, name.length() - 4));
                    }
                }
            }
        } catch (Throwable ignored) {} Map<String, Object> all = new HashMap<String, Object>();
        for (String file : files) {
            SharedPreferences prefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
            for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
                all.put(file + "/" + entry.getKey(), entry.getValue());
            }
        }
        try {
            SharedPreferences own = AccountAppearance.prefs(context);
            if (own != null) {
                for (Map.Entry<String, ?> entry : own.getAll().entrySet()) {
                    all.put("appearance:" + AccountAppearance.scope(context) + "/" + entry.getKey(), entry.getValue());
                }
            }
        } catch (Throwable ignored) {} return all;
    }
}

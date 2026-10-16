package bk.soufit.ui;
import bk.soufit.core.Diary;
import bk.soufit.core.Text;
import bk.soufit.core.Version;
import bk.soufit.core.Watch;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
public final class SettingsCell {
    private SettingsCell() {} private static final String CELLS = "X.0MUp", STATE = "X.0MUl", COPY = "soufit";
    private static final String TRIED = "settings_cell_tried";
    private static volatile Object state;
    private static volatile Method cell;
    private static volatile Object unit;
    private static volatile boolean broken, drawn, counted;
    public static boolean drawn() {
        return drawn;
    }

    public static void newScreen() {
        drawn = false;
    }

    public static boolean likely() {
        if (broken) return false;
        try {
            cell();
            return true;
        } catch (Throwable missing) {
            Diary.note("settings row: not in TikTok's list, the build has no copy: " + missing);
            return false;
        }
    }

    public static String title() {
        return Text.ROW;
    }

    public static boolean draw(boolean first, Object composer) {
        if (broken || composer == null) return first;
        try {
            if (!counted) {
                counted = true;
                if (!mayTry()) {
                    broken = true;
                    return first;
                }
            }
            Object given = state();
            Method copy = cell();
            copy.invoke(null, given, first, false, composer, 0);
            if (!drawn) {
                drawn = true;
                Diary.note("settings row: in TikTok's own list");
            }
            return false;
        } catch (InvocationTargetException inside) {
            throw new RuntimeException(inside.getCause());
        } catch (Throwable error) {
            broken = true;
            Diary.note("settings row: stays above the screen: " + error);
            return first;
        }
    }

    public static Object again(final Object given, final boolean first, final boolean last, final int changed) {
        try {
            Class<?> kind = Class.forName("kotlin.jvm.functions.Function2");
            return Proxy.newProxyInstance(kind.getClassLoader(), new Class<?>[] {kind}, (proxy, method, args) -> {
                String name = method.getName();
                if (name.equals("invoke") && args != null && args.length == 2) {
                    try {
                        cell().invoke(null, given, first, last, args[0], changed | 1);
                    } catch (InvocationTargetException inside) {
                        throw inside.getCause();
                    }
                    return unit();
                }
                return plain(proxy, name, args);
            });
        } catch (Throwable error) {
            Diary.note("settings row: " + error);
            return null;
        }
    }

    private static Object plain(Object proxy, String name, Object[] args) {
        if (name.equals("hashCode")) return System.identityHashCode(proxy);
        if (name.equals("equals")) return args != null && args.length == 1 && proxy == args[0];
        if (name.equals("toString")) return "soufit-settings-row";
        return null;
    }

    private static Method cell() throws Exception {
        Method known = cell;
        if (known != null) return known;
        for (Method method : Class.forName(CELLS).getDeclaredMethods()) {
            if (method.getName().equals(COPY) && Modifier.isStatic(method.getModifiers())) {
                method.setAccessible(true);
                cell = method;
                return method;
            }
        }
        throw new NoSuchMethodException("this build has no copy of the row to draw");
    }

    private static Object state() throws Exception {
        Object known = state;
        if (known != null) return known;
        Class<?> type = Class.forName(STATE);
        Object made = null;
        for (Constructor<?> maker : type.getDeclaredConstructors()) {
            if (maker.getParameterTypes().length != 3) continue;
            maker.setAccessible(true);
            made = maker.newInstance(null, null, null);
            break;
        }
        if (made == null) throw new NoSuchMethodException("the row's state is made some other way now");
        Class<?> one = Class.forName("kotlin.jvm.functions.Function1");
        Class<?> two = Class.forName("kotlin.jvm.functions.Function2");
        Object tap = Proxy.newProxyInstance(one.getClassLoader(), new Class<?>[] {one, two}, (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("invoke")) {
                open(args);
                return unit();
            }
            return plain(proxy, name, args);
        });
        int set = 0;
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (field.getType() != one && field.getType() != two) continue;
            field.setAccessible(true);
            field.set(made, tap);
            set ++;
        }
        if (set == 0) throw new NoSuchFieldException("the row's state has nowhere to say what a tap does");
        state = made;
        return made;
    }

    private static void open(Object[] args) {
        try {
            Context from = Watch.here();
            if (from == null && args != null) {
                for (Object given : args) {
                    if (given instanceof Context) from = (Context) given;
                    if (from != null || given == null) continue;
                    for (Field field : given.getClass().getDeclaredFields()) {
                        if (!Context.class.isAssignableFrom(field.getType())) continue;
                        field.setAccessible(true);
                        Object held = field.get(given);
                        if (held instanceof Context) from = (Context) held;
                    }
                }
            }
            if (from == null) from = soufiT.context();
            if (from == null) return;
            Intent intent = new Intent(from, SettingsActivity.class);
            if (!(from instanceof android.app.Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            from.startActivity(intent);
        } catch (Throwable error) {
            Diary.note("settings row: could not open: " + error);
        }
    }

    private static Object unit() throws Exception {
        Object known = unit;
        if (known != null) return known;
        Class<?> type = Class.forName("kotlin.Unit");
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != type) continue;
            field.setAccessible(true);
            unit = field.get(null);
            if (unit != null) return unit;
        }
        throw new NoSuchFieldException("kotlin.Unit has no instance to give back");
    }

    private static boolean mayTry() {
        Context context = soufiT.context();
        if (context == null) return false;
        final SharedPreferences prefs = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
        final String key = TRIED + "_" + Version.MOD;
        int before = prefs.getInt(key, 0);
        if (before >= 2) {
            Diary.note("settings row: drawing it in the list ended the app before; it stays above the screen");
            return false;
        }
        prefs.edit().putInt(key, before + 1).commit();
        new Handler(Looper.getMainLooper()).postDelayed(() -> prefs.edit().remove(key).apply(), 1500L);
        return true;
    }
}

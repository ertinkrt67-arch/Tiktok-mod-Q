package bk.soufit.feed;
import android.view.MotionEvent;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
public final class SpeedGesture {
    private SpeedGesture() {} private static final float NATIVE = 2f;
    private static Runnable watching;
    public static boolean onTouch(Object listener, final View target, MotionEvent event) {
        try {
            if (listener == null || target == null || event == null) return false;
            if (event.getActionMasked() != MotionEvent.ACTION_DOWN) return false;
            if (Math.abs(PlayerTools.speed() - NATIVE) < 0.01f) return false;
            Object inside = read(listener, "l0");
            final Object component = inside == null ? listener : inside;
            if (watching != null) target.removeCallbacks(watching);
            watching = new Runnable() {
                private int looks;
                @Override public void run() {
                    if (watching != this) return;
                    if (isSpeeding(ability(component))) {
                        watching = null;
                        PlayerTools.applyHoldRate();
                        return;
                    }
                    if (++ looks < 40 && target.isAttachedToWindow()) target.postDelayed(this, 50L);
                    else watching = null;
                }
            };
            target.postDelayed(watching, 50L);
        } catch (Throwable ignored) {} return false;
    }

    private static Object ability(Object component) {
        Object edge = read(component, "LLJL");
        if (edge != null) return edge;
        edge = read(component, "LLLILZJ");
        if (edge != null) return edge;
        return call(read(component, "LLLL"), "getValue");
    }

    private static boolean isSpeeding(Object ability) {
        Object value = call(ability, "R62");
        return value instanceof Boolean && ((Boolean) value).booleanValue();
    }

    private static Object call(Object receiver, String name) {
        if (receiver == null) return null;
        try {
            return method(receiver.getClass(), name).invoke(receiver);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static final java.util.concurrent.ConcurrentHashMap<String, Method> methods = new java.util.concurrent.ConcurrentHashMap<String, Method>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Field> fields = new java.util.concurrent.ConcurrentHashMap<String, Field>();
    private static final java.util.Set<String> absent = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String,
            Boolean>());
    private static Method method(Class<?> type, String name) throws NoSuchMethodException {
        String key = type.getName() + "#" + name;
        Method cached = methods.get(key);
        if (cached != null) return cached;
        if (absent.contains(key)) throw new NoSuchMethodException(name);
        Class<?> current = type;
        while (current != null) {
            try {
                Method found = current.getDeclaredMethod(name);
                found.setAccessible(true);
                methods.put(key, found);
                return found;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        absent.add(key);
        throw new NoSuchMethodException(name);
    }

    private static Object read(Object receiver, String name) {
        if (receiver == null) return null;
        String key = receiver.getClass().getName() + "@" + name;
        if (absent.contains(key)) return null;
        Field cached = fields.get(key);
        if (cached != null) {
            try {
                return cached.get(receiver);
            } catch (Throwable ignored) {
                return null;
            }
        }
        Class<?> current = receiver.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                fields.put(key, field);
                return field.get(receiver);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return null;
            }
        }
        absent.add(key);
        return null;
    }
}

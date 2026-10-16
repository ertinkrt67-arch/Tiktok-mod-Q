package bk.soufit.media;
import bk.soufit.core.Diary;
import bk.soufit.core.Net;
import bk.soufit.ui.Screen;
import bk.soufit.ui.SettingsGlyph;
import bk.soufit.core.Text;
import bk.soufit.core.soufiT;
import android.app.Activity;
import android.content.Context;
import com.ss.android.ugc.aweme.im.message.template.card.StickerTemplate;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public final class Stickers {
    public static void reloadSettings() {
        cached = null;
    }

    private Stickers() {} public static final String KEY = "save_stickers";
    private static volatile Boolean cached;
    public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        try {
            android.content.Context context = soufiT.context();
            if (context == null) return true;
            boolean on = context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE).getBoolean(KEY,
                    true);
            cached = on;
            return on;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        try {
            android.content.Context context = soufiT.context();
            if (context == null) return;
            context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE).edit().putBoolean(KEY, enabled)
                    .apply();
        } catch (Throwable ignored) {}
    }

    private static volatile String latest;
    public static void RQ1(Object ability, Object view, Object a, Object b, StickerTemplate sticker) {
        seen(sticker);
        onwards(ability, "RQ1", view, a, b, sticker);
    }

    public static void rP(Object ability, Object view, Object a, Object b, StickerTemplate sticker) {
        seen(sticker);
        onwards(ability, "rP", view, a, b, sticker);
    }

    public static void DD2(Object ability, Object picture, Object manager, Object a, StickerTemplate sticker) {
        seen(sticker);
        onwards(ability, "DD2", picture, manager, a, sticker);
    }

    public static void vQ1(Object ability, Object corner, StickerTemplate sticker) {
        seen(sticker);
        onwards(ability, "vQ1", corner, sticker);
    }

    private static void onwards(Object ability, String name, Object ... args) {
        if (ability == null) return;
        try {
            for (Method method : ability.getClass().getMethods()) {
                if (!method.getName().equals(name)) continue;
                if (method.getParameterTypes().length != args.length) continue;
                method.setAccessible(true);
                method.invoke(ability, args);
                return;
            }
            Diary.note("sticker tap: no " + name + " taking " + args.length);
        } catch (Throwable error) {
            Diary.note("sticker tap: " + error);
        }
    }

    public static void seen(Object sticker) {
        if (!isEnabled()) return;
        try {
            String url = find(sticker, new HashSet<Object>(), 0);
            if (url == null) return;
            latest = url;
            offer();
        } catch (Throwable error) {
            Diary.note("sticker: " + error);
        }
    }

    private static final int PLACED = 0x4D617274;
    private static final long LOOK_FOR = 2500L;
    private static final android.os.Handler MAIN = new android.os.Handler(android.os.Looper.getMainLooper());
    private static Runnable looking;
    public static void sheet(Object sticker) {
        if (!isEnabled()) return;
        try {
            String url = find(sticker, new HashSet<Object>(), 0);
            if (url == null) return;
            latest = url;
            final long until = android.os.SystemClock.uptimeMillis() + LOOK_FOR;
            if (looking != null) MAIN.removeCallbacks(looking);
            looking = new Runnable() {
                @Override public void run() {
                    if (looking != this) return;
                    boolean placed = false;
                    try {
                        placed = place();
                    } catch (Throwable error) {
                        Diary.note("sticker button: " + error);
                    }
                    if (placed) {
                        looking = null;
                    } else if (android.os.SystemClock.uptimeMillis() < until) {
                        MAIN.postDelayed(this, 60L);
                    } else {
                        looking = null;
                        offer();
                    }
                }
            };
            MAIN.post(looking);
        } catch (Throwable error) {
            Diary.note("sticker: " + error);
        }
    }

    private static void offer() {
        Screen.offer(Text.SAVE_STICKER, 0.46f, new Runnable() {
            @Override public void run() {
                save();
            }
        });
    }

    private static List<android.view.View> windows() {
        try {
            Class<?> global = Class.forName("android.view.WindowManagerGlobal");
            Object manager = global.getMethod("getInstance").invoke(null);
            Field views = global.getDeclaredField("mViews");
            views.setAccessible(true);
            Object held = views.get(manager);
            if (held instanceof List) {
                return new java.util.ArrayList<android.view.View>((List<android.view.View>) held);
            }
        } catch (Throwable ignored) {} return new java.util.ArrayList<android.view.View>();
    }

    private static boolean place() {
        List<android.view.View> open = windows();
        for (int i = open.size() - 1; i >= 0; i --) {
            android.widget.LinearLayout row = row(open.get(i), 0, new int[] {4000});
            if (row == null) continue;
            if (row.getTag(PLACED) != null) return true;
            if (row.getChildAt(0).getHeight() <= 0) return false;
            add(row);
            return true;
        }
        return false;
    }

    private static android.widget.LinearLayout row(android.view.View view, int depth, int[] budget) {
        if (view == null || depth > 40 || -- budget[0] < 0 || view.getVisibility() != android.view.View.VISIBLE) return null;
        if (!(view instanceof android.view.ViewGroup)) return null;
        android.view.ViewGroup group = (android.view.ViewGroup) view;
        if (group instanceof android.widget.LinearLayout && ((android.widget.LinearLayout) group).getOrientation() == android.widget.LinearLayout.HORIZONTAL) {
            if (group.getTag(PLACED) != null) return (android.widget.LinearLayout) group;
            if (group.getChildCount() == 2 && button(group.getChildAt(0)) && button(group.getChildAt(1)) && underPicture(group)) {
                return (android.widget.LinearLayout) group;
            }
        }
        for (int i = 0; i < group.getChildCount(); i ++) {
            android.widget.LinearLayout found = row(group.getChildAt(i), depth + 1, budget);
            if (found != null) return found;
        }
        return null;
    }

    private static boolean button(android.view.View view) {
        if (!(view instanceof android.widget.TextView) || view.getVisibility() != android.view.View.VISIBLE || !view.isClickable()) {
            return false;
        }
        CharSequence says = ((android.widget.TextView) view).getText();
        if (says == null || says.length() == 0) return false;
        return view instanceof android.widget.Button || "android.widget.Button".contentEquals(view.getAccessibilityClassName());
    }

    private static boolean underPicture(android.view.View row) {
        if (!(row.getParent() instanceof android.view.ViewGroup)) return false;
        android.view.ViewGroup around = (android.view.ViewGroup) row.getParent();
        for (int i = 0; i < around.getChildCount(); i ++) {
            if (around.getChildAt(i) instanceof android.widget.ImageView) return true;
        }
        return false;
    }

    private static void add(android.widget.LinearLayout row) {
        Context context = row.getContext();
        android.widget.TextView first = (android.widget.TextView) row.getChildAt(0);
        android.view.View second = row.getChildAt(1);
        float density = context.getResources().getDisplayMetrics().density;
        int tall = first.getHeight();
        int gap = Math.max(second.getLeft() - first.getRight(), Math.round(8 * density));
        for (android.view.View one : new android.view.View[] {first, second}) {
            android.widget.LinearLayout.LayoutParams how = (android.widget.LinearLayout.LayoutParams) one.getLayoutParams();
            if (how.weight <= 0f) {
                how.width = 0;
                how.weight = 1f;
                one.setLayoutParams(how);
            }
        }
        android.widget.ImageView mine = new android.widget.ImageView(context);
        android.graphics.drawable.Drawable ground = null;
        try {
            android.graphics.drawable.Drawable theirs = first.getBackground();
            if (theirs != null && theirs.getConstantState() != null) {
                ground = theirs.getConstantState().newDrawable(context.getResources()).mutate();
            }
        } catch (Throwable ignored) {} int ink = first.getCurrentTextColor();
        if (ground == null) {
            android.graphics.drawable.GradientDrawable plain = new android.graphics.drawable.GradientDrawable();
            plain.setColor((ink & 0x00FFFFFF) | 0x1F000000);
            plain.setCornerRadius(4 * density);
            ground = plain;
        }
        mine.setBackground(ground);
        mine.setImageDrawable(SettingsGlyph.make("download", ink));
        mine.setScaleType(android.widget.ImageView.ScaleType.FIT_XY);
        int inset = Math.max(0, (tall - Math.round(24 * density)) / 2);
        mine.setPadding(inset, inset, inset, inset);
        mine.setContentDescription(Text.SAVE_STICKER);
        mine.setClickable(true);
        mine.setOnClickListener(new android.view.View.OnClickListener() {
            @Override public void onClick(android.view.View pressed) {
                save();
            }
        });
        android.widget.LinearLayout.LayoutParams where = new android.widget.LinearLayout.LayoutParams(tall, tall);
        where.setMarginStart(gap);
        where.gravity = ((android.widget.LinearLayout.LayoutParams) first.getLayoutParams()).gravity;
        row.addView(mine, where);
        row.setTag(PLACED, Boolean.TRUE);
    }

    private static final int DEEP = 4;
    private static String find(Object thing, Set<Object> seen, int depth) {
        if (thing == null || depth > DEEP) return null;
        if (thing instanceof String) {
            String value = (String) thing;
            return value.startsWith("http") ? value : null;
        }
        if (thing instanceof List) {
            String best = null;
            for (Object item : (List) thing) {
                String found = find(item, seen, depth + 1);
                if (found != null && (best == null || found.length() > best.length())) best = found;
            }
            return best;
        }
        Class<?> type = thing.getClass();
        String name = type.getName();
        if (name.startsWith("java.") || name.startsWith("android.")) return null;
        if (!seen.add(thing)) return null;
        String best = null;
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                try {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    String found = find(field.get(thing), seen, depth + 1);
                    if (found != null && (best == null || found.length() > best.length())) {
                        best = found;
                    }
                } catch (Throwable ignored) {}
            }
            type = type.getSuperclass();
        }
        return best;
    }

    private static void save() {
        final String url = latest;
        final Context context = soufiT.context();
        if (url == null || context == null) return;
        Net.away("sticker", new Runnable() {
            @Override public void run() {
                byte[] data = Net.bytes(url);
                boolean webp = url.contains(".webp") || url.contains("webp");
                String at = data == null ? null : Gallery.save(context, data, Gallery.name("sticker", url, webp ? "webp" : "png"),
                        webp ? "image/webp" : "image/png");
                Screen.say(at == null ? Text.STICKER_FAILED : Text.SAVED);
            }
        });
    }

    public static boolean have() {
        return latest != null;
    }
}

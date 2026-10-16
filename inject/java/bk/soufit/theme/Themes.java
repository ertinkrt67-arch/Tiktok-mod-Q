package bk.soufit.theme;
import bk.soufit.profile.AccountAppearance;
import bk.soufit.profile.Badge;
import bk.soufit.feed.DateOverlay;
import bk.soufit.core.DevMode;
import bk.soufit.core.Diary;
import bk.soufit.feed.Dim;
import bk.soufit.core.Probe;
import bk.soufit.core.Sliced;
import bk.soufit.core.Watch;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
public final class Themes {
    private Themes() {} public static final String KEY_ON = "theme_on";
    public static final String KEY_MATERIAL = "theme_material";
    public static final String KEY_TEXT = "theme_text";
    public static final String KEY_AMOLED = "theme_amoled";
    public static final String KEY_FULL_AMOLED = "theme_full_amoled";
    public static final String KEY_BACKGROUND = "theme_background";
    public static final String KEY_STRENGTH = "theme_strength";
    private static final int TEXT = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xFF121212;
    public static boolean isEnabled() {
        return current().on;
    }

    public static void setEnabled(boolean on) {
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY_ON, on).apply();
        forget();
    }

    public static boolean isMaterial() {
        return current().material;
    }

    public static void setMaterial(boolean on) {
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY_MATERIAL, on).apply();
        forget();
    }

    public static int text() {
        return current().text;
    }

    public static int background() {
        return current().background;
    }

    public static int strength() {
        return current().strength;
    }

    public static void setStrength(int percent) {
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        put(KEY_STRENGTH, percent);
    }

    public static int backgroundInUse() {
        return current().ground;
    }

    public static boolean available() {
        return fromSystem(true) != 0 && fromSystem(false) != 0;
    }

    private static int toned(int chosen, int percent, boolean dark) {
        try {
            float[] hsv = new float[3];
            android.graphics.Color.colorToHSV(chosen, hsv);
            float how = percent / 100.0f;
            if (dark) {
                hsv[2] = 0.04f + (hsv[2] - 0.04f) * how;
            } else {
                hsv[2] = 1.0f - (1.0f - hsv[2]) * how;
                hsv[1] = hsv[1] * how;
            }
            return 0xFF000000 | (android.graphics.Color.HSVToColor(hsv) & 0xFFFFFF);
        } catch (Throwable ignored) {
            return chosen;
        }
    }

    private static final class Settled {
        public final boolean on;
        public final boolean material;
        public final boolean dark;
        public final int text;
        public final int background;
        public final int strength;
        public final boolean fullAmoled;
        public final int ground;
        public Settled(boolean on, boolean material, boolean dark, int text, int background, int strength, boolean fullAmoled) {
            this.ground = toned(background, strength, dark);
            this.on = on;
            this.material = material;
            this.dark = dark;
            this.text = text;
            this.background = background;
            this.strength = strength;
            this.fullAmoled = fullAmoled;
        }
    }

    private static volatile Settled settled;
    private static Settled current() {
        Settled now = settled;
        return now == null || now.dark != isDark() ? read() : now;
    }

    private static Settled read() {
        boolean dark = isDark();
        boolean on = flag(KEY_ON, available());
        boolean material = flag(KEY_MATERIAL, true);
        int chosenText = 0;
        int chosenBackground = 0;
        if (material) {
            chosenText = fromSystem(true);
            chosenBackground = fromSystem(false);
        }
        if (chosenText == 0) chosenText = number(modeKey(KEY_TEXT, dark), number(KEY_TEXT, dark ? TEXT : 0xFF161823));
        if (chosenBackground == 0) chosenBackground = number(modeKey(KEY_BACKGROUND, dark), number(KEY_BACKGROUND, dark ? BACKGROUND
                : 0xFFFFFFFF));
        int strength = number(KEY_STRENGTH, 100);
        if (strength < 0) strength = 0;
        if (strength > 100) strength = 100;
        boolean fullAmoled = flag(KEY_FULL_AMOLED, false) && flag(KEY_AMOLED, false);
        Settled fresh = new Settled(on, material, dark, chosenText, chosenBackground, strength, fullAmoled);
        if (soufiT.context() != null) settled = fresh;
        return fresh;
    }

    public static void setText(int colour) {
        put(modeKey(KEY_TEXT, isDark()), colour);
    }

    public static void setBackground(int colour) {
        put(modeKey(KEY_BACKGROUND, isDark()), colour);
    }

    private static String modeKey(String key, boolean dark) {
        return key + (dark ? "_dark" : "_light");
    }

    public static void reset() {
        SharedPreferences p = prefs();
        if (p != null) p.edit().putBoolean(KEY_ON, false).remove(KEY_MATERIAL).remove(KEY_TEXT).remove(KEY_BACKGROUND).remove(modeKey(KEY_TEXT,
                true)).remove(modeKey(KEY_TEXT, false)).remove(modeKey(KEY_BACKGROUND, true)).remove(modeKey(KEY_BACKGROUND,
                false)).remove(KEY_STRENGTH).apply();
        forget();
        AppearanceColors.reset();
    }

    public static boolean isAmoled() {
        return flag(KEY_AMOLED, false);
    }

    public static void setAmoled(boolean on) {
        SharedPreferences p = prefs();
        if (p == null) return;
        SharedPreferences.Editor edit = p.edit().putBoolean(KEY_AMOLED, on);
        if (on) {
            edit.putBoolean(KEY_ON, true).putBoolean(KEY_MATERIAL, false).putInt(modeKey(KEY_BACKGROUND, true), 0xFF000000)
                    .putInt(modeKey(KEY_TEXT, true), 0xFFFFFFFF).putInt(KEY_STRENGTH, 100);
        } else {
            edit.remove(modeKey(KEY_BACKGROUND, true)).remove(modeKey(KEY_TEXT, true)).putBoolean(KEY_FULL_AMOLED, false)
                    .putBoolean(KEY_ON, false);
        }
        edit.apply();
        forget();
    }

    public static boolean isFullAmoled() {
        return current().fullAmoled;
    }

    public static void setFullAmoled(boolean on) {
        SharedPreferences p = prefs();
        if (p != null) p.edit().putBoolean(KEY_FULL_AMOLED, on).apply();
        if (on) setAmoled(true);
        forget();
    }

    public static void preset(int chosen) {
        boolean dark = isDark();
        int background = dark ? (chosen == 1 ? 0xFF0D1B2A : 0xFF161823) : (chosen == 1 ? 0xFFEAF3FA : 0xFFF8F8F8);
        int text = dark ? 0xFFFFFFFF : 0xFF161823;
        SharedPreferences p = prefs();
        if (p != null) p.edit().putBoolean(KEY_ON, true).putBoolean(KEY_MATERIAL, false).putInt(modeKey(KEY_BACKGROUND, dark),
                background).putInt(modeKey(KEY_TEXT, dark), text).putInt(KEY_STRENGTH, 100).apply();
        forget();
    }

    private static int fromSystem(boolean forText) {
        Context context = soufiT.context();
        if (context == null || Build.VERSION.SDK_INT < 31) return 0;
        try {
            boolean dark = isDark();
            int id = forText == dark ? android.R.color.system_neutral1_50 : android.R.color.system_neutral1_900;
            return context.getColor(id);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static int recolour(int colour, boolean fromTheme) {
        Settled now = current();
        if (!now.on) return colour;
        if (!(fromTheme ? Nightly.owns(colour) : Nightly.unmistakable(colour))) {
            return colour;
        }
        float level = brightness(colour);
        if (!isDark()) level = 1.0f - level;
        int mixed = mix(now.ground, now.text, level);
        return (colour & 0xFF000000) | (mixed & 0xFFFFFF);
    }

    private static float brightness(int colour) {
        int red = (colour >> 16) & 0xFF;
        int green = (colour >> 8) & 0xFF;
        int blue = colour & 0xFF;
        return (0.2126f * red + 0.7152f * green + 0.0722f * blue) / 255.0f;
    }

    private static int mix(int from, int to, float how) {
        if (how < 0f) how = 0f;
        if (how > 1f) how = 1f;
        int red = round(((from >> 16) & 0xFF), ((to >> 16) & 0xFF), how);
        int green = round(((from >> 8) & 0xFF), ((to >> 8) & 0xFF), how);
        int blue = round((from & 0xFF), (to & 0xFF), how);
        return (red << 16) | (green << 8) | blue;
    }

    private static int round(int from, int to, float how) {
        return (int)(from + (to - from) * how + 0.5f);
    }

    public static void repaint(View root) {
        repaint(root, BUDGET);
    }

    public static void repaint(View root, int budget) {
        Settled now = settled;
        if (now == null) now = read();
        if (!now.on || root == null) return;
        long probe = Probe.start();
        try {
            seen = BUDGET - budget;
            walk(root, 0);
        } catch (Throwable ignored) {} Probe.stop(Probe.THEME, probe);
    }

    private static final ArrayList<WeakReference<View>> watchedRoots = new ArrayList<WeakReference<View>>();
    public static void repaintVisible() {
        Accent.epoch ++;
        synchronized (watchedRoots) {
            for (int i = watchedRoots.size() - 1; i >= 0; i --) {
                View root = watchedRoots.get(i).get();
                if (root == null) {
                    watchedRoots.remove(i);
                    continue;
                }
                repaint(root);
                Fonts.applyTree(root);
                AppearanceColors.repaint(root);
                Badge.rewrite(root);
                Accent.repaintTree(root);
                root.invalidate();
            }
        }
    }

    private static final int DEEP = 40;
    private static final int BUDGET = 12000;
    private static int seen;
    private static void paint(android.graphics.drawable.Drawable drawable, View view, int depth) {
        if (drawable == null || depth > 3) return;
        try {
            android.graphics.drawable.Drawable first = brushed.get(drawable);
            if (first != null) drawable = first;
            if (drawable instanceof android.graphics.drawable.ShapeDrawable) {
                brush((android.graphics.drawable.ShapeDrawable) drawable, view);
                return;
            }
            if (drawable instanceof android.graphics.drawable.ColorDrawable) {
                int was = ((android.graphics.drawable.ColorDrawable) drawable).getColor();
                int now = recolour(was, true);
                if (now == was) return;
                if (view != null) {
                    view.setBackground(new android.graphics.drawable.ColorDrawable(now));
                } else {
                    ((android.graphics.drawable.ColorDrawable) drawable.mutate()).setColor(now);
                }
                return;
            }
            if (drawable instanceof android.graphics.drawable.GradientDrawable) {
                android.graphics.drawable.GradientDrawable shape = (android.graphics.drawable.GradientDrawable) drawable;
                android.content.res.ColorStateList held = Build.VERSION.SDK_INT >= 24 ? shape.getColor() : null;
                if (held == null) {
                    fade(shape, view);
                    return;
                }
                int was = held.getDefaultColor();
                int now = recolour(was, true);
                if (now != was) {
                    android.graphics.drawable.Drawable copy = shape.mutate();
                    ((android.graphics.drawable.GradientDrawable) copy).setColor(now);
                    if (view != null) view.setBackground(copy);
                }
                return;
            }
            if (drawable instanceof android.graphics.drawable.LayerDrawable) {
                android.graphics.drawable.LayerDrawable layers = (android.graphics.drawable.LayerDrawable) drawable;
                for (int i = 0; i < layers.getNumberOfLayers(); i ++) {
                    paint(layers.getDrawable(i), null, depth + 1);
                }
                return;
            }
            if (drawable instanceof android.graphics.drawable.InsetDrawable) {
                paint(((android.graphics.drawable.InsetDrawable) drawable).getDrawable(), null, depth + 1);
                return;
            }
            if (drawable instanceof android.graphics.drawable.DrawableContainer) {
                android.graphics.drawable.Drawable.ConstantState state = drawable.getConstantState();
                if (state instanceof android.graphics.drawable.DrawableContainer.DrawableContainerState) {
                    android.graphics.drawable.Drawable[] each = ((android.graphics.drawable.DrawableContainer.DrawableContainerState) state)
                            .getChildren();
                    if (each != null) {
                        for (android.graphics.drawable.Drawable one : each) {
                            paint(one, null, depth + 1);
                        }
                    }
                }
                return;
            }
            other(drawable);
            if (Probe.ON && view != null) unknown(drawable, view);
        } catch (Throwable ignored) {}
    }

    private static final java.util.Set<String> told = new java.util.HashSet<String>();
    private static void unknown(android.graphics.drawable.Drawable drawable, View view) {
        if (!DevMode.isOn()) return;
        String what = drawable.getClass().getName() + " on " + view.getClass().getName() + " " + view.getWidth() + "x" + view.getHeight();
        if (told.size() < 80 && told.add(what)) android.util.Log.e("soufiT", "background: " + what);
    }

    private static void fade(android.graphics.drawable.GradientDrawable shape, View view) {
        if (Build.VERSION.SDK_INT < 24) return;
        int[] stops = shape.getColors();
        if (Probe.ON && view != null) {
            String what = "fade " + view.getWidth() + "x" + view.getHeight() + " " + (stops == null ? "no stops" : java.util.Arrays.toString(stops));
            if (told.size() < 80 && told.add(what)) {
                StringBuilder owned = new StringBuilder();
                if (stops != null) for (int stop : stops) {
                    owned.append(Integer.toHexString(stop)).append(Nightly.owns(stop | 0xFF000000) ? "+ " : "- ");
                }
                android.util.Log.e("soufiT", "background: " + what + " | " + owned);
            }
        }
        if (stops == null || stops.length == 0) return;
        int[] moved = null;
        for (int i = 0; i < stops.length; i ++) {
            int solid = recolour(stops[i] | 0xFF000000, true);
            int now = (stops[i] & 0xFF000000) | (solid & 0x00FFFFFF);
            if (now == stops[i]) continue;
            if (moved == null) moved = stops.clone();
            moved[i] = now;
        }
        if (moved == null) return;
        android.graphics.drawable.Drawable copy = shape.mutate();
        ((android.graphics.drawable.GradientDrawable) copy).setColors(moved);
        if (view != null) {
            view.setBackground(copy);
            view.invalidate();
        }
    }

    private static final java.util.Map<android.graphics.drawable.Drawable, android.graphics.drawable.Drawable> brushed = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<android.graphics.drawable.Drawable,
            android.graphics.drawable.Drawable>());
    private static final int SAMPLES = 32;
    private static boolean later;
    private static void brush(android.graphics.drawable.ShapeDrawable shape, View view) {
        android.graphics.Paint paint = shape.getPaint();
        if (shape.getShaderFactory() == null && paint.getShader() == null) {
            int was = paint.getColor();
            int now = recolour(was, true);
            if (now != was) {
                paint.setColor(now);
                shape.invalidateSelf();
            }
            return;
        }
        if (view == null || Build.VERSION.SDK_INT < 24) return;
        android.graphics.drawable.shapes.Shape form = shape.getShape();
        if (form != null && form.getClass() != android.graphics.drawable.shapes.RectShape.class) {
            return;
        }
        int wide = view.getWidth(), tall = view.getHeight();
        if (wide <= 0 || tall <= 0) {
            later = true;
            return;
        }
        if (shape.getBounds().isEmpty()) shape.setBounds(0, 0, wide, tall);
        android.graphics.Rect bounds = shape.getBounds();
        int[] seen = new int[SAMPLES * SAMPLES];
        android.graphics.Bitmap square = android.graphics.Bitmap.createBitmap(SAMPLES, SAMPLES, android.graphics.Bitmap.Config.ARGB_8888);
        try {
            android.graphics.Canvas canvas = new android.graphics.Canvas(square);
            canvas.scale(SAMPLES / (float) bounds.width(), SAMPLES / (float) bounds.height());
            canvas.translate(- bounds.left, - bounds.top);
            shape.draw(canvas);
            square.getPixels(seen, 0, SAMPLES, 0, 0, SAMPLES, SAMPLES);
        } finally {
            square.recycle();
        }
        boolean down = true, across = true;
        for (int y = 0; y < SAMPLES && (down || across); y ++) {
            for (int x = 0; x < SAMPLES; x ++) {
                int here = seen[y * SAMPLES + x];
                if (!near(here, seen[y * SAMPLES])) down = false;
                if (!near(here, seen[x])) across = false;
            }
        }
        if (!down && !across) return;
        int[] stops = new int[SAMPLES];
        boolean moved = false;
        for (int i = 0; i < SAMPLES; i ++) {
            int was = down ? seen[i * SAMPLES] : seen[i];
            int solid = recolour(was | 0xFF000000, true);
            stops[i] = (was & 0xFF000000) | (solid & 0x00FFFFFF);
            if (stops[i] != was && (was >>> 24) > 40) moved = true;
        }
        if (!moved) return;
        android.graphics.drawable.GradientDrawable rebuilt = new android.graphics.drawable.GradientDrawable(down ? android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
                : android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT, stops);
        brushed.put(rebuilt, shape);
        view.setBackground(rebuilt);
    }

    private static boolean near(int one, int two) {
        return Math.abs((one >>> 24) - (two >>> 24)) <= 6 && Math.abs(((one >> 16) & 0xFF) - ((two >> 16) & 0xFF)) <= 6 && Math.abs(((one >> 8) & 0xFF) - ((two >> 8) & 0xFF)) <= 6
                && Math.abs((one & 0xFF) - (two & 0xFF)) <= 6;
    }

    private static final java.util.concurrent.ConcurrentHashMap<Class<?>, Object[]> colourOf = new java.util.concurrent.ConcurrentHashMap<Class<
            ?>, Object[]>();
    private static final Object[] NONE = new Object[0];
    private static void other(android.graphics.drawable.Drawable drawable) throws Exception {
        Class<?> type = drawable.getClass();
        Object[] how = colourOf.get(type);
        if (how == null) {
            how = NONE;
            if (!type.getName().startsWith("android.graphics.")) {
                for (String[] pair : new String[][] { {
                    "getColor", "setColor"
                }, {"getFillColor", "setFillColor"}}) {
                    try {
                        java.lang.reflect.Method get = type.getMethod(pair[0]);
                        if (get.getReturnType() != android.content.res.ColorStateList.class) {
                            continue;
                        }
                        java.lang.reflect.Method set = type.getMethod(pair[1], android.content.res.ColorStateList.class);
                        get.setAccessible(true);
                        set.setAccessible(true);
                        how = new Object[] {get, set};
                        break;
                    } catch (NoSuchMethodException none) {}
                }
            }
            colourOf.put(type, how);
        }
        if (how.length == 0) return;
        Object held = ((java.lang.reflect.Method) how[0]).invoke(drawable);
        if (!(held instanceof android.content.res.ColorStateList)) return;
        int was = ((android.content.res.ColorStateList) held).getDefaultColor();
        int now = recolour(was, true);
        if (now != was) {
            ((java.lang.reflect.Method) how[1]).invoke(drawable, android.content.res.ColorStateList.valueOf(now));
            drawable.invalidateSelf();
        }
    }

    private static final int WORE = 0x4D617270;
    private static final int UNDER = 0x4D617271;
    private static void walk(View view, int depth) {
        if (view == null || depth > DEEP || ++ seen > BUDGET) return;
        android.graphics.drawable.Drawable wears = view.getBackground();
        if (wears != null) {
            if (wears instanceof android.graphics.drawable.ColorDrawable) {
                paint(wears, view, 0);
            } else {
                Settled now = settled;
                if (view.getTag(WORE) != wears || view.getTag(UNDER) != now) {
                    later = false;
                    paint(wears, view, 0);
                    if (!later) {
                        view.setTag(WORE, view.getBackground());
                        view.setTag(UNDER, now);
                    }
                }
            }
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            int many = group.getChildCount();
            for (int i = 0; i < many; i ++) walk(group.getChildAt(i), depth + 1);
        }
    }

    public static void watch(final android.app.Activity activity) {
        if (activity == null || activity.getClass().getName().startsWith("bk.soufit.")) return;
        try {
            final View root = activity.getWindow().getDecorView();
            Fonts.applyTree(root);
            DateOverlay.resume(root);
            synchronized (watchedRoots) {
                boolean found = false;
                for (int i = watchedRoots.size() - 1; i >= 0; i --) {
                    View held = watchedRoots.get(i).get();
                    if (held == null) watchedRoots.remove(i);
                    else if (held == root) found = true;
                }
                if (!found) watchedRoots.add(new WeakReference<View>(root));
            }
            if (Boolean.TRUE.equals(root.getTag(WATCHING))) {
                repaint(root);
                Badge.rewrite(root);
                sweep(root);
                Dim.watch(activity);
                return;
            }
            root.setTag(WATCHING, Boolean.TRUE);
            root.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                private long updated;
                private boolean owed;
                private int shape = shape(root, 0);
                private final Runnable later = new Runnable() {
                    @Override public void run() {
                        owed = false;
                        if (root.isAttachedToWindow()) look();
                    }
                };
                @Override public void onGlobalLayout() {
                    long now = android.os.SystemClock.uptimeMillis();
                    if (now - updated < QUIET) {
                        if (!owed) {
                            owed = true;
                            root.postDelayed(later, QUIET - (now - updated) + 20);
                        }
                        return;
                    }
                    look();
                }
                private void look() {
                    isDark();
                    long probe = Probe.start();
                    int next = shape(root, 0);
                    Probe.stop(Probe.DIM_PRINT, probe);
                    if (next == shape) return;
                    shape = next;
                    updated = android.os.SystemClock.uptimeMillis();
                    repaint(root);
                    Fonts.applyTree(root);
                    probe = Probe.start();
                    DateOverlay.resume(root);
                    Probe.stop(Probe.DATES, probe);
                    probe = Probe.start();
                    Badge.rewrite(root);
                    Probe.stop(Probe.BADGES, probe);
                    sweep(root);
                }
            });
            repaint(root);
            Badge.rewrite(root);
            sweep(root);
            Dim.watch(activity);
        } catch (Throwable error) {
            Diary.note("theme: " + error);
        }
    }

    private static final Sliced.Visit ACCENT = new Sliced.Visit() {
        @Override public void view(View view) {
            Accent.look(view);
        }
    };

    private static final Sliced.Visit ROLES = new Sliced.Visit() {
        @Override public void view(View view) {
            AppearanceColors.look(view);
        }
    };

    private static int shape(View view, int depth) {
        if (view == null || depth > 40) return 0;
        int visibility = view.getVisibility();
        int hash = System.identityHashCode(view) * 31 + visibility;
        if (visibility != View.VISIBLE) return hash;
        hash = 31 * hash + view.getWidth() * 17 + view.getHeight();
        hash = 31 * hash + (view.isSelected() ? 8 : 0) + (view.isActivated() ? 16 : 0);
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            int many = group.getChildCount();
            hash = 31 * hash + many;
            for (int i = 0; i < many; i ++) hash = 31 * hash + shape(group.getChildAt(i), depth + 1);
        }
        return hash;
    }

    private static void sweep(View root) {
        Sliced.walk(root, ACCENT, ROLES);
    }

    private static volatile long last;
    private static final long QUIET = 400;
    private static final int WATCHING = 0x4D61726A;
    private static volatile int mode;
    private static volatile long asked;
    public static boolean isDark() {
        long now = android.os.SystemClock.uptimeMillis();
        if (mode != 0 && now - asked < 1000) return mode == 1;
        Boolean seen = painted();
        boolean dark = seen != null ? seen.booleanValue() : mode != 0 ? mode == 1 : configured();
        int was = mode;
        mode = dark ? 1 : 2;
        asked = now;
        if (was != 0 && was != mode) {
            settled = null;
            Accent.forget();
        }
        return dark;
    }

    private static Boolean painted() {
        try {
            android.app.Activity here = Watch.here();
            if (here == null) return null;
            if (here.getClass().getName().startsWith("bk.soufit.")) return null;
            android.view.View decor = here.getWindow() == null ? null : here.getWindow().getDecorView();
            android.graphics.drawable.Drawable ground = decor == null ? null : decor.getBackground();
            if (ground instanceof android.graphics.drawable.ColorDrawable) {
                int colour = ((android.graphics.drawable.ColorDrawable) ground).getColor();
                if (android.graphics.Color.alpha(colour) == 255) {
                    return Boolean.valueOf(brightness(colour) < 0.5f);
                }
            }
            Configuration config = here.getResources().getConfiguration();
            return Boolean.valueOf((config.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean configured() {
        try {
            Context context = soufiT.context();
            if (context != null) {
                Configuration config = context.getResources().getConfiguration();
                return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            }
        } catch (Throwable ignored) {} return true;
    }

    public static void forget() {
        mode = 0;
        settled = null;
        Accent.forget();
    }

    private static boolean flag(String key, boolean fallback) {
        try {
            SharedPreferences prefs = prefs();
            return prefs == null ? fallback : prefs.getBoolean(key, fallback);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static int number(String key, int fallback) {
        try {
            SharedPreferences prefs = prefs();
            return prefs == null ? fallback : prefs.getInt(key, fallback);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static void put(String key, int value) {
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putInt(key, value).apply();
        forget();
    }

    private static SharedPreferences prefs() {
        Context context = soufiT.context();
        if (context == null) return null;
        return AccountAppearance.prefs(context);
    }
}

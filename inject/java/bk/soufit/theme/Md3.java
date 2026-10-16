package bk.soufit.theme;
import bk.soufit.feed.BurnEye;
import bk.soufit.core.Diary;
import bk.soufit.feed.FeedRail;
import bk.soufit.ui.Screen;
import bk.soufit.core.soufiT;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
public final class Md3 {
    private Md3() {} public static final String KEY_ICONS = "md3_icons";
    private static final String[] KEYS = {KEY_ICONS};
    private static final int ICONS = 0;
    private static final Boolean[] ON = new Boolean[KEYS.length];
    public static boolean isOn(String key) {
        for (int i = 0; i < KEYS.length; i ++) if (KEYS[i].equals(key)) return on(i);
        return false;
    }

    public static void set(String key, boolean value) {
        for (int i = 0; i < KEYS.length; i ++) {
            if (!KEYS[i].equals(key)) continue;
            ON[i] = value;
            Context context = soufiT.context();
            if (context != null) {
                context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
            }
        }
    }

    private static boolean on(int which) {
        Boolean known = ON[which];
        if (known != null) return known;
        Context context = soufiT.context();
        if (context == null) return false;
        boolean value = false;
        try {
            value = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).getBoolean(KEYS[which], value);
        } catch (Throwable ignored) {} ON[which] = value;
        return value;
    }

    private static final int DISC = 0, CARD = 1, PILL = 2;
    private static volatile float rimUnit = 2f;
    private static volatile boolean rimUnitKnown;
    private static final class Shape extends Drawable {
        public final int kind;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF box = new RectF();
        public int ground;
        public float left, top, right, bottom;
        public float radius;
        public float wide;
        public float foot = - 1f;
        private final android.graphics.Path edge = new android.graphics.Path();
        private final float[] corners = new float[8];
        public WeakReference<View> around;
        public WeakReference<View> host;
        public Shape(int kind) {
            this.kind = kind;
        }
        public int role = SHELL;
        public void tint(int colour) {
            if (paint.getColor() == colour) return;
            paint.setColor(colour);
            invalidateSelf();
        }
        public void dress(View view, int as) {
            if (!rimUnitKnown) {
                rimUnit = view.getResources().getDisplayMetrics().density;
                rimUnitKnown = true;
            }
            role = as;
            ground = Ground.behind(view);
            synchronized (WORN) {
                WORN.put(view, this);
            }
            tint(colour(view, as));
        }
        public void ends(float head, float tail) {
            if (radius == head && foot == tail) return;
            radius = head;
            foot = tail;
            invalidateSelf();
        }
        @Override public void draw(Canvas canvas) {
            Rect whole = getBounds();
            box.set(whole.left + left, whole.top + top, whole.right - right, whole.bottom - bottom);
            if (wide > 0f) box.left = box.right - wide;
            if (kind == DISC) {
                float x = box.centerX(), y = box.centerY();
                View centre = around == null ? null : around.get(), on = host == null ? null : host.get();
                if (centre != null && on != null && centre.getWidth() > 0) {
                    float offX = centre.getWidth() / 2f, offY = centre.getHeight() / 2f;
                    View step = centre;
                    for (int i = 0; i < 6 && step != null && step != on; i ++) {
                        offX += step.getLeft();
                        offY += step.getTop();
                        Object parent = step.getParent();
                        step = parent instanceof View ? (View) parent : null;
                    }
                    if (step == on) {
                        x = offX;
                        y = offY;
                    }
                }
                canvas.drawCircle(x, y, radius, paint);
            } else if (kind == CARD && foot >= 0f && foot != radius) {
                corners[0] = corners[1] = corners[2] = corners[3] = radius;
                corners[4] = corners[5] = corners[6] = corners[7] = foot;
                edge.rewind();
                edge.addRoundRect(box, corners, android.graphics.Path.Direction.CW);
                canvas.drawPath(edge, paint);
            } else {
                float round = kind == PILL ? box.height() / 2f : radius;
                canvas.drawRoundRect(box, round, round, paint);
            }
        }
        @Override public void setAlpha(int alpha) {} @Override public void setColorFilter(ColorFilter filter) {} @Override public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static final int OURS = 0x4D61724D, WAS = 0x4D61724E, ROUND = 0x4D61724F, FLAT = 0x4D617250, PUT = 0x4D617252;
    private static final Object NONE = new Object();
    private static Shape wear(View view, int kind) {
        return wear(view, kind, false);
    }

    private static Shape wear(View view, int kind, boolean under) {
        Object worn = view.getTag(OURS);
        Shape shape;
        if (worn instanceof Shape) {
            shape = (Shape) worn;
        } else {
            shape = new Shape(kind);
            Drawable before = view.getBackground();
            view.setTag(WAS, before == null ? NONE : before);
            view.setTag(OURS, shape);
            view.setTag(PUT, under && before != null ? new android.graphics.drawable.LayerDrawable(new Drawable[] {shape,
                    before}) : shape);
        }
        Object put = view.getTag(PUT);
        if (put instanceof Drawable && view.getBackground() != put) view.setBackground((Drawable) put);
        return shape;
    }

    private static void strip(View view) {
        Object worn = view.getTag(OURS);
        if (!(worn instanceof Shape)) return;
        Object before = view.getTag(WAS);
        if (view.getBackground() == view.getTag(PUT)) view.setBackground(before instanceof Drawable ? (Drawable) before : null);
        view.setTag(OURS, null);
        view.setTag(WAS, null);
        view.setTag(PUT, null);
        synchronized (WORN) {
            WORN.remove(view);
        }
    }

    private static final int SHELL = 0, CHOSEN = 1, OVER_VIDEO = 2, CHOSEN_OVER_VIDEO = 3;
    private static final java.util.WeakHashMap<View, Shape> WORN = new java.util.WeakHashMap<View, Shape>();
    private static int colour(View view, int role) {
        if (role == OVER_VIDEO) return overVideo();
        if (role == CHOSEN_OVER_VIDEO) return chosenOverVideo();
        int ground = Ground.behind(view);
        int shell = Tint.surface(Ground.bright(ground) < 0.5f, Themes.isFullAmoled(), Ground.mix(ground, Ground.ink(ground), 0.07f));
        return role == CHOSEN ? Ground.mix(shell, Accent.colour() | 0xFF000000, 0.30f) : shell;
    }

    private static boolean onDark(View view) {
        return Ground.bright(Ground.behind(view)) < 0.5f;
    }

    private static long coloured;
    private static void recolour() {
        long now = SystemClock.uptimeMillis();
        if (now - coloured < 500L) return;
        coloured = now;
        View[] views;
        Shape[] shapes;
        synchronized (WORN) {
            views = WORN.keySet().toArray(new View[0]);
            shapes = new Shape[views.length];
            for (int i = 0; i < views.length; i ++) shapes[i] = WORN.get(views[i]);
        }
        for (int i = 0; i < views.length; i ++) {
            View view = views[i];
            Shape shape = shapes[i];
            if (view == null || shape == null || !view.isAttachedToWindow()) continue;
            shape.ground = Ground.behind(view);
            shape.tint(colour(view, shape.role));
        }
    }

    private static int overVideo() {
        return (Ground.mix(0xFF17161B, Accent.colour() | 0xFF000000, 0.10f) & 0x00FFFFFF) | 0x99000000;
    }

    private static int chosenOverVideo() {
        return (Ground.mix(0xFF17161B, Accent.colour() | 0xFF000000, 0.45f) & 0x00FFFFFF) | 0xE6000000;
    }

    public static void rail(View slot, int which) {
        try {
            if (slot == null) return;
            forgetIcons(slot, 0);
            icons(slot, which);
            if (!Surfaces.mdThree(Surfaces.FEED)) {
                strip(slot);
                return;
            }
            boolean first = true, last = true;
            for (int i = 0; i < which; i ++) if (FeedRail.shown(i)) first = false;
            for (int i = which + 1; i < FeedRail.KEYS.length; i ++) if (FeedRail.shown(i)) last = false;
            float unit = slot.getResources().getDisplayMetrics().density;
            Shape card = wear(slot, CARD, true);
            card.right = 6f * unit;
            card.wide = 48f * unit;
            card.top = card.bottom = unit;
            card.ends(first ? 24f * unit : 6f * unit, last ? 24f * unit : 6f * unit);
            card.dress(slot, OVER_VIDEO);
        } catch (Throwable error) {
            Diary.detail("md3: rail: " + error);
        }
    }

    private static final String LIKE = "M13.35 20.13c-.76.69-1.93.69-2.69-.01l-.11-.1C5.3 15.27 1.87 12.16 2 8.28" + "c.06-1.7.93-3.33 2.34-4.29 2.64-1.8 5.9-.96 7.66 1.1 1.76-2.06 5.02-2.91 7.66-1.1 1.41.96 2.28 2.59 2.34 4.29"
            + ".14 3.88-3.3 6.99-8.55 11.76l-.1.09z";
    private static final String COMMENT = "M12 3C6.48 3 2 6.8 2 11.5c0 2.6 1.38 4.93 3.55 6.5-.2 1.2-.83 2.4-1.75 3.3" + "-.3.3-.08.8.34.76 2.1-.2 3.9-1 5.2-2.03.85.18 1.74.27 2.66.27 5.52 0 10-3.8 10-8.5S17.52 3 12 3z"
            + "M7.4 10.1c.77 0 1.4.63 1.4 1.4s-.63 1.4-1.4 1.4S6 12.27 6 11.5s.63-1.4 1.4-1.4z" + "M12 10.1c.77 0 1.4.63 1.4 1.4s-.63 1.4-1.4 1.4-1.4-.63-1.4-1.4.63-1.4 1.4-1.4z"
            + "M16.6 10.1c.77 0 1.4.63 1.4 1.4s-.63 1.4-1.4 1.4-1.4-.63-1.4-1.4.63-1.4 1.4-1.4z";
    private static final String SAVE = "M7.5 3h9q2.5 0 2.5 2.5v14.1q0 1.4-1.3.85L12 18.2l-5.7 2.25Q5 21 5 19.6V5.5Q5 3 7.5 3z";
    private static final String SHARE = "M10 9V7.41c0-.89-1.08-1.34-1.71-.71L3.7 11.29c-.39.39-.39 1.02 0 1.41l4.59 4.59"
            + "c.63.63 1.71.19 1.71-.7V14.9c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z";
    private static final String[] GLYPHS = {LIKE, COMMENT, SAVE, SHARE};
    private static final android.graphics.Path[] DRAWN = new android.graphics.Path[GLYPHS.length];
    private static android.graphics.Path glyph(int which) {
        android.graphics.Path known = DRAWN[which];
        if (known != null) return known;
        final android.graphics.Path path = new android.graphics.Path();
        path.setFillType(android.graphics.Path.FillType.EVEN_ODD);
        NavPlan.trace(GLYPHS[which], new NavPlan.Pen() {
            @Override public void move(float x, float y) {
                path.moveTo(x, y);
            }
            @Override public void line(float x, float y) {
                path.lineTo(x, y);
            }
            @Override public void curve(float x1, float y1, float x2, float y2, float x, float y) {
                path.cubicTo(x1, y1, x2, y2, x, y);
            }
            @Override public void close() {
                path.close();
            }
        });
        DRAWN[which] = path;
        return path;
    }

    private static final class Glyph extends Drawable {
        public final int which;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public Glyph(int which) {
            this.which = which;
            paint.setColor(0xFFFFFFFF);
        }
        @Override public void draw(Canvas canvas) {
            Rect box = getBounds();
            if (box.isEmpty()) return;
            int saved = canvas.save();
            float grown = 1.14f, side = Math.min(box.width(), box.height()) * grown;
            canvas.translate(box.exactCenterX() - side / 2f, box.exactCenterY() - side / 2f);
            if (which == 3) canvas.scale(- 1f, 1f, side / 2f, 0f);
            canvas.scale(side / 24f, side / 24f);
            canvas.drawPath(glyph(which), paint);
            canvas.restoreToCount(saved);
        }
        @Override public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }
        @Override public void setColorFilter(ColorFilter filter) {
            paint.setColorFilter(filter);
        }
        @Override public void setTint(int colour) {
            paint.setColor(colour);
        }
        @Override public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static final String TUX_ICON = "com.bytedance.tux.drawable.TuxIconDrawable";
    private static java.lang.reflect.Field tuxPicture;
    private static boolean tuxAsked;
    private static final java.util.WeakHashMap<Drawable, Drawable> HELD = new java.util.WeakHashMap<Drawable, Drawable>();
    private static final int PICTURE = 0x4D617254;
    private static void icon(View view, int which, int depth) {
        if (view == null || depth > 5 || which < 0 || which >= GLYPHS.length) return;
        if (view instanceof android.widget.ImageView) {
            android.widget.ImageView image = (android.widget.ImageView) view;
            Drawable shown = image.getDrawable();
            if (shown == null || shown == view.getTag(PICTURE)) return;
            view.setTag(PICTURE, shown);
            if (!shown.getClass().getName().equals(TUX_ICON)) return;
            try {
                if (!tuxAsked) {
                    tuxAsked = true;
                    java.lang.reflect.Field first = null;
                    for (java.lang.reflect.Field field : shown.getClass().getDeclaredFields()) {
                        if (field.getType() != Drawable.class || java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                        if (first == null || field.getName().compareTo(first.getName()) < 0) first = field;
                    }
                    if (first != null) first.setAccessible(true);
                    tuxPicture = first;
                }
                if (tuxPicture == null) return;
                Object inside = tuxPicture.get(shown);
                boolean on = on(ICONS);
                if (on && inside instanceof Drawable && !(inside instanceof Glyph)) {
                    synchronized (HELD) {
                        HELD.put(shown, (Drawable) inside);
                    }
                    Glyph ours = new Glyph(which);
                    ours.setBounds(((Drawable) inside).getBounds());
                    tuxPicture.set(shown, ours);
                    shown.invalidateSelf();
                } else if (!on && inside instanceof Glyph) {
                    Drawable before;
                    synchronized (HELD) {
                        before = HELD.remove(shown);
                    }
                    if (before != null) {
                        tuxPicture.set(shown, before);
                        shown.invalidateSelf();
                    }
                }
            } catch (Throwable error) {
                Diary.detail("md3: icon: " + error);
                tuxPicture = null;
            }
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i ++) icon(group.getChildAt(i), which, depth + 1);
        }
    }

    public static void icons(View slot, int which) {
        try {
            if (slot != null && slot.getVisibility() == View.VISIBLE) icon(slot, which, 0);
        } catch (Throwable ignored) {}
    }

    private static void forgetIcons(View view, int depth) {
        if (view == null || depth > 5) return;
        view.setTag(PICTURE, null);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i ++) forgetIcons(group.getChildAt(i), depth + 1);
        }
    }

    private static final String NAV_BAR = "com.bytedance.tux.navigation.TuxNavBar";
    private static final String ICON = "com.bytedance.tux.icon.TuxIconView";
    private static final String ROW = "com.ss.android.ugc.aweme.notice.api.view.MultiSelectConstraintLayout";
    private static final String CELL = "com.ss.android.ugc.aweme.base.widget.FixedRatioFrameLayout";
    private static final String PAGER = "com.ss.android.ugc.aweme.common.widget.VerticalViewPager";
    private static final int K_OTHER = 0, K_NAV = 1, K_ICON = 2, K_ROW = 3, K_CELL = 4, K_PAGER = 5;
    private static final IdentityHashMap<Class<?>, Integer> KINDS = new IdentityHashMap<Class<?>, Integer>();
    private static int kind(View view) {
        Class<?> type = view.getClass();
        Integer known = KINDS.get(type);
        if (known != null) return known;
        String name = type.getName();
        int kind = name.equals(NAV_BAR) ? K_NAV : name.equals(ICON) ? K_ICON : name.equals(ROW) ? K_ROW : name.equals(CELL)
                ? K_CELL : name.equals(PAGER) ? K_PAGER : K_OTHER;
        KINDS.put(type, kind);
        return kind;
    }

    private static int feedLeft, feedSearch, feedTop, profileRoot, profileTabs, statNumber;
    private static boolean named;
    private static void names(Context context) {
        if (named) return;
        named = true;
        try {
            android.content.res.Resources resources = context.getResources();
            String pack = context.getPackageName();
            feedLeft = resources.getIdentifier("k_2", "id", pack);
            feedSearch = resources.getIdentifier("k_5", "id", pack);
            feedTop = resources.getIdentifier("uvh", "id", pack);
            profileRoot = resources.getIdentifier("t7i", "id", pack);
            profileTabs = resources.getIdentifier("z2h", "id", pack);
            statNumber = resources.getIdentifier("t7t", "id", pack);
            Diary.detail("md3: names " + (feedLeft != 0 ? 1 : 0) + (feedSearch != 0 ? 1 : 0) + (profileRoot != 0 ? 1 : 0)
                    + (profileTabs != 0 ? 1 : 0) + (statNumber != 0 ? 1 : 0));
        } catch (Throwable error) {
            Diary.detail("md3: names: " + error);
        }
    }

    private static final int WATCHED = 0x4D617251;
    private static final long QUIET = 350L;
    private static final class Screen implements ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnPreDrawListener {
        public final WeakReference<View> root;
        public long looked;
        public boolean owed;
        public WeakReference<ViewGroup> rows = new WeakReference<ViewGroup>(null), cells = new WeakReference<ViewGroup>(null);
        public WeakReference<ViewGroup> tabs = new WeakReference<ViewGroup>(null), feedTabs = new WeakReference<ViewGroup>(null);
        public int rowsSeen, cellsSeen, tabChosen = - 2, feedChosen = - 2;
        public final Runnable later = () -> {
            owed = false;
            look();
        };
        public Screen(View root) {
            this.root = new WeakReference<View>(root);
        }
        @Override public void onGlobalLayout() {
            View view = root.get();
            if (view == null) return;
            long now = SystemClock.uptimeMillis();
            if (now - looked < QUIET) {
                if (!owed) {
                    owed = true;
                    view.postDelayed(later, QUIET - (now - looked) + 20);
                }
                return;
            }
            look();
        }
        public void look() {
            View view = root.get();
            if (view == null || !view.isAttachedToWindow()) return;
            looked = SystemClock.uptimeMillis();
            try {
                names(view.getContext());
                visit(view, 0, false);
                FeedRail.restyle();
            } catch (Throwable error) {
                Diary.detail("md3: " + error);
            }
        }
        @Override public boolean onPreDraw() {
            try {
                FeedRail.icons();
                recolour();
                ViewGroup list = rows.get();
                if (list != null && list.isAttachedToWindow()) {
                    int many = list.getChildCount();
                    for (int i = 0; i < many; i ++) {
                        View child = list.getChildAt(i);
                        if (kind(child) != K_ROW) {
                            row(child, 0, true, true);
                            continue;
                        }
                        boolean head = i == 0 ? !list.canScrollVertically(- 1) : kind(list.getChildAt(i - 1)) != K_ROW;
                        boolean tail = i == many - 1 ? !list.canScrollVertically(1) : kind(list.getChildAt(i + 1)) != K_ROW;
                        row(child, 0, head, tail);
                    }
                }
                ViewGroup grid = cells.get();
                if (grid != null && grid.isAttachedToWindow() && grid.getChildCount() > 0) {
                    if (grid.getChildCount() != cellsSeen) {
                        cellsSeen = grid.getChildCount();
                        for (int i = 0; i < grid.getChildCount(); i ++) cell(grid.getChildAt(i));
                    }
                    for (int i = 0; i < 3 && i < grid.getChildCount(); i ++) {
                        cell(grid.getChildAt(i));
                        cell(grid.getChildAt(grid.getChildCount() - 1 - i));
                    }
                }
                ViewGroup strip = tabs.get();
                if (strip != null && strip.isAttachedToWindow()) {
                    int chosen = chosenIn(strip);
                    if (chosen != tabChosen) {
                        tabChosen = chosen;
                        pills(strip, Surfaces.mdThree(Surfaces.PROFILE), 3f, 3f, CHOSEN);
                    }
                }
                strip = feedTabs.get();
                if (strip != null && strip.isAttachedToWindow()) {
                    int chosen = chosenIn(strip);
                    if (chosen != feedChosen) {
                        feedChosen = chosen;
                        pills(strip, Surfaces.mdThree(Surfaces.FEED), 2f, 13f, CHOSEN_OVER_VIDEO);
                    }
                    underline(strip, false);
                }
            } catch (Throwable ignored) {} return true;
        }
        private void visit(View view, int depth, boolean inProfile) {
            if (view == null || depth > 45 || view.getVisibility() != View.VISIBLE) return;
            int id = view.getId();
            if (id != View.NO_ID && id != 0) {
                if (id == feedLeft || id == feedSearch) {
                    disc(view, Surfaces.mdThree(Surfaces.FEED), OVER_VIDEO, 19f);
                    return;
                }
                if (id == feedTop && view instanceof ViewGroup) feedTop((ViewGroup) view);
                if (id == profileRoot) inProfile = true;
                if (id == profileTabs && view instanceof ViewGroup) {
                    tabStrip((ViewGroup) view);
                    return;
                }
                if (id == statNumber) {
                    stats(view);
                    return;
                }
            }
            if (BurnEye.eye(view)) {
                disc(view, Surfaces.mdThree(Surfaces.FEED), OVER_VIDEO, 19f);
                return;
            }
            int kind = kind(view);
            if (kind == K_PAGER) return;
            if (kind == K_ROW) {
                Object parent = view.getParent();
                if (parent instanceof ViewGroup) {
                    ViewGroup list = (ViewGroup) parent;
                    if (list.getChildCount() > 2 || rows.get() == null) {
                        if (rows.get() != list) rowsSeen = - 1;
                        rows = new WeakReference<ViewGroup>(list);
                    }
                }
                row(view, 0, true, true);
                return;
            }
            if (kind == K_CELL) {
                if (!inProfile) return;
                Object parent = view.getParent();
                if (parent instanceof ViewGroup && cells.get() != parent) {
                    cells = new WeakReference<ViewGroup>((ViewGroup) parent);
                    cellsSeen = - 1;
                }
                cell(view);
                return;
            }
            if (kind == K_NAV) {
                icons(view, 0, inProfile ? Surfaces.mdThree(Surfaces.PROFILE) && onDark(view) : Surfaces.mdThree(Surfaces.INBOX));
                return;
            }
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i ++) visit(group.getChildAt(i), depth + 1, inProfile);
            }
        }
        private void feedTop(ViewGroup row) {
            ViewGroup window = null;
            for (int i = 0; i < row.getChildCount() && window == null; i ++) {
                View child = row.getChildAt(i);
                if (child instanceof ViewGroup && kind(child) != K_ICON && child.getVisibility() == View.VISIBLE) {
                    window = (ViewGroup) child;
                }
            }
            View inner = window == null || window.getChildCount() == 0 ? null : window.getChildAt(0);
            View words = inner instanceof ViewGroup && ((ViewGroup) inner).getChildCount() > 0 ? ((ViewGroup) inner).getChildAt(0)
                    : null;
            if (!(words instanceof ViewGroup) || ((ViewGroup) words).getChildCount() < 2) return;
            ViewGroup strip = (ViewGroup) words;
            if (!Surfaces.mdThree(Surfaces.FEED)) {
                Md3.strip(window);
                for (int i = 0; i < strip.getChildCount(); i ++) Md3.strip(strip.getChildAt(i));
                if (!Surfaces.glass(Surfaces.FEED)) underline(strip, true);
                feedTabs = new WeakReference<ViewGroup>(null);
                return;
            }
            float unit = row.getResources().getDisplayMetrics().density;
            Shape capsule = wear(window, PILL);
            capsule.top = capsule.bottom = 9f * unit;
            capsule.dress(window, OVER_VIDEO);
            if (feedTabs.get() != strip) {
                feedTabs = new WeakReference<ViewGroup>(strip);
                feedChosen = - 2;
            }
        }
        private void tabStrip(ViewGroup holder) {
            ViewGroup strip = holder.getChildCount() == 1 && holder.getChildAt(0) instanceof ViewGroup ? (ViewGroup) holder.getChildAt(0)
                    : null;
            if (strip == null) return;
            if (!Surfaces.mdThree(Surfaces.PROFILE)) {
                Md3.strip(holder);
                for (int i = 0; i < strip.getChildCount(); i ++) Md3.strip(strip.getChildAt(i));
                tabs = new WeakReference<ViewGroup>(null);
                return;
            }
            Shape pill = wear(holder, PILL);
            pill.dress(holder, SHELL);
            if (strip.getTag(FLAT) == null) {
                strip.setTag(FLAT, Boolean.TRUE);
                try {
                    strip.getClass().getMethod("setSelectedIndicatorHeight", int.class).invoke(strip, 0);
                } catch (Throwable error) {
                    Diary.detail("md3: the line under the tabs stays: " + error);
                }
                Object parent = holder.getParent();
                if (parent instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) parent;
                    int at = group.indexOfChild(holder);
                    View next = at >= 0 && at + 1 < group.getChildCount() ? group.getChildAt(at + 1) : null;
                    if (next != null && !(next instanceof ViewGroup) && next.getHeight() > 0 && next.getHeight() <= 3 * next.getResources()
                            .getDisplayMetrics().density) {
                        next.setAlpha(0f);
                    }
                }
            }
            tabs = new WeakReference<ViewGroup>(strip);
            tabChosen = - 2;
        }
    }

    private static int chosenIn(ViewGroup strip) {
        for (int i = 0; i < strip.getChildCount(); i ++) if (strip.getChildAt(i).isSelected()) return i;
        return - 1;
    }

    private static void pills(ViewGroup strip, boolean on, float sides, float ends, int role) {
        float unit = strip.getResources().getDisplayMetrics().density;
        for (int i = 0; i < strip.getChildCount(); i ++) {
            View tab = strip.getChildAt(i);
            if (tab.isSelected() && on) {
                Shape pill = wear(tab, PILL);
                pill.left = pill.right = sides * unit;
                pill.top = pill.bottom = ends * unit;
                pill.dress(tab, role);
            } else {
                strip(tab);
            }
        }
    }

    private static java.lang.reflect.Method linePaint;
    private static boolean lineAsked;
    private static void underline(ViewGroup strip, boolean shown) {
        try {
            if (!lineAsked) {
                lineAsked = true;
                linePaint = strip.getClass().getMethod("getMSelectedIndicatorPaint");
            }
            if (linePaint == null) return;
            Object paint = linePaint.invoke(strip);
            if (!(paint instanceof Paint)) return;
            int wanted = shown ? 255 : 0;
            if (((Paint) paint).getAlpha() != wanted) {
                ((Paint) paint).setAlpha(wanted);
                if (!shown)((Paint) paint).clearShadowLayer();
                strip.invalidate();
            }
        } catch (Throwable error) {
            if (linePaint != null) Diary.detail("md3: the line under the feed's tabs stays: " + error);
            linePaint = null;
        }
    }

    private static void disc(View view, boolean on, int role, float radius) {
        if (!on) {
            strip(view);
            return;
        }
        if (view.getBackground() != null && !(view.getTag(OURS) instanceof Shape)) return;
        Shape disc = wear(view, DISC);
        disc.radius = radius * view.getResources().getDisplayMetrics().density;
        disc.dress(view, role);
    }

    private static void icons(View view, int depth, boolean on) {
        if (depth > 6 || view.getVisibility() != View.VISIBLE) return;
        if (kind(view) == K_ICON) {
            float unit = view.getResources().getDisplayMetrics().density;
            if (view.isFocusable() && view.getWidth() >= 32 * unit) disc(view, on, SHELL, 19f);
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i ++) icons(group.getChildAt(i), depth + 1, on);
        }
    }

    private static void row(View view, int depth, boolean head, boolean tail) {
        if (view == null) return;
        if (kind(view) != K_ROW) {
            if (depth == 0 && view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount() && i < 4; i ++) row(group.getChildAt(i), 1, true, true);
            }
            return;
        }
        if (!Surfaces.mdThree(Surfaces.INBOX)) {
            strip(view);
            return;
        }
        float unit = view.getResources().getDisplayMetrics().density;
        Shape card = wear(view, CARD);
        card.left = card.right = 8f * unit;
        card.top = card.bottom = unit;
        card.ends(head ? 22f * unit : 5f * unit, tail ? 22f * unit : 5f * unit);
        card.dress(view, SHELL);
    }

    private static final ViewOutlineProvider ROUNDED = new ViewOutlineProvider() {
        @Override public void getOutline(View view, Outline outline) {
            float unit = view.getResources().getDisplayMetrics().density;
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 12f * unit);
        }
    };

    private static void cell(View view) {
        if (view == null || kind(view) != K_CELL) return;
        boolean on = Surfaces.mdThree(Surfaces.PROFILE);
        if (on == (view.getTag(ROUND) != null)) return;
        if (on) {
            view.setTag(ROUND, Boolean.TRUE);
            view.setOutlineProvider(ROUNDED);
            view.setClipToOutline(true);
        } else {
            view.setTag(ROUND, null);
            view.setClipToOutline(false);
            view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
    }

    private static void stats(View number) {
        Object stat = number.getParent();
        Object line = stat instanceof View ? ((View) stat).getParent() : null;
        if (!(line instanceof ViewGroup)) return;
        ViewGroup row = (ViewGroup) line;
        if (!Surfaces.mdThree(Surfaces.PROFILE)) {
            strip(row);
            return;
        }
        if (row.getTag(OURS) instanceof Shape && row.getBackground() == row.getTag(OURS)) {
            Shape worn = (Shape) row.getTag(OURS);
            worn.dress(row, SHELL);
            return;
        }
        if (row.getBackground() != null && !(row.getTag(OURS) instanceof Shape)) return;
        float unit = row.getResources().getDisplayMetrics().density;
        Shape card = wear(row, CARD);
        card.left = - 10f * unit;
        card.top = - 6f * unit;
        card.bottom = - 4f * unit;
        card.right = 0f;
        card.radius = 18f * unit;
        card.dress(row, SHELL);
        Object above = row.getParent();
        for (int i = 0; i < 2 && above instanceof ViewGroup; i ++) {
            ((ViewGroup) above).setClipChildren(false);
            above = ((ViewGroup) above).getParent();
        }
    }

    public static void attach(Activity activity) {
        try {
            String name = activity.getClass().getName();
            if (!name.endsWith(".SplashActivity") && !name.endsWith(".MainActivity")) return;
            View root = activity.getWindow().getDecorView();
            Object known = root.getTag(WATCHED);
            Screen screen;
            if (known instanceof Screen) {
                screen = (Screen) known;
            } else {
                screen = new Screen(root);
                root.setTag(WATCHED, screen);
                root.getViewTreeObserver().addOnGlobalLayoutListener(screen);
                root.getViewTreeObserver().addOnPreDrawListener(screen);
            }
            screen.rowsSeen = screen.cellsSeen = - 1;
            screen.tabChosen = screen.feedChosen = - 2;
            root.post(screen.later);
        } catch (Throwable error) {
            Diary.detail("md3: " + error);
        }
    }
}

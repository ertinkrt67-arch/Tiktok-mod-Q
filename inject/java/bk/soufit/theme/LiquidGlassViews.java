package bk.soufit.theme;
import bk.soufit.core.Diary;
import bk.soufit.feed.BurnEye;
import bk.soufit.feed.FeedRail;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.WeakHashMap;
public final class LiquidGlassViews {
    private LiquidGlassViews() {}

    private static final float DISC = 19f, WINDOW_TOP = 9f, FEED_TAB_SIDE = 2f, FEED_TAB_END = 13f, PROFILE_TAB_SIDE = 3f,
            PROFILE_TAB_END = 3f, RAIL_RIGHT = 6f, RAIL_WIDE = 48f, RAIL_END = 24f, RAIL_SEAM = 6f, ROW_SIDE = 8f,
            ROW_END = 22f, ROW_SEAM = 5f, STAT_LEFT = -10f, STAT_TOP = -6f, STAT_BOTTOM = -4f, STAT_RADIUS = 18f,
            CELL_RADIUS = 12f, ICON_MIN = 32f;

    private static final int LIGHT_BODY = 0x59FFFFFF, LIGHT_CHOSEN = 0x66FFFFFF, DARK_CHOSEN = 0x44FFFFFF, DARK_BODY_ALPHA = 148;

    static int bodyColour(boolean dark, boolean chosen) {
        if (!dark) return chosen ? LIGHT_CHOSEN : LIGHT_BODY;
        if (chosen) return DARK_CHOSEN;
        return (Tint.glassBody(Themes.isFullAmoled()) & 0x00FFFFFF) | (DARK_BODY_ALPHA << 24);
    }

    private static final int KIND_CARD = 0, KIND_PILL = 1, KIND_DISC = 2;

    private static final class Pane extends Drawable {
        final int kind;
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF box = new RectF();
        final float[] corners = new float[8];
        final Path edge = new Path();
        float left, top, right, bottom, wide, radius, foot = - 1f, unit = 1f;
        int ground;
        boolean chosen;
        Pane(int kind) {
            this.kind = kind;
        }
        void frame(float l, float t, float r, float b, float w, float rad, float f) {
            if (left == l && top == t && right == r && bottom == b && wide == w && radius == rad && foot == f) return;
            left = l;
            top = t;
            right = r;
            bottom = b;
            wide = w;
            radius = rad;
            foot = f;
            invalidateSelf();
        }
        @Override public void draw(Canvas canvas) {
            Rect whole = getBounds();
            box.set(whole.left + left, whole.top + top, whole.right - right, whole.bottom - bottom);
            if (wide > 0f) box.left = box.right - wide;
            boolean dark = Tint.darkGlass(Themes.isDark(), ground);
            paint.setColor(bodyColour(dark, chosen));
            float round = kind == KIND_PILL ? box.height() / 2f : kind == KIND_DISC ? Math.min(box.width(), box.height()) / 2f : radius;
            LiquidGlass.shadow(paint, unit);
            if (kind == KIND_CARD && foot >= 0f && foot != radius) {
                corners[0] = corners[1] = corners[2] = corners[3] = radius;
                corners[4] = corners[5] = corners[6] = corners[7] = foot;
                edge.rewind();
                edge.addRoundRect(box, corners, Path.Direction.CW);
                canvas.drawPath(edge, paint);
            } else {
                canvas.drawRoundRect(box, round, round, paint);
            }
            LiquidGlass.unshadow(paint);
            LiquidGlass.light(canvas, box, round, unit, dark);
        }
        @Override public void setAlpha(int alpha) {} @Override public void setColorFilter(ColorFilter filter) {} @Override public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static final int OURS = 0x4C475631, WAS = 0x4C475632, PUT = 0x4C475633, WATCHED = 0x4C475634, FLAT = 0x4C475635,
            ROUND = 0x4C475636;
    private static final Object NONE = new Object();
    private static final WeakHashMap<View, Pane> WORN = new WeakHashMap<View, Pane>();

    private static float density(View view) {
        return view.getResources().getDisplayMetrics().density;
    }

    private static Pane wear(View view, int kind, boolean under) {
        Object worn = view.getTag(OURS);
        Pane pane;
        if (worn instanceof Pane && ((Pane) worn).kind == kind) {
            pane = (Pane) worn;
        } else {
            strip(view);
            pane = new Pane(kind);
            Drawable before = view.getBackground();
            view.setTag(WAS, before == null ? NONE : before);
            view.setTag(OURS, pane);
            view.setTag(PUT, under && before != null ? new LayerDrawable(new Drawable[] {pane, before}) : pane);
        }
        Object put = view.getTag(PUT);
        if (put instanceof Drawable && view.getBackground() != put) view.setBackground((Drawable) put);
        synchronized (WORN) {
            WORN.put(view, pane);
        }
        return pane;
    }

    private static void dress(View view, Pane pane, boolean chosen) {
        float unit = density(view);
        int ground = Ground.behind(view);
        if (pane.ground == ground && pane.chosen == chosen && pane.unit == unit) return;
        pane.ground = ground;
        pane.chosen = chosen;
        pane.unit = unit;
        pane.invalidateSelf();
    }

    private static void strip(View view) {
        if (!(view.getTag(OURS) instanceof Pane)) return;
        Object before = view.getTag(WAS);
        if (view.getBackground() == view.getTag(PUT)) view.setBackground(before instanceof Drawable ? (Drawable) before : null);
        view.setTag(OURS, null);
        view.setTag(WAS, null);
        view.setTag(PUT, null);
        synchronized (WORN) {
            WORN.remove(view);
        }
    }

    private static long coloured;
    private static void recolour() {
        long now = SystemClock.uptimeMillis();
        if (now - coloured < 500L) return;
        coloured = now;
        View[] views;
        Pane[] panes;
        synchronized (WORN) {
            views = WORN.keySet().toArray(new View[0]);
            panes = new Pane[views.length];
            for (int i = 0; i < views.length; i ++) panes[i] = WORN.get(views[i]);
        }
        for (int i = 0; i < views.length; i ++) {
            if (views[i] == null || panes[i] == null || !views[i].isAttachedToWindow()) continue;
            dress(views[i], panes[i], panes[i].chosen);
        }
    }


    public static void rail(View slot, int which) {
        try {
            if (slot == null) return;
            if (!Surfaces.glass(Surfaces.FEED)) {
                strip(slot);
                return;
            }
            boolean first = true, last = true;
            for (int i = 0; i < which; i ++) if (FeedRail.shown(i)) first = false;
            for (int i = which + 1; i < FeedRail.KEYS.length; i ++) if (FeedRail.shown(i)) last = false;
            float unit = density(slot);
            Pane card = wear(slot, KIND_CARD, true);
            card.unit = unit;
            card.frame(0f, unit, RAIL_RIGHT * unit, unit, RAIL_WIDE * unit, first ? RAIL_END * unit : RAIL_SEAM * unit,
                    last ? RAIL_END * unit : RAIL_SEAM * unit);
            dress(slot, card, false);
        } catch (Throwable error) {
            Diary.detail("liquid glass: rail: " + error);
        }
    }

    private static void disc(View view, boolean on, float radius) {
        if (!on) {
            strip(view);
            return;
        }
        if (view.getBackground() != null && !(view.getTag(OURS) instanceof Pane)) return;
        float unit = density(view);
        Pane disc = wear(view, KIND_DISC, false);
        disc.frame(0f, 0f, 0f, 0f, 0f, radius * unit, - 1f);
        dress(view, disc, false);
    }

    private static void stats(View number) {
        Object stat = number.getParent();
        Object line = stat instanceof View ? ((View) stat).getParent() : null;
        if (!(line instanceof ViewGroup)) return;
        ViewGroup row = (ViewGroup) line;
        if (!Surfaces.glass(Surfaces.PROFILE)) {
            strip(row);
            return;
        }
        float unit = density(row);
        Pane card = wear(row, KIND_CARD, false);
        card.frame(STAT_LEFT * unit, STAT_TOP * unit, 0f, STAT_BOTTOM * unit, 0f, STAT_RADIUS * unit, - 1f);
        dress(row, card, false);
        Object above = row.getParent();
        for (int i = 0; i < 2 && above instanceof ViewGroup; i ++) {
            ((ViewGroup) above).setClipChildren(false);
            above = ((ViewGroup) above).getParent();
        }
    }

    private static final ViewOutlineProvider ROUNDED = new ViewOutlineProvider() {
        @Override public void getOutline(View view, Outline outline) {
            float unit = density(view);
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), CELL_RADIUS * unit);
        }
    };

    private static void cell(View view) {
        if (view == null || kind(view) != K_CELL) return;
        boolean on = Surfaces.glass(Surfaces.PROFILE);
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

    private static void row(View view, boolean head, boolean tail) {
        if (!Surfaces.glass(Surfaces.INBOX)) {
            strip(view);
            return;
        }
        float unit = density(view);
        Pane card = wear(view, KIND_CARD, false);
        card.frame(ROW_SIDE * unit, unit, ROW_SIDE * unit, unit, 0f, head ? ROW_END * unit : ROW_SEAM * unit,
                tail ? ROW_END * unit : ROW_SEAM * unit);
        dress(view, card, false);
    }

    private static void pills(ViewGroup strip, boolean on, float sides, float ends) {
        float unit = density(strip);
        for (int i = 0; i < strip.getChildCount(); i ++) {
            View tab = strip.getChildAt(i);
            if (tab.isSelected() && on) {
                Pane pill = wear(tab, KIND_PILL, false);
                pill.frame(sides * unit, ends * unit, sides * unit, ends * unit, 0f, 0f, - 1f);
                dress(tab, pill, true);
            } else {
                strip(tab);
            }
        }
    }

    private static int chosenIn(ViewGroup strip) {
        for (int i = 0; i < strip.getChildCount(); i ++) if (strip.getChildAt(i).isSelected()) return i;
        return - 1;
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
                if (!shown) ((Paint) paint).clearShadowLayer();
                strip.invalidate();
            }
        } catch (Throwable error) {
            if (linePaint != null) Diary.detail("liquid glass: the line under the tabs stays: " + error);
            linePaint = null;
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

    private static int feedLeft, feedSearch, feedTopId, profileRoot, profileTabs, statNumber;
    private static boolean named;
    private static void names(android.content.Context context) {
        if (named) return;
        named = true;
        try {
            android.content.res.Resources resources = context.getResources();
            String pack = context.getPackageName();
            feedLeft = resources.getIdentifier("k_2", "id", pack);
            feedSearch = resources.getIdentifier("k_5", "id", pack);
            feedTopId = resources.getIdentifier("uvh", "id", pack);
            profileRoot = resources.getIdentifier("t7i", "id", pack);
            profileTabs = resources.getIdentifier("z2h", "id", pack);
            statNumber = resources.getIdentifier("t7t", "id", pack);
        } catch (Throwable error) {
            Diary.detail("liquid glass: names: " + error);
        }
    }

    private static final long QUIET = 350L;
    private static final class Lens implements ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnPreDrawListener {
        final WeakReference<View> root;
        long looked;
        boolean owed;
        WeakReference<ViewGroup> rows = new WeakReference<ViewGroup>(null), cells = new WeakReference<ViewGroup>(null);
        WeakReference<ViewGroup> tabs = new WeakReference<ViewGroup>(null), feedTabs = new WeakReference<ViewGroup>(null);
        int cellsSeen = - 1, tabChosen = - 2, feedChosen = - 2;
        final Runnable later = () -> {
            owed = false;
            look();
        };
        Lens(View root) {
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
        void look() {
            View view = root.get();
            if (view == null || !view.isAttachedToWindow()) return;
            looked = SystemClock.uptimeMillis();
            try {
                names(view.getContext());
                visit(view, 0, false);
                FeedRail.restyle();
            } catch (Throwable error) {
                Diary.detail("liquid glass: " + error);
            }
        }
        @Override public boolean onPreDraw() {
            try {
                recolour();
                ViewGroup list = rows.get();
                if (list != null && list.isAttachedToWindow()) {
                    int many = list.getChildCount();
                    for (int i = 0; i < many; i ++) {
                        View child = list.getChildAt(i);
                        if (kind(child) != K_ROW) continue;
                        boolean head = i == 0 ? !list.canScrollVertically(- 1) : kind(list.getChildAt(i - 1)) != K_ROW;
                        boolean tail = i == many - 1 ? !list.canScrollVertically(1) : kind(list.getChildAt(i + 1)) != K_ROW;
                        row(child, head, tail);
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
                        pills(strip, Surfaces.glass(Surfaces.PROFILE), PROFILE_TAB_SIDE, PROFILE_TAB_END);
                    }
                }
                strip = feedTabs.get();
                if (strip != null && strip.isAttachedToWindow()) {
                    int chosen = chosenIn(strip);
                    if (chosen != feedChosen) {
                        feedChosen = chosen;
                        pills(strip, Surfaces.glass(Surfaces.FEED), FEED_TAB_SIDE, FEED_TAB_END);
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
                    disc(view, Surfaces.glass(Surfaces.FEED), DISC);
                    return;
                }
                if (id == feedTopId && view instanceof ViewGroup) feedTop((ViewGroup) view);
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
                disc(view, Surfaces.glass(Surfaces.FEED), DISC);
                return;
            }
            int kind = kind(view);
            if (kind == K_PAGER) return;
            if (kind == K_ROW) {
                Object parent = view.getParent();
                if (parent instanceof ViewGroup) {
                    ViewGroup list = (ViewGroup) parent;
                    if (list.getChildCount() > 2 || rows.get() == null) rows = new WeakReference<ViewGroup>(list);
                }
                row(view, true, true);
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
                navIcons(view, 0, inProfile ? Surfaces.glass(Surfaces.PROFILE) && onDark(view) : Surfaces.glass(Surfaces.INBOX));
                return;
            }
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i ++) visit(group.getChildAt(i), depth + 1, inProfile);
            }
        }
        private void navIcons(View view, int depth, boolean on) {
            if (depth > 6 || view.getVisibility() != View.VISIBLE) return;
            if (kind(view) == K_ICON) {
                float unit = density(view);
                if (view.isFocusable() && view.getWidth() >= ICON_MIN * unit) disc(view, on, DISC);
                return;
            }
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i ++) navIcons(group.getChildAt(i), depth + 1, on);
            }
        }
        private boolean onDark(View view) {
            return Ground.bright(Ground.behind(view)) < 0.5f;
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
            if (!Surfaces.glass(Surfaces.FEED)) {
                strip(window);
                for (int i = 0; i < strip.getChildCount(); i ++) strip(strip.getChildAt(i));
                feedTabs = new WeakReference<ViewGroup>(null);
                return;
            }
            float unit = density(row);
            Pane capsule = wear(window, KIND_PILL, false);
            capsule.frame(0f, WINDOW_TOP * unit, 0f, WINDOW_TOP * unit, 0f, 0f, - 1f);
            dress(window, capsule, false);
            if (feedTabs.get() != strip) {
                feedTabs = new WeakReference<ViewGroup>(strip);
                feedChosen = - 2;
            }
        }
        private void tabStrip(ViewGroup holder) {
            ViewGroup strip = holder.getChildCount() == 1 && holder.getChildAt(0) instanceof ViewGroup ? (ViewGroup) holder.getChildAt(0)
                    : null;
            if (strip == null) return;
            if (!Surfaces.glass(Surfaces.PROFILE)) {
                strip(holder);
                for (int i = 0; i < strip.getChildCount(); i ++) strip(strip.getChildAt(i));
                tabs = new WeakReference<ViewGroup>(null);
                return;
            }
            Pane pill = wear(holder, KIND_PILL, false);
            dress(holder, pill, false);
            if (strip.getTag(FLAT) == null) {
                strip.setTag(FLAT, Boolean.TRUE);
                try {
                    strip.getClass().getMethod("setSelectedIndicatorHeight", int.class).invoke(strip, 0);
                } catch (Throwable error) {
                    Diary.detail("liquid glass: the line under the tabs stays: " + error);
                }
            }
            tabs = new WeakReference<ViewGroup>(strip);
            tabChosen = - 2;
        }
    }

    public static void attach(Activity activity) {
        try {
            String name = activity.getClass().getName();
            if (!name.endsWith(".SplashActivity") && !name.endsWith(".MainActivity")) return;
            View root = activity.getWindow().getDecorView();
            Object known = root.getTag(WATCHED);
            Lens lens;
            if (known instanceof Lens) {
                lens = (Lens) known;
            } else {
                lens = new Lens(root);
                root.setTag(WATCHED, lens);
                root.getViewTreeObserver().addOnGlobalLayoutListener(lens);
                root.getViewTreeObserver().addOnPreDrawListener(lens);
            }
            lens.tabChosen = lens.feedChosen = - 2;
            root.post(lens.later);
        } catch (Throwable error) {
            Diary.detail("liquid glass: " + error);
        }
    }
}

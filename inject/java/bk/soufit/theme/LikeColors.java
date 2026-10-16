package bk.soufit.theme;
import bk.soufit.profile.AccountAppearance;
import bk.soufit.core.Names;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
public final class LikeColors {
    private LikeColors() {} public static final String KEY = "like_color";
    public static final String COMMENT_KEY = "comment_like_color";
    private static volatile int chosen;
    private static volatile int commentChosen;
    public static int colour() {
        return Accent.colour();
    }

    public static void set(int colour) {
        chosen = colour == 0 ? Accent.TIKTOK : 0xFF000000 | (colour & 0xFFFFFF);
        Context context = soufiT.context();
        if (context != null) AccountAppearance.prefs(context).edit().putInt(KEY, chosen).apply();
        Accent.forget();
        Accent.refreshLikeViews();
    }

    public static int commentColour() {
        return Accent.colour();
    }

    public static void setComment(int colour) {
        commentChosen = colour == 0 ? Accent.TIKTOK : 0xFF000000 | (colour & 0xFFFFFF);
        Context context = soufiT.context();
        if (context != null) AccountAppearance.prefs(context).edit().putInt(COMMENT_KEY, commentChosen).apply();
        Accent.forget();
        Accent.refreshLikeViews();
    }

    public static void reset() {
        set(Accent.TIKTOK);
        setComment(Accent.TIKTOK);
        Context context = soufiT.context();
        if (context != null) AccountAppearance.prefs(context).edit().remove(KEY).remove(COMMENT_KEY).apply();
        chosen = 0;
        commentChosen = 0;
    }

    public static void reload() {
        chosen = 0;
        commentChosen = 0;
    }

    public static int forView(View view, int source, int fallback) {
        if (!isLikeView(view)) return fallback;
        String name = resourceName(view);
        int target = name.contains("comment") ? commentColour() : colour();
        int mapped = Palette.map(source, Accent.TIKTOK, target);
        if ((view.isSelected() || view.isActivated()) && mapped == source) return (source & 0xFF000000) | (target & 0x00FFFFFF);
        return mapped == source ? fallback : mapped;
    }

    private static final java.util.WeakHashMap<android.widget.TextView, Mark> painted = new java.util.WeakHashMap<>();
    private static final java.util.WeakHashMap<android.widget.TextView, android.content.res.ColorStateList> originals = new java.util.WeakHashMap<>();
    public static boolean liked(int source) {
        return Palette.captures(source, Accent.TIKTOK) || (Accent.colour() != Accent.TIKTOK && Palette.captures(source, Accent.colour()));
    }

    public static boolean inComment(View view) {
        View at = view;
        for (int i = 0; at != null && i < 8; i ++) {
            try {
                if (Boolean.TRUE.equals(at.getTag(0x5454434D))) return true;
            } catch (Throwable ignored) {} if (resourceName(at).contains("comment")) return true;
            android.view.ViewParent parent = at.getParent();
            at = parent instanceof View ? (View) parent : null;
        }
        return false;
    }

    public static void syncCount(View heart, boolean isLiked) {
        try {
            if (heart == null || !isLikeView(heart) || resourceName(heart).contains("dislike") || !inComment(heart)) return;
            android.view.ViewParent parent = heart.getParent();
            android.view.ViewGroup likeControls = parent instanceof android.view.ViewGroup ? (android.view.ViewGroup) parent
                    : null;
            View commentCell = null;
            for (View at = heart; at != null;) {
                if (Boolean.TRUE.equals(at.getTag(0x5454434D))) {
                    commentCell = at;
                    break;
                }
                android.view.ViewParent next = at.getParent();
                at = next instanceof View ? (View) next : null;
            }
            android.view.ViewParent actionParent = parent;
            for (int level = 0; level<4 && actionParent instanceof android.view.ViewGroup; level ++) {
                android.view.ViewGroup actions = (android.view.ViewGroup) actionParent;
                if (actions == commentCell) break;
                android.view.ViewParent outer = actions.getParent();
                for (int i = 0; i<actions.getChildCount(); i ++) {
                    View item = actions.getChildAt(i);
                    if (item instanceof android.widget.TextView && item != heart && !(item instanceof android.widget.EditText)
                            && looksLikeCount((android.widget.TextView) item)) {
                        paintCount((android.widget.TextView) item, isLiked);
                        return;
                    }
                }
                if (outer instanceof android.view.ViewGroup) {
                    android.view.ViewGroup row = (android.view.ViewGroup) outer;
                    for (int i = 0; i<row.getChildCount(); i ++) {
                        View item = row.getChildAt(i);
                        if (item instanceof android.widget.TextView && item != heart && !(item instanceof android.widget.EditText)
                                && looksLikeCount((android.widget.TextView) item)) {
                            paintCount((android.widget.TextView) item, isLiked);
                            return;
                        }
                    }
                }
                actionParent = actions.getParent();
            }
            android.view.ViewParent ancestor = likeControls == null ? parent : likeControls.getParent();
            for (int level = 0; level < 8 && ancestor instanceof android.view.ViewGroup; level ++) {
                android.view.ViewGroup group = (android.view.ViewGroup) ancestor;
                if (group == commentCell) break;
                if (paintCounts(group, likeControls, isLiked)) return;
                ancestor = group.getParent();
            }
        } catch (Throwable ignored) {}
    }

    private static boolean paintCounts(View root, android.view.ViewGroup likeControls, boolean isLiked) {
        if (!(root instanceof android.view.ViewGroup)) return false;
        android.view.ViewGroup group = (android.view.ViewGroup) root;
        boolean found = false;
        for (int i = 0; i < group.getChildCount(); i ++) {
            View child = group.getChildAt(i);
            if (child instanceof android.widget.TextView && !(child instanceof android.widget.EditText) && looksLikeCount((android.widget.TextView) child)) {
                android.view.ViewParent owner = child.getParent();
                if (owner == likeControls || owner == root || (likeControls != null && owner == likeControls.getParent())
                        || resourceName(child).contains("like") || resourceName(child).contains("digg")) {
                    paintCount((android.widget.TextView) child, isLiked);
                    found = true;
                }
            } else if (child instanceof android.view.ViewGroup && child != likeControls) {
                android.view.ViewGroup wrapper = (android.view.ViewGroup) child;
                for (int n = 0; n < wrapper.getChildCount(); n ++) {
                    View nested = wrapper.getChildAt(n);
                    if (nested instanceof android.widget.TextView && !(nested instanceof android.widget.EditText) && looksLikeCount((android.widget.TextView) nested)
                            && (nested.getParent() == likeControls || likeControls == null)) {
                        paintCount((android.widget.TextView) nested, isLiked);
                        found = true;
                    }
                }
            }
        }
        return found;
    }

    private static boolean looksLikeCount(android.widget.TextView text) {
        CharSequence value = text.getText();
        return value != null && value.length() > 0 && value.length() < 12 && Character.isDigit(value.charAt(0));
    }

    private static final class Mark {
        public int colour;
        public final long until;
        public Mark(int colour, long until) {
            this.colour = colour;
            this.until = until;
        }
    }

    private static final long GUARD_MS = 1500L;
    private static void paintCount(final android.widget.TextView text, boolean isLiked) {
        synchronized (painted) {
            Mark mine = painted.get(text);
            boolean ours = mine != null && text.getCurrentTextColor() == mine.colour;
            if (isLiked) {
                if (!ours && !originals.containsKey(text)) originals.put(text, text.getTextColors());
                text.setTextColor(commentColour());
                Mark mark = new Mark(text.getCurrentTextColor(), android.os.SystemClock.uptimeMillis() + GUARD_MS);
                painted.put(text, mark);
                guard(text, mark);
            } else {
                if (ours) {
                    android.content.res.ColorStateList back = originals.get(text);
                    if (back != null) text.setTextColor(back);
                }
                painted.remove(text);
            }
        }
    }

    private static void guard(final android.widget.TextView text, final Mark mark) {
        final android.view.ViewTreeObserver tree = text.getViewTreeObserver();
        if (!tree.isAlive()) return;
        tree.addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                boolean current;
                synchronized (painted) {
                    current = painted.get(text) == mark;
                }
                if (!current || android.os.SystemClock.uptimeMillis() > mark.until) {
                    android.view.ViewTreeObserver now = text.getViewTreeObserver();
                    if (now.isAlive()) now.removeOnPreDrawListener(this);
                    return true;
                }
                if (text.getCurrentTextColor() != mark.colour) {
                    text.setTextColor(commentColour());
                    mark.colour = text.getCurrentTextColor();
                }
                return true;
            }
        });
    }

    public static boolean isLikeView(View view) {
        if (view == null) return false;
        String name = resourceName(view);
        return name.contains("like") || name.contains("heart") || name.contains("digg");
    }

    private static String resourceName(View view) {
        String name = Names.of(view);
        if (name.contains("avatar") || name.contains("photo") || name.contains("thumbnail")) return "";
        return name;
    }
}

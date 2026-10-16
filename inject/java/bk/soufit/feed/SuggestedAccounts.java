package bk.soufit.feed;
import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import bk.soufit.core.Diary;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
public final class SuggestedAccounts {
    private static final int PACE = 0x7e0d0344;
    private static final long GAP = 250L;
    private static final String RAIL_HEADER = "q3l", RAIL_TITLE = "u1d", RAIL_FOLLOW = "u96", RAIL_AVATAR = "ip9";
    private static final String PAGE_TITLE = "wm3", PAGE_LIST = "wlx";
    private static final String SUGGESTED_LABEL = "People you may know";
    private static final Map<View, Saved> hidden = new WeakHashMap<View, Saved>();
    private static int kept = -1;
    private SuggestedAccounts() {}

    private static final class Pace {
        long last;
        boolean queued;
    }

    private static final class Saved {
        final int visibility, height, top, bottom;

        Saved(View view) {
            visibility = view.getVisibility();
            ViewGroup.LayoutParams params = view.getLayoutParams();
            height = params == null ? 0 : params.height;
            top = params instanceof ViewGroup.MarginLayoutParams ? ((ViewGroup.MarginLayoutParams) params).topMargin : 0;
            bottom = params instanceof ViewGroup.MarginLayoutParams ? ((ViewGroup.MarginLayoutParams) params).bottomMargin : 0;
        }

        void restore(View view) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null) {
                params.height = height;
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) params).topMargin = top;
                    ((ViewGroup.MarginLayoutParams) params).bottomMargin = bottom;
                }
                view.setLayoutParams(params);
            }
            view.setVisibility(visibility);
        }
    }

    private static void collapse(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            boolean changed = params.height != 0;
            params.height = 0;
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) params;
                changed |= margins.topMargin != 0 || margins.bottomMargin != 0;
                margins.topMargin = 0;
                margins.bottomMargin = 0;
            }
            if (changed) view.setLayoutParams(params);
        }
        if (view.getVisibility() != View.GONE) view.setVisibility(View.GONE);
    }

    public static void watch(Activity activity) {
        if (activity == null || activity.getClass().getName().startsWith("bk.soufit.")) return;
        try {
            final View decor = activity.getWindow().getDecorView();
            if (decor.getTag(PACE) != null) return;
            final Pace pace = new Pace();
            decor.setTag(PACE, pace);
            decor.getViewTreeObserver().addOnGlobalLayoutListener(() -> soon(decor, pace));
            soon(decor, pace);
        } catch (Throwable error) {
            Diary.fail("suggested accounts", error);
        }
    }

    private static void soon(final View decor, final Pace pace) {
        long now = SystemClock.uptimeMillis();
        if (now - pace.last >= GAP) {
            pace.last = now;
            sweep(decor);
            return;
        }
        if (pace.queued) return;
        pace.queued = true;
        decor.postDelayed(() -> {
            pace.queued = false;
            soon(decor, pace);
        }, GAP - (now - pace.last) + 5);
    }

    public static void sweep(View root) {
        if (root == null) return;
        try {
            boolean on = Feed.hides(Feed.KEY_SUGGESTIONS);
            if (!on && hidden.isEmpty()) return;
            Set<View> wanted = on ? find(root) : Collections.<View>emptySet();
            for (Iterator<Map.Entry<View, Saved>> it = hidden.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<View, Saved> entry = it.next();
                View view = entry.getKey();
                if (wanted.contains(view) || !under(view, root)) continue;
                entry.getValue().restore(view);
                it.remove();
            }
            for (View view : wanted) {
                if (!hidden.containsKey(view)) hidden.put(view, new Saved(view));
                collapse(view);
            }
            if (wanted.size() != kept) {
                kept = wanted.size();
                if (kept > 0) Diary.note("suggested accounts: " + kept + " views kept hidden");
            }
        } catch (Throwable error) {
            Diary.fail("suggested accounts", error);
        }
    }

    private static Set<View> find(View root) {
        Set<View> out = Collections.newSetFromMap(new IdentityHashMap<View, Boolean>());
        collect(root, out);
        return out;
    }

    private static void collect(View view, Set<View> out) {
        String name = nameOf(view);
        if (RAIL_HEADER.equals(name) && view.getParent() instanceof ViewGroup && hasDescendant(view, RAIL_TITLE)) addRail(view, out);
        if (PAGE_TITLE.equals(name)) addPage(view, out);
        if (suggestionLabel(view)) addInboxCard(view, out);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i ++) collect(group.getChildAt(i), out);
        }
    }

    private static void addRail(View header, Set<View> out) {
        View wrapper = (View) header.getParent();
        if (!(wrapper.getParent() instanceof ViewGroup)) return;
        ViewGroup page = (ViewGroup) wrapper.getParent();
        out.add(wrapper);
        int at = indexOf(page, wrapper);
        for (int i = at + 1; at >= 0 && i < page.getChildCount(); i ++) {
            View card = page.getChildAt(i);
            if (!(card instanceof LinearLayout)) break;
            if (!(hasDescendant(card, RAIL_FOLLOW) || hasChild(card, RAIL_AVATAR))) break;
            out.add(card);
        }
    }

    private static boolean suggestionLabel(View view) {
        if (!(view instanceof TextView)) return false;
        CharSequence text = ((TextView) view).getText();
        return text != null && SUGGESTED_LABEL.equals(text.toString().trim());
    }

    private static void addInboxCard(View label, Set<View> out) {
        View at = label;
        for (int depth = 0; depth < 4 && at.getParent() instanceof View; depth ++) {
            View parent = (View) at.getParent();
            if (parent instanceof ViewGroup && (hasChild(parent, RAIL_AVATAR) || hasChild(parent, RAIL_FOLLOW))) {
                out.add(parent);
                return;
            }
            at = parent;
        }
    }

    private static void addPage(View title, Set<View> out) {
        if (!(title.getParent() instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) title.getParent();
        View list = null;
        for (int i = 0; i < group.getChildCount(); i ++) {
            if (PAGE_LIST.equals(nameOf(group.getChildAt(i)))) list = group.getChildAt(i);
        }
        if (list == null) return;
        out.add(title);
        out.add(list);
    }

    private static int indexOf(ViewGroup group, View child) {
        for (int i = 0; i < group.getChildCount(); i ++) if (group.getChildAt(i) == child) return i;
        return -1;
    }

    private static boolean hasChild(View view, String name) {
        if (!(view instanceof ViewGroup)) return false;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i ++) if (name.equals(nameOf(group.getChildAt(i)))) return true;
        return false;
    }

    private static boolean hasDescendant(View view, String name) {
        if (!(view instanceof ViewGroup)) return false;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i ++) {
            View child = group.getChildAt(i);
            if (name.equals(nameOf(child)) || hasDescendant(child, name)) return true;
        }
        return false;
    }

    private static boolean under(View view, View root) {
        for (View at = view; at != null; ) {
            if (at == root) return true;
            at = at.getParent() instanceof View ? (View) at.getParent() : null;
        }
        return false;
    }

    private static String nameOf(View view) {
        int id = view.getId();
        if (id <= 0 || view.getResources() == null) return null;
        try {
            return view.getResources().getResourceEntryName(id);
        } catch (Throwable ignored) {
            return null;
        }
    }
}

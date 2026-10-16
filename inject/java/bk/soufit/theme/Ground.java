package bk.soufit.theme;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
public final class Ground {
    private Ground() {}

    public static float bright(int colour) {
        return (0.299f * Color.red(colour) + 0.587f * Color.green(colour) + 0.114f * Color.blue(colour)) / 255f;
    }

    public static int mix(int from, int to, float how) {
        if (how <= 0f) return from;
        if (how >= 1f) return to;
        return Color.argb(255, Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * how), Math.round(Color.green(from)
                + (Color.green(to) - Color.green(from)) * how), Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * how));
    }

    public static int behind(View view) {
        Object parent = view.getParent();
        for (int i = 0; i < 28 && parent instanceof View; i ++) {
            Drawable back = ((View) parent).getBackground();
            if (back instanceof android.graphics.drawable.ColorDrawable) {
                int colour = ((android.graphics.drawable.ColorDrawable) back).getColor();
                if (android.graphics.Color.alpha(colour) == 255) return colour;
            }
            parent = ((View) parent).getParent();
        }
        if (Themes.isEnabled()) return Themes.backgroundInUse() | 0xFF000000;
        return Themes.isDark() ? 0xFF121212 : 0xFFFFFFFF;
    }

    public static int under(View view) {
        try {
            int[] at = new int[2];
            view.getLocationOnScreen(at);
            int x = at[0] + view.getWidth() / 2, y = at[1] + view.getHeight() / 2;
            int[] left = {600, Math.round(view.getWidth() * 0.8f)};
            View child = view;
            Object parent = view.getParent();
            for (int level = 0; level < 28 && parent instanceof ViewGroup; level ++) {
                ViewGroup group = (ViewGroup) parent;
                for (int i = group.indexOfChild(child) - 1; i >= 0; i --) {
                    int found = painted(group.getChildAt(i), x, y, 0, left, at);
                    if (found != 0) return found;
                }
                Drawable back = group.getBackground();
                if (back instanceof android.graphics.drawable.ColorDrawable) {
                    int colour = ((android.graphics.drawable.ColorDrawable) back).getColor();
                    if (android.graphics.Color.alpha(colour) == 255) return colour;
                }
                child = group;
                parent = group.getParent();
            }
        } catch (Throwable ignored) {} return behind(view);
    }

    private static int painted(View view, int x, int y, int depth, int[] left, int[] at) {
        if (view == null || depth > 24 || left[0] -- <= 0) return 0;
        if (view.getVisibility() != View.VISIBLE || view.getAlpha() < 0.9f || view.getWidth() == 0) return 0;
        view.getLocationOnScreen(at);
        if (x < at[0] || y < at[1] || x >= at[0] + view.getWidth() || y >= at[1] + view.getHeight()) return 0;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = group.getChildCount() - 1; i >= 0; i --) {
                int found = painted(group.getChildAt(i), x, y, depth + 1, left, at);
                if (found != 0) return found;
            }
        }
        Drawable back = view.getBackground();
        if (view.getWidth() >= left[1] && back instanceof android.graphics.drawable.ColorDrawable) {
            int colour = ((android.graphics.drawable.ColorDrawable) back).getColor();
            if (android.graphics.Color.alpha(colour) == 255) return colour;
        }
        return 0;
    }

    public static int ink(int ground) {
        boolean dark = bright(ground) < 0.5f;
        int ink = dark ? 0xFFFFFFFF : 0xFF161823;
        if (Themes.isEnabled()) {
            int chosen = Themes.text() | 0xFF000000;
            if ((bright(chosen) < 0.5f) != dark) ink = chosen;
        }
        return ink;
    }
}

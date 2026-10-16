package bk.soufit.theme;
import bk.soufit.core.Diary;
import bk.soufit.core.soufiT;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;
public final class Corners {
    private Corners() {} public static final String KEY = "round_cards";
    private static final float RADIUS = 8f;
    private static volatile Boolean cached;
    public static void reloadSettings() {
        cached = null;
    }

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

    private static int cover;
    private static final ViewOutlineProvider ROUND = new ViewOutlineProvider() {
        @Override public void getOutline(View view, Outline outline) {
            int wide = view.getWidth();
            int tall = view.getHeight();
            float radius = 0f;
            try {
                if (isEnabled() && holdsCover(view)) {
                    radius = RADIUS * view.getResources().getDisplayMetrics().density;
                }
            } catch (Throwable ignored) {} outline.setRoundRect(0, 0, wide, tall, radius);
        }
    };

    private static boolean holdsCover(View cell) {
        if (cover == 0) {
            cover = cell.getResources().getIdentifier("cover", "id", cell.getContext().getPackageName());
            if (cover == 0) cover = - 1;
        }
        return cover > 0 && cell.findViewById(cover) != null;
    }

    public static void cell(View cell) {
        try {
            if (cell == null || cell.getOutlineProvider() == ROUND) return;
            cell.setOutlineProvider(ROUND);
            cell.setClipToOutline(true);
            cell.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override public void onLayoutChange(View view, int l, int t, int r, int b, int ol, int ot, int or, int ob) {
                    view.invalidateOutline();
                }
            });
        } catch (Throwable error) {
            Diary.note("corners: " + error);
        }
    }
}

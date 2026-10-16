package bk.soufit.ui;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import bk.soufit.core.Diagnostics;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public final class ScreenDump {
    private static final int VIEWS = 1500, DEPTH = 40, TEXT = 200;
    private ScreenDump() {}
    public static String capture(Activity activity, View skip) {
        StringBuilder out = new StringBuilder();
        out.append("== soufiT screen dump ==\n");
        out.append("time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(new Date())).append('\n');
        out.append("screen: ").append(activity == null ? "none" : activity.getClass().getName()).append('\n');
        if (activity != null) {
            try {
                View root = activity.getWindow().getDecorView();
                out.append("window: ").append(root.getWidth()).append('x').append(root.getHeight()).append('\n');
                out.append("\n== view tree ==\n");
                int[] budget = {VIEWS};
                tree(root, skip, 0, budget, out);
                if (budget[0] <= 0) out.append("(stopped after ").append(VIEWS).append(" views)\n");
            } catch (Throwable error) {
                out.append("view tree failed: ").append(error).append('\n');
            }
        }
        out.append('\n').append(Diagnostics.fullReport());
        return out.toString();
    }

    private static void tree(View view, View skip, int depth, int[] budget, StringBuilder out) {
        if (view == null || view == skip || depth > DEPTH || budget[0] <= 0) return;
        budget[0] --;
        for (int i = 0; i < depth; i ++) out.append("  ");
        out.append(view.getClass().getSimpleName());
        String id = idName(view);
        if (id != null) out.append(" #").append(id);
        out.append(' ').append(view.getWidth()).append('x').append(view.getHeight()).append(" at ").append(view.getLeft()).append(',')
                .append(view.getTop());
        if (view.getVisibility() == View.GONE) out.append(" gone");
        else if (view.getVisibility() == View.INVISIBLE) out.append(" invisible");
        if (view.getAlpha() < 1f) out.append(" alpha ").append(Math.round(view.getAlpha() * 100)).append('%');
        if (view.isClickable()) out.append(" clickable");
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null && text.length() > 0) out.append(" text=\"").append(clip(text)).append('"');
        }
        CharSequence description = view.getContentDescription();
        if (description != null && description.length() > 0) out.append(" desc=\"").append(clip(description)).append('"');
        out.append('\n');
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount() && budget[0] > 0; i ++) tree(group.getChildAt(i), skip, depth + 1, budget, out);
        }
    }

    private static String clip(CharSequence text) {
        String shown = text.toString().replace('\n', ' ');
        return shown.length() > TEXT ? shown.substring(0, TEXT) + "…" : shown;
    }

    private static String idName(View view) {
        int id = view.getId();
        if (id <= 0) return null;
        try {
            return view.getResources().getResourceEntryName(id);
        } catch (Throwable ignored) {
            return Integer.toHexString(id);
        }
    }
}

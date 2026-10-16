package bk.soufit.core;
import bk.soufit.theme.Accent;
import bk.soufit.theme.Nightly;
import bk.soufit.theme.Themes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
public final class ShareProbe {
    private ShareProbe() {} private static int told;
    public static void look(View view) {
        if (!DevMode.isOn()) return;
        if (told >= 3 || !(view instanceof TextView)) return;
        CharSequence said = ((TextView) view).getText();
        if (said == null || said.length()<4 || said.length()>14) return;
        String text = said.toString();
        if (!(text.equals("Жалоба") || text.equals("Скачать") || text.equals("Неинтересно") || text.equals("Report") || text.equals("Download"))) return;
        told ++;
        android.util.Log.e("soufiT", "share cell: theme dark " + Themes.isDark() + " text " + Integer.toHexString(Themes.text())
                + " ground " + Integer.toHexString(Themes.backgroundInUse()));
        describe(view.getParent() instanceof View ? (View) view.getParent() : view, 0);
    }

    private static String filterColour(android.graphics.ColorFilter filter) {
        try {
            java.lang.reflect.Method colour = filter.getClass().getMethod("getColor");
            return " " + Integer.toHexString((Integer) colour.invoke(filter));
        } catch (Throwable hidden) {
            return "";
        }
    }

    public static void describe(View view, int depth) {
        if (!DevMode.isOn()) return;
        if (view == null || depth>4) return;
        StringBuilder out = new StringBuilder("share cell: ");
        for (int i = 0; i<depth; i ++) out.append("  ");
        out.append(view.getClass().getName()).append(" alpha ").append(view.getAlpha()).append(" dark ").append(Themes.isDark());
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            out.append(" text '").append(text.getText()).append("' colour ").append(Integer.toHexString(text.getCurrentTextColor()))
                    .append(" owned ").append(Nightly.owns(text.getCurrentTextColor())).append(" was ").append(Accent.sourceOf(text))
                    .append(" paint alpha ").append(text.getPaint().getAlpha());
        }
        if (view instanceof android.widget.ImageView) {
            android.widget.ImageView image = (android.widget.ImageView) view;
            out.append(" drawable ").append(image.getDrawable() == null ? "none" : image.getDrawable().getClass().getName())
                    .append(" tint ").append(image.getImageTintList() == null ? "none" : Integer.toHexString(image.getImageTintList()
                    .getDefaultColor())).append(" filter ").append(image.getColorFilter() == null ? "none" : image.getColorFilter()
                    .getClass().getSimpleName() + filterColour(image.getColorFilter())).append(" image alpha ").append(image.getImageAlpha());
        }
        android.graphics.drawable.Drawable back = view.getBackground();
        if (back != null) {
            out.append(" background ").append(back.getClass().getName());
            if (back instanceof android.graphics.drawable.ColorDrawable) out.append(' ').append(Integer.toHexString(((android.graphics.drawable.ColorDrawable) back)
                    .getColor()));
            if (back instanceof android.graphics.drawable.GradientDrawable && android.os.Build.VERSION.SDK_INT >= 24 && ((android.graphics.drawable.GradientDrawable) back)
                    .getColor() != null) out.append(' ').append(Integer.toHexString(((android.graphics.drawable.GradientDrawable) back)
                    .getColor().getDefaultColor()));
        }
        android.util.Log.e("soufiT", out.toString());
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i<group.getChildCount(); i ++) describe(group.getChildAt(i), depth + 1);
        }
    }
}

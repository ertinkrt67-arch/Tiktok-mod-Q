package bk.soufit.profile;
import bk.soufit.core.soufiT;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.TextView;
public final class Showcase {
    private Showcase() {} public static final String KEY_BADGE = "showcase_badge";
    public static final String KEY_GRADIENT = "showcase_gradient";
    public static final String NONE = "none";
    private static final String GRADIENT = "g";
    public static String badgeChoice() {
        return stored(KEY_BADGE);
    }

    public static String gradientChoice() {
        return stored(KEY_GRADIENT);
    }

    public static void setBadge(bk.soufit.badges.model.Badge badge) {
        put(KEY_BADGE, badge == null ? NONE : badge.id);
    }

    public static void setGradient(int[] colours) {
        put(KEY_GRADIENT, colours == null ? NONE : keyOf(colours));
    }

    public static bk.soufit.badges.model.Badge badgeFor(bk.soufit.badges.model.Badge[] every, bk.soufit.badges.model.Badge[] held, String choice) {
        if (NONE.equals(choice)) return null;
        if (choice != null && every != null) {
            for (bk.soufit.badges.model.Badge badge : every) {
                if (badge != null && choice.equals(badge.id)) return badge;
            }
        }
        if (held != null && held.length > 0 && held[0] != null) return held[0];
        if (every != null && every.length > 0) return every[0];
        return null;
    }

    public static int[] coloursFor(String choice, int[] own) {
        if (choice == null) return own;
        if (NONE.equals(choice)) return null;
        if (choice.startsWith(GRADIENT)) {
            int[] picked = parse(choice);
            if (picked != null) return picked;
        }
        return own;
    }

    public static String keyOf(int[] colours) {
        StringBuilder out = new StringBuilder(GRADIENT);
        for (int i = 0; i < colours.length; i ++) {
            if (i > 0) out.append(',');
            out.append(String.format("%06X", colours[i] & 0xFFFFFF));
        }
        return out.toString();
    }

    private static int[] parse(String key) {
        try {
            String[] parts = key.substring(GRADIENT.length()).split(",");
            if (parts.length < 2) return null;
            int[] colours = new int[parts.length];
            for (int i = 0; i < parts.length; i ++) {
                colours[i] = 0xFF000000 | Integer.parseInt(parts[i], 16);
            }
            return colours;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static boolean owned(bk.soufit.badges.model.Badge badge, bk.soufit.badges.model.Badge[] held) {
        if (badge == null || held == null) return false;
        for (bk.soufit.badges.model.Badge one : held) {
            if (one != null && badge.id.equals(one.id)) return true;
        }
        return false;
    }

    public static void draw(TextView view, String name, bk.soufit.badges.model.Badge badge, int[] colours) {
        SpannableStringBuilder text = new SpannableStringBuilder(name);
        if (badge != null) {
            Drawable picture = Badge.picture(view, badge);
            if (picture != null) {
                text.append(' ');
                int mark = text.length();
                text.append('￼');
                text.setSpan(new ImageSpan(picture, ImageSpan.ALIGN_BOTTOM), mark, mark + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        view.setText(text);
        if (colours != null) Looks.paint(view, colours);
    }

    private static String stored(String key) {
        try {
            android.content.SharedPreferences prefs = prefs();
            if (prefs == null) return null;
            return prefs.getString(key, null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void put(String key, String value) {
        try {
            android.content.SharedPreferences prefs = prefs();
            if (prefs != null) prefs.edit().putString(key, value).apply();
        } catch (Throwable ignored) {}
    }

    private static android.content.SharedPreferences prefs() {
        android.content.Context context = soufiT.context();
        if (context == null) return null;
        return context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE);
    }
}

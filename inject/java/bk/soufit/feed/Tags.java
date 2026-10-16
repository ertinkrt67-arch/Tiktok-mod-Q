package bk.soufit.feed;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.SharedPreferences;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public final class Tags {
    public static final String KEY = "blocked_tags";
    public static final String KEY_ON = "blocked_tags_on";
    public static final int MOST = 40;
    private static volatile List<String> cached;
    private Tags() {} private static volatile Boolean on;
    public static boolean isEnabled() {
        Boolean known = on;
        if (known != null) return known.booleanValue();
        SharedPreferences prefs = prefs();
        if (prefs == null) return true;
        boolean value = prefs.getBoolean(KEY_ON, true);
        on = Boolean.valueOf(value);
        return value;
    }

    public static void setEnabled(boolean enabled) {
        on = Boolean.valueOf(enabled);
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY_ON, enabled).apply();
    }

    public static List<String> all() {
        List<String> known = cached;
        if (known != null) return new ArrayList<String>(known);
        SharedPreferences prefs = prefs();
        String saved = prefs == null ? "" : prefs.getString(KEY, "");
        List<String> out = new ArrayList<String>();
        for (String one : saved.split(",")) {
            String tag = tidy(one);
            if (tag.length() > 0 && !out.contains(tag)) out.add(tag);
        }
        cached = out;
        return new ArrayList<String>(out);
    }

    public static boolean add(String tag) {
        String clean = tidy(tag);
        if (clean.length() == 0) return false;
        List<String> now = all();
        if (now.contains(clean) || now.size() >= MOST) return false;
        now.add(clean);
        keep(now);
        return true;
    }

    public static void remove(String tag) {
        List<String> now = all();
        now.remove(tidy(tag));
        keep(now);
    }

    private static void keep(List<String> tags) {
        cached = new ArrayList<String>(tags);
        SharedPreferences prefs = prefs();
        if (prefs == null) return;
        StringBuilder out = new StringBuilder();
        for (String tag : tags) {
            if (out.length() > 0) out.append(',');
            out.append(tag);
        }
        prefs.edit().putString(KEY, out.toString()).apply();
    }

    public static String tidy(String tag) {
        if (tag == null) return "";
        String out = tag.trim().toLowerCase(Locale.ROOT);
        while (out.startsWith("#")) out = out.substring(1);
        return out.replace(" ", "");
    }

    public static boolean blocks(Aweme post) {
        if (!isEnabled()) return false;
        List<String> tags = all();
        if (tags.isEmpty()) return false;
        try {
            String said = post.getDesc();
            if (said == null || said.length() == 0) return false;
            String low = said.toLowerCase(Locale.ROOT);
            for (String tag : tags) {
                if (carries(low, tag)) return true;
            }
        } catch (Throwable ignored) {} return false;
    }

    private static boolean carries(String text, String tag) {
        int at = 0;
        while ((at = text.indexOf('#', at)) >= 0) {
            at ++;
            int end = at;
            while (end < text.length() && letterish(text.charAt(end))) end ++;
            if (end - at == tag.length() && text.regionMatches(at, tag, 0, tag.length())) {
                return true;
            }
            at = end;
        }
        return false;
    }

    private static boolean letterish(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static SharedPreferences prefs() {
        Context context = soufiT.context();
        if (context == null) return null;
        try {
            return context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
        } catch (Throwable ignored) {
            return null;
        }
    }
}

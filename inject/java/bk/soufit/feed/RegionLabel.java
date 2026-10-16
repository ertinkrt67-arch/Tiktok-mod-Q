package bk.soufit.feed;
import bk.soufit.core.DevMode;
import bk.soufit.core.Diary;
import bk.soufit.core.Flags;
import bk.soufit.core.NativeRead;
import java.lang.reflect.Method;
import java.util.Locale;
public final class RegionLabel {
    public static final String KEY = "show_region_language";
    private static final String[] REGION = {"getRegion", "getCountry", "getCountryCode"};
    private static final String[] LANGUAGE = {"getLanguage", "getDescLanguage", "getDescriptionLanguage"};
    private static boolean reported;
    private RegionLabel() {} public static boolean isEnabled() {
        return Flags.isOn(KEY);
    }

    public static void setEnabled(boolean on) {
        Flags.set(KEY, on);
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> DateOverlay.update());
    }

    public static String of(Object post) {
        if (post == null) return "";
        Object author = NativeRead.get(post, "getAuthor");
        String region = first(REGION, author, post, true);
        String language = first(LANGUAGE, author, post, false);
        if (region.isEmpty() && language.isEmpty()) {
            report(author, post);
            return "";
        }
        if (region.isEmpty()) return language;
        if (language.isEmpty()) return region;
        return region + " · " + language;
    }

    public static String with(String date, Object post) {
        if (!isEnabled()) return date;
        String line = of(post);
        if (line.isEmpty()) return date;
        if (!Dates.isEnabled() || date == null) return line;
        return date + " · " + line;
    }

    private static String first(String[] names, Object author, Object post, boolean region) {
        for (String name : names) {
            String value = clean(NativeRead.string(NativeRead.get(author, name)), region);
            if (value.isEmpty()) value = clean(NativeRead.string(NativeRead.get(post, name)), region);
            if (!value.isEmpty()) return value;
        }
        return "";
    }

    private static String clean(String raw, boolean region) {
        String value = raw == null ? "" : raw.trim();
        if (region) return value.matches("[A-Za-z]{2}") ? value.toUpperCase(Locale.ROOT) : "";
        return value.matches("[A-Za-z]{2,3}([-_][A-Za-z0-9]{2,8})?") ? value : "";
    }

    private static void report(Object author, Object post) {
        if (reported || !DevMode.isOn()) return;
        reported = true;
        Diary.note("region: none found; author accessors: " + accessors(author) + "; post accessors: " + accessors(post));
    }

    private static String accessors(Object object) {
        if (object == null) return "none";
        StringBuilder out = new StringBuilder();
        for (Method method : object.getClass().getMethods()) {
            if (method.getParameterTypes().length != 0) continue;
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (!(name.contains("region") || name.contains("countr") || name.contains("lang") || name.contains("locale"))) continue;
            if (out.length() > 0) out.append(", ");
            out.append(method.getName());
        }
        return out.length() == 0 ? "none" : out.toString();
    }
}

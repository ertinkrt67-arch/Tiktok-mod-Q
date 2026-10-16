package bk.soufit.badges.source;

import bk.soufit.badges.model.BadgeGradient;
import java.util.Arrays;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

final class Wire {
    private Wire() {}

    static Integer colour(String text) {
        if (text == null) return null;
        String hex = text.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6) return null;
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static int[] colours(JSONArray list) {
        if (list == null) return null;
        int[] out = new int[list.length()];
        int count = 0;
        for (int i = 0; i < list.length(); i++) {
            Integer value = colour(list.optString(i, ""));
            if (value != null) out[count++] = value;
        }
        return count >= 2 ? Arrays.copyOf(out, count) : null;
    }

    static String localised(JSONObject object, String field) {
        String translated = object.optString(field + "_" + Locale.getDefault().getLanguage(), "");
        return translated.isEmpty() ? object.optString(field, "") : translated;
    }

    static String pick(JSONObject labels) {
        if (labels == null) return "";
        String value = labels.optString(Locale.getDefault().getLanguage(), "");
        return value.isEmpty() ? labels.optString("en", "") : value;
    }

    static BadgeGradient.Type type(String name) {
        if ("radial".equalsIgnoreCase(name)) return BadgeGradient.Type.RADIAL;
        if ("sweep".equalsIgnoreCase(name)) return BadgeGradient.Type.SWEEP;
        return BadgeGradient.Type.LINEAR;
    }

    static String stripSuffix(String address, String suffix) {
        return address.endsWith(suffix) ? address.substring(0, address.length() - suffix.length()) : address;
    }

    static String join(String base, String path) {
        String head = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return head + "/" + path;
    }
}

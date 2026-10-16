package bk.soufit.badges.source;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import org.json.JSONArray;
import org.json.JSONObject;

public final class SourceThreeAdapter implements SourceAdapter {
    private static final int PRIORITY = 30;

    @Override public Badge.Origin origin() { return Badge.Origin.SOURCE_THREE; }
    @Override public String endpoint() { return Endpoints.three; }

    @Override public SourceSnapshot adapt(JSONObject root) {
        Collector out = new Collector(origin());
        JSONArray rows = root.optJSONArray("assignments");
        for (int i = 0; rows != null && i < rows.length(); i++) {
            JSONObject row = rows.optJSONObject(i);
            if (row == null) continue;
            String user = row.optString("user", "");
            String code = row.optString("code", "");
            if (user.isEmpty() || code.isEmpty()) continue;
            String key = "three:" + code;
            out.badge(new Badge(key, origin(), icon(row.optString("icon_path", "")), 0,
                    row.optString("name", ""), row.optString("desc", ""), row.optString("button", "OK"),
                    null, row.optInt("rank", PRIORITY), false));
            out.owns(user, key);
            out.gradient(user, gradientOf(row.optJSONArray("gradient")));
        }
        return out.build(0, 0);
    }

    private static BadgeGradient gradientOf(JSONArray stops) {
        int[] colours = Wire.colours(stops);
        return colours == null ? null : new BadgeGradient(colours, 0f, BadgeGradient.Type.LINEAR);
    }

    private static String icon(String path) {
        if (path.isEmpty()) return "";
        if (path.startsWith("http")) return path;
        return Wire.join(Endpoints.three, "icons/" + path);
    }
}

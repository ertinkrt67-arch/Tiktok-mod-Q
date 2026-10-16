package bk.soufit.badges.source;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public final class SourceOneAdapter implements SourceAdapter {
    private static final int PRIORITY = 10;

    @Override public Badge.Origin origin() { return Badge.Origin.SOURCE_ONE; }
    @Override public String endpoint() { return Endpoints.one; }

    @Override public SourceSnapshot adapt(JSONObject root) {
        Collector out = new Collector(origin());
        String base = Wire.stripSuffix(Endpoints.one, "/badges");

        JSONArray list = root.optJSONArray("badges");
        for (int i = 0; list != null && i < list.length(); i++) {
            JSONObject raw = list.optJSONObject(i);
            if (raw == null) continue;
            String key = "one:" + raw.optString("id", "badge" + i);
            Integer colour = Wire.colour(raw.optString("colour", ""));
            out.badge(new Badge(key, origin(), icon(base, raw.optString("image", "")),
                    colour == null ? 0 : 0xFF000000 | colour,
                    Wire.localised(raw, "title"), Wire.localised(raw, "text"), Wire.localised(raw, "button"),
                    null, PRIORITY, false));
            JSONArray users = raw.optJSONArray("users");
            for (int u = 0; users != null && u < users.length(); u++) {
                out.owns(users.optString(u, ""), key);
            }
        }

        JSONObject gradients = root.optJSONObject("gradients");
        if (gradients != null) {
            Iterator<String> uids = gradients.keys();
            while (uids.hasNext()) {
                String uid = uids.next();
                int[] colours = Wire.colours(gradients.optJSONArray(uid));
                if (colours != null) out.gradient(uid, new BadgeGradient(colours, 0f, BadgeGradient.Type.LINEAR));
            }
        }
        return out.build(root.optLong("free_until", 0), root.optLong("now", 0));
    }

    private static String icon(String base, String image) {
        if (image.isEmpty()) return "";
        if (image.startsWith("http")) return image;
        return base + "/icon/" + image;
    }
}

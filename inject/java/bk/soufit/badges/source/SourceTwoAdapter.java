package bk.soufit.badges.source;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

public final class SourceTwoAdapter implements SourceAdapter {
    private static final int PRIORITY = 20;

    @Override public Badge.Origin origin() { return Badge.Origin.SOURCE_TWO; }
    @Override public String endpoint() { return Endpoints.two; }

    @Override public SourceSnapshot adapt(JSONObject root) {
        Collector out = new Collector(origin());

        JSONArray items = root.optJSONArray("items");
        for (int i = 0; items != null && i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;
            String key = item.optString("key", "");
            if (key.isEmpty()) continue;
            Integer tint = Wire.colour(item.optString("tint", ""));
            Badge badge = new Badge("two:" + key, origin(), item.optString("icon", ""),
                    tint == null ? 0 : 0xFF000000 | tint,
                    Wire.pick(item.optJSONObject("label")), Wire.pick(item.optJSONObject("info")),
                    item.optString("action", ""), null, item.optInt("rank", PRIORITY), false);
            out.badge(badge);
            JSONArray holders = item.optJSONArray("holders");
            for (int h = 0; holders != null && h < holders.length(); h++) {
                JSONObject holder = holders.optJSONObject(h);
                if (holder != null) out.owns(holder.optString("uid", ""), badge.id);
            }
        }

        JSONObject profiles = root.optJSONObject("profiles");
        if (profiles != null) {
            Iterator<String> uids = profiles.keys();
            while (uids.hasNext()) {
                String uid = uids.next();
                JSONObject profile = profiles.optJSONObject(uid);
                JSONObject gradient = profile == null ? null : profile.optJSONObject("gradient");
                if (gradient == null) continue;
                int[] colours = Wire.colours(gradient.optJSONArray("colors"));
                if (colours == null) continue;
                out.gradient(uid, new BadgeGradient(colours, (float) gradient.optDouble("angle", 0),
                        Wire.type(gradient.optString("type", "linear"))));
            }
        }
        return out.build(0, 0);
    }
}

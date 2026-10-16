package bk.soufit.badges.source;

import bk.soufit.badges.model.Badge;
import org.json.JSONObject;

public interface SourceAdapter {
    Badge.Origin origin();

    String endpoint();

    SourceSnapshot adapt(JSONObject root);
}

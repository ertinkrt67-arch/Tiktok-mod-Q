package bk.soufit.badges.source;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SourceSnapshot {
    public final Badge.Origin origin;
    public final List<Badge> badges;
    public final Map<String, List<String>> owners;
    public final Map<String, BadgeGradient> gradients;
    public final long freeUntil;
    public final long serverNow;

    public SourceSnapshot(Badge.Origin origin, List<Badge> badges, Map<String, List<String>> owners,
                          Map<String, BadgeGradient> gradients, long freeUntil, long serverNow) {
        this.origin = origin;
        this.badges = Collections.unmodifiableList(new ArrayList<>(badges));
        this.owners = Collections.unmodifiableMap(new LinkedHashMap<>(owners));
        this.gradients = Collections.unmodifiableMap(new LinkedHashMap<>(gradients));
        this.freeUntil = freeUntil;
        this.serverNow = serverNow;
    }
}

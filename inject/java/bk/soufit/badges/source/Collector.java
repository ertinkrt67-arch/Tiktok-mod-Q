package bk.soufit.badges.source;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Collector {
    private final Badge.Origin origin;
    private final Map<String, Badge> badges = new LinkedHashMap<>();
    private final Map<String, Set<String>> owners = new LinkedHashMap<>();
    private final Map<String, BadgeGradient> gradients = new LinkedHashMap<>();

    Collector(Badge.Origin origin) {
        this.origin = origin;
    }

    void badge(Badge badge) {
        if (!badges.containsKey(badge.id)) badges.put(badge.id, badge);
    }

    void owns(String uid, String badgeId) {
        if (uid == null || uid.isEmpty()) return;
        Set<String> set = owners.get(uid);
        if (set == null) {
            set = new LinkedHashSet<>();
            owners.put(uid, set);
        }
        set.add(badgeId);
    }

    void gradient(String uid, BadgeGradient gradient) {
        if (uid == null || uid.isEmpty() || gradient == null) return;
        if (!gradients.containsKey(uid)) gradients.put(uid, gradient);
    }

    SourceSnapshot build(long freeUntil, long serverNow) {
        Map<String, List<String>> lists = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : owners.entrySet()) {
            lists.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return new SourceSnapshot(origin, new ArrayList<>(badges.values()), lists, gradients, freeUntil, serverNow);
    }
}

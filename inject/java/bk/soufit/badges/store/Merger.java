package bk.soufit.badges.store;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import bk.soufit.badges.model.UserBadgeProfile;
import bk.soufit.badges.source.CreatorBadgeProvider;
import bk.soufit.badges.source.SourceSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Merger {
    private Merger() {}

    public static Snapshot merge(List<SourceSnapshot> sources) {
        Map<String, Badge> badges = new HashMap<>();
        Map<String, Set<String>> owners = new HashMap<>();
        Map<String, BadgeGradient> gradients = new HashMap<>();
        long freeUntil = 0;
        long serverNow = 0;

        for (SourceSnapshot source : sources) {
            if (source == null) continue;
            boolean trusted = source.origin == Badge.Origin.CREATOR;

            if (source.freeUntil > 0 && source.serverNow > 0) {
                freeUntil = source.freeUntil;
                serverNow = source.serverNow;
            }
            for (Badge badge : source.badges) {
                if (badge == null || badges.containsKey(badge.id)) continue;
                badges.put(badge.id, badge);
            }
            for (Map.Entry<String, List<String>> entry : source.owners.entrySet()) {
                String uid = entry.getKey();
                if (!trusted && CreatorBadgeProvider.isCreator(uid)) continue;
                Set<String> set = owners.get(uid);
                if (set == null) {
                    set = new LinkedHashSet<>();
                    owners.put(uid, set);
                }
                for (String id : entry.getValue()) {
                    if (badges.containsKey(id)) set.add(id);
                }
            }
            for (Map.Entry<String, BadgeGradient> entry : source.gradients.entrySet()) {
                String uid = entry.getKey();
                if (!trusted && CreatorBadgeProvider.isCreator(uid)) continue;
                if (!gradients.containsKey(uid)) gradients.put(uid, entry.getValue());
            }
        }

        List<Badge> sorted = new ArrayList<>(badges.values());
        Collections.sort(sorted, Badge.ORDER);
        Map<String, Badge> byId = new LinkedHashMap<>();
        for (Badge badge : sorted) byId.put(badge.id, badge);

        Set<String> uids = new HashSet<>(owners.keySet());
        uids.addAll(gradients.keySet());
        Map<String, UserBadgeProfile> users = new HashMap<>();
        for (String uid : uids) {
            List<Badge> held = new ArrayList<>();
            Set<String> ids = owners.get(uid);
            if (ids != null) {
                for (String id : ids) {
                    Badge badge = byId.get(id);
                    if (badge != null) held.add(badge);
                }
            }
            users.put(uid, new UserBadgeProfile(uid, held, gradients.get(uid)));
        }
        return new Snapshot(byId, users, gradients, freeUntil, serverNow);
    }
}

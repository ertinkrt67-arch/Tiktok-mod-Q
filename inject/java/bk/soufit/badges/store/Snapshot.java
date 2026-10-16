package bk.soufit.badges.store;

import android.os.SystemClock;
import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import bk.soufit.badges.model.UserBadgeProfile;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Snapshot {
    public static final Snapshot EMPTY = new Snapshot(new LinkedHashMap<String, Badge>(),
            new HashMap<String, UserBadgeProfile>(), new HashMap<String, BadgeGradient>(), 0, 0);

    public final Map<String, Badge> badges;
    public final Map<String, UserBadgeProfile> users;
    public final Map<String, BadgeGradient> gradients;
    public final long freeUntil;
    public final long serverNow;
    private final long readAt;

    Snapshot(Map<String, Badge> badges, Map<String, UserBadgeProfile> users,
             Map<String, BadgeGradient> gradients, long freeUntil, long serverNow) {
        this.badges = Collections.unmodifiableMap(new LinkedHashMap<>(badges));
        this.users = Collections.unmodifiableMap(new HashMap<>(users));
        this.gradients = Collections.unmodifiableMap(new HashMap<>(gradients));
        this.freeUntil = freeUntil;
        this.serverNow = serverNow;
        this.readAt = SystemClock.elapsedRealtime();
    }

    public boolean freeStillOpen() {
        if (serverNow <= 0) return false;
        long since = (SystemClock.elapsedRealtime() - readAt) / 1000;
        return serverNow + since < freeUntil;
    }
}

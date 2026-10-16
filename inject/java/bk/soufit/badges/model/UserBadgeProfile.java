package bk.soufit.badges.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UserBadgeProfile {
    public final String userId;
    private final List<Badge> badges;
    private final BadgeGradient gradient;

    public UserBadgeProfile(String userId, List<Badge> badges, BadgeGradient gradient) {
        this.userId = userId;
        List<Badge> sorted = new ArrayList<>(badges);
        Collections.sort(sorted, Badge.ORDER);
        this.badges = Collections.unmodifiableList(sorted);
        this.gradient = gradient;
    }

    public List<Badge> badges() { return badges; }
    public BadgeGradient gradient() { return gradient; }

    public boolean holds(String badgeId) {
        for (Badge badge : badges) {
            if (badge.id.equals(badgeId)) return true;
        }
        return false;
    }
}

package bk.soufit.badges.source;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CreatorBadgeProvider {
    public static final String BADGE_ID = "creator";
    private static final String[] UIDS = {"7565935876690428946"};
    private static final int[] PALETTE = {0xE2FF70, 0x8CF58F, 0x3DD9C4, 0x7B8CFF, 0xFFB561};
    private static final String ICON = "packed:soufi.png";

    private CreatorBadgeProvider() {}

    public static boolean isCreator(String uid) {
        if (uid == null) return false;
        for (String id : UIDS) {
            if (id.equals(uid)) return true;
        }
        return false;
    }

    public static SourceSnapshot snapshot() {
        BadgeGradient gradient = new BadgeGradient(PALETTE, 0f, BadgeGradient.Type.LINEAR);
        Badge badge = new Badge(BADGE_ID, Badge.Origin.CREATOR, ICON, 0, "soufiT", text(), "Got it",
                null, Integer.MAX_VALUE, true);

        Map<String, List<String>> owners = new LinkedHashMap<>();
        Map<String, BadgeGradient> gradients = new LinkedHashMap<>();
        for (String uid : UIDS) {
            owners.put(uid, Collections.singletonList(BADGE_ID));
            gradients.put(uid, gradient);
        }
        return new SourceSnapshot(Badge.Origin.CREATOR, Collections.singletonList(badge), owners, gradients, 0, 0);
    }

    private static String text() {
        String language = Locale.getDefault().getLanguage();
        if ("ru".equals(language)) return "Создатель форка";
        if ("uk".equals(language)) return "Творець форку";
        return "Creator of the fork";
    }
}

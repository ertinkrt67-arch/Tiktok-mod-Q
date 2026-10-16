package bk.soufit.badges.render;

import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import bk.soufit.badges.model.UserBadgeProfile;
import bk.soufit.badges.store.BadgeRepository;

public final class Marks {
    public static final int SLOTS = 64;
    private static final char BASE = pickBase();
    private static final String[] badgeIds = new String[SLOTS];
    private static final String[] gradientUids = new String[SLOTS];
    private static int badgeCount;
    private static int gradientCount;

    private Marks() {}

    private static char pickBase() {
        int room = 0xF8FF - 0xE000 + 1 - 2 * SLOTS;
        return (char) (0xE000 + (int) (Math.random() * room));
    }

    public static boolean isPrivate(char c) {
        return c >= '\uE000' && c <= '\uF8FF';
    }

    public static boolean isBadgeMark(char c) {
        return c >= BASE && c < BASE + SLOTS;
    }

    public static boolean isGradientMark(char c) {
        return c >= BASE + SLOTS && c < BASE + 2 * SLOTS;
    }

    public static synchronized String badgeMarks(String uid) {
        UserBadgeProfile profile = BadgeRepository.get().profile(uid);
        if (profile == null || profile.badges().isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (Badge badge : profile.badges()) {
            char mark = badgeMark(badge.id);
            if (mark != 0) out.append(mark);
        }
        return out.toString();
    }

    private static synchronized char badgeMark(String id) {
        for (int i = 0; i < badgeCount; i++) {
            if (id.equals(badgeIds[i])) return (char) (BASE + i);
        }
        if (badgeCount == SLOTS) return 0;
        badgeIds[badgeCount] = id;
        return (char) (BASE + badgeCount++);
    }

    public static synchronized Badge badgeAt(char c) {
        int slot = c - BASE;
        if (slot < 0 || slot >= badgeCount) return null;
        return BadgeRepository.get().badge(badgeIds[slot]);
    }

    public static synchronized String gradientMark(String uid) {
        if (uid == null || BadgeRepository.get().gradient(uid) == null) return "";
        for (int i = 0; i < gradientCount; i++) {
            if (uid.equals(gradientUids[i])) return String.valueOf((char) (BASE + SLOTS + i));
        }
        if (gradientCount == SLOTS) return "";
        gradientUids[gradientCount] = uid;
        return String.valueOf((char) (BASE + SLOTS + gradientCount++));
    }

    public static synchronized int[] coloursAt(char c) {
        int slot = c - BASE - SLOTS;
        if (slot < 0 || slot >= gradientCount) return null;
        BadgeGradient gradient = BadgeRepository.get().gradient(gradientUids[slot]);
        return gradient == null ? null : gradient.colours();
    }
}

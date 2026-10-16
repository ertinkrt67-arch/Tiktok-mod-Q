package bk.soufit.profile;

import android.content.Context;
import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.UserBadgeProfile;
import bk.soufit.badges.render.IconCache;
import bk.soufit.badges.render.Marks;
import bk.soufit.badges.source.Endpoints;
import bk.soufit.badges.store.BadgeRepository;
import bk.soufit.badges.sync.Aggregator;
import bk.soufit.core.DevMode;
import bk.soufit.core.Local;
import bk.soufit.core.Net;
import bk.soufit.core.soufiT;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

public final class Badges {
    public static final String DEFAULT_SERVER = "https://margy.narez.xyz/api/badges";
    public static volatile String SERVER = DEFAULT_SERVER;
    public static final String KEY = "badges_on";
    private static final String KEY_SERVER = "badge_server";

    private static volatile Boolean cached;
    private static volatile boolean started;
    private static volatile Aggregator aggregator;
    private static volatile Runnable waiting;

    private Badges() {}

    public interface Said {
        void said(boolean reached);
    }

    public static String base() {
        return stripBadges(SERVER);
    }

    private static String stripBadges(String address) {
        String tail = "/badges";
        return address.endsWith(tail) ? address.substring(0, address.length() - tail.length()) : address;
    }

    public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        try {
            Context context = soufiT.context();
            if (context == null) return true;
            boolean on = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true);
            cached = on;
            return on;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        try {
            Context context = soufiT.context();
            if (context == null) return;
            context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, enabled).apply();
        } catch (Throwable ignored) {}
    }

    public static synchronized void start(Context context) {
        if (started) return;
        started = true;
        loadServer(context);
        Aggregator next = Aggregator.standard(context);
        aggregator = next;
        next.warm();
    }

    public static void loadServer(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
        boolean developer = prefs.getBoolean(DevMode.KEY, false);
        SERVER = developer && prefs.contains(KEY_SERVER) ? prefs.getString(KEY_SERVER, "") : DEFAULT_SERVER;
        Endpoints.one = SERVER;
    }

    public static void setServer(Context context, String address) throws Exception {
        String value = address == null ? "" : address.trim();
        if (value.length() > 0) {
            java.net.URI uri = new java.net.URI(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new Exception(Local.of("The address must start with https://"));
            }
            value = value.replaceAll("/+$", "");
        }
        SERVER = value;
        Endpoints.one = value;
        context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putString(KEY_SERVER, value).apply();
    }

    public static void refresh() {
        final Aggregator next = aggregator;
        if (next == null || SERVER.isEmpty()) return;
        Net.away("badges", () -> {
            if (next.refresh()) told();
        });
    }

    public static void refreshNow(final Said then) {
        final Aggregator next = aggregator;
        if (next == null || SERVER.isEmpty()) {
            answer(then, false);
            return;
        }
        Net.away("badges", () -> {
            boolean reached = next.refresh();
            if (reached) told();
            answer(then, reached);
        });
    }

    private static void answer(final Said then, final boolean reached) {
        if (then == null) return;
        new Handler(Looper.getMainLooper()).post(() -> then.said(reached));
    }

    public static void tell(Runnable then) {
        waiting = then;
    }

    public static void stopTelling(Runnable then) {
        if (waiting == then) waiting = null;
    }

    private static void told() {
        final Runnable then = waiting;
        if (then == null) return;
        new Handler(Looper.getMainLooper()).post(then);
    }

    public static boolean freeStillOpen() {
        return BadgeRepository.get().snapshot().freeStillOpen();
    }

    public static Badge[] of(String uid) {
        UserBadgeProfile profile = BadgeRepository.get().profile(uid);
        if (profile == null || profile.badges().isEmpty()) return null;
        return profile.badges().toArray(new Badge[0]);
    }

    public static Badge[] all() {
        return BadgeRepository.get().all().toArray(new Badge[0]);
    }

    public static Badge byId(String id) {
        return BadgeRepository.get().badge(id);
    }

    public static String marksFor(String uid) {
        return isEnabled() ? Marks.badgeMarks(uid) : "";
    }

    public static boolean isPrivate(char c) {
        return Marks.isPrivate(c);
    }

    public static Bitmap picture(Context context, String path) {
        return IconCache.request(context, path, null);
    }

    public static Bitmap note() {
        return IconCache.emblem();
    }
}

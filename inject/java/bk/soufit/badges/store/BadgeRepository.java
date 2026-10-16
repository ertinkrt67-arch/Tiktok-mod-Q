package bk.soufit.badges.store;

import android.content.Context;
import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import bk.soufit.badges.model.UserBadgeProfile;
import bk.soufit.core.Net;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class BadgeRepository {
    private static final BadgeRepository INSTANCE = new BadgeRepository();

    private volatile Snapshot current = Snapshot.EMPTY;

    private BadgeRepository() {}

    public static BadgeRepository get() { return INSTANCE; }

    public Snapshot snapshot() { return current; }

    public synchronized void publish(Snapshot next) { current = next; }

    public UserBadgeProfile profile(String uid) {
        return uid == null ? null : current.users.get(uid);
    }

    public Badge badge(String id) {
        return id == null ? null : current.badges.get(id);
    }

    public BadgeGradient gradient(String uid) {
        return uid == null ? null : current.gradients.get(uid);
    }

    public List<Badge> all() {
        return new ArrayList<>(current.badges.values());
    }

    public static byte[] readCache(Context context, String name) {
        byte[] body = Net.read(cacheFile(context, name));
        if (body == null && "one".equals(name)) {
            body = Net.read(new File(context.getFilesDir(), "soufit/badges.json"));
        }
        return body;
    }

    public static void writeCache(Context context, String name, byte[] body) {
        Net.save(cacheFile(context, name), body);
    }

    private static File cacheFile(Context context, String name) {
        return new File(context.getFilesDir(), "soufit/badges/" + name + ".json");
    }
}

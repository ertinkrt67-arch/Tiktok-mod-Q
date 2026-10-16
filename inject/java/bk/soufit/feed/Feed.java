package bk.soufit.feed;
import bk.soufit.profile.Account;
import bk.soufit.core.Diary;
import bk.soufit.core.NativeRead;
import bk.soufit.plugins.Plugins;
import bk.soufit.core.Probe;
import bk.soufit.core.soufiT;
import android.content.SharedPreferences;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.FeedItemList;
import java.util.ArrayList;
import java.util.List;
public final class Feed {
    private Feed() {} public static void reloadSettings() {
        cached = null;
    }

    public static final String KEY = "hide_ads";
    public static final String KEY_LIVE = "hide_live";
    public static final String KEY_PHOTOS = "hide_photos";
    public static final String KEY_STORIES = "hide_stories", KEY_SUGGESTIONS = "hide_suggestions";
    private static volatile Boolean cached;
    public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        if (prefs == null) return true;
        boolean on = true;
        try {
            if (!prefs.getBoolean("ads_enabled_v024", false)) prefs.edit().putBoolean(KEY, true).putBoolean("ads_enabled_v024",
                    true).apply();
            on = prefs.getBoolean(KEY, true);
        } catch (Throwable ignored) {} cached = on;
        return on;
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY, enabled).apply();
    }

    public static boolean hides(String key) {
        SharedPreferences prefs = prefs();
        return prefs != null && prefs.getBoolean(key, false);
    }

    public static void setHides(String key, boolean on) {
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(key, on).apply();
    }

    private static final int LIVE = 101;
    private static boolean isLive(Aweme post) {
        try {
            if (post.getAwemeType() == LIVE) return true;
            if (post.getLiveId() != 0) return true;
            return post.getRoomFeedCellStruct() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isPhotos(Aweme post) {
        try {
            return post.getPhotoModeImageInfo() != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean mine(Aweme post) {
        try {
            String me = Account.id();
            if (me == null || me.length() == 0) return false;
            return me.equals(post.getAuthorUid());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean unwanted(Object item, boolean ads, boolean live, boolean photos) {
        if (!(item instanceof Aweme)) return false;
        Aweme post = (Aweme) item;
        if (mine(post)) return false;
        try {
            if (ads && advert(post)) return true;
            if (live && isLive(post)) return true;
            if (isLive(post)) return false;
            if (photos && isPhotos(post)) return true;
            if (Tags.blocks(post)) return true;
        } catch (Throwable ignored) {} return false;
    }

    public static boolean advert(Object post) {
        if (!(post instanceof Aweme)) return false;
        return ((Aweme) post).isAd() || Boolean.TRUE.equals(NativeRead.field(post, "_isAd"));
    }

    public static List withoutAds(List items) {
        if (items == null || !isEnabled()) return items;
        ArrayList kept = null;
        for (int i = 0; i<items.size(); i ++) {
            Object item = items.get(i);
            Object post = item instanceof Aweme ? item : NativeRead.get(item, "getAweme");
            boolean card = !(item instanceof Aweme) && (NativeRead.get(item, "getPreciseAd") != null || NativeRead.get(item,
                    "getAiAdCard") != null);
            if (advert(post) || card) {
                if (kept == null) {
                    kept = new ArrayList(items.size());
                    kept.addAll(items.subList(0, i));
                }
            } else if (kept != null) kept.add(item);
        }
        return kept == null ? items : kept;
    }

    public static List searchItems(com.ss.android.ugc.aweme.search.pages.result.topsearch.core.model.SearchMixFeedList page) {
        return page == null ? null : withoutAds(page.mItems);
    }

    public static List rawItems(FeedItemList page) {
        return page == null ? null : withoutAds(page.items);
    }

    private static SharedPreferences prefs() {
        try {
            android.content.Context context = soufiT.context();
            if (context == null) return null;
            return context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean socialRepost(Object post) {
        return NativeRead.get(post, "getUpvoteReason") != null || NativeRead.get(post, "getUpvoteInfo") != null || NativeRead.get(post,
                "getUpvotePreload") != null;
    }

    public static boolean suggestion(Object post) {
        if (NativeRead.number(NativeRead.get(post, "getRecommendCardType")) <= 0) return false;
        if (socialRepost(post)) return false;
        return true;
    }

    private static int dropped;
    private static long reportedAt;
    public static List getItems(FeedItemList page) {
        if (page == null) return null;
        long probe = Probe.start();
        List out = without(page.getItems());
        Probe.stop(Probe.FEED, probe);
        return out;
    }

    private static List without(List items) {
        if (items == null) return items;
        boolean ads = isEnabled();
        SharedPreferences options = prefs();
        boolean live = options != null && options.getBoolean(KEY_LIVE, false);
        boolean photos = options != null && options.getBoolean(KEY_PHOTOS, false);
        boolean stories = options != null && options.getBoolean(KEY_STORIES, false);
        boolean suggestions = options != null && options.getBoolean(KEY_SUGGESTIONS, false);
        boolean authors = !FeedBlacklist.items().isEmpty() || !FeedBlacklist.cards().isEmpty();
        boolean others = !Tags.all().isEmpty();
        if (!ads && !live && !photos && !stories && !suggestions && !authors && !others) {
            return Plugins.feed(items);
        }
        try {
            int out = 0, index = 0;
            List kept = null;
            for (Object item : items) {
                boolean extra = item instanceof Aweme && ((stories && !socialRepost(item) && Boolean.TRUE.equals(NativeRead.get(item,
                        "getIsTikTokStory"))) || (suggestions && suggestion(item)) || (authors && FeedBlacklist.hides(item)));
                if (extra || unwanted(item, ads, live, photos)) {
                    if (kept == null) {
                        kept = new ArrayList(items.size());
                        kept.addAll(items.subList(0, index));
                    }
                    out ++;
                } else if (kept != null) kept.add(item);
                index ++;
            }
            if (kept == null) return Plugins.feed(items);
            synchronized (Feed.class) {
                dropped += out;
                long now = System.currentTimeMillis();
                if (now - reportedAt >= 1000L) {
                    reportedAt = now;
                    Diary.note("feed: " + out + " left out, " + dropped + " so far");
                }
            }
            return Plugins.feed(kept);
        } catch (Throwable error) {
            Diary.note("feed: leaving the page alone, " + error);
            return items;
        }
    }
}

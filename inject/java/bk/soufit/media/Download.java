package bk.soufit.media;
import bk.soufit.core.soufiT;
import android.content.SharedPreferences;
import com.ss.android.ugc.aweme.base.model.UrlModel;
import com.ss.android.ugc.aweme.feed.model.ACLCommonShare;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.PhotoModeImageUrlModel;
import com.ss.android.ugc.aweme.feed.model.Video;
import com.ss.android.ugc.aweme.feed.model.VideoControl;
import java.util.List;
import com.ss.android.ugc.aweme.profile.model.User;
public final class Download {
    private Download() {} public static void reloadSettings() {
        cached = cachedAlways = null;
    }

    public static final String KEY = "download_no_watermark";
    public static final String KEY_ALWAYS = "download_always";
    private static volatile Boolean cached;
    private static volatile Boolean cachedAlways;
    public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        Boolean read = read(KEY);
        if (read == null) return false;
        cached = read;
        return read;
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        write(KEY, enabled);
    }

    public static boolean isAlways() {
        Boolean known = cachedAlways;
        if (known != null) return known;
        Boolean read = read(KEY_ALWAYS);
        if (read == null) return false;
        cachedAlways = read;
        return read;
    }

    public static void setAlways(boolean enabled) {
        cachedAlways = enabled;
        write(KEY_ALWAYS, enabled);
    }

    private static Boolean read(String key) {
        SharedPreferences prefs = prefs();
        if (prefs == null) return null;
        try {
            return prefs.getBoolean(key, false);
        } catch (Throwable ignored) {
            return Boolean.FALSE;
        }
    }

    private static void write(String key, boolean value) {
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(key, value).apply();
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

    public static UrlModel getDownloadAddr(Video video) {
        if (video == null) return null;
        UrlModel stamped = video.getDownloadAddr();
        if (!isEnabled()) return stamped;
        try {
            UrlModel clean = video.getDownloadNoWatermarkAddr();
            if (usable(clean)) return (UrlModel) DownloadQuality.video(video, clean);
            UrlModel playing = video.getPlayAddr();
            if (usable(playing)) return (UrlModel) DownloadQuality.video(video, playing);
        } catch (Throwable ignored) {} return stamped;
    }

    private static boolean usable(UrlModel url) {
        if (url == null) return false;
        try {
            List urls = url.getUrlList();
            return urls != null && !urls.isEmpty();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int getCode(ACLCommonShare acl) {
        if (acl == null) return 0;
        return isAlways() ? 0 : acl.getCode();
    }

    public static int getShowType(ACLCommonShare acl) {
        if (acl == null) return 2;
        return isAlways() ? 2 : acl.getShowType();
    }

    public static int getTranscode(ACLCommonShare acl) {
        if (acl == null) return 1;
        return isAlways() ? 1 : acl.getTranscode();
    }

    public static UrlModel ownerWatermarkImage(PhotoModeImageUrlModel image) {
        return clean(image, image == null ? null : image.ownerWatermarkImage);
    }

    public static UrlModel userWatermarkImage(PhotoModeImageUrlModel image) {
        return clean(image, image == null ? null : image.userWatermarkImage);
    }

    private static UrlModel clean(PhotoModeImageUrlModel image, UrlModel stamped) {
        if (image == null || !isEnabled()) return stamped;
        try {
            UrlModel plain = image.displayImageNoWatermark;
            if (plain != null) return plain;
        } catch (Throwable ignored) {} return stamped;
    }

    public static boolean isPreventDownload(Aweme aweme) {
        if (aweme == null) return false;
        if (isAlways()) return false;
        return aweme.isPreventDownload();
    }

    public static boolean isPreventDownload(User user) {
        if (user == null) return false;
        if (isAlways()) return false;
        return user.isPreventDownload();
    }

    public static Boolean allowDownload(VideoControl control) {
        if (control == null) return null;
        if (isAlways()) return Boolean.TRUE;
        return control.allowDownload;
    }
}

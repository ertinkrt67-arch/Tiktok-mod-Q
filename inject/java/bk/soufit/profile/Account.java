package bk.soufit.profile;
import bk.soufit.media.Avatars;
import bk.soufit.core.Diary;
import bk.soufit.core.soufiT;
import bk.soufit.feed.RegionLabel;
import android.content.SharedPreferences;
import com.ss.android.ugc.aweme.IAccountUserService;
import com.ss.android.ugc.aweme.profile.model.User;
import com.ss.android.ugc.profile.platform.base.data.UserProfileInfo;
public final class Account {
    private Account() {} public static final String KEY_ID = "account_id";
    public static final String KEY_SEC_ID = "account_sec_id";
    private static final String KEY_PROFILE_NAME = "profile_preview_name";
    private static final String KEY_PROFILE_HANDLE = "profile_preview_handle";
    private static final String KEY_PROFILE_AVATAR = "profile_preview_avatar";
    private static volatile String id;
    private static volatile String activeId;
    public static String activeId() {
        return activeId;
    }

    public static String liveId() {
        return activeId;
    }

    private static volatile String secId;
    private static volatile String profileName;
    private static volatile String profileHandle;
    private static volatile String profileAvatar;
    private static volatile String profileRegion = "";
    private static String pendingUid;
    private static String pendingName;
    private static String pendingHandle;
    private static String currentUserUid;
    private static long currentUserCapturedAt;
    public static String getCurUserId(IAccountUserService service) {
        if (service == null) return null;
        String answer = service.getCurUserId();
        if (answer != null && answer.equals(activeId)) return answer;
        activeId = numeric(answer) ? answer : null;
        if (activeId == null) AccountAppearance.select(null);
        remember(KEY_ID, answer);
        return answer;
    }

    private static final long LOOK_AGAIN = 1000L;
    private static volatile String lookedUid;
    private static volatile long lookedAt;
    public static boolean numeric(String uid) {
        if (uid == null || uid.length() == 0 || uid.length() > 24) return false;
        for (int i = 0; i < uid.length(); i ++) {
            char one = uid.charAt(i);
            if (one < '0' || one > '9') return false;
        }
        return true;
    }

    public static User getCurUser(IAccountUserService service) {
        if (service == null) return null;
        User user = service.getCurUser();
        if (user == null) {
            activeId = null;
            lookedUid = null;
            AccountAppearance.select(null);
            return null;
        }
        try {
            String uid = user.getUid();
            long now = android.os.SystemClock.uptimeMillis();
            if (uid != null && uid.equals(lookedUid) && now - lookedAt < LOOK_AGAIN) return user;
            lookedUid = uid;
            lookedAt = now;
            activeId = numeric(uid) ? uid : null;
            remember(KEY_ID, uid);
            rememberProfile(user);
            rememberRegion(uid, RegionLabel.of(user));
            if (uid != null && (!uid.equals(currentUserUid) || now - currentUserCapturedAt >= 120000L)) {
                currentUserUid = uid;
                currentUserCapturedAt = now;
                rememberProfile(user);
                Avatars.rememberCurrent(user);
            }
        } catch (Throwable error) {
            Diary.note("current profile: " + error);
        }
        return user;
    }

    public static String getCurSecUserId(IAccountUserService service) {
        if (service == null) return null;
        String answer = service.getCurSecUserId();
        remember(KEY_SEC_ID, answer);
        return answer;
    }

    private static void remember(String key, String value) {
        if (value == null || value.length() == 0) return;
        boolean numeric = KEY_ID.equals(key);
        String known = numeric ? id : secId;
        if (value.equals(known)) {
            if (numeric) AccountAppearance.select(value);
            return;
        }
        if (numeric) {
            String previous = stored(KEY_ID);
            id = value;
            if (previous != null && !previous.equals(value)) clearProfile();
            AccountAppearance.select(value);
        } else {
            secId = value;
        }
        try {
            SharedPreferences prefs = prefs();
            if (prefs != null) prefs.edit().putString(key, value).apply();
        } catch (Throwable ignored) {} if (numeric) publishPending();
    }

    public static void rememberProfile(User user) {
        if (user == null) return;
        try {
            rememberProfile(user, user.getUid(), user.getNickname());
        } catch (Throwable ignored) {}
    }

    public static void rememberRegion(String uid, String line) {
        if (uid == null || !uid.equals(id)) return;
        profileRegion = line == null ? "" : line;
    }

    public static String profileRegion() {
        return profileRegion;
    }

    public static boolean isOwnHandle(String text) {
        String handle = profileHandle;
        if (text == null || handle == null) return false;
        String shown = text.trim();
        if (shown.startsWith("@")) shown = shown.substring(1);
        return shown.equals(handle);
    }

    public static void rememberProfile(UserProfileInfo user) {
        if (user == null) return;
        if (user.getUid() != null && user.getUid().equals(activeId) && currentUserUid != null && currentUserUid.equals(activeId)
                && profileName != null) return;
        try {
            rememberProfile(user, user.getUid(), user.getNickname());
        } catch (Throwable ignored) {}
    }

    public static String canonicalName(String uid, String fallback) {
        if (uid != null && uid.equals(activeId) && profileName != null && !profileName.isEmpty()) return profileName;
        return fallback;
    }

    private static synchronized void rememberProfile(Object model, String uid, String name) {
        if (uid == null || uid.length() == 0 || name == null || name.length() == 0) return;
        String clean = withoutPrivateUse(name).trim();
        if (clean.length() == 0 || clean.length() > 80) return;
        String handle = invokeString(model, "getUniqueId");
        if (handle != null) {
            handle = handle.trim();
            if (handle.startsWith("@")) handle = handle.substring(1);
            if (!handle.matches("[A-Za-z0-9._]{1,40}")) handle = null;
        }
        String signedIn = id();
        if (signedIn == null) {
            pendingUid = uid;
            pendingName = clean;
            pendingHandle = handle;
            return;
        }
        if (!signedIn.equals(uid)) return;
        saveProfile(clean, handle, null);
    }

    private static synchronized void publishPending() {
        if (pendingUid == null || !pendingUid.equals(id) || pendingName == null) return;
        saveProfile(pendingName, pendingHandle, null);
        pendingUid = pendingName = pendingHandle = null;
    }

    private static void saveProfile(String name, String handle, String avatar) {
        boolean changed = false;
        if (name != null && !name.equals(profileName)) {
            profileName = name;
            changed = true;
        }
        if (handle != null && !handle.equals(profileHandle)) {
            profileHandle = handle;
            changed = true;
        }
        if (avatar != null && !avatar.equals(profileAvatar)) {
            profileAvatar = avatar;
            changed = true;
        }
        if (!changed) return;
        try {
            SharedPreferences p = prefs();
            if (p != null) {
                SharedPreferences.Editor edit = p.edit();
                if (name != null) edit.putString(KEY_PROFILE_NAME, name);
                if (handle != null) edit.putString(KEY_PROFILE_HANDLE, handle);
                if (avatar != null) edit.putString(KEY_PROFILE_AVATAR, avatar);
                edit.apply();
            }
        } catch (Throwable ignored) {}
    }

    private static void clearProfile() {
        profileName = profileHandle = profileAvatar = null;
        pendingUid = pendingName = pendingHandle = null;
        try {
            SharedPreferences p = prefs();
            if (p != null) p.edit().remove(KEY_PROFILE_NAME).remove(KEY_PROFILE_HANDLE).remove(KEY_PROFILE_AVATAR).apply();
        } catch (Throwable ignored) {}
    }

    public static synchronized void rememberAvatar(String uid, String address) {
        if (uid == null || address == null || !uid.equals(id())) return;
        try {
            java.net.URI uri = new java.net.URI(address);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || uri.getUserInfo() != null || !trustedAvatarHost(host)) return;
            saveProfile(null, null, uri.toString());
        } catch (Throwable ignored) {}
    }

    private static boolean trustedAvatarHost(String host) {
        String lower = host.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".tiktokcdn.com") || lower.equals("tiktokcdn.com") || lower.endsWith(".tiktokcdn-us.com") || lower.endsWith(".tiktokv.com")
                || lower.equals("tiktokv.com") || lower.endsWith(".byteoversea.com") || lower.endsWith(".ibytedtos.com");
    }

    private static String invokeString(Object object, String method) {
        try {
            Object value = object.getClass().getMethod(method).invoke(object);
            return value instanceof String ? (String) value : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String withoutPrivateUse(String value) {
        StringBuilder clean = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i ++) {
            char c = value.charAt(i);
            if (c != '\u2063' && c != '\u2064' && !(c >= '\uE000' && c <= '\uF8FF')) clean.append(c);
        }
        return clean.toString();
    }

    public static String profileName() {
        return profileValue(KEY_PROFILE_NAME, profileName);
    }

    public static String profileHandle() {
        return profileValue(KEY_PROFILE_HANDLE, profileHandle);
    }

    public static String profileAvatar() {
        return profileValue(KEY_PROFILE_AVATAR, profileAvatar);
    }

    private static String profileValue(String key, String known) {
        if (known != null) return known;
        String saved = stored(key);
        if (KEY_PROFILE_NAME.equals(key)) profileName = saved;
        else if (KEY_PROFILE_HANDLE.equals(key)) profileHandle = saved;
        else profileAvatar = saved;
        return saved;
    }

    public static String id() {
        String known = id;
        if (known != null) return known;
        known = stored(KEY_ID);
        id = known;
        return known;
    }

    public static String secId() {
        String known = secId;
        if (known != null) return known;
        return stored(KEY_SEC_ID);
    }

    private static String stored(String key) {
        try {
            SharedPreferences prefs = prefs();
            if (prefs == null) return null;
            String value = prefs.getString(key, null);
            return value == null || value.length() == 0 ? null : value;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static SharedPreferences prefs() {
        android.content.Context context = soufiT.context();
        if (context == null) return null;
        return context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE);
    }
}

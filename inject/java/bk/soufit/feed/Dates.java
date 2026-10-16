package bk.soufit.feed;
import bk.soufit.core.Diary;
import bk.soufit.core.NativeRead;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Method;
public final class Dates {
    public static void reloadSettings() {
        on = null;
        DateOverlay.update();
    }

    private Dates() {} public static final String KEY = "always_date";
    private static volatile Boolean on;
    private static String selectedAid;
    private static String selectedDate;
    public static volatile DateInfo.Data selectedInfo;
    private static final java.util.LinkedHashMap<String, java.lang.ref.WeakReference<android.view.View>> authorRows = new java.util.LinkedHashMap<>();
    public static void bindAuthor(Object holder) {
        Object params = NativeRead.item(holder);
        Object post = NativeRead.get(params, "getAweme");
        String aid = NativeRead.aid(post);
        Object content = NativeRead.get(holder, "getContentView");
        if (aid.isEmpty() || !(content instanceof android.view.View)) return;
        android.view.View view = (android.view.View) content;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            view.setTag(0x7e0d0334, aid);
            view.setTag(DateInfo.CELL_TAG, DateInfo.Data.of(post));
            authorRows.put(aid, new java.lang.ref.WeakReference<>(view));
            while (authorRows.size()>32) authorRows.remove(authorRows.keySet().iterator().next());
            String publication = label(NativeRead.number(NativeRead.get(post, "getCreateTime")), System.currentTimeMillis() / 1000L);
            String shown = RegionLabel.with(publication, post);
            DateOverlay.register(view, shown);
            if (aid.equals(selectedAid) && view.isShown()) DateOverlay.bind(view, shown);
        });
    }

    public static boolean isEnabled() {
        Boolean known = on;
        if (known != null) return known.booleanValue();
        SharedPreferences prefs = prefs();
        if (prefs == null) return false;
        boolean value = prefs.getBoolean(KEY, false);
        on = Boolean.valueOf(value);
        return value;
    }

    public static void setEnabled(boolean enabled) {
        on = Boolean.valueOf(enabled);
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putBoolean(KEY, enabled).apply();
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> DateOverlay.update());
    }

    public static String getDesc(Object post) {
        if (post == null) return null;
        String original = null;
        try {
            Object value = post.getClass().getMethod("getDesc").invoke(post);
            original = value instanceof String ? (String) value : null;
            return original;
        } catch (Throwable error) {
            Diary.note("date caption: " + error);
            return original;
        }
    }

    public static void bindPost(Object holder, Object post) {
        VideoActions.selected(holder, post);
        try {
            final android.view.View view = (android.view.View) holder.getClass().getMethod("getContentView").invoke(holder);
            long time = post == null ? 0 : ((Number) post.getClass().getMethod("getCreateTime").invoke(post)).longValue();
            final String date = RegionLabel.with(label(time, System.currentTimeMillis() / 1000L), post);
            String name = null;
            try {
                Object user = post.getClass().getMethod("getAuthor").invoke(post);
                if (user != null) name = (String) user.getClass().getMethod("getNickname").invoke(user);
            } catch (Throwable ignored) {} final String author = name;
            final String aid = NativeRead.aid(post);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                selectedAid = aid;
                selectedDate = date;
                selectedInfo = DateInfo.Data.of(post);
                java.lang.ref.WeakReference<android.view.View> saved = authorRows.get(aid);
                android.view.View row = saved == null ? null : saved.get();
                DateOverlay.bind(row != null && aid.equals(row.getTag(0x7e0d0334)) && row.isAttachedToWindow() && row.isShown()
                        ? row : view, date, author);
            });
        } catch (Throwable error) {
            Diary.note("visible date: " + error);
        }
    }

    public static String label(long seconds, long now) {
        if (seconds>100000000000L) seconds /= 1000L;
        if (seconds<1000000000L || seconds>now + 86400L) return null;
        return new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(new java.util.Date(seconds * 1000L));
    }

    private static SharedPreferences prefs() {
        Context context = soufiT.context();
        return context == null ? null : context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
    }
}

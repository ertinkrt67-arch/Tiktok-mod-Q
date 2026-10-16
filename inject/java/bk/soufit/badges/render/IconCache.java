package bk.soufit.badges.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Base64;
import android.util.LruCache;
import bk.soufit.badges.model.Badge;
import bk.soufit.core.BadgeArt;
import bk.soufit.core.Diary;
import bk.soufit.core.Emblem;
import bk.soufit.core.Net;
import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class IconCache {
    public interface Ready {
        void ready(Bitmap bitmap);
    }

    private static final String PACKED = "packed:";
    private static final long RETRY_MS = 60000L;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> memory = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };
    private static final ExecutorService DECODE = Executors.newFixedThreadPool(2, new ThreadFactory() {
        @Override public Thread newThread(Runnable work) {
            Thread thread = new Thread(work, "soufit-badge-icons");
            thread.setDaemon(true);
            return thread;
        }
    });
    private static final Object LOCK = new Object();
    private static final Map<String, List<Ready>> waiting = new HashMap<>();
    private static final Set<String> loading = new HashSet<>();
    private static final Map<String, Long> failedAt = new HashMap<>();
    private static Bitmap emblem;

    private IconCache() {}

    public static Bitmap request(Context context, String key, Ready onReady) {
        if (key == null || key.isEmpty() || context == null) return null;
        Bitmap known = memory.get(key);
        if (known != null) return known;
        synchronized (LOCK) {
            Long failed = failedAt.get(key);
            if (failed != null && SystemClock.elapsedRealtime() - failed < RETRY_MS) return null;
            if (onReady != null) {
                List<Ready> list = waiting.get(key);
                if (list == null) {
                    list = new ArrayList<>(2);
                    waiting.put(key, list);
                }
                list.add(onReady);
            }
            if (!loading.add(key)) return null;
        }
        final Context app = context.getApplicationContext();
        DECODE.execute(new Runnable() {
            @Override public void run() {
                finish(key, load(app, key));
            }
        });
        return null;
    }

    public static void prefetch(Context context, Collection<Badge> badges) {
        if (context == null || badges == null) return;
        for (Badge badge : badges) request(context, badge.image, null);
    }

    public static synchronized Bitmap emblem() {
        if (emblem != null) return emblem;
        try {
            byte[] png = Base64.decode(Emblem.PNG, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(png, 0, png.length);
            emblem = bitmap == null ? null : trim(bitmap);
        } catch (Throwable error) {
            Diary.fail("badge emblem", error);
        }
        return emblem;
    }

    public static Bitmap trim(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int left = width, top = height, right = -1, bottom = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((pixels[y * width + x] >>> 24) < 8) continue;
                if (x < left) left = x;
                if (x > right) right = x;
                if (y < top) top = y;
                if (y > bottom) bottom = y;
            }
        }
        if (right < left || bottom < top) return bitmap;
        return Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1);
    }

    private static Bitmap load(Context app, String key) {
        try {
            byte[] raw = key.startsWith("http")
                    ? remote(app, key)
                    : BadgeArt.png(key.startsWith(PACKED) ? key.substring(PACKED.length()) : key);
            if (raw == null) return null;
            Bitmap bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.length);
            return bitmap == null ? null : trim(bitmap);
        } catch (Throwable error) {
            Diary.fail("badge icon", error);
            return null;
        }
    }

    private static byte[] remote(Context app, String url) {
        File file = new File(app.getFilesDir(), "soufit/badges/" + digest(url) + ".png");
        byte[] have = Net.read(file);
        if (have != null) return have;
        byte[] raw = Net.bytes(url);
        if (raw != null) Net.save(file, raw);
        return raw;
    }

    private static void finish(String key, Bitmap bitmap) {
        if (bitmap != null) memory.put(key, bitmap);
        List<Ready> found;
        synchronized (LOCK) {
            loading.remove(key);
            if (bitmap == null) failedAt.put(key, SystemClock.elapsedRealtime());
            found = waiting.remove(key);
        }
        final List<Ready> ready = found;
        if (bitmap == null || ready == null) return;
        MAIN.post(new Runnable() {
            @Override public void run() {
                for (Ready listener : ready) listener.ready(bitmap);
            }
        });
    }

    private static String digest(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"));
            StringBuilder out = new StringBuilder(hash.length * 2);
            for (byte b : hash) out.append(String.format("%02x", b & 0xFF));
            return out.toString();
        } catch (Exception error) {
            return String.valueOf(text.hashCode());
        }
    }
}

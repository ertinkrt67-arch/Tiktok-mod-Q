package bk.soufit.core;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
public final class Diary {
    private Diary() {} private static final int SHOWN = 3000;
    private static final int RECENT = SHOWN;
    private static final long ROLL = 1024L * 1024L;
    private static final String DIR = "soufit";
    private static final String FILE = "diary.log";
    private static final String OLD = "diary.log.1";
    private static final int BURST = 6;
    private static final long WINDOW = 60000L;
    private static final HashSet<String> LAID = new HashSet<String>();
    private static final int LAYOUT_VIEWS = 160;
    private static final int SWEEP_VIEWS = 600;
    private static volatile boolean capturing;
    private static final List<String> MENU = new ArrayList<String>();
    private static final int LAYOUT_DEPTH = 14;
    private static final LinkedList<String> RECENT_LINES = new LinkedList<String>();
    private static final List<String> PENDING = new ArrayList<String>();
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
        @Override public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "soufit-diary");
            thread.setDaemon(true);
            return thread;
        }
    });

    private static final SimpleDateFormat CLOCK = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private static final java.util.HashMap<String, long[]> SAID = new java.util.HashMap<String, long[]>();
    public static void note(String line) {
        if (line == null || !DevMode.isOn()) return;
        String kind = line.length() > 80 ? line.substring(0, 80) : line;
        kind = kind.replaceAll("[0-9]+", "#");
        long now = android.os.SystemClock.uptimeMillis();
        int skipped = 0;
        synchronized (SAID) {
            long[] count = SAID.get(kind);
            if (count == null) {
                if (SAID.size() > 400) SAID.clear();
                SAID.put(kind, new long[] {now, 1, 0});
            } else if (now - count[0] > WINDOW) {
                skipped = (int) count[2];
                count[0] = now;
                count[1] = 1;
                count[2] = 0;
            } else if (++ count[1] > BURST) {
                count[2] ++;
                return;
            }
        }
        if (skipped > 0) record("    (" + skipped + " more like the line above, left out)");
        record(line);
    }

    public static void detail(String line) {
        if (DevMode.isOn()) note("· " + line);
    }

    public static void fail(String where, Throwable error) {
        if (!DevMode.isOn()) return;
        if (error == null) {
            note(where);
            return;
        }
        StringBuilder place = new StringBuilder();
        StackTraceElement[] trace = error.getStackTrace();
        for (int i = 0; i < Math.min(8, trace.length); i ++) {
            if (i > 0) place.append(" < ");
            place.append(trace[i].getClassName().replaceFirst(".*\\.", "")).append('.').append(trace[i].getMethodName()).append(':')
                    .append(trace[i].getLineNumber());
        }
        note(where + ": " + error + (place.length() > 0 ? "  [" + place + "]" : ""));
    }

    public static void beginMenuDump() {
        synchronized (MENU) {
            MENU.clear();
        }
        capturing = true;
    }

    public static List<String> endMenuDump() {
        capturing = false;
        synchronized (MENU) {
            return new ArrayList<String>(MENU);
        }
    }

    public static void snapshot(final Activity activity, final String label) {
        if (activity == null || !DevMode.isOn()) return;
        try {
            final View root = activity.getWindow().getDecorView();
            root.post(new Runnable() {
                @Override public void run() {
                    try {
                        record("snapshot: " + label + " (" + activity.getClass().getName() + ")");
                        int[] budget = {SWEEP_VIEWS};
                        dump(activity, root, 0, budget);
                        if (budget[0] <= 0) record("    (stopped after " + SWEEP_VIEWS + " views)");
                    } catch (Throwable error) {
                        note("snapshot failed: " + error);
                    }
                }
            });
        } catch (Throwable error) {
            note("snapshot failed: " + error);
        }
    }

    public static void layout(Activity activity) {
        if (!DevMode.isOn()) return;
        try {
            String name = activity.getClass().getName();
            synchronized (LAID) {
                if (LAID.contains(name) || LAID.size() > 120) return;
                LAID.add(name);
            }
            final View root = activity.getWindow().getDecorView();
            root.post(new Runnable() {
                @Override public void run() {
                    try {
                        record("layout of " + name + ":");
                        int[] budget = {LAYOUT_VIEWS};
                        dump(activity, root, 0, budget);
                        if (budget[0] <= 0) record("    (stopped after " + LAYOUT_VIEWS + " views)");
                    } catch (Throwable error) {
                        note("layout failed: " + error);
                    }
                }
            });
        } catch (Throwable error) {
            note("layout failed: " + error);
        }
    }

    private static void dump(Context context, View view, int depth, int[] budget) {
        if (view == null || budget[0] <= 0 || depth > LAYOUT_DEPTH) return;
        budget[0] --;
        StringBuilder out = new StringBuilder("    ");
        for (int i = 0; i < depth; i ++) out.append("  ");
        out.append(view.getClass().getSimpleName());
        String id = idName(context, view.getId());
        if (id != null) out.append(" #").append(id);
        out.append(' ').append(view.getWidth()).append('x').append(view.getHeight()).append(" at ").append(view.getLeft())
                .append(',').append(view.getTop());
        if (view.getVisibility() == View.GONE) out.append(" gone");
        else if (view.getVisibility() == View.INVISIBLE) out.append(" invisible");
        if (view.getAlpha() < 1f) out.append(" alpha ").append(Math.round(view.getAlpha() * 100)).append("%");
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null && text.length() > 0) {
                String shown = text.toString().replace('\n', ' ');
                if (shown.length() > 40) shown = shown.substring(0, 40) + "…";
                out.append(" \"").append(shown).append('"');
            }
        }
        record(out.toString());
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount() && budget[0] > 0; i ++) {
                dump(context, group.getChildAt(i), depth + 1, budget);
            }
        }
    }

    private static String idName(Context context, int id) {
        if (id <= 0 || context == null) return null;
        try {
            return context.getResources().getResourceEntryName(id);
        } catch (Throwable ignored) {
            return "0x" + Integer.toHexString(id);
        }
    }

    private static void record(String line) {
        if (capturing) {
            synchronized (MENU) {
                MENU.add(line);
            }
        }
        String stamped = stamp() + "  " + line;
        synchronized (RECENT_LINES) {
            RECENT_LINES.add(stamped);
            while (RECENT_LINES.size() > RECENT) RECENT_LINES.removeFirst();
        }
        synchronized (PENDING) {
            PENDING.add(stamped);
        }
        ensureLoaded();
        WRITER.execute(new Runnable() {
            @Override public void run() {
                drain();
            }
        });
    }

    private static void drain() {
        List<String> batch;
        synchronized (PENDING) {
            if (PENDING.isEmpty()) return;
            Context context = soufiT.context();
            if (context == null) return;
            batch = new ArrayList<String>(PENDING);
            PENDING.clear();
        }
        try {
            File folder = new File(soufiT.context().getFilesDir(), DIR);
            if (!folder.isDirectory() && !folder.mkdirs()) return;
            File current = new File(folder, FILE);
            StringBuilder out = new StringBuilder();
            for (String line : batch) out.append(line).append('\n');
            try (FileOutputStream stream = new FileOutputStream(current, true)) {
                stream.write(out.toString().getBytes(StandardCharsets.UTF_8));
            }
            if (current.length() > ROLL) {
                File old = new File(folder, OLD);
                if (old.exists()) old.delete();
                current.renameTo(old);
            }
        } catch (Throwable error) {
            synchronized (PENDING) {
                PENDING.addAll(0, batch);
            }
        }
    }

    public static List<String> lines() {
        ensureLoaded();
        List<String> stored;
        synchronized (RECENT_LINES) {
            stored = new ArrayList<String>(RECENT_LINES);
        }
        List<String> out = new ArrayList<String>(header());
        out.add("== diary: " + stored.size() + " lines ==");
        out.addAll(stored);
        return out;
    }

    public static void clear() {
        synchronized (RECENT_LINES) {
            RECENT_LINES.clear();
        }
        synchronized (PENDING) {
            PENDING.clear();
        }
        synchronized (LAID) {
            LAID.clear();
        }
        Context context = soufiT.context();
        if (context == null) return;
        File folder = new File(context.getFilesDir(), DIR);
        new File(folder, FILE).delete();
        new File(folder, OLD).delete();
    }

    private static boolean loaded, loadQueued;
    private static void ensureLoaded() {
        synchronized (RECENT_LINES) {
            if (loaded || loadQueued) return;
            loadQueued = true;
        }
        WRITER.execute(new Runnable() {
            @Override public void run() {
                load();
            }
        });
    }

    private static void load() {
        Context context = soufiT.context();
        if (context == null) {
            synchronized (RECENT_LINES) {
                loadQueued = false;
            }
            return;
        }
        List<String> before = readAll(context);
        synchronized (RECENT_LINES) {
            List<String> merged = new ArrayList<String>(before);
            merged.addAll(RECENT_LINES);
            int from = Math.max(0, merged.size() - RECENT);
            RECENT_LINES.clear();
            RECENT_LINES.addAll(merged.subList(from, merged.size()));
            loaded = true;
            loadQueued = false;
        }
    }

    public static void settle() {
        try {
            WRITER.submit(new Runnable() {
                @Override public void run() {}
            }).get(5, TimeUnit.SECONDS);
        } catch (Throwable ignored) {}
    }

    private static List<String> readAll(Context context) {
        List<String> out = new ArrayList<String>();
        File folder = new File(context.getFilesDir(), DIR);
        out.addAll(read(new File(folder, OLD)));
        out.addAll(read(new File(folder, FILE)));
        return out;
    }

    private static List<String> read(File file) {
        List<String> out = new ArrayList<String>();
        if (!file.isFile()) return out;
        try (InputStream in = new java.io.FileInputStream(file)) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int got;
            while ((got = in.read(buffer)) != - 1) bytes.write(buffer, 0, got);
            for (String line : new String(bytes.toByteArray(), StandardCharsets.UTF_8).split("\n")) {
                if (line.length() > 0) out.add(line);
            }
        } catch (IOException ignored) {} return out;
    }

    private static List<String> header() {
        List<String> out = new ArrayList<String>();
        out.add("== soufiT " + Version.MOD + " / TikTok " + Version.TIKTOK + " ==");
        try {
            out.add("Phone: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL + ", Android " + android.os.Build.VERSION.RELEASE
                    + " (API " + android.os.Build.VERSION.SDK_INT + ")");
        } catch (Throwable ignored) {} out.add("Written: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
        return out;
    }

    private static String stamp() {
        synchronized (CLOCK) {
            return CLOCK.format(new Date());
        }
    }
}

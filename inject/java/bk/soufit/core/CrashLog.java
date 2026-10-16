package bk.soufit.core;
import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public final class CrashLog {
    private static final long MAX = 256 * 1024;
    private static final String KEY = "crash_log";
    private CrashLog() {} public static File file(Context context) {
        return new File(context.getFilesDir(), "soufit/crash.log");
    }

    public static boolean enabled(Context context) {
        return prefs(context).getBoolean(KEY, true);
    }

    public static void set(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY, on).apply();
    }

    public static void install(final Context context) {
        final Thread.UncaughtExceptionHandler before = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread thread, Throwable error) {
                try {
                    if (enabled(context)) write(context, thread, error);
                } catch (Throwable ignored) {} if (before != null) before.uncaughtException(thread, error);
            }
        });
    }

    public static synchronized void write(Context context, Thread thread, Throwable error) throws IOException {
        StringWriter trace = new StringWriter();
        error.printStackTrace(new PrintWriter(trace));
        String entry = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "  thread " + (thread == null
                ? "?" : thread.getName()) + "\n" + trace + "\n";
        String all = read(context) + entry;
        if (all.length() > MAX) all = all.substring(all.length() - (int) MAX);
        File target = file(context);
        target.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(target)) {
            out.write(all.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static synchronized String read(Context context) {
        File target = file(context);
        if (!target.isFile()) return "";
        try (InputStream in = new FileInputStream(target)) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int got;
            while ((got = in.read(buffer)) != - 1) bytes.write(buffer, 0, got);
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            return "";
        }
    }

    public static String summary(Context context) {
        long size = file(context).length();
        return size == 0 ? Local.of("Empty") : (size / 1024 + " KB");
    }

    public static synchronized void clear(Context context) {
        file(context).delete();
    }

    private static android.content.SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
    }
}

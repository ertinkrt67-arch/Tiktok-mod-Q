package bk.soufit.core;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.view.FrameMetrics;
import android.view.View;
import android.view.Window;
public final class Lag {
    private static final long BAD = 700;
    private static final long SHORT = 100;
    private static final int ENOUGH = 12;
    private static final long LOOK = 50;
    private static final long ABSURD = 30000;
    private static final String[] STAGES = {"waiting for the main thread", "input", "animation", "layout", "draw", "sync",
            "commands", "swap", "gpu"};
    private static final LagTally TALLY = new LagTally(STAGES);
    private static final int WATCHED = 0x4D61724C;
    private static int told;
    private static volatile long worst;
    private static volatile long asked;
    private static volatile int looks;
    private static volatile StackTraceElement[] early, late;
    private static Handler frames;
    private Lag() {} public static void watch() {
        try {
            Thread watcher = new Thread(Lag::look, "soufit-lag");
            watcher.setDaemon(true);
            watcher.setPriority(Thread.MIN_PRIORITY);
            watcher.start();
        } catch (Throwable error) {
            Diary.note("lag: " + error);
        }
    }

    private static void look() {
        final Thread main = Looper.getMainLooper().getThread();
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable answer = Lag::answered;
        while (true) {
            try {
                if (Watch.here() == null) {
                    Thread.sleep(1000);
                    continue;
                }
                Thread.sleep(LOOK);
                if (Probe.ON) sample(main.getStackTrace());
                long now = SystemClock.uptimeMillis(), since = asked;
                if (since == 0) {
                    early = null;
                    late = null;
                    looks = 0;
                    asked = now;
                    handler.post(answer);
                    continue;
                }
                looks ++;
                if (now - since >= SHORT && early == null) early = main.getStackTrace();
                if (now - since >= BAD && late == null) late = main.getStackTrace();
            } catch (InterruptedException over) {
                return;
            } catch (Throwable ignored) {}
        }
    }

    private static final java.util.HashMap<String, int[]> BUSY_IN = new java.util.HashMap<String, int[]>();
    private static int samples, busy, ours;
    private static void sample(StackTraceElement[] stack) {
        if (stack == null || stack.length == 0) return;
        synchronized (BUSY_IN) {
            samples ++;
            if (where(stack).length() == 0) return;
            busy ++;
            String mine = mine(stack);
            if (mine == null) return;
            ours ++;
            int[] count = BUSY_IN.get(mine);
            if (count == null) {
                if (BUSY_IN.size() >= 200) return;
                count = new int[1];
                BUSY_IN.put(mine, count);
            }
            count[0] ++;
        }
    }

    private static String profile() {
        synchronized (BUSY_IN) {
            if (samples == 0) return "";
            StringBuilder out = new StringBuilder();
            out.append("main thread: busy in ").append(busy).append(" of ").append(samples).append(" looks, in the mod in ")
                    .append(ours).append('\n');
            java.util.ArrayList<String> names = new java.util.ArrayList<String>(BUSY_IN.keySet());
            java.util.Collections.sort(names, (one, two) -> BUSY_IN.get(two)[0] - BUSY_IN.get(one)[0]);
            for (int i = 0; i < names.size() && i < 25; i ++) {
                out.append("  ").append(BUSY_IN.get(names.get(i))[0]).append("  ").append(names.get(i)).append('\n');
            }
            return out.toString();
        }
    }

    private static void answered() {
        try {
            long took = SystemClock.uptimeMillis() - asked;
            StackTraceElement[] stack = took >= BAD && late != null ? late : early;
            int seen = looks;
            asked = 0;
            if (took < SHORT || took > ABSURD || stack == null || Watch.here() == null) return;
            if (seen < took / (LOOK * 4)) return;
            String where = where(stack);
            if (where.length() == 0) return;
            String mine = mine(stack);
            TALLY.stall(mine != null ? mine : brief(where), where, took);
            if (took > worst) worst = took;
            if (took >= BAD && told < ENOUGH) {
                told ++;
                Diary.note("lag: the app stopped for " + took + " ms in " + where);
            }
        } catch (Throwable ignored) {
            asked = 0;
        }
    }

    public static void frames(Activity activity) {
        if (Build.VERSION.SDK_INT < 24 || activity == null) return;
        try {
            Window window = activity.getWindow();
            View decor = window == null ? null : window.getDecorView();
            if (decor == null || decor.getTag(WATCHED) != null) return;
            decor.setTag(WATCHED, Boolean.TRUE);
            if (frames == null) {
                HandlerThread thread = new HandlerThread("soufit-frames", android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
                frames = new Handler(thread.getLooper());
            }
            final String screen = activity.getClass().getSimpleName();
            float rate = 60f;
            try {
                rate = activity.getWindowManager().getDefaultDisplay().getRefreshRate();
            } catch (Throwable ignored) {} final long slow = (long)(2e9 / Math.max(30f, rate));
            final long[] spent = new long[STAGES.length];
            window.addOnFrameMetricsAvailableListener((from, metrics, dropped) -> {
                try {
                    long total = metrics.getMetric(FrameMetrics.TOTAL_DURATION);
                    if (total <= slow) {
                        TALLY.frame(screen, false, false, spent);
                        return;
                    }
                    spent[0] = metrics.getMetric(FrameMetrics.UNKNOWN_DELAY_DURATION);
                    spent[1] = metrics.getMetric(FrameMetrics.INPUT_HANDLING_DURATION);
                    spent[2] = metrics.getMetric(FrameMetrics.ANIMATION_DURATION);
                    spent[3] = metrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION);
                    spent[4] = metrics.getMetric(FrameMetrics.DRAW_DURATION);
                    spent[5] = metrics.getMetric(FrameMetrics.SYNC_DURATION);
                    spent[6] = metrics.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION);
                    spent[7] = metrics.getMetric(FrameMetrics.SWAP_BUFFERS_DURATION);
                    spent[8] = Build.VERSION.SDK_INT >= 31 ? metrics.getMetric(FrameMetrics.GPU_DURATION) : 0;
                    TALLY.frame(screen, true, total > SHORT * 1000000L, spent);
                } catch (Throwable ignored) {}
            }, frames);
        } catch (Throwable error) {
            Diary.detail("lag: frames: " + error);
        }
    }

    public static String report() {
        return TALLY.report() + (Probe.ON ? profile() : "");
    }

    public static String mine(StackTraceElement[] stack) {
        if (stack == null) return null;
        for (StackTraceElement frame : stack) {
            if (frame.getClassName().startsWith("bk.soufit.")) return name(frame);
        }
        return null;
    }

    public static String brief(String where) {
        int first = where.indexOf(" < ");
        int second = first < 0 ? - 1 : where.indexOf(" < ", first + 3);
        return second < 0 ? where : where.substring(0, second);
    }

    public static String where(StackTraceElement[] stack) {
        if (stack == null || stack.length == 0) return "";
        String top = stack[0].getClassName() + "." + stack[0].getMethodName();
        if (top.equals("android.os.MessageQueue.nativePollOnce")) return "";
        StringBuilder out = new StringBuilder();
        int said = 0;
        String mine = null;
        for (StackTraceElement frame : stack) {
            String type = frame.getClassName();
            if (mine == null && type.startsWith("bk.soufit.")) mine = name(frame);
            if (said < 5) {
                if (out.length() > 0) out.append(" < ");
                out.append(name(frame));
                said ++;
            }
        }
        if (mine != null && out.indexOf(mine) < 0) out.append(" << ").append(mine);
        return out.toString();
    }

    private static String name(StackTraceElement frame) {
        String type = frame.getClassName();
        int dot = type.lastIndexOf('.');
        String shortened = dot > 0 && !type.startsWith("X.") ? type.substring(dot + 1) : type;
        return shortened + "." + frame.getMethodName() + (frame.getLineNumber() > 0 && frame.getLineNumber() < 100000 ? ":"
                + frame.getLineNumber() : "");
    }

    public static long worst() {
        return worst;
    }
}

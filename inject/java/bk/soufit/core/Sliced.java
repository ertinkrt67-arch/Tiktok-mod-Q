package bk.soufit.core;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayDeque;
import java.util.WeakHashMap;
public final class Sliced {
    private Sliced() {} public interface Visit {
        void view(View view);
    }

    private static final long STRETCH = 2;
    private static final int MOST = 12000;
    private static final int DEEP = 40;
    private static final class Walk implements Runnable {
        public final View root;
        public final Visit[] visits;
        public final ArrayDeque<View> ahead = new ArrayDeque<View>();
        public final ArrayDeque<Integer> depths = new ArrayDeque<Integer>();
        public boolean again;
        public int seen;
        public Walk(View root, Visit[] visits) {
            this.root = root;
            this.visits = visits;
        }
        public void begin() {
            ahead.clear();
            depths.clear();
            seen = 0;
            ahead.push(root);
            depths.push(0);
        }
        @Override public void run() {
            long until = SystemClock.uptimeMillis() + STRETCH;
            long probe = Probe.start();
            int since = 0;
            try {
                while (!ahead.isEmpty()) {
                    View view = ahead.pop();
                    int depth = depths.pop();
                    if (view == null || ++ seen > MOST) continue;
                    for (Visit visit : visits) {
                        try {
                            visit.view(view);
                        } catch (Throwable ignored) {}
                    }
                    if (depth < DEEP && view instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) view;
                        for (int i = group.getChildCount() - 1; i >= 0; i --) {
                            ahead.push(group.getChildAt(i));
                            depths.push(depth + 1);
                        }
                    }
                    if (++ since >= 24) {
                        since = 0;
                        if (SystemClock.uptimeMillis() >= until) break;
                    }
                }
            } catch (Throwable error) {
                ahead.clear();
                depths.clear();
            }
            Probe.stop(Probe.SLICE, probe);
            if (!ahead.isEmpty() && root.isAttachedToWindow()) {
                root.postOnAnimation(this);
                return;
            }
            if (again && root.isAttachedToWindow()) {
                again = false;
                begin();
                root.postOnAnimation(this);
                return;
            }
            synchronized (running) {
                running.remove(root);
            }
        }
    }

    private static final WeakHashMap<View, Walk> running = new WeakHashMap<View, Walk>();
    public static void walk(View root, Visit ... visits) {
        if (root == null || visits.length == 0) return;
        Walk walk;
        synchronized (running) {
            walk = running.get(root);
            if (walk != null) {
                walk.again = true;
                return;
            }
            walk = new Walk(root, visits);
            running.put(root, walk);
        }
        walk.begin();
        walk.run();
    }
}

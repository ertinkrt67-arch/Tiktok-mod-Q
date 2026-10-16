package bk.soufit.media;
import bk.soufit.profile.Account;
import bk.soufit.core.Local;
import bk.soufit.core.Net;
import java.util.*;
public final class DownloadTasks {
    public static final class Control {
        public volatile long limit;
        public volatile boolean cancelled;
        public volatile java.net.HttpURLConnection connection;
        public boolean cancelled() {
            return cancelled;
        }
        public void cancel() {
            cancelled = true;
            final java.net.HttpURLConnection live = connection;
            if (live != null) Net.away("cancel download", () -> live.disconnect());
        }
    }

    public static final class Task {
        public final String key, label, origin, owner;
        public final int percent;
        public final String state;
        public final boolean cancel, retry;
        public Task(Entry e) {
            key = e.key;
            label = e.label;
            origin = e.origin;
            owner = e.owner;
            percent = e.percent;
            state = e.state;
            cancel = !e.readOnly && e.state.equals(Local.of("Downloading")) && !e.control.cancelled;
            retry = !cancel && !e.state.equals(Local.of("Cancelling…")) && e.retry != null && !e.state.equals(Local.of("Done"));
        }
    }

    private static final class Entry {
        public String key, label, origin, owner, state = Local.of("Downloading");
        public int percent;
        public boolean readOnly;
        public Control control = new Control();
        public Runnable retry;
    }

    private static final LinkedHashMap<String, Entry> tasks = new LinkedHashMap<>();
    public static synchronized Control begin(String key, String label, String origin, Runnable retry) {
        Entry old = tasks.get(key);
        if (old != null && (old.state.equals(Local.of("Downloading")) || old.state.equals(Local.of("Cancelling…")))) throw new IllegalStateException("task running");
        Entry e = new Entry();
        e.key = key;
        e.label = label;
        e.origin = origin;
        e.retry = retry;
        String handle = Account.profileHandle();
        e.owner = Account.liveId() == null ? Local.of("No account") : handle == null ? Local.of("Account ") + Account.liveId()
                : "@" + handle;
        tasks.remove(key);
        tasks.put(key, e);
        trim();
        return e.control;
    }

    public static synchronized void observe(String key, String label, int percent) {
        Entry e = tasks.get(key);
        if (e == null || e.readOnly && !e.state.equals(Local.of("Downloading"))) {
            e = new Entry();
            e.key = key;
            e.label = label;
            e.origin = Local.of("Mod");
            String handle = Account.profileHandle();
            e.owner = handle == null ? Local.of("This account") : "@" + handle;
            e.readOnly = true;
            tasks.put(key, e);
            trim();
        }
        e.percent = percent;
    }

    public static synchronized void endObserved(String key, boolean success) {
        Entry e = tasks.get(key);
        if (e != null && e.readOnly) {
            e.state = success ? Local.of("Done") : Local.of("Finished");
            trim();
        }
    }

    private static void trim() {
        Iterator<Entry> i = tasks.values().iterator();
        while (tasks.size()>16 && i.hasNext()) {
            Entry e = i.next();
            if (!e.state.equals(Local.of("Downloading")) && !e.state.equals(Local.of("Cancelling…"))) i.remove();
        }
    }

    public static synchronized void progress(String key, int percent) {
        Entry e = tasks.get(key);
        if (e != null) e.percent = percent<0 ? - 1 : Math.min(100, percent);
    }

    public static synchronized void finish(String key, boolean success) {
        Entry e = tasks.get(key);
        if (e == null) return;
        e.state = e.control.cancelled ? Local.of("Cancelled") : success ? Local.of("Done") : Local.of("Failed");
        e.control.connection = null;
        if (success) e.retry = null;
        trim();
    }

    public static void cancel(String key) {
        Control control = null;
        synchronized (DownloadTasks.class) {
            Entry e = tasks.get(key);
            if (e != null && e.state.equals(Local.of("Downloading"))) {
                e.state = Local.of("Cancelling…");
                control = e.control;
            }
        }
        if (control != null) control.cancel();
    }

    public static void retry(String key) {
        Runnable action = null;
        synchronized (DownloadTasks.class) {
            Entry e = tasks.get(key);
            if (e != null && !e.state.equals(Local.of("Downloading")) && !e.state.equals(Local.of("Cancelling…"))) {
                action = e.retry;
                e.retry = null;
            }
        }
        if (action != null) action.run();
    }

    public static synchronized List<Task> snapshot() {
        List<Task> result = new ArrayList<>();
        for (Entry e : tasks.values()) result.add(new Task(e));
        return result;
    }
}

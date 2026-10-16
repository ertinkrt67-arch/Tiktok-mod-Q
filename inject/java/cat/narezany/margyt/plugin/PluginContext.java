package cat.narezany.margyt.plugin;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
public final class PluginContext {
    private final Context context;
    private final String id;
    private final File folder;
    private final Diarist diarist;
    private final java.util.Set<String> downloads = new java.util.HashSet<>();
    public interface Diarist {
        void note(String line);
    }

    public PluginContext(Context context, String id, File folder, Diarist diarist) {
        this.context = context;
        this.id = id;
        this.folder = folder;
        this.diarist = diarist;
    }

    public Context context() {
        return context;
    }

    public String id() {
        return id;
    }

    public File folder() {
        return folder;
    }

    public SharedPreferences prefs() {
        return context.getSharedPreferences("margyt_plugin_" + id, Context.MODE_PRIVATE);
    }

    public SharedPreferences accountPrefs() {
        String uid = bk.soufit.profile.Account.liveId();
        return context.getSharedPreferences("margyt_plugin_" + id + "_" + (uid == null ? "guest" : uid), Context.MODE_PRIVATE);
    }

    public String currentVideoId() {
        return bk.soufit.feed.VideoActions.currentId();
    }

    public long currentVideoPublishedTime() {
        return bk.soufit.feed.VideoActions.currentPublishedTime();
    }

    public String getString(String key, String fallback) {
        return prefs().getString(key, fallback);
    }

    public void putString(String key, String value) {
        if (key == null || key.length()>80 || value == null || value.length()>8192) throw new IllegalArgumentException("setting size");
        prefs().edit().putString(key, value).apply();
    }

    public interface Selected {
        void selected(int index);
    }

    public void choose(String title, String[] options, Selected callback) {
        if (options == null || options.length == 0 || options.length>32) throw new IllegalArgumentException("options");
        final String[] copy = options.clone();
        for (String item : copy) if (item == null || item.length()>160) throw new IllegalArgumentException("option length");
        onUiThread(() -> {
            android.app.Activity a = activity();
            if (a == null || a.isFinishing()) return;
            bk.soufit.plugins.Plugins.choose(a, title, copy, callback);
        });
    }

    public String accountId() {
        return bk.soufit.profile.Account.liveId();
    }

    public int accentColour() {
        return bk.soufit.theme.Accent.colour();
    }

    public android.app.Activity activity() {
        return bk.soufit.ui.Screen.now();
    }

    public void onUiThread(Runnable action) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(action);
    }

    public void copyText(final String text) {
        onUiThread(() -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) clipboard.setPrimaryClip(android.content.ClipData.newPlainText(id, text == null ? "" : text));
        });
    }

    public interface Downloaded {
        void finished(File file, boolean success);
    }

    public void download(final String url, final String name, final Downloaded callback) {
        if (name == null || !name.matches("[A-Za-z0-9._-]{1,80}") || name.equals(".") || name.equals("..")) throw new IllegalArgumentException("file name");
        final File file = new File(context.getFilesDir(), "margyt/plugin-downloads/" + id + "/" + name);
        final String job = "plugin-api:" + id + ":" + name;
        synchronized (downloads) {
            if (!downloads.add(job)) throw new IllegalStateException("download already running");
        }
        final bk.soufit.media.DownloadTasks.Control control = bk.soufit.media.DownloadTasks.begin(job, name, id, () -> download(url,
                name, callback));
        bk.soufit.ui.Screen.progress(job, name, 0);
        bk.soufit.core.Net.away(job, () -> {
            boolean ok = false;
            try {
                ok = bk.soufit.core.Net.download(url, file, (percent, got, total) -> {
                    bk.soufit.media.DownloadTasks.progress(job, percent);
                    bk.soufit.ui.Screen.progress(job, name, percent);
                }, control);
            } catch (Throwable error) {
                log("download: " + error);
            } finally {
                bk.soufit.media.DownloadTasks.finish(job, ok);
                if (ok) bk.soufit.ui.Screen.progressDone(job);
                else bk.soufit.ui.Screen.progressGone(job);
                synchronized (downloads) {
                    downloads.remove(job);
                }
            }
            final boolean done = ok;
            onUiThread(() -> {
                if (callback != null) callback.finished(file, done);
            });
        });
    }

    public interface Tapped {
        void tapped();
    }

    public void addSettingsRow(String title, String detail, Tapped action) {
        bk.soufit.plugins.Plugins.addRow(id, title, detail, action);
    }

    public void addSettingsSection(String title) {
        bk.soufit.plugins.Plugins.addSection(id, title);
    }

    public void addSettingsToggle(String title, String detail, String key, boolean def) {
        bk.soufit.plugins.Plugins.addToggle(id, title, detail, key, def);
    }

    public void addSettingsChoice(String title, String detail, String key, String[] options, int def) {
        bk.soufit.plugins.Plugins.addChoice(id, title, detail, key, options, def);
    }

    public void addSettingsSlider(String title, String detail, String key, int min, int max, int def) {
        bk.soufit.plugins.Plugins.addSlider(id, title, detail, key, min, max, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return prefs().getBoolean(key, def);
    }

    public int getInt(String key, int def) {
        return prefs().getInt(key, def);
    }

    public void showWindow(String title, String message) {
        onUiThread(() -> {
            android.app.Activity activity = activity();
            if (activity != null && !activity.isFinishing()) bk.soufit.ui.Popup.show(activity, title, message);
        });
    }

    public void offer(String label, final Tapped action) {
        bk.soufit.ui.Screen.offer(label, new Runnable() {
            @Override public void run() {
                action.tapped();
            }
        });
    }

    public void progress(String what, int percent) {
        bk.soufit.ui.Screen.progress("plugin-api:" + id, what, percent);
    }

    public void progressDone() {
        bk.soufit.ui.Screen.progressDone("plugin-api:" + id);
    }

    public void progressGone() {
        bk.soufit.ui.Screen.progressGone("plugin-api:" + id);
    }

    public android.app.Activity screen() {
        return bk.soufit.ui.Screen.now();
    }

    public byte[] fetch(String url) {
        return bk.soufit.core.Net.bytes(url);
    }

    public void away(String what, Runnable work) {
        bk.soufit.core.Net.away(id + ": " + what, work);
    }

    public void log(String line) {
        diarist.note(id + ": " + line);
    }
}

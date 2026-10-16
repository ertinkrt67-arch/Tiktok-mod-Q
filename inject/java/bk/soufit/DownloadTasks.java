package bk.soufit;
public final class DownloadTasks {
    private DownloadTasks() {} public static bk.soufit.media.DownloadTasks.Control begin(String key, String label, String origin,
            Runnable retry) {
        return bk.soufit.media.DownloadTasks.begin(key, label, origin, retry);
    }

    public static void progress(String key, int percent) {
        bk.soufit.media.DownloadTasks.progress(key, percent);
    }

    public static void finish(String key, boolean success) {
        bk.soufit.media.DownloadTasks.finish(key, success);
    }
}

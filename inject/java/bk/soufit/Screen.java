package bk.soufit;
public final class Screen {
    private Screen() {} public static android.app.Activity now() {
        return bk.soufit.ui.Screen.now();
    }

    public static void progress(String job, String what, int percent) {
        bk.soufit.ui.Screen.progress(job, what, percent);
    }

    public static void progressDone(String job) {
        bk.soufit.ui.Screen.progressDone(job);
    }

    public static void progressGone(String job) {
        bk.soufit.ui.Screen.progressGone(job);
    }

    public static void offer(String label, Runnable action) {
        bk.soufit.ui.Screen.offer(label, action);
    }
}

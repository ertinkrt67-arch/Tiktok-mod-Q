package bk.soufit.core;
import bk.soufit.feed.Brightness;
import bk.soufit.theme.Md3;
import bk.soufit.theme.NavBar;
import bk.soufit.feed.Rate;
import bk.soufit.social.TabBadges;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
public final class Watch implements Application.ActivityLifecycleCallbacks {
    private Watch() {} private static volatile java.lang.ref.WeakReference<Activity> here = new java.lang.ref.WeakReference<Activity>(null);
    public static Activity here() {
        return here.get();
    }

    public static void start(Application application) {
        try {
            application.registerActivityLifecycleCallbacks(new Watch());
        } catch (Throwable error) {
            Diary.note("watch: " + error);
        }
    }

    @Override public void onActivityResumed(Activity activity) {
        here = new java.lang.ref.WeakReference<Activity>(activity);
        try {
            Rate.apply(activity);
        } catch (Throwable ignored) {} try {
            Brightness.apply(activity);
        } catch (Throwable ignored) {} try {} catch (Throwable ignored) {} Lag.frames(activity);
        TabBadges.attach(activity);
        NavBar.attach(activity);
        Md3.attach(activity);
        bk.soufit.theme.LiquidGlassViews.attach(activity);
        Screamer.attach(activity);
    }

    @Override public void onActivityPaused(Activity activity) {
        if (here.get() == activity) {
            here = new java.lang.ref.WeakReference<Activity>(null);
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) {} @Override public void onActivityStarted(Activity activity) {} @Override public void onActivityStopped(Activity activity) {} @Override public void onActivitySaveInstanceState(Activity activity,
            Bundle out) {} @Override public void onActivityDestroyed(Activity activity) {}
}

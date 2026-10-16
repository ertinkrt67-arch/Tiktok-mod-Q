package cat.narezany.margyt.plugin;
import android.app.Activity;
import android.content.Context;
public abstract class MargyPlugin {
    public static final int API = 5;
    private PluginContext margyt;
    public final void attach(PluginContext context) {
        this.margyt = context;
    }

    public final PluginContext margyt() {
        return margyt;
    }

    public void onStart(Context context) {} public void onActivityCreated(Activity activity) {} public void onActivityResumed(Activity activity) {} public void onActivityPaused(Activity activity) {} public boolean onOpenSettings(Activity activity) {
        return false;
    }

    public void onAccountChanged(String uid) {} public void onSettingsChanged(String key) {} public int onColour(int colour) {
        return colour;
    }

    public String onRegion(String key, String value) {
        return value;
    }

    public void onScreen(Activity activity, String name) {} public java.util.List onFeed(java.util.List posts) {
        return posts;
    }

    public String onName(String uid, String name) {
        return name;
    }

    public CharSequence onText(CharSequence text) {
        return text;
    }

    public void onStop() {}
}

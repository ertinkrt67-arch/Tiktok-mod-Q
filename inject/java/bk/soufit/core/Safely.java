package bk.soufit.core;
import android.content.Context;
import android.content.res.Resources;
public final class Safely {
    private Safely() {} private static volatile boolean told;
    public static String getString(Context context, int id) {
        try {
            return context.getString(id);
        } catch (Resources.NotFoundException missing) {
            if (!told) {
                told = true;
                Diary.note("TikTok asked for a string this apk does not have: 0x" + Integer.toHexString(id));
            }
            return "";
        }
    }
}

package bk.soufit.core;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
public final class FlagGuard {
    private FlagGuard() {} private static final String FILE = "flagguard_soufit";
    private static final String STARTS = "starts";
    private static final int TOO_MANY = 3;
    private static final long LASTED = 15000L;
    private static volatile int answer;
    public static boolean allows() {
        int known = answer;
        if (known != 0) return known == 2;
        Context context = soufiT.context();
        if (context == null) return false;
        try {
            final SharedPreferences prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
            int starts = prefs.getInt(STARTS, 0);
            if (starts >= TOO_MANY) {
                answer = 1;
                Diary.note("flags: held back, TikTok did not stay up with them");
                return false;
            }
            if (context.getPackageName().equals(processName())) {
                prefs.edit().putInt(STARTS, starts + 1).commit();
                new Handler(Looper.getMainLooper()).postDelayed(() -> prefs.edit().putInt(STARTS, 0).apply(), LASTED);
            }
            answer = 2;
            return true;
        } catch (Throwable error) {
            return false;
        }
    }

    public static boolean heldBack() {
        return answer == 1;
    }

    public static void again() {
        answer = 2;
        Context context = soufiT.context();
        if (context == null) return;
        try {
            context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putInt(STARTS, 0).apply();
        } catch (Throwable ignored) {}
    }

    private static String processName() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 28) return android.app.Application.getProcessName();
            return (String) Class.forName("android.app.ActivityThread").getMethod("currentProcessName").invoke(null);
        } catch (Throwable error) {
            return null;
        }
    }
}

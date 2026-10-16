package bk.soufit.social;
import bk.soufit.profile.AccountAppearance;
import bk.soufit.core.Local;
import bk.soufit.ui.Panel;
import bk.soufit.theme.Skin;
import bk.soufit.core.Text;
import bk.soufit.ui.TimeWheel;
import bk.soufit.core.soufiT;
import java.util.Calendar;
import android.content.Context;
public final class StreakSchedule {
    public static final String KEY = "streak_send_minute";
    public static int minute() {
        try {
            int v = AccountAppearance.prefs(soufiT.context()).getInt(KEY, - 1);
            return v >= 0 && v<1440 ? v : - 1;
        } catch (Throwable ignored) {
            return - 1;
        }
    }

    public static boolean due(long now, int minute) {
        if (minute<0) return true;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE) >= minute;
    }

    public static long delay(long now, long cadence) {
        int minute = minute();
        if (minute<0) return cadence;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.set(Calendar.HOUR_OF_DAY, minute / 60);
        c.set(Calendar.MINUTE, minute % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long until = c.getTimeInMillis() - now;
        return until>0 ? Math.max(1000, Math.min(cadence, until)) : cadence;
    }

    public static long next(long now, int minute, boolean retry, long cadence) {
        if (minute<0) return now + cadence;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.set(Calendar.HOUR_OF_DAY, minute / 60);
        c.set(Calendar.MINUTE, minute % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long today = c.getTimeInMillis();
        if (now<today) return today;
        if (retry && now<today + 3 * 60 * 60 * 1000L) return now + 5 * 60 * 1000L;
        c.add(Calendar.DAY_OF_YEAR, 1);
        return c.getTimeInMillis();
    }

    public static boolean sameDay(long a, long b) {
        if (b <= 0) return false;
        Calendar x = Calendar.getInstance(), y = Calendar.getInstance();
        x.setTimeInMillis(a);
        y.setTimeInMillis(b);
        return x.get(Calendar.ERA) == y.get(Calendar.ERA) && x.get(Calendar.YEAR) == y.get(Calendar.YEAR) && x.get(Calendar.DAY_OF_YEAR) == y.get(Calendar.DAY_OF_YEAR);
    }

    public static int parse(String value) {
        if (value == null || !value.matches("[0-9]{1,2}:[0-9]{2}")) return - 2;
        String[] p = value.split(":");
        int h = Integer.parseInt(p[0]), m = Integer.parseInt(p[1]);
        return h<24 && m<60 ? h * 60 + m : - 2;
    }

    public static String label() {
        int m = minute();
        return m<0 ? Local.of("When the streak is waiting to be extended") : String.format(java.util.Locale.ROOT, Local.of("At %02d:%02d"),
                m / 60, m % 60);
    }

    public static void choose(Context c, Runnable changed) {
        Skin skin = Skin.remembered(c);
        Panel p = Panel.with(c, skin, Local.of("Auto-extend time"));
        p.text(Local.of("At the chosen device time, one message a day to friends with an active streak. For an exact background start, allow alarms and unrestricted battery use."));
        int m = minute();
        TimeWheel wheel = new TimeWheel(c, skin, m<0 ? 1200 : m);
        p.view(wheel);
        p.primaryKeepOpen(Local.of("Save"), () -> {
            set(wheel.minuteOfDay());
            Streaks.setEnabled(true);
            StreakAlarm.permissions(c);
            p.close();
            if (changed != null) changed.run();
        });
        p.quiet(Local.of("No schedule"), () -> {
            set(- 1);
            if (changed != null) changed.run();
        });
        p.quiet(Text.CLOSE, null);
        p.show();
    }

    public static void set(int minute) {
        AccountAppearance.prefs(soufiT.context()).edit().putInt(KEY, minute).apply();
        android.content.Context c = soufiT.context();
        if (c != null) {
            StreakAlarm.schedule(c, false);
            Streaks.round(c);
        }
    }
}

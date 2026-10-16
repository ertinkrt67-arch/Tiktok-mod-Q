package bk.soufit.social;
import bk.soufit.core.Diary;
import bk.soufit.core.Local;
import bk.soufit.core.soufiT;
import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.List;
public final class StreakAlarm extends BroadcastReceiver {
    public static final String FIRE = "bk.soufit.STREAK_FIRE";
    public static final String NET = "bk.soufit.STREAK_NET";
    private static final int REQUEST = 0x5712;
    private static final long CADENCE = 15 * 60 * 1000L;
    private static final long WAIT = 1500L;
    private static final String CHANNEL = "soufit_streak";
    private static volatile long exactAt;
    private static volatile String lastMode;
    private static volatile long lastDenied;
    public static final String KEY_ARMED = "streak_armed_at";
    public static boolean exactAllowed(Context c) {
        try {
            if (Build.VERSION.SDK_INT < 31) return true;
            AlarmManager alarms = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            return alarms != null && alarms.canScheduleExactAlarms();
        } catch (Throwable error) {
            return false;
        }
    }

    public static String lastMode() {
        return lastMode;
    }

    public static long lastExactAt() {
        return exactAt;
    }

    public static boolean notificationsAllowed(Context c) {
        try {
            if (Build.VERSION.SDK_INT < 33) return true;
            return c.checkPermission(Manifest.permission.POST_NOTIFICATIONS, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void channel(Context c) {
        try {
            if (Build.VERSION.SDK_INT < 26) return;
            NotificationManager manager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null || manager.getNotificationChannel(CHANNEL) != null) return;
            manager.createNotificationChannel(new NotificationChannel(CHANNEL, Local.of("Streaks"), NotificationManager.IMPORTANCE_HIGH));
        } catch (Throwable error) {
            Diary.note("streak channel: " + error);
        }
    }

    @Override public void onReceive(Context context, Intent intent) {
        final Context app = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        String action = intent == null ? null : intent.getAction();
        final boolean fire = FIRE.equals(action) || NET.equals(action);
        final PendingResult result = goAsync();
        Thread thread = new Thread(() -> {
            PowerManager.WakeLock lock = null;
            try {
                try {
                    PowerManager power = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
                    lock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "soufit:streak");
                    lock.acquire(60 * 1000L);
                } catch (Throwable ignored) {} schedule(app, false);
                if (fire && Streaks.isEnabled()) {
                    boolean retry = attempt(app);
                    if (retry) schedule(app, true);
                }
            } catch (Throwable error) {
                Diary.note("streak alarm: " + error);
            } finally {
                try {
                    if (lock != null && lock.isHeld()) lock.release();
                } catch (Throwable ignored) {} result.finish();
            }
        }, "soufit-streak-alarm");
        thread.setDaemon(true);
        thread.start();
    }

    private static boolean attempt(Context app) {
        boolean ready = Streaks.ready(WAIT);
        boolean retry = Streaks.runNow(app);
        if (retry && !ready && StreakSchedule.minute() >= 0) nudge(app);
        if (retry) watchNetwork(app);
        else unwatchNetwork(app);
        return retry;
    }

    public static boolean online(Context c) {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            if (Build.VERSION.SDK_INT >= 23) {
                android.net.Network network = cm.getActiveNetwork();
                if (network == null) return false;
                android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                return caps != null && caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Throwable error) {
            return true;
        }
    }

    private static PendingIntent netIntent(Context c) {
        Intent i = new Intent(c, StreakAlarm.class).setAction(NET);
        int f = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
        return PendingIntent.getBroadcast(c, REQUEST + 2, i, f);
    }

    public static void watchNetwork(Context c) {
        try {
            if (Build.VERSION.SDK_INT < 26 || online(c)) return;
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return;
            cm.registerNetworkCallback(new android.net.NetworkRequest.Builder().addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(), netIntent(c));
        } catch (Throwable error) {
            Diary.note("streak network watch: " + error);
        }
    }

    public static void unwatchNetwork(Context c) {
        try {
            if (Build.VERSION.SDK_INT < 26) return;
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) cm.unregisterNetworkCallback(netIntent(c));
        } catch (Throwable ignored) {}
    }

    private static PendingIntent intent(Context c, int flags) {
        Intent i = new Intent(c, StreakAlarm.class).setAction(FIRE);
        int f = flags | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        return PendingIntent.getBroadcast(c, REQUEST, i, f);
    }

    public static boolean pending(Context c) {
        return intent(c, PendingIntent.FLAG_NO_CREATE) != null;
    }

    public static void disable(Context c) {
        try {
            AlarmManager alarms = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            if (alarms != null) alarms.cancel(intent(c, PendingIntent.FLAG_UPDATE_CURRENT));
        } catch (Throwable ignored) {} unwatchNetwork(c);
        setReceiver(c, false);
    }

    public static void enable(Context c) {
        setReceiver(c, true);
    }

    private static void setReceiver(Context c, boolean on) {
        try {
            c.getPackageManager().setComponentEnabledSetting(new ComponentName(c, StreakAlarm.class), on ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Throwable error) {
            Diary.note("streak receiver: " + error);
        }
    }

    public static void schedule(Context c, boolean retry) {
        Streaks.rescheduleForeground();
        try {
            AlarmManager alarms = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            if (alarms == null) return;
            PendingIntent pi = intent(c, PendingIntent.FLAG_UPDATE_CURRENT);
            if (!Streaks.isEnabled()) {
                alarms.cancel(pi);
                lastMode = "off";
                return;
            }
            long at = StreakSchedule.next(System.currentTimeMillis(), StreakSchedule.minute(), retry, CADENCE);
            try {
                c.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putLong(KEY_ARMED, System.currentTimeMillis())
                        .apply();
            } catch (Throwable ignored) {} if (exactAllowed(c)) {
                try {
                    if (Build.VERSION.SDK_INT >= 23) alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
                    else alarms.setExact(AlarmManager.RTC_WAKEUP, at, pi);
                    exactAt = at;
                    lastDenied = 0;
                    lastMode = "exact";
                    return;
                } catch (SecurityException denied) {
                    lastDenied = System.currentTimeMillis();
                    Diary.note("streak alarm: the system refused an exact alarm");
                }
            }
            if (Build.VERSION.SDK_INT >= 23) alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            else alarms.set(AlarmManager.RTC_WAKEUP, at, pi);
            lastMode = "inexact";
        } catch (Throwable error) {
            Diary.note("streak alarm schedule: " + error);
        }
    }

    public static void permissions(Context c) {
        try {
            if (c == null) return;
            if (Build.VERSION.SDK_INT >= 31) {
                AlarmManager alarms = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                if (alarms != null && !alarms.canScheduleExactAlarms()) {
                    open(c, new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + c.getPackageName())));
                    return;
                }
            }
            if (Build.VERSION.SDK_INT >= 23) {
                PowerManager power = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                android.content.SharedPreferences prefs = c.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
                if (power != null && !power.isIgnoringBatteryOptimizations(c.getPackageName()) && !prefs.getBoolean("streak_battery_asked",
                        false)) {
                    prefs.edit().putBoolean("streak_battery_asked", true).apply();
                    open(c, new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + c.getPackageName())));
                }
            }
        } catch (Throwable error) {
            Diary.note("streak permissions: " + error);
        }
    }

    private static void open(Context c, Intent intent) {
        if (!(c instanceof android.app.Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(intent);
    }

    private static void nudge(Context app) {
        try {
            android.content.SharedPreferences prefs = app.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
            long now = System.currentTimeMillis();
            if (StreakSchedule.sameDay(now, prefs.getLong("streak_nudge_at", 0))) return;
            Intent open = app.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
            if (open == null) return;
            if (!notificationsAllowed(app)) {
                Diary.note("streak nudge: notifications are not allowed for this app");
                return;
            }
            prefs.edit().putLong("streak_nudge_at", now).apply();
            NotificationManager manager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;
            channel(app);
            PendingIntent tap = PendingIntent.getActivity(app, REQUEST + 1, open, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23
                    ? PendingIntent.FLAG_IMMUTABLE : 0));
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(app, CHANNEL) : new Notification.Builder(app);
            b.setSmallIcon(android.R.drawable.stat_notify_chat).setContentTitle(Local.of("Streak is waiting to be extended")).setContentText(Local.of("Open TikTok: auto-extend will run right away"))
                    .setContentIntent(tap).setAutoCancel(true).setPriority(Notification.PRIORITY_HIGH);
            manager.notify(REQUEST, b.build());
        } catch (Throwable error) {
            Diary.note("streak nudge: " + error);
        }
    }

    public static String status(Context c) {
        try {
            if (!Streaks.isEnabled()) return Local.of("Auto-extend is off");
            List<String> problems = new ArrayList<String>();
            if (!exactAllowed(c)) {
                problems.add(Local.of("exact alarms are not allowed (Android 12+: “Alarms & reminders”)"));
            }
            try {
                PowerManager power = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                if (power != null && !power.isIgnoringBatteryOptimizations(c.getPackageName())) {
                    problems.add(Local.of("battery saving is not turned off for the app"));
                }
            } catch (Throwable ignored) {} if (!notificationsAllowed(c)) problems.add(Local.of("app notifications are off"));
            int minute = StreakSchedule.minute();
            if (minute < 0) problems.add(Local.of("no time chosen"));
            if (problems.isEmpty()) {
                return Local.of("Done: sending at ") + String.format(java.util.Locale.ROOT, "%02d:%02d", minute / 60, minute % 60);
            }
            StringBuilder out = new StringBuilder(Local.of("Check: "));
            for (int i = 0; i < problems.size(); i ++) {
                if (i > 0) out.append("; ");
                out.append(problems.get(i));
            }
            return out.toString();
        } catch (Throwable error) {
            return Local.of("State unknown");
        }
    }
}

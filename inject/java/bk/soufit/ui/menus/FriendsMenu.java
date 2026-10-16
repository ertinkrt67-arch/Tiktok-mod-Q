package bk.soufit.ui.menus;
import bk.soufit.ui.SettingsUI;
import android.widget.LinearLayout;
import android.widget.Toast;
import bk.soufit.core.Local;
import bk.soufit.core.Text;
import bk.soufit.feed.Feed;
import bk.soufit.profile.AccountAppearance;
import bk.soufit.social.ModNotifications;
import bk.soufit.social.PinnedFriends;
import bk.soufit.social.StreakAlarm;
import bk.soufit.social.StreakSchedule;
import bk.soufit.social.Streaks;
import bk.soufit.ui.Popup;
import bk.soufit.ui.SettingsActivity;
public final class FriendsMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        column.addView(SettingsUI.section(host, Text.SUGGESTED_ACCOUNTS));
        LinearLayout suggested = SettingsUI.card(host);
        suggested.addView(SettingsUI.liveToggleRow(host, "visibility_off", Text.HIDE_SUGGESTED_ACCOUNTS, Feed.hides(Feed.KEY_SUGGESTIONS),
                on -> Feed.setHides(Feed.KEY_SUGGESTIONS, on)));
        suggested.addView(SettingsUI.quiet(host, Text.HIDE_SUGGESTED_ACCOUNTS_NOTE));
        column.addView(SettingsUI.wrap(host, suggested));
        column.addView(SettingsUI.section(host, Text.PINNED_FRIENDS));
        LinearLayout friends = SettingsUI.card(host);
        friends.addView(SettingsUI.actionRow(host, "people", Text.PINNED_FRIENDS, null, () -> PinnedFriends.show(host)));
        column.addView(SettingsUI.wrap(host, friends));
        column.addView(SettingsUI.section(host, Text.STREAKS));
        LinearLayout streaks = SettingsUI.card(host);
        streaks.addView(SettingsUI.betaRow(host, "repeat", Text.STREAK_AUTO, Streaks.isEnabled(), Streaks::setEnabled));
        streaks.addView(SettingsUI.line(host));
        streaks.addView(SettingsUI.liveToggleRow(host, "people", Text.STREAK_PINNED_ONLY, Streaks.pinnedOnly(), Streaks::setPinnedOnly));
        streaks.addView(SettingsUI.line(host));
        streaks.addView(SettingsUI.actionRow(host, "bug_report", Text.STREAK_HEALTH, StreakAlarm.status(host), () -> {
            StreakAlarm.permissions(host);
            Toast.makeText(host, StreakAlarm.status(host), Toast.LENGTH_LONG).show();
        }));
        streaks.addView(SettingsUI.line(host));
        streaks.addView(SettingsUI.actionRow(host, "schedule", Local.of("Sent time"), StreakSchedule.label(), () -> StreakSchedule.choose(host,
                host::rebuild)));
        streaks.addView(host.stickerHead());
        if (host.streakOpen) {
            streaks.addView(SettingsUI.line(host));
            streaks.addView(SettingsUI.appear(host, host.stickerChoices()));
        }
        streaks.addView(SettingsUI.line(host));
        streaks.addView(SettingsUI.actionRow(host, "play_circle", Text.STREAK_ALL, Text.STREAK_ALL_NOTE, () -> Popup.ask(host,
                Text.STREAK_ALL, Text.STREAK_ALL_ASK, Text.STREAK_ALL_SEND, () -> Streaks.sendAll(host), Text.CANCEL, null,
                null, null)));
        streaks.addView(SettingsUI.line(host));
        streaks.addView(SettingsUI.actionRow(host, "play_circle", Text.STREAK_TEST, Text.STREAK_TEST_NOTE, () -> Popup.ask(host,
                Text.STREAK_TEST, Text.STREAK_TEST_ASK, Text.STREAK_ALL_SEND, () -> {
            Streaks.test(host);
            Toast.makeText(host, Text.STREAK_TEST_GOING, Toast.LENGTH_SHORT).show();
        }, Text.CANCEL, null, null, null)));
        column.addView(SettingsUI.wrap(host, streaks));
        column.addView(SettingsUI.section(host, Text.NOTIFICATIONS));
        LinearLayout notifications = SettingsUI.card(host);
        notifications.addView(SettingsUI.liveToggleRow(host, "chat", Text.NOTIFY_MESSAGES, ModNotifications.option(0), on -> {
            ModNotifications.set(ModNotifications.MESSAGES, on);
            host.rebuildAfterSwitchAnimation();
        }));
        notifications.addView(SettingsUI.liveToggleRow(host, "notifications", Text.NOTIFY_PROMOS, ModNotifications.option(1),
                on -> {
            ModNotifications.set(ModNotifications.PROMOS, on);
            host.rebuildAfterSwitchAnimation();
        }));
        notifications.addView(SettingsUI.liveToggleRow(host, "notifications", Text.NOTIFY_OTHER, ModNotifications.option(2),
                on -> {
            ModNotifications.set(ModNotifications.OTHER, on);
            host.rebuildAfterSwitchAnimation();
        }));
        notifications.addView(SettingsUI.liveToggleRow(host, "notifications_off", Text.NOTIFY_NO_PROMPTS, ModNotifications.noPrompts(),
                on -> {
            AccountAppearance.prefs(host).edit().putBoolean(ModNotifications.NO_PROMPTS, on).apply();
            ModNotifications.reload();
        }));
        notifications.addView(SettingsUI.actionRow(host, "settings", Text.NOTIFY_SETTINGS, null, () -> ModNotifications.settings(host)));
        column.addView(SettingsUI.wrap(host, notifications));
    }
}

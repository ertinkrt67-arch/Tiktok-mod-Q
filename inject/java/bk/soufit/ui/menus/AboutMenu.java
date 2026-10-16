package bk.soufit.ui.menus;
import android.widget.LinearLayout;
import bk.soufit.core.Text;
import bk.soufit.profile.Account;
import bk.soufit.ui.SettingsActivity;
import bk.soufit.ui.SettingsUI;
public final class AboutMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        column.addView(host.aboutIntro());
        column.addView(SettingsUI.section(host, Text.CHANNEL));
        column.addView(host.channelBanner());
        column.addView(SettingsUI.section(host, Text.LINKS));
        LinearLayout links = SettingsUI.card(host);
        links.addView(SettingsUI.linkRow(host, "group", Text.FORUM, "@margeletforum", SettingsActivity.FORUM));
        links.addView(SettingsUI.line(host));
        links.addView(SettingsUI.linkRow(host, "extension", Text.SOURCE, "narezany/MargyT", SettingsActivity.GIT));
        links.addView(SettingsUI.line(host));
        links.addView(SettingsUI.linkRow(host, "extension", Text.SOUFIT_SOURCE, "soufied/soufit", SettingsActivity.SOUFIT_GIT));
        column.addView(SettingsUI.wrap(host, links));
        column.addView(SettingsUI.section(host, Text.THANKS));
        column.addView(SettingsUI.wrap(host, host.thanks()));
    }

    public static void donatePage(SettingsActivity host) {
        LinearLayout what = SettingsUI.card(host);
        what.addView(SettingsUI.quiet(host, Text.DONATE_BANNER_TEXT));
        host.column.addView(SettingsUI.wrap(host, what));
        host.column.addView(SettingsUI.section(host, Text.DONATE_STEP_PAY));
        LinearLayout pay = SettingsUI.card(host);
        pay.addView(SettingsUI.linkRow(host, "star", Text.YOOMONEY, Text.YOOMONEY_NOTE, SettingsActivity.YOOMONEY));
        host.column.addView(SettingsUI.wrap(host, pay));
        host.column.addView(SettingsUI.section(host, Text.DONATE_STEP_ID));
        LinearLayout mine = SettingsUI.card(host);
        mine.addView(SettingsUI.idRow(host, Text.ACCOUNT_ID, Account.id()));
        mine.addView(SettingsUI.quiet(host, Text.DONATE_ID_NOTE));
        host.column.addView(SettingsUI.wrap(host, mine));
        host.column.addView(SettingsUI.section(host, Text.DONATE_STEP_WRITE));
        LinearLayout write = SettingsUI.card(host);
        write.addView(SettingsUI.actionRow(host, "chat", Text.DONATE_BANNER_WRITE, Text.DONATE_WRITE_NOTE, () -> host.open(SettingsActivity.CHANNEL_WRITE)));
        write.addView(SettingsUI.quiet(host, Text.DONATE_AFTER));
        host.column.addView(SettingsUI.wrap(host, write));
    }
}

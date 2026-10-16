package bk.soufit.ui.menus;
import bk.soufit.ui.SettingsUI;
import android.widget.LinearLayout;
import bk.soufit.core.Text;
import bk.soufit.media.Avatars;
import bk.soufit.media.Download;
import bk.soufit.media.FrameCapture;
import bk.soufit.media.Stickers;
import bk.soufit.profile.AccountAppearance;
import bk.soufit.ui.ModDialog;
import bk.soufit.ui.SettingsActivity;
public final class ToolsMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        column.addView(SettingsUI.section(host, Text.DOWNLOADS));
        LinearLayout downloads = SettingsUI.card(host);
        downloads.addView(SettingsUI.actionRow(host, "image", Text.FRAME_QUALITY, Integer.toString(FrameCapture.quality()),
                () -> {
            new ModDialog.Builder(host).setItems(new String[] {"480", "720", "1080"}, (dialog, index) -> {
                AccountAppearance.prefs(host).edit().putInt(FrameCapture.KEY, new int[] {480, 720, 1080}[index]).apply();
                host.rebuild();
            }).show();
        }));
        downloads.addView(SettingsUI.toggleRow(host, "image", Text.NO_WATERMARK, Download.isEnabled(), Download::setEnabled));
        downloads.addView(SettingsUI.line(host));
        downloads.addView(SettingsUI.toggleRow(host, "download", Text.DOWNLOAD_ALWAYS, Download.isAlways(), Download::setAlways));
        downloads.addView(SettingsUI.line(host));
        downloads.addView(SettingsUI.toggleRow(host, "place", Text.SAVE_AVATARS_ON, Avatars.isEnabled(), Avatars::setEnabled));
        downloads.addView(SettingsUI.line(host));
        downloads.addView(SettingsUI.toggleRow(host, "star", Text.SAVE_STICKERS_ON, Stickers.isEnabled(), Stickers::setEnabled));
        column.addView(SettingsUI.wrap(host, downloads));
        column.addView(SettingsUI.section(host, Text.BACKUP));
        LinearLayout backup = SettingsUI.card(host);
        backup.addView(SettingsUI.actionRow(host, "upload", Text.BACKUP_EXPORT, Text.BACKUP_EXPORT_NOTE, host::createBackup));
        backup.addView(SettingsUI.line(host));
        backup.addView(SettingsUI.actionRow(host, "download", Text.BACKUP_IMPORT, Text.BACKUP_IMPORT_NOTE, host::pickBackup));
        backup.addView(SettingsUI.line(host));
        backup.addView(SettingsUI.actionRow(host, "restore", Text.BACKUP_RESET, Text.BACKUP_RESET_NOTE, host::pickResetGroup));
        column.addView(SettingsUI.wrap(host, backup));
    }
}

package bk.soufit.ui.menus;
import bk.soufit.ui.SettingsUI;
import android.widget.LinearLayout;
import bk.soufit.core.Flags;
import bk.soufit.core.Text;
import bk.soufit.theme.Accent;
import bk.soufit.theme.AppearanceColors;
import bk.soufit.theme.AvatarShape;
import bk.soufit.theme.Corners;
import bk.soufit.theme.Loading;
import bk.soufit.theme.Md3;
import bk.soufit.theme.NavBar;
import bk.soufit.theme.StatusBar;
import bk.soufit.theme.Themes;
import bk.soufit.ui.SettingsActivity;
public final class AppearanceMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        column.addView(SettingsUI.section(host, Text.APPEARANCE));
        LinearLayout icons = SettingsUI.card(host);
        icons.addView(host.iconHead());
        if (host.iconOpen) {
            icons.addView(SettingsUI.line(host));
            icons.addView(SettingsUI.appear(host, host.iconChoices()));
        }
        column.addView(SettingsUI.wrap(host, icons));
        column.addView(SettingsUI.section(host, Text.ACCENT));
        LinearLayout accent = SettingsUI.card(host);
        accent.addView(host.accentHead());
        if (host.accentOpen) {
            if (Accent.fromWallpaper() != 0) {
                accent.addView(SettingsUI.line(host));
                accent.addView(SettingsUI.appear(host, host.wallpaperRow()));
            }
            accent.addView(SettingsUI.line(host));
            accent.addView(SettingsUI.appear(host, host.palette()));
        }
        column.addView(SettingsUI.wrap(host, accent));
        column.addView(SettingsUI.section(host, Text.LIQUID_GLASS));
        LinearLayout glass = SettingsUI.card(host);
        glass.addView(host.glassHead());
        if (host.glassOpen) {
            glass.addView(SettingsUI.line(host));
            glass.addView(SettingsUI.appear(host, host.glassChoices()));
        }
        column.addView(SettingsUI.wrap(host, glass));
        LinearLayout fullAmoled = SettingsUI.card(host);
        fullAmoled.addView(SettingsUI.toggleRow(host, "palette", Text.FULL_AMOLED, Themes.isAmoled(), on -> {
            if (on) Themes.setFullAmoled(true);
            else Themes.setAmoled(false);
            host.markChanged();
            host.rebuildAfterSwitchAnimation();
        }));
        fullAmoled.addView(SettingsUI.quiet(host, Text.FULL_AMOLED_NOTE));
        column.addView(SettingsUI.wrap(host, fullAmoled));
        LinearLayout loading = SettingsUI.card(host);
        loading.addView(SettingsUI.liveToggleRow(host, "palette", Text.LOADING_SHAPES, Loading.isEnabled(), Loading::setEnabled));
        loading.addView(SettingsUI.quiet(host, Text.LOADING_NOTE));
        loading.addView(SettingsUI.line(host));
        loading.addView(SettingsUI.toggleRow(host, "image", Text.ROUND_CARDS, Corners.isEnabled(), Corners::setEnabled));
        loading.addView(SettingsUI.line(host));
        loading.addView(SettingsUI.actionRow(host, "account_circle", Text.AVATAR_SHAPE, AvatarShape.nameOf(AvatarShape.chosen()),
                host::chooseAvatarShape));
        loading.addView(SettingsUI.line(host));
        loading.addView(SettingsUI.toggleRow(host, "view_compact", Text.NEW_PROFILE, Flags.isOn(Flags.KEY_NEW_PROFILE), on -> Flags.set(Flags.KEY_NEW_PROFILE,
                on)));
        loading.addView(SettingsUI.line(host));
        loading.addView(SettingsUI.toggleRow(host, "visibility_off", Text.AUTO_HIDE_BAR, NavBar.isAutoHide(), NavBar::setAutoHide));
        loading.addView(SettingsUI.line(host));
        loading.addView(SettingsUI.toggleRow(host, "tune", Text.MD3_ICONS, Md3.isOn(Md3.KEY_ICONS), on -> Md3.set(Md3.KEY_ICONS,
                on)));
        column.addView(SettingsUI.wrap(host, loading));
        column.addView(SettingsUI.section(host, Text.THEME));
        LinearLayout theme = SettingsUI.card(host);
        if (Themes.available()) {
            theme.addView(SettingsUI.toggleRow(host, "wallpaper", Text.THEME_MATERIAL, Themes.isEnabled() && Themes.isMaterial(),
                    on -> {
                Themes.setMaterial(on);
                Themes.setEnabled(on);
                host.markChanged();
                host.rebuildAfterSwitchAnimation();
            }));
            theme.addView(SettingsUI.quiet(host, Text.THEME_NOTE));
        } else {
            theme.addView(SettingsUI.quiet(host, Text.THEME_OLD));
        }
        theme.addView(SettingsUI.line(host));
        theme.addView(SettingsUI.toggleRow(host, "visibility_off", Text.STATUS_HIDDEN, StatusBar.isOn(), on -> {
            StatusBar.setOn(on);
            host.markChanged();
        }));
        theme.addView(SettingsUI.line(host));
        theme.addView(SettingsUI.actionRow(host, "palette", Text.THEME_PRESETS, Text.THEME_PRESETS_NOTE, host::chooseThemePreset));
        theme.addView(SettingsUI.line(host));
        theme.addView(host.themeColoursHead());
        if (host.themeColoursOpen) {
            theme.addView(SettingsUI.line(host));
            for (String role : AppearanceColors.ROLES) {
                theme.addView(host.appearanceColourRow(role));
                theme.addView(SettingsUI.line(host));
            }
        }
        column.addView(SettingsUI.wrap(host, theme));
        column.addView(SettingsUI.section(host, Text.FONT));
        LinearLayout fonts = SettingsUI.card(host);
        fonts.addView(host.fontHead());
        if (host.fontOpen) {
            fonts.addView(SettingsUI.line(host));
            fonts.addView(SettingsUI.appear(host, host.fontChoices()));
        }
        column.addView(SettingsUI.wrap(host, fonts));
    }
}

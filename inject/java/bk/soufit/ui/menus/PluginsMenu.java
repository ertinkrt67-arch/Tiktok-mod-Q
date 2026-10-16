package bk.soufit.ui.menus;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import bk.soufit.core.Diary;
import bk.soufit.core.Local;
import bk.soufit.core.Net;
import bk.soufit.core.Text;
import bk.soufit.plugins.AdminAccess;
import bk.soufit.plugins.PluginAdmin;
import bk.soufit.plugins.PluginAudit;
import bk.soufit.plugins.PluginEditorActivity;
import bk.soufit.plugins.Plugins;
import bk.soufit.plugins.ScriptPlugins;
import bk.soufit.theme.Accent;
import bk.soufit.ui.Dot;
import bk.soufit.ui.M3Switch;
import bk.soufit.ui.ModDialog;
import bk.soufit.ui.Motion;
import bk.soufit.ui.Panel;
import bk.soufit.ui.Screen;
import bk.soufit.ui.SettingsActivity;
import bk.soufit.ui.SettingsGlyph;
import bk.soufit.ui.SettingsUI;
import java.util.List;
import java.util.Locale;
public final class PluginsMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        List<Plugins.Info> installed = Plugins.list();
        List<ScriptPlugins.Program> commands = ScriptPlugins.list(host);
        int total = installed.size() + commands.size();
        int on = 0;
        for (Plugins.Info info : installed) if (info.trouble == null && Plugins.isEnabled(info.id)) on ++;
        for (ScriptPlugins.Program program : commands) if (scriptOn(host, program)) on ++;
        column.addView(SettingsUI.section(host, Text.PLUGINS));
        LinearLayout head = SettingsUI.card(host);
        head.addView(SettingsUI.caption(host, String.format(Locale.ROOT, Text.PLUGINS_SUMMARY, total, on)));
        head.addView(SettingsUI.line(host));
        head.addView(SettingsUI.actionRow(host, "add", Local.of("Add plugin"), null, () -> showPluginAdd(host)));
        column.addView(SettingsUI.wrap(host, head));
        if (total == 0) {
            LinearLayout empty = SettingsUI.card(host);
            empty.addView(SettingsUI.quiet(host, Text.PLUGINS_EMPTY));
            column.addView(SettingsUI.wrap(host, empty));
        } else {
            LinearLayout list = SettingsUI.card(host);
            boolean first = true;
            for (ScriptPlugins.Program program : commands) {
                if (!first) list.addView(SettingsUI.line(host));
                first = false;
                list.addView(scriptRow(host, program));
            }
            for (Plugins.Info info : installed) {
                if (!first) list.addView(SettingsUI.line(host));
                first = false;
                list.addView(pluginRow(host, info));
            }
            column.addView(SettingsUI.wrap(host, list));
        }
        LinearLayout docs = SettingsUI.card(host);
        docs.addView(SettingsUI.actionRow(host, "article", Text.PLUGIN_DOCS, Text.PLUGIN_DOCS_NOTE, () -> host.open(DOCS_PLUGINS)));
        column.addView(SettingsUI.wrap(host, docs));
    }

    private static View scriptRow(final SettingsActivity host, final ScriptPlugins.Program program) {
        return SettingsUI.actionRow(host, "extension", program.name, scriptOn(host, program) ? Text.PLUGIN_ON : Text.PLUGIN_OFF,
                () -> ScriptPlugins.details(host, program, host::rebuild));
    }

    private static boolean scriptOn(SettingsActivity host, ScriptPlugins.Program program) {
        return host.getSharedPreferences(ScriptPlugins.STORE, 0).getBoolean("on:" + program.id, false);
    }

    public static final String DOCS_PLUGINS = "https://git.narez.xyz/narezany/MargyT/src/branch/main/docs/plugins.md";
    public static void showPluginAdd(SettingsActivity host) {
        Panel panel = Panel.with(host, host.skin, Local.of("Add plugin"));
        panel.primary(Local.of("Create from buttons"), () -> ScriptPlugins.edit(host, ScriptPlugins.SAMPLE, host::rebuild));
        panel.quiet(Local.of("Paste JSON"), () -> ScriptPlugins.edit(host, "", host::rebuild));
        panel.quiet(Local.of("Install .mtp file"), () -> pickPlugin(host));
        panel.quiet(Local.of("Documentation"), () -> host.startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(DOCS_PLUGINS))));
        panel.quiet(Text.CLOSE, null);
        panel.show();
    }

    public static final int PICK_PLUGIN = 0x4D50;
    public static View pluginSettingRow(SettingsActivity host, final Plugins.Row item, final android.content.SharedPreferences store) {
        switch (item.kind) {
            case Plugins.Row.SECTION: return SettingsUI.caption(host, item.title);
            case Plugins.Row.TOGGLE: return SettingsUI.toggleRow(host, "extension", item.title, store.getBoolean(item.key,
                    item.def != 0), on -> {
                store.edit().putBoolean(item.key, on).apply();
                Plugins.settingsChanged(item.plugin, item.key);
            });
            case Plugins.Row.CHOICE: {final String[] options = item.options; if (options == null || options.length == 0) return null; int now = Math.max(0,
                    Math.min(options.length - 1, store.getInt(item.key, item.def))); return SettingsUI.actionRow(host, "tune",
                    item.title, options[now], () -> new ModDialog.Builder(host, host.skin).setTitle(item.title).setSingleChoiceItems(options,
                    Math.max(0, Math.min(options.length - 1, store.getInt(item.key, item.def))), (dialog, which) -> {
                store.edit().putInt(item.key, which).apply();
                Plugins.settingsChanged(item.plugin, item.key);
                host.markChanged();
                dialog.dismiss();
                host.rebuild();
            }).show());} case Plugins.Row.SLIDER: {int now = Math.max(item.min, Math.min(item.max, store.getInt(item.key,
                    item.def))); return SettingsUI.slider(host, item.title, now - item.min, item.max - item.min, item.min,
                    "", value -> {
                store.edit().putInt(item.key, value + item.min).apply();
                Plugins.settingsChanged(item.plugin, item.key);
                host.markChanged();
            });} default: return SettingsUI.actionRow(host, "extension", item.title, item.detail, () -> {
                try {
                    if (item.action != null) item.action.tapped();
                } catch (Throwable error) {
                    Diary.note("plugin " + item.plugin + ": " + error);
                }
            });
        }
    }

    public static View installRow(SettingsActivity host) {
        LinearLayout row = SettingsUI.row(host);
        row.addView(SettingsUI.icon(host, "extension"));
        LinearLayout text = new LinearLayout(host);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(host, Text.PLUGIN_INSTALL));
        text.addView(SettingsUI.detail(host, Text.PLUGIN_INSTALL_NOTE));
        row.addView(text, SettingsUI.grow());
        TextView plus = new TextView(host);
        plus.setText("+");
        plus.setTextColor(Accent.colour());
        plus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        plus.setPadding(host.dp(12), 0, 0, 0);
        row.addView(plus);
        row.setOnClickListener(v -> pickPlugin(host));
        return SettingsUI.sized(host, row, 60);
    }

    public static View pluginRow(final SettingsActivity host, final Plugins.Info info) {
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(host.dp(12), host.dp(10), host.dp(4), host.dp(10));
        row.setMinimumHeight(host.dp(64));
        Bitmap icon = info.icon();
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(host.dp(40), host.dp(40));
        size.rightMargin = host.dp(14);
        if (icon != null) {
            ImageView view = new ImageView(host);
            view.setImageBitmap(icon);
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(view, size);
        } else {
            row.addView(new Dot(host, Accent.colour(), false), size);
        }
        LinearLayout text = new LinearLayout(host);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(host, info.name + "  " + info.version));
        TextView status = SettingsUI.detail(host, statusOf(info));
        status.setTextColor(info.trouble != null ? host.skin.text : Plugins.isEnabled(info.id) ? Accent.colour() : host.skin.muted());
        text.addView(status);
        String by = info.author;
        if (!info.description.isEmpty()) by = by + "  ·  " + info.description;
        if (by.length() > 0) text.addView(SettingsUI.detail(host, by));
        if (info.trouble != null) text.addView(SettingsUI.detail(host, info.trouble));
        row.addView(text, SettingsUI.grow());
        final M3Switch toggle = new M3Switch(host);
        toggle.colours(Accent.colour(), host.skin.muted(), host.skin.card);
        toggle.setChecked(Plugins.isEnabled(info.id));
        toggle.setEnabled(true);
        toggle.setOnChanged(checked -> {
            activatePlugin(host, info, checked);
        });
        row.addView(toggle);
        ImageView more = new ImageView(host);
        more.setImageDrawable(SettingsGlyph.make("settings", host.skin.muted()));
        more.setPadding(host.dp(8), host.dp(8), host.dp(8), host.dp(8));
        more.setContentDescription(Text.PLUGIN_MANAGE);
        more.setClickable(true);
        more.setOnClickListener(v -> openActions(host, info));
        row.addView(more, new LinearLayout.LayoutParams(host.dp(40), host.dp(40)));
        row.setOnClickListener(v -> openPluginSettings(host, info));
        Motion.press(row);
        row.setOnLongClickListener(v -> {
            openActions(host, info);
            return true;
        });
        return row;
    }

    private static String statusOf(Plugins.Info info) {
        if (info.trouble != null) return Text.PLUGIN_PROBLEM;
        return Plugins.isEnabled(info.id) ? Text.PLUGIN_ON : Text.PLUGIN_OFF;
    }

    private static void openActions(SettingsActivity host, final Plugins.Info info) {
        Panel actions = Panel.with(host, host.skin, info.name).compact();
        actions.primary(Local.of("Edit"), () -> PluginEditorActivity.binary(host, info.id));
        actions.quiet(Local.of("Remove from device"), () -> askToRemove(host, info));
        if (AdminAccess.current()) actions.quiet(Local.of("Manage store"), () -> PluginAdmin.show(host));
        actions.quiet(Text.CLOSE, null);
        actions.show();
    }

    public static void activatePlugin(SettingsActivity host, final Plugins.Info info, boolean enabled) {
        if (!enabled) {
            Net.away("plugin stop", () -> {
                Plugins.setEnabled(info.id, false);
                host.runOnUiThread(() -> {
                    if (!host.isFinishing()) {
                        host.markChanged();
                        host.rebuild();
                    }
                });
            });
            return;
        }
        Net.away("plugin start", () -> {
            Plugins.setEnabled(info.id, true);
            host.runOnUiThread(() -> {
                if (!host.isFinishing()) {
                    host.markChanged();
                    host.rebuild();
                }
            });
        });
    }

    public static void openPluginSettings(SettingsActivity host, final Plugins.Info info) {
        if (Plugins.isEnabled(info.id) && Plugins.openSettings(info.id, host)) return;
        Panel panel = Panel.with(host, host.skin, info.name);
        if (info.safety != null && !info.safety.isEmpty()) panel.text(info.safety);
        LinearLayout body = new LinearLayout(host);
        body.setOrientation(LinearLayout.VERTICAL);
        android.content.SharedPreferences store = host.getSharedPreferences("margyt_plugin_" + info.id, host.MODE_PRIVATE);
        int count = 0;
        if (Plugins.isEnabled(info.id)) for (Plugins.Row item : Plugins.rows()) {
            if (!info.id.equals(item.plugin)) continue;
            try {
                View control = pluginSettingRow(host, item, store);
                if (control != null) {
                    body.addView(control);
                    count ++;
                }
            } catch (Throwable error) {
                Diary.note("plugin settings " + info.id + ": " + error);
            }
        }
        if (count == 0) {
            if (!Plugins.isEnabled(info.id)) panel.text(Local.of("Turn the plugin on to open its settings."));
            else if (Plugins.isRunning(info.id)) panel.text(Local.of("The plugin provides no settings. Version ") + info.version);
            else {
                String why = Plugins.problem(info.id);
                panel.text(why != null ? Local.of("The plugin is on but not running: ") + why : Local.of("The plugin is on but has not started in this session yet."));
                panel.primary(Local.of("Restart"), () -> Net.away("plugin revive", () -> {
                    final boolean ok = Plugins.revive(info.id);
                    host.runOnUiThread(() -> {
                        if (host.isFinishing()) return;
                        host.rebuild();
                        if (ok) openPluginSettings(host, info);
                    });
                }));
            }
        } else panel.view(body);
        panel.quiet(Local.of("Back"), null).show();
    }

    public static void pickPlugin(SettingsActivity host) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            host.startActivityForResult(intent, PICK_PLUGIN);
        } catch (Throwable error) {
            Toast.makeText(host, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    public static void reviewPlugin(SettingsActivity host, final Uri source) {
        Net.away("plugin inspection", () -> {
            try {
                final PluginAudit.Report report = Plugins.inspect(host, source);
                host.runOnUiThread(() -> {
                    if (host.isFinishing()) return;
                    Panel panel = Panel.with(host, host.skin, Text.PLUGIN_REVIEW).text(report.summary());
                    Runnable install = () -> Net.away("plugin install", () -> {
                        try {
                            Plugins.install(host, source, report.digest);
                            host.runOnUiThread(() -> {
                                if (!host.isFinishing()) {
                                    host.markChanged();
                                    host.rebuild();
                                }
                            });
                            Screen.say(Text.PLUGIN_INSTALLED);
                        } catch (Throwable error) {
                            Screen.say(String.valueOf(error.getMessage()));
                        }
                    });
                    panel.primary(Text.PLUGIN_INSTALL, () -> {
                        if (!report.highRisk()) {
                            install.run();
                            return;
                        }
                        new ModDialog.Builder(host, host.skin).setTitle(Text.PLUGIN_RISK_TITLE).setMessage(Text.PLUGIN_RISK_ASK)
                                .setNegativeButton(Text.CANCEL, null).setPositiveButton(Text.PLUGIN_RISK_INSTALL, (dialog,
                                which) -> install.run()).show();
                    });
                    panel.quiet(Local.of("Back"), null).show();
                });
            } catch (Throwable error) {
                host.runOnUiThread(() -> {
                    if (!host.isFinishing()) Panel.with(host, host.skin, Local.of("Plugin rejected")).text(String.valueOf(error.getMessage()))
                            .quiet(Local.of("Back"), null).show();
                });
            }
        });
    }

    public static void askToRemove(SettingsActivity host, final Plugins.Info info) {
        try {
            new ModDialog.Builder(host, host.skin).setTitle(Text.PLUGIN_REMOVE_ASK).setMessage(info.name + "  " + info.version)
                    .setNegativeButton(Text.CANCEL, null).setPositiveButton(Text.PLUGIN_REMOVE, (dialog, which) -> {
                Plugins.uninstall(host, info.id);
                host.markChanged();
                host.rebuild();
            }).show();
        } catch (Throwable error) {
            Toast.makeText(host, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }
}

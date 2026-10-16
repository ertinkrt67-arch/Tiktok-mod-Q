package bk.soufit.ui.menus;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import bk.soufit.core.CrashLog;
import bk.soufit.core.DevMode;
import bk.soufit.core.Diagnostics;
import bk.soufit.core.Diary;
import bk.soufit.core.FlagGuard;
import bk.soufit.core.Flags;
import bk.soufit.core.Local;
import bk.soufit.core.Text;
import bk.soufit.core.soufiT;
import bk.soufit.theme.Accent;
import bk.soufit.ui.DevPill;
import bk.soufit.ui.Expressive;
import bk.soufit.ui.Panel;
import bk.soufit.ui.Screen;
import bk.soufit.ui.SettingsActivity;
import bk.soufit.ui.SettingsUI;
import java.util.List;
import java.util.Locale;
public final class DeveloperMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        if (DevMode.isOn()) {
            column.addView(SettingsUI.section(host, Text.DIAGNOSTICS));
            LinearLayout diagnostics = SettingsUI.card(host);
            diagnostics.addView(SettingsUI.quiet(host, Diagnostics.report()));
            diagnostics.addView(SettingsUI.line(host));
            diagnostics.addView(SettingsUI.liveToggleRow(host, "bug_report", Local.of("Crash log on this phone"), CrashLog.enabled(host),
                    on -> CrashLog.set(host, on)));
            diagnostics.addView(SettingsUI.quiet(host, Local.of("Written only to this phone. Never sent anywhere.")));
            diagnostics.addView(SettingsUI.actionRow(host, "article", Local.of("Copy crash log"), CrashLog.summary(host),
                    () -> host.copy(Local.of("Crash log"), CrashLog.read(host))));
            diagnostics.addView(SettingsUI.actionRow(host, "info", Local.of("Clear crash log"), null, () -> {
                CrashLog.clear(host);
                host.rebuild();
            }));
            diagnostics.addView(SettingsUI.line(host));
            diagnostics.addView(SettingsUI.actionRow(host, "article", Text.COPY_DIAGNOSTICS, null, () -> host.copy("soufiT diagnostics",
                    DevMode.isOn() ? Diagnostics.fullReport() : Diagnostics.report())));
            diagnostics.addView(SettingsUI.line(host));
            diagnostics.addView(SettingsUI.liveToggleRow(host, "article", Text.DEV_PILL, DevPill.isOn(), DevPill::setOn));
            diagnostics.addView(SettingsUI.quiet(host, Text.DEV_PILL_NOTE));
            column.addView(SettingsUI.wrap(host, diagnostics));
            column.addView(SettingsUI.section(host, Text.DIARY));
            LinearLayout diary = SettingsUI.card(host);
            diary.addView(host.diaryHead());
            if (host.diaryOpen) {
                diary.addView(SettingsUI.line(host));
                diary.addView(SettingsUI.appear(host, host.diaryLines()));
            }
            if (DevMode.isOn()) host.addDeveloperRows(diary);
            column.addView(SettingsUI.wrap(host, diary));
        }
    }

    public static final int FLAG_ROWS = 60;
    public static void flagsPage(SettingsActivity host) {
        LinearLayout warning = SettingsUI.card(host);
        warning.addView(SettingsUI.quiet(host, Text.FLAGS_WARNING));
        host.column.addView(SettingsUI.wrap(host, warning));
        final java.util.Map<String, Object> mine = Flags.overrides();
        if (FlagGuard.heldBack() && !mine.isEmpty()) {
            LinearLayout held = SettingsUI.card(host);
            held.addView(SettingsUI.quiet(host, Text.FLAGS_HELD));
            held.addView(SettingsUI.actionRow(host, "restore", Text.FLAGS_AGAIN, null, () -> {
                FlagGuard.again();
                Flags.forget();
                SettingsActivity.restartOwed();
                host.rebuild();
            }));
            host.column.addView(SettingsUI.wrap(host, held));
        }
        if (host.flagList == null) {
            LinearLayout wait = SettingsUI.card(host);
            wait.addView(SettingsUI.quiet(host, Text.FLAGS_LOADING));
            host.column.addView(SettingsUI.wrap(host, wait));
            if (host.flagsLoading) return;
            host.flagsLoading = true;
            new Thread(() -> {
                final java.util.List<Flags.Known> all = Flags.catalogue();
                host.runOnUiThread(() -> {
                    host.flagList = all;
                    host.flagsLoading = false;
                    if (host.settingsGroup == SettingsActivity.FLAGS_PAGE && !host.isFinishing()) host.rebuild();
                });
            }, "soufit-flags").start();
            return;
        }
        host.column.addView(flagSearch(host));
        java.util.List<Flags.Known> shown = new java.util.ArrayList<Flags.Known>();
        int found = 0;
        for (Flags.Known flag : host.flagList) {
            boolean wanted = host.flagQuery.length() == 0 ? mine.containsKey(flag.key) : flag.key.toLowerCase(Locale.ROOT)
                    .contains(host.flagQuery);
            if (!wanted) continue;
            found ++;
            if (shown.size() < FLAG_ROWS) shown.add(flag);
        }
        host.column.addView(SettingsUI.section(host, host.flagQuery.length() == 0 ? Text.FLAGS_CHANGED : String.format(Locale.ROOT,
                Text.FLAGS_FOUND, found)));
        LinearLayout list = SettingsUI.card(host);
        if (host.flagList.isEmpty()) list.addView(SettingsUI.quiet(host, Text.FLAGS_NO_LIST));
        else if (shown.isEmpty()) list.addView(SettingsUI.quiet(host, host.flagQuery.length() == 0 ? String.format(Locale.ROOT,
                Text.FLAGS_NONE_CHANGED, host.flagList.size()) : Text.NOTHING_FOUND));
        for (int i = 0; i < shown.size(); i ++) {
            if (i > 0) list.addView(SettingsUI.line(host));
            list.addView(flagRow(host, shown.get(i), mine.get(shown.get(i).key)));
        }
        if (found > shown.size()) {
            list.addView(SettingsUI.quiet(host, String.format(Locale.ROOT, Text.FLAGS_MORE, shown.size())));
        }
        host.column.addView(SettingsUI.wrap(host, list));
        if (!mine.isEmpty()) {
            host.column.addView(SettingsUI.wrap(host, SettingsUI.actionRow(host, "restore", Text.FLAGS_RESET_ALL, null, () -> {
                Flags.clearOverrides();
                SettingsActivity.restartOwed();
                host.rebuild();
            })));
        }
    }

    public static View flagSearch(SettingsActivity host) {
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(host.dp(20), host.dp(4), host.dp(8), host.dp(4));
        GradientDrawable background = new GradientDrawable();
        background.setColor(host.skin.card);
        background.setCornerRadius(host.dp(28));
        row.setBackground(background);
        final EditText input = new EditText(host);
        input.setSingleLine(true);
        input.setText(host.flagQuery);
        input.setHint(Text.FLAGS_SEARCH);
        input.setHintTextColor(host.skin.muted());
        input.setTextColor(host.skin.text);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        input.setTypeface(Typeface.MONOSPACE);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        final Runnable look = () -> {
            host.flagQuery = input.getText().toString().trim().toLowerCase(Locale.ROOT);
            host.page.scrollTo(0, 0);
            host.rebuild();
        };
        input.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEARCH) return false;
            look.run();
            return true;
        });
        row.addView(input, SettingsUI.grow());
        TextView go = SettingsUI.label(host, host.flagQuery.length() == 0 ? Text.SEARCH : Text.SEARCH_CLEAR);
        go.setTextColor(host.accentInk());
        go.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        go.setGravity(Gravity.CENTER);
        go.setPadding(host.dp(16), 0, host.dp(16), 0);
        go.setBackground(Expressive.tile(host, host.accentTonal(), (Accent.colour() & 0xFFFFFF) | 0x33000000, host.dp(20),
                host.dp(20), host.dp(10)));
        go.setOnClickListener(v -> {
            if (host.flagQuery.length() > 0) input.setText("");
            look.run();
        });
        row.addView(go, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, host.dp(40)));
        LinearLayout box = new LinearLayout(host);
        box.setPadding(host.dp(12), host.dp(4), host.dp(12), host.dp(8));
        box.addView(SettingsUI.sized(host, row, 56));
        return box;
    }

    public static View flagRow(SettingsActivity host, final Flags.Known flag, final Object mine) {
        LinearLayout row = SettingsUI.row(host);
        row.setPadding(host.dp(16), host.dp(10), host.dp(16), host.dp(10));
        LinearLayout words = new LinearLayout(host);
        words.setOrientation(LinearLayout.VERTICAL);
        TextView name = SettingsUI.label(host, flag.key);
        name.setTypeface(Typeface.MONOSPACE);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        words.addView(name);
        final Object own = flag.settable() ? Flags.own(flag) : null;
        final Object theirs = own != null ? own : flag.fallback;
        String says = flag.type.toLowerCase(Locale.ROOT) + "  ·  TikTok: " + flagValue(theirs);
        if (mine != null) says += "  ·  " + Text.FLAGS_HERE + ": " + flagValue(mine);
        TextView about = SettingsUI.detail(host, says);
        about.setPadding(0, host.dp(4), 0, 0);
        if (mine != null) about.setTextColor(host.accentInk());
        words.addView(about);
        row.addView(words, SettingsUI.grow());
        row.setOnClickListener(v -> editFlag(host, flag, theirs, mine));
        return SettingsUI.sized(host, row, 56);
    }

    public static String flagValue(Object value) {
        if (value == null) return "—";
        String text = value instanceof Object[] ? java.util.Arrays.toString((Object[]) value) : String.valueOf(value);
        if (value instanceof String) text = '"' + text + '"';
        return text.length() > 48 ? text.substring(0, 48) + "…" : text;
    }

    public static void editFlag(SettingsActivity host, final Flags.Known flag, Object theirs, Object mine) {
        if (!flag.settable()) {
            Toast.makeText(host, Text.FLAGS_FIXED, Toast.LENGTH_SHORT).show();
            return;
        }
        final Panel panel = Panel.with(host, host.skin, flag.key);
        if (flag.type.equals("BOOLEAN")) {
            panel.option(Text.FLAGS_AS_TIKTOK + " (" + flagValue(theirs) + ")", mine == null, false, () -> answerFlag(host,
                    flag, null));
            panel.option(Text.FLAGS_ON, Boolean.TRUE.equals(mine), false, () -> answerFlag(host, flag, Boolean.TRUE));
            panel.option(Text.FLAGS_OFF, Boolean.FALSE.equals(mine), false, () -> answerFlag(host, flag, Boolean.FALSE));
            panel.quiet(Text.CLOSE, null);
            panel.show();
            return;
        }
        panel.text(flag.type.toLowerCase(Locale.ROOT) + "  ·  TikTok: " + flagValue(theirs));
        final EditText input = panel.field(String.valueOf(theirs));
        Object now = mine != null ? mine : theirs;
        if (now != null) input.setText(String.valueOf(now));
        if (!flag.type.equals("STRING")) {
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED | (flag.type.equals("FLOAT")
                    || flag.type.equals("DOUBLE") ? android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));
        }
        panel.primaryKeepOpen(Text.APPLY, () -> {
            Object value = Flags.parse(flag, input.getText().toString());
            if (value == null) {
                Screen.say(Text.FLAGS_BAD);
                return;
            }
            panel.close();
            answerFlag(host, flag, value);
        });
        if (mine != null) panel.quiet(Text.FLAGS_RESET, () -> answerFlag(host, flag, null));
        panel.quiet(Text.CLOSE, null);
        panel.show();
    }

    public static void answerFlag(SettingsActivity host, Flags.Known flag, Object value) {
        Flags.override(flag, value);
        Diary.note("flag " + flag.key + " = " + (value == null ? "TikTok's own" : value));
        SettingsActivity.restartOwed();
        host.rebuild();
    }
}

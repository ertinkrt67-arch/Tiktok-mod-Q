package bk.soufit.ui;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import bk.soufit.core.Icons;
import bk.soufit.core.Text;
import bk.soufit.theme.Accent;
import java.util.List;
public final class SettingsUI {
    public static View spacer(SettingsActivity host) {
        View view = new View(host);
        view.setLayoutParams(new LinearLayout.LayoutParams(host.dp(34), 1));
        return view;
    }

    public static View disclosure(SettingsActivity host, boolean expanded, String key) {
        host.renderDisclosureKey = expanded ? key : null;
        Chevron arrow = new Chevron(host, host.skin.muted(), expanded, key.equals(host.disclosureKey));
        host.disclosureArrows.put(key, arrow);
        return arrow;
    }

    public static View button(SettingsActivity host, String title, final Runnable action) {
        TextView view = new TextView(host);
        view.setText(title);
        view.setTextColor(host.onAccent());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, host.dp(15), 0, host.dp(15));
        view.setBackground(Expressive.tile(host, Accent.colour(), 0x33FFFFFF, host.dp(28), host.dp(28), host.dp(12)));
        view.setOnClickListener(v -> action.run());
        LinearLayout holder = new LinearLayout(host);
        holder.setPadding(host.dp(16), host.dp(12), host.dp(16), host.dp(16));
        holder.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return holder;
    }

    public static View appear(SettingsActivity host, View view) {
        boolean animated = !host.revealAnimationUsed && Motion.enabled(host);
        if (animated) host.revealAnimationUsed = true;
        DisclosureBody body = new DisclosureBody(host, view, animated);
        if (host.renderDisclosureKey != null) {
            java.util.List<DisclosureBody> list = host.disclosureBodies.get(host.renderDisclosureKey);
            if (list == null) {
                list = new java.util.ArrayList<>();
                host.disclosureBodies.put(host.renderDisclosureKey, list);
            }
            list.add(body);
        }
        return body;
    }

    public static View slider(SettingsActivity host, String title, int now, int most, final SettingsActivity.Chosen chosen) {
        return slider(host, title, now, most, 0, "", chosen);
    }

    public static View slider(SettingsActivity host, String title, int now, int most, final int shift, final String unit,
            final SettingsActivity.Chosen chosen) {
        return slider(host, "tune", title, now, most, shift, unit, chosen);
    }

    public static View slider(SettingsActivity host, String picture, String title, int now, int most, final int shift, final String unit,
            final SettingsActivity.Chosen chosen) {
        return slider(host, picture, title, now, most, shift, unit, chosen, false);
    }

    public static View liveSlider(SettingsActivity host, String picture, String title, int now, int most, int shift, String unit,
            SettingsActivity.Chosen chosen) {
        return slider(host, picture, title, now, most, shift, unit, chosen, true);
    }

    public static View slider(SettingsActivity host, String picture, String title, int now, int most, final int shift, final String unit,
            final SettingsActivity.Chosen chosen, final boolean live) {
        LinearLayout rows = new LinearLayout(host);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(host.dp(16), host.dp(10), host.dp(20), host.dp(10));
        LinearLayout head = new LinearLayout(host);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(icon(host, picture));
        head.addView(label(host, title), new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1f));
        final TextView number = new TextView(host);
        number.setTextColor(host.accentInk());
        number.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15);
        number.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        number.setText((Math.min(now, most) + shift) + unit);
        head.addView(number);
        rows.addView(head);
        android.widget.SeekBar bar = new android.widget.SeekBar(host);
        bar.setMax(most);
        bar.setProgress(Math.min(now, most));
        Expressive.slider(bar, Accent.colour(), Expressive.mix(host.skin.card, host.skin.text, 0.2f));
        bar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar seek, int value, boolean human) {
                number.setText((value + shift) + unit);
                if (live && human) chosen.at(value);
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar seek) {} @Override public void onStopTrackingTouch(android.widget.SeekBar seek) {
                if (!live) chosen.at(seek.getProgress());
            }
        });
        LinearLayout.LayoutParams barSize = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barSize.leftMargin = host.dp(56);
        rows.addView(bar, barSize);
        return rows;
    }

    public static View pickRow(SettingsActivity host, String title, boolean chosen, String preview, final Runnable action) {
        LinearLayout row = row(host);
        row.setPadding(host.dp(12), 0, host.dp(12), 0);
        TextView name = label(host, title);
        if (preview != null) {
            try {
                name.setTypeface(android.graphics.Typeface.create(preview, android.graphics.Typeface.NORMAL));
            } catch (Throwable ignored) {}
        }
        row.addView(name, grow());
        if (chosen) row.addView(new Check(host, Accent.colour()));
        row.setOnClickListener(v -> action.run());
        return sized(host, row, 52);
    }

    public static View icon(SettingsActivity host, String name) {
        return icon(host, name, false, 40);
    }

    public static final java.util.Map<String, Integer> SHAPES = new java.util.HashMap<String, Integer>();
    static {
        SHAPES.put("palette", Expressive.COOKIE9);
        SHAPES.put("group", Expressive.CLOVER4);
        SHAPES.put("play_circle", Expressive.SUNNY);
        SHAPES.put("tune", Expressive.FLOWER6);
        SHAPES.put("info", Expressive.PENTAGON);
        SHAPES.put("extension", Expressive.COOKIE7);
        SHAPES.put("chat", Expressive.BURST12);
    }

    public static int shapeFor(String name) {
        Integer fixed = SHAPES.get(name);
        return fixed != null ? fixed : (name.hashCode() & 0x7FFFFFFF) % Expressive.KINDS;
    }

    public static View icon(SettingsActivity host, String name, boolean toggle, int sizeDp) {
        android.graphics.drawable.Drawable vector = name.equals("search") ? new Expressive.Magnifier(0xFFFFFFFF) : SettingsGlyph.make(name,
                0xFFFFFFFF);
        if (vector == null) {
            Bitmap picture = pictureFor(name);
            if (picture != null) vector = new android.graphics.drawable.BitmapDrawable(host.getResources(), picture);
        }
        int accent = Accent.colour();
        int tonal = host.accentTonal();
        int ink = host.accentInk();
        Expressive.Badge badge = toggle ? new Expressive.Badge(host, shapeFor(name), vector, sizeDp, Expressive.mix(host.skin.card,
                host.skin.text, 0.14f), accent, host.skin.muted(), host.onAccent()) : new Expressive.Badge(host, shapeFor(name),
                vector, sizeDp, tonal, tonal, ink, ink);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(host.dp(sizeDp), host.dp(sizeDp));
        size.rightMargin = host.dp(16);
        badge.setLayoutParams(size);
        return badge;
    }

    public static Bitmap pictureFor(String name) {
        Bitmap bitmap = ICONS.get(name);
        if (bitmap == null) {
            try {
                String data = Icons.PNG.get(name);
                if (data != null) {
                    byte[] png = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
                    bitmap = android.graphics.BitmapFactory.decodeByteArray(png, 0, png.length);
                    if (bitmap != null) ICONS.put(name, bitmap);
                }
            } catch (Throwable ignored) {}
        }
        return bitmap;
    }

    public static final java.util.Map<String, Bitmap> ICONS = new java.util.HashMap<String, Bitmap>();
    public static View toggleRow(SettingsActivity host, String picture, String title, boolean on, final SettingsActivity.Setting setting) {
        return toggleRow(host, picture, title, on, setting, true);
    }

    public static View liveToggleRow(SettingsActivity host, String picture, String title, boolean on, final SettingsActivity.Setting setting) {
        return toggleRow(host, picture, title, on, setting, false);
    }

    public static View toggleRow(SettingsActivity host, String picture, String title, boolean on, final SettingsActivity.Setting setting,
            final boolean requiresRestart) {
        LinearLayout row = row(host);
        final View lead = icon(host, picture, true, 40);
        if (lead instanceof Expressive.Badge)((Expressive.Badge) lead).set(on, false);
        row.addView(lead);
        row.addView(label(host, title), grow());
        host.settingTargets.put(title, row);
        final M3Switch toggle = new M3Switch(host);
        toggle.colours(Accent.colour(), host.skin.muted(), host.skin.card);
        toggle.setContentDescription(title);
        toggle.setChecked(on);
        toggle.setOnChanged(checked -> {
            if (lead instanceof Expressive.Badge)((Expressive.Badge) lead).set(checked, true);
            setting.set(checked);
            if (requiresRestart) host.markChanged();
        });
        row.addView(toggle);
        row.setOnClickListener(v -> {
            toggle.performClick();
        });
        return sized(host, row, 48);
    }

    public static View betaRow(SettingsActivity host, String picture, String title, boolean on, final SettingsActivity.Setting setting) {
        LinearLayout row = row(host);
        final View lead = icon(host, picture, true, 40);
        if (lead instanceof Expressive.Badge)((Expressive.Badge) lead).set(on, false);
        row.addView(lead);
        row.addView(label(host, title), grow());
        TextView beta = new TextView(host);
        beta.setText(Text.BETA);
        beta.setTextColor(host.onAccent());
        beta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        beta.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        beta.setPadding(host.dp(8), host.dp(3), host.dp(8), host.dp(3));
        GradientDrawable chip = new GradientDrawable();
        chip.setColor(Accent.colour());
        chip.setCornerRadius(host.dp(12));
        beta.setBackground(chip);
        LinearLayout.LayoutParams place = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        place.leftMargin = host.dp(8);
        row.addView(beta, place);
        final M3Switch toggle = new M3Switch(host);
        toggle.colours(Accent.colour(), host.skin.muted(), host.skin.card);
        toggle.setChecked(on);
        toggle.setOnChanged(checked -> {
            if (lead instanceof Expressive.Badge)((Expressive.Badge) lead).set(checked, true);
            setting.set(checked);
            host.markChanged();
        });
        LinearLayout.LayoutParams togglePlace = new LinearLayout.LayoutParams(host.dp(52), host.dp(32));
        togglePlace.leftMargin = host.dp(10);
        row.addView(toggle, togglePlace);
        return sized(host, row, 52);
    }

    public static View actionRow(SettingsActivity host, String picture, String title, String detail, final Runnable action) {
        LinearLayout row = row(host);
        row.addView(icon(host, picture));
        boolean value = detail != null && detail.length() <= 14;
        LinearLayout text = new LinearLayout(host);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(label(host, title));
        if (detail != null && !value) text.addView(detail(host, detail));
        row.addView(text, grow());
        if (value) {
            TextView shown = detail(host, detail);
            shown.setGravity(Gravity.CENTER_VERTICAL);
            shown.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            shown.setPadding(host.dp(8), 0, host.dp(6), 0);
            row.addView(shown);
        }
        ImageView chevron = new ImageView(host);
        chevron.setImageDrawable(SettingsGlyph.make("chevron_right", host.skin.muted()));
        row.addView(chevron, new LinearLayout.LayoutParams(host.dp(24), host.dp(24)));
        row.setOnClickListener(v -> action.run());
        return sized(host, row, detail == null || value ? 48 : 60);
    }

    public static View idRow(SettingsActivity host, String title, final String value) {
        LinearLayout row = row(host);
        row.addView(icon(host, "fingerprint"));
        LinearLayout text = new LinearLayout(host);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(label(host, title));
        TextView shown = detail(host, value == null ? Text.ACCOUNT_UNKNOWN : shorten(value));
        if (value != null) shown.setTextColor(host.skin.text);
        text.addView(shown);
        row.addView(text, grow());
        if (value != null) {
            row.addView(away(host));
            row.setOnClickListener(v -> host.copy(title, value));
        }
        return sized(host, row, 60);
    }

    public static String shorten(String value) {
        if (value.length() <= 26) return value;
        return value.substring(0, 14) + "…" + value.substring(value.length() - 8);
    }

    public static View linkRow(SettingsActivity host, String picture, String title, String handle, final String url) {
        LinearLayout row = row(host);
        row.addView(icon(host, picture));
        LinearLayout text = new LinearLayout(host);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(label(host, title));
        text.addView(detail(host, handle));
        row.addView(text, grow());
        row.addView(away(host));
        row.setOnClickListener(v -> host.open(url));
        return sized(host, row, 60);
    }

    public static View away(SettingsActivity host) {
        Chevron arrow = new Chevron(host, host.skin.muted(), false, false);
        arrow.setRotation(- 90f);
        return arrow;
    }

    public static TextView quiet(SettingsActivity host, String message) {
        TextView view = new TextView(host);
        view.setText(message);
        view.setTextColor(host.skin.muted());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        view.setPadding(host.dp(16), host.dp(2), host.dp(16), host.dp(8));
        return view;
    }

    public static View section(SettingsActivity host, String text) {
        host.renderDisclosureKey = null;
        TextView view = new TextView(host);
        view.setText(text);
        view.setTextColor(host.accentInk());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setLetterSpacing(0.04f);
        view.setPadding(host.dp(28), host.dp(24), host.dp(16), host.dp(8));
        host.sectionTargets.put(text, view);
        return view;
    }

    public static View caption(SettingsActivity host, String message) {
        TextView view = new TextView(host);
        view.setText(message);
        view.setTextColor(host.skin.muted());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        view.setPadding(host.dp(16), host.dp(12), host.dp(16), host.dp(4));
        return view;
    }

    public static LinearLayout card(SettingsActivity host) {
        return new Expressive.Group(host, host.skin.card, (Accent.colour() & 0xFFFFFF) | 0x33000000);
    }

    public static View wrap(SettingsActivity host, View card) {
        LinearLayout box = new LinearLayout(host);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(host.dp(12), 0, host.dp(12), host.dp(6));
        if (!(card instanceof Expressive.Group)) {
            LinearLayout single = card(host);
            single.addView(card);
            card = single;
        }
        box.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return box;
    }

    public static View line(SettingsActivity host) {
        View spacer = new View(host);
        spacer.setVisibility(View.GONE);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        return spacer;
    }

    public static LinearLayout row(SettingsActivity host) {
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(host.dp(16), host.dp(4), host.dp(20), host.dp(4));
        row.setMinimumHeight(host.dp(56));
        Motion.press(row);
        return row;
    }

    public static TextView label(SettingsActivity host, String text) {
        TextView view = new TextView(host);
        view.setText(text);
        view.setTextColor(host.skin.text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        view.setIncludeFontPadding(false);
        view.setPadding(0, 0, host.dp(12), 0);
        return view;
    }

    public static TextView detail(SettingsActivity host, String text) {
        TextView view = new TextView(host);
        view.setText(text);
        view.setTextColor(host.skin.muted());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        view.setIncludeFontPadding(false);
        return view;
    }

    public static LinearLayout.LayoutParams grow() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    public static View sized(SettingsActivity host, View view, int height) {
        if (height > 1) view.setMinimumHeight(host.dp(Math.max(56, height)));
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height <= 1 ? host.dp(height)
                : ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }
}

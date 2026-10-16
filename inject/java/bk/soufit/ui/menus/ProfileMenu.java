package bk.soufit.ui.menus;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import bk.soufit.core.DevMode;
import bk.soufit.core.Local;
import bk.soufit.core.Text;
import bk.soufit.profile.Account;
import bk.soufit.profile.Badges;
import bk.soufit.profile.BioProof;
import bk.soufit.profile.Gradient;
import bk.soufit.profile.Looks;
import bk.soufit.profile.Mine;
import bk.soufit.profile.Proof;
import bk.soufit.profile.Showcase;
import bk.soufit.theme.Accent;
import bk.soufit.ui.Chevron;
import bk.soufit.ui.Dot;
import bk.soufit.ui.Expressive;
import bk.soufit.ui.M3Switch;
import bk.soufit.ui.Motion;
import bk.soufit.ui.Panel;
import bk.soufit.ui.Popup;
import bk.soufit.ui.Screen;
import bk.soufit.ui.SettingsActivity;
import bk.soufit.ui.SettingsGlyph;
import bk.soufit.ui.SettingsUI;
public final class ProfileMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        column.addView(donateBanner(host));
        column.addView(profileHero(host));
        column.addView(profileSection(host, "star", Text.SHOWCASE, Text.SHOWCASE_NOTE));
        column.addView(SettingsUI.wrap(host, showcaseCard(host)));
        column.addView(profileSection(host, "account_circle", Text.ACCOUNT, null));
        LinearLayout account = SettingsUI.card(host);
        account.addView(SettingsUI.idRow(host, Text.ACCOUNT_ID, Account.id()));
        account.addView(SettingsUI.line(host));
        account.addView(SettingsUI.idRow(host, Text.ACCOUNT_SEC_ID, Account.secId()));
        column.addView(SettingsUI.wrap(host, account));
        column.addView(perksCard(host));
        column.addView(profileSection(host, "sync", Local.of("Badge server"), null));
        LinearLayout badgeServer = SettingsUI.card(host);
        if (DevMode.isOn()) {
            badgeServer.addView(SettingsUI.actionRow(host, "settings", Local.of("Server address"), badgeServerNow(), () -> editBadgeServer(host)));
            badgeServer.addView(SettingsUI.line(host));
        }
        badgeServer.addView(SettingsUI.actionRow(host, "sync", Text.BADGES_LOAD, Text.BADGES_LOAD_NOTE, () -> fetchBadgesNow(host)));
        if (BioProof.waiting()) {
            badgeServer.addView(SettingsUI.line(host));
            badgeServer.addView(SettingsUI.actionRow(host, "restore", Text.BIO_RESTORE, Text.BIO_RESTORE_NOTE, () -> {
                BioProof.recover();
                Screen.say(Text.BIO_RESTORE_GOING);
            }));
        }
        column.addView(SettingsUI.wrap(host, badgeServer));
    }

    public static String badgeServerNow() {
        return Badges.SERVER.isEmpty() ? Local.of("not set") : Badges.SERVER;
    }

    public static void editBadgeServer(SettingsActivity host) {
        final Panel panel = Panel.with(host, host.skin, Local.of("Badge server"));
        final android.widget.EditText field = panel.field("https://…");
        field.setText(Badges.SERVER);
        panel.primary(Local.of("Save"), () -> {
            try {
                Badges.setServer(host, field.getText().toString());
                host.rebuild();
            } catch (Exception invalid) {
                Screen.say(invalid.getMessage());
            }
        });
        panel.quiet(Local.of("Cancel"), null).show();
    }

    public static void fetchBadgesNow(SettingsActivity host) {
        if (Badges.SERVER.isEmpty()) {
            Screen.say(Local.of("Set the server address first"));
            return;
        }
        Badges.refreshNow(reached -> {
            Screen.say(reached ? Text.BADGES_LOADED : Text.BADGES_UNREACHED);
            host.rebuild();
        });
        Mine.fetchNow(host::rebuild);
    }

    public static View donateBanner(SettingsActivity host) {
        LinearLayout card = new LinearLayout(host);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(host.dp(20), host.dp(20), host.dp(20), host.dp(20));
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[] {host.accentTonal(),
                host.skin.card});
        background.setCornerRadius(host.dp(28));
        card.setBackground(background);
        LinearLayout top = new LinearLayout(host);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout medal = new LinearLayout(host);
        medal.setGravity(Gravity.CENTER);
        GradientDrawable disc = new GradientDrawable();
        disc.setShape(GradientDrawable.OVAL);
        disc.setColor(Accent.colour());
        medal.setBackground(disc);
        medal.addView(SettingsUI.icon(host, "star"), new LinearLayout.LayoutParams(host.dp(22), host.dp(22)));
        top.addView(medal, new LinearLayout.LayoutParams(host.dp(46), host.dp(46)));
        TextView head = new TextView(host);
        head.setText(Text.DONATE_BANNER);
        head.setTextColor(host.skin.text);
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        head.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams headAt = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        headAt.leftMargin = host.dp(14);
        top.addView(head, headAt);
        card.addView(top);
        TextView body = new TextView(host);
        body.setText(Text.DONATE_BANNER_TEXT);
        body.setTextColor(host.skin.text);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        body.setLineSpacing(host.dp(2), 1f);
        body.setPadding(0, host.dp(14), 0, 0);
        card.addView(body);
        TextView how = new TextView(host);
        how.setText(Text.DONATE_BANNER_HOW);
        how.setTextColor(host.skin.muted());
        how.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        how.setPadding(0, host.dp(10), 0, 0);
        card.addView(how);
        int btnHeight = host.dp(52);
        FrameLayout goBox = new FrameLayout(host);
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(Accent.colour());
        pill.setCornerRadius(host.dp(16));
        goBox.setBackground(pill);
        goBox.setOnClickListener(v -> host.showDonations());
        Motion.touch(goBox);
        TextView go = new TextView(host);
        go.setText(Text.DONATE_BANNER_BUTTON);
        go.setTextColor(host.onAccent());
        go.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        go.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        go.setGravity(Gravity.CENTER);
        go.setIncludeFontPadding(false);
        goBox.addView(go, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        FrameLayout writeBox = new FrameLayout(host);
        GradientDrawable outline = new GradientDrawable();
        outline.setColor(0x00000000);
        outline.setCornerRadius(host.dp(16));
        outline.setStroke(host.dp(1), host.skin.muted());
        writeBox.setBackground(outline);
        writeBox.setPadding(host.dp(10), host.dp(4), host.dp(10), host.dp(4));
        writeBox.setOnClickListener(v -> host.open(SettingsActivity.CHANNEL_WRITE));
        Motion.touch(writeBox);
        TextView write = new TextView(host);
        write.setText(Text.DONATE_BANNER_WRITE);
        write.setTextColor(host.skin.text);
        write.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        write.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        write.setGravity(Gravity.CENTER);
        write.setMaxLines(2);
        write.setLineSpacing(0, 0.95f);
        write.setIncludeFontPadding(false);
        writeBox.addView(write, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        LinearLayout buttons = new LinearLayout(host);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setBaselineAligned(false);
        buttons.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams below = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        below.topMargin = host.dp(18);
        LinearLayout.LayoutParams primary = new LinearLayout.LayoutParams(0, btnHeight, 1f);
        LinearLayout.LayoutParams secondary = new LinearLayout.LayoutParams(0, btnHeight, 1.15f);
        secondary.leftMargin = host.dp(10);
        buttons.addView(goBox, primary);
        buttons.addView(writeBox, secondary);
        card.addView(buttons, below);
        LinearLayout holder = new LinearLayout(host);
        holder.setPadding(host.dp(20), host.dp(14), host.dp(20), host.dp(2));
        holder.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return holder;
    }

    public static void tookFree(SettingsActivity host, boolean ok, String trouble) {
        if (needsProof(host, ok, trouble)) return;
        Screen.say(ok ? Text.FREE_BADGE_GOT : Text.MINE_TOO_OFTEN);
        host.rebuild();
    }

    public static View proveRow(SettingsActivity host) {
        boolean done = Proof.proved();
        LinearLayout row = SettingsUI.row(host);
        row.setPadding(host.dp(16), 0, host.dp(16), 0);
        row.addView(SettingsUI.icon(host, done ? "verified_user" : "fingerprint"));
        LinearLayout text = new LinearLayout(host);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(host, done ? Text.PROVE_DONE : Text.PROVE));
        text.addView(SettingsUI.detail(host, done ? Text.PROVE_DONE_NOTE : Text.PROVE_NOTE));
        row.addView(text, SettingsUI.grow());
        if (!done) row.setOnClickListener(v -> proveAccount(host));
        return SettingsUI.sized(host, row, 64);
    }

    public static void proveAccount(SettingsActivity host) {
        if (!BioProof.available()) {
            proveByHand(host);
            return;
        }
        Screen.say(Text.PROVE_AUTO_GOING);
        BioProof.start((proved, said) -> {
            if (host.isFinishing() || host.isDestroyed()) return;
            if (proved) {
                Screen.say(Text.PROVE_OK);
                host.ordering = null;
                Mine.ask(host::rebuild);
                host.rebuild();
                return;
            }
            Screen.say(said);
            proveByHand(host);
        });
    }

    public static void proveByHand(SettingsActivity host) {
        String already = Proof.waiting();
        if (already.length() > 0) {
            askToCheck(host, already);
            return;
        }
        Screen.say(Text.MINE_WAIT);
        Proof.want((ok, trouble) -> {
            if (!ok) {
                Screen.say(trouble);
                return;
            }
            askToCheck(host, Proof.waiting());
        });
    }

    public static boolean needsProof(SettingsActivity host, boolean ok, String trouble) {
        if (ok || !Mine.PROVE.equals(trouble)) return false;
        proveAccount(host);
        return true;
    }

    public static void askToCheck(SettingsActivity host, String code) {
        Popup.prove(host, code, (name, reply) -> Proof.check(name, (ok, trouble) -> {
            if (ok) {
                Screen.say(Text.PROVE_OK);
                host.ordering = null;
                Mine.ask(host::rebuild);
            }
            reply.said(ok, trouble, Proof.waiting(), Proof.wantsName());
            host.rebuild();
        }), reply -> Proof.fresh((ok, trouble) -> reply.said(ok, trouble, Proof.waiting(), false)));
    }

    public static View perksCard(SettingsActivity host) {
        LinearLayout box = new LinearLayout(host);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(profileSection(host, "favorite_border", Text.PERKS, Text.PERKS_NOTE));
        box.addView(SettingsUI.wrap(host, gradientCard(host)));
        LinearLayout badges = SettingsUI.card(host);
        badges.addView(SettingsUI.toggleRow(host, "favorite_border", Text.BADGES_ON, Badges.isEnabled(), Badges::setEnabled));
        badges.addView(SettingsUI.line(host));
        badges.addView(mineHead(host));
        if (host.mineOpen) badges.addView(SettingsUI.appear(host, mineRows(host)));
        box.addView(SettingsUI.wrap(host, badges));
        return box;
    }

    public static View insideCard(SettingsActivity host, View card) {
        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gap.topMargin = host.dp(10);
        card.setLayoutParams(gap);
        return card;
    }

    public static LinearLayout gradientCard(SettingsActivity host) {
        LinearLayout card = SettingsUI.card(host);
        if (!Mine.holds("supporter")) {
            card.addView(SettingsUI.actionRow(host, "gradient", Text.GRADIENT, null, () -> Popup.show(host, Text.GRADIENT,
                    Text.GRADIENT_ONLY, Text.TEST_CLOSE)));
            card.addView(SettingsUI.caption(host, Text.GRADIENT_ONLY));
            return card;
        }
        if (host.gradientColours == null) host.gradientColours = Gradient.starting();
        if (host.gradientSlot >= host.gradientColours.size()) host.gradientSlot = 0;
        card.addView(gradientPreview(host));
        card.addView(gradientDots(host));
        int[] hsv = Gradient.hsv(host.gradientColours.get(host.gradientSlot));
        card.addView(SettingsUI.slider(host, Text.GRADIENT_HUE, hsv[0], 359, value -> setSlot(host, value, - 1, - 1)));
        card.addView(SettingsUI.slider(host, Text.GRADIENT_SAT, hsv[1], 100, value -> setSlot(host, - 1, value, - 1)));
        card.addView(SettingsUI.slider(host, Text.GRADIENT_VALUE, hsv[2], 100, value -> setSlot(host, - 1, - 1, value)));
        card.addView(SettingsUI.line(host));
        if (host.gradientColours.size() < Gradient.MOST) {
            card.addView(SettingsUI.actionRow(host, "palette", Text.GRADIENT_ADD, null, () -> {
                host.gradientColours.add(host.gradientColours.get(host.gradientColours.size() - 1));
                host.gradientSlot = host.gradientColours.size() - 1;
                host.rebuild();
            }));
        }
        if (host.gradientColours.size() > Gradient.FEWEST) {
            card.addView(SettingsUI.actionRow(host, "block", Text.GRADIENT_DROP_ONE, null, () -> {
                host.gradientColours.remove(host.gradientSlot);
                host.gradientSlot = 0;
                host.rebuild();
            }));
        }
        card.addView(SettingsUI.line(host));
        card.addView(SettingsUI.actionRow(host, "star", Text.GRADIENT_SAVE, null, () -> Gradient.save(host.gradientColours,
                (ok, trouble) -> {
            if (needsProof(host, ok, trouble)) return;
            Popup.show(host, Text.GRADIENT, ok ? Text.GRADIENT_SAVED : trouble, Text.TEST_CLOSE);
        })));
        card.addView(SettingsUI.actionRow(host, "visibility_off", Text.GRADIENT_OFF, null, () -> Gradient.drop((ok, trouble) -> {
            if (needsProof(host, ok, trouble)) return;
            Popup.show(host, Text.GRADIENT, ok ? Text.GRADIENT_SAVED : trouble, Text.TEST_CLOSE);
        })));
        card.addView(SettingsUI.caption(host, Text.GRADIENT_NOTE));
        return card;
    }

    public static LinearLayout showcaseCard(SettingsActivity host) {
        LinearLayout card = SettingsUI.card(host);
        host.badgeStrip = null;
        host.gradientStrip = null;
        String uid = Account.id();
        bk.soufit.badges.model.Badge[] held = uid == null ? null : Badges.of(uid);
        bk.soufit.badges.model.Badge[] every = Badges.all();
        int[] own = uid == null ? null : Looks.of(uid);
        int[][] gradients = Looks.all();
        String badgeChoice = Showcase.badgeChoice();
        String gradientChoice = Showcase.gradientChoice();
        bk.soufit.badges.model.Badge shown = Showcase.badgeFor(every, held, badgeChoice);
        int[] colours = Showcase.coloursFor(gradientChoice, own);
        card.addView(sectionLabel(host, Text.SHOWCASE_BADGES, every.length));
        if (every.length == 0) {
            card.addView(SettingsUI.caption(host, Text.SHOWCASE_NO_BADGES));
        } else {
            LinearLayout strip = new LinearLayout(host);
            strip.setOrientation(LinearLayout.HORIZONTAL);
            strip.addView(noneTile(host, 64, true, shown == null, () -> {
                rememberStrips(host);
                Showcase.setBadge(null);
                host.rebuild();
            }));
            for (final bk.soufit.badges.model.Badge badge : every) {
                strip.addView(badgeTile(host, badge, badge == shown, Showcase.owned(badge, held), () -> {
                    rememberStrips(host);
                    Showcase.setBadge(badge);
                    host.rebuild();
                }));
            }
            card.addView(strip(host, strip, true));
        }
        card.addView(sectionLabel(host, Text.SHOWCASE_GRADIENTS, gradients.length));
        if (gradients.length == 0) {
            card.addView(SettingsUI.caption(host, Text.SHOWCASE_NO_BADGES));
        } else {
            LinearLayout strip = new LinearLayout(host);
            strip.setOrientation(LinearLayout.HORIZONTAL);
            strip.addView(noneTile(host, 64, false, colours == null, () -> {
                rememberStrips(host);
                Showcase.setGradient(null);
                host.rebuild();
            }));
            for (final int[] picked : gradients) {
                LinearLayout content = new LinearLayout(host);
                content.setOrientation(LinearLayout.VERTICAL);
                content.setGravity(Gravity.CENTER_HORIZONTAL);
                content.addView(gradientBar(host, picked));
                TextView state = stateLabel(host, own != null && java.util.Arrays.equals(picked, own));
                state.setPadding(0, host.dp(10), 0, host.dp(2));
                content.addView(state);
                strip.addView(choiceTile(host, content, java.util.Arrays.equals(picked, colours), () -> {
                    rememberStrips(host);
                    Showcase.setGradient(picked);
                    host.rebuild();
                }));
            }
            card.addView(strip(host, strip, false));
        }
        return card;
    }

    public static View profileHero(SettingsActivity host) {
        String uid = Account.id();
        bk.soufit.badges.model.Badge[] held = uid == null ? null : Badges.of(uid);
        bk.soufit.badges.model.Badge shown = Showcase.badgeFor(Badges.all(), held, Showcase.badgeChoice());
        int[] own = uid == null ? null : Looks.of(uid);
        int[] colours = Showcase.coloursFor(Showcase.gradientChoice(), own);
        boolean preview = !Showcase.owned(shown, held);
        TextView name = new TextView(host);
        name.setTextColor(host.skin.text);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setIncludeFontPadding(false);
        name.setGravity(Gravity.CENTER);
        String nick = Account.profileName();
        Showcase.draw(name, nick == null || nick.length() == 0 ? Text.SHOWCASE_NAME: nick, shown, colours);
        int[] wash = new int[colours == null ? 3 : Math.max(2, colours.length)];
        for (int i = 0; i < wash.length; i ++) {
            wash[i] = colours == null ? host.accentTonal() : Expressive.mix(host.skin.card, colours[Math.min(i, colours.length - 1)],
                    0.42f);
        }
        GradientDrawable box = new GradientDrawable(GradientDrawable.Orientation.TL_BR, wash);
        box.setCornerRadius(host.dp(28));
        LinearLayout inside = new LinearLayout(host);
        inside.setOrientation(LinearLayout.VERTICAL);
        inside.setGravity(Gravity.CENTER_HORIZONTAL);
        inside.setPadding(host.dp(20), host.dp(18), host.dp(20), host.dp(22));
        inside.setBackground(box);
        LinearLayout top = new LinearLayout(host);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView kicker = new TextView(host);
        kicker.setText(Text.SHOWCASE);
        kicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        kicker.setLetterSpacing(0.12f);
        kicker.setTextColor(host.accentInk());
        kicker.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        top.addView(kicker, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView chip = new TextView(host);
        chip.setText(preview ? Text.SHOWCASE_PREVIEW : Text.SHOWCASE_OWNED);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        chip.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        chip.setTextColor(preview ? host.skin.muted() : host.accentInk());
        chip.setPadding(host.dp(10), host.dp(4), host.dp(10), host.dp(4));
        GradientDrawable pill = new GradientDrawable();
        pill.setCornerRadius(host.dp(12));
        pill.setColor(preview ? host.skin.card : host.accentTonal());
        chip.setBackground(pill);
        top.addView(chip);
        inside.addView(top, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams under = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        under.topMargin = host.dp(20);
        inside.addView(name, under);
        TextView note = SettingsUI.detail(host, preview ? Text.SHOWCASE_PREVIEW_NOTE : shown == null ? Text.SHOWCASE_NAME_NOTE
                : named(shown));
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, host.dp(10), 0, 0);
        inside.addView(note, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (uid != null) {
            TextView id = SettingsUI.detail(host, Text.ACCOUNT_ID + "  " + SettingsUI.shorten(uid));
            id.setGravity(Gravity.CENTER);
            id.setPadding(0, host.dp(18), 0, 0);
            inside.addView(id, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        LinearLayout holder = new LinearLayout(host);
        holder.setPadding(host.dp(16), host.dp(8), host.dp(16), host.dp(4));
        holder.addView(inside, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return holder;
    }

    public static View profileSection(SettingsActivity host, String picture, String title, String note) {
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(host.dp(18), host.dp(26), host.dp(18), host.dp(10));
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(host.dp(40), host.dp(40));
        size.rightMargin = host.dp(14);
        row.addView(SettingsUI.icon(host, picture), size);
        LinearLayout words = new LinearLayout(host);
        words.setOrientation(LinearLayout.VERTICAL);
        TextView head = new TextView(host);
        head.setText(title);
        head.setTextColor(host.skin.text);
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        head.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        head.setLetterSpacing(0.01f);
        host.sectionTargets.put(title, head);
        words.addView(head);
        if (note != null) {
            TextView sub = new TextView(host);
            sub.setText(note);
            sub.setTextColor(host.skin.muted());
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            sub.setPadding(0, host.dp(2), 0, 0);
            words.addView(sub);
        }
        row.addView(words, SettingsUI.grow());
        return row;
    }

    public static TextView sectionLabel(SettingsActivity host, String text, int count) {
        TextView view = (TextView) SettingsUI.caption(host, text + "  ·  " + count);
        view.setTextColor(host.accentInk());
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setPadding(host.dp(16), host.dp(14), host.dp(16), host.dp(6));
        return view;
    }

    public static View strip(SettingsActivity host, LinearLayout row, boolean badgeRow) {
        row.setPadding(host.dp(12), host.dp(2), host.dp(12), host.dp(12));
        final android.widget.HorizontalScrollView scroll = new android.widget.HorizontalScrollView(host);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.addView(row);
        if (badgeRow) host.badgeStrip = scroll;
        else host.gradientStrip = scroll;
        final int at = badgeRow ? host.badgeScrollAt : host.gradientScrollAt;
        if (at > 0) scroll.post(() -> scroll.scrollTo(at, 0));
        return scroll;
    }

    public static void rememberStrips(SettingsActivity host) {
        if (host.badgeStrip != null) host.badgeScrollAt = host.badgeStrip.getScrollX();
        if (host.gradientStrip != null) host.gradientScrollAt = host.gradientStrip.getScrollX();
    }

    public static View noneTile(SettingsActivity host, int width, boolean titled, boolean chosen, Runnable action) {
        LinearLayout content = new LinearLayout(host);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout slot = new FrameLayout(host);
        GradientDrawable outline = new GradientDrawable();
        if (titled) outline.setShape(GradientDrawable.OVAL);
        else outline.setCornerRadius(host.dp(10));
        outline.setStroke(host.dp(1), host.skin.muted());
        slot.setBackground(outline);
        android.widget.ImageView ban = new android.widget.ImageView(host);
        android.graphics.drawable.Drawable glyph = SettingsGlyph.make("block", Accent.colour());
        if (glyph != null) ban.setImageDrawable(glyph);
        slot.addView(ban, new FrameLayout.LayoutParams(host.dp(22), host.dp(22), Gravity.CENTER));
        content.addView(slot, titled ? new LinearLayout.LayoutParams(host.dp(38), host.dp(38)) : new LinearLayout.LayoutParams(host.dp(width),
                host.dp(36)));
        if (titled) {
            TextView title = SettingsUI.detail(host, Text.SHOWCASE_NO_BADGE);
            title.setGravity(Gravity.CENTER);
            title.setSingleLine(true);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            title.setPadding(0, host.dp(10), 0, 0);
            content.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView state = stateLabel(host, false);
            state.setText(" ");
            state.setPadding(0, host.dp(4), 0, 0);
            content.addView(state);
        } else {
            TextView state = stateLabel(host, false);
            state.setText(Text.SHOWCASE_NO_GRADIENT);
            state.setSingleLine(true);
            state.setPadding(0, host.dp(10), 0, host.dp(2));
            content.addView(state, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return choiceTile(host, content, chosen, action);
    }

    public static View badgeTile(SettingsActivity host, final bk.soufit.badges.model.Badge badge, boolean chosen, boolean owned, Runnable action) {
        LinearLayout content = new LinearLayout(host);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        android.widget.ImageView picture = new android.widget.ImageView(host);
        picture.setImageBitmap(badgePicture(host, badge));
        if (badge.colour != 0) {
            picture.setColorFilter(badge.colour, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        content.addView(picture, new LinearLayout.LayoutParams(host.dp(38), host.dp(38)));
        TextView title = SettingsUI.detail(host, named(badge));
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setPadding(0, host.dp(10), 0, 0);
        content.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView state = stateLabel(host, owned);
        state.setPadding(0, host.dp(4), 0, 0);
        content.addView(state);
        return choiceTile(host, content, chosen, action);
    }

    public static View choiceTile(SettingsActivity host, View content, boolean chosen, Runnable action) {
        LinearLayout tile = new LinearLayout(host);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(host.dp(12), host.dp(14), host.dp(12), host.dp(14));
        GradientDrawable ring = new GradientDrawable();
        ring.setCornerRadius(host.dp(20));
        ring.setColor(chosen ? host.accentTonal() : host.skin.page);
        ring.setStroke(host.dp(2), chosen ? Accent.colour() : 0x00000000);
        tile.setBackground(ring);
        tile.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tile.setOnClickListener(v -> action.run());
        Motion.press(tile);
        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(host.dp(98), ViewGroup.LayoutParams.WRAP_CONTENT);
        gap.rightMargin = host.dp(10);
        tile.setLayoutParams(gap);
        return tile;
    }

    public static View gradientBar(SettingsActivity host, int[] colours) {
        View bar = new View(host);
        GradientDrawable paint = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colours);
        paint.setCornerRadius(host.dp(10));
        bar.setBackground(paint);
        bar.setLayoutParams(new LinearLayout.LayoutParams(host.dp(64), host.dp(36)));
        return bar;
    }

    public static TextView stateLabel(SettingsActivity host, boolean owned) {
        TextView view = new TextView(host);
        view.setText(owned ? Text.SHOWCASE_OWNED : Text.SHOWCASE_PREVIEW);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        view.setTextColor(owned ? host.accentInk() : host.skin.muted());
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        return view;
    }

    public static android.graphics.Bitmap badgePicture(SettingsActivity host, bk.soufit.badges.model.Badge badge) {
        android.graphics.Bitmap picture = badge.image.length() == 0 ? null : Badges.picture(host, badge.image);
        return picture != null ? picture : Badges.note();
    }

    public static void setSlot(SettingsActivity host, int hue, int saturation, int value) {
        int[] hsv = Gradient.hsv(host.gradientColours.get(host.gradientSlot));
        if (hue >= 0) hsv[0] = hue;
        if (saturation >= 0) hsv[1] = saturation;
        if (value >= 0) hsv[2] = value;
        host.gradientColours.set(host.gradientSlot, Gradient.from(hsv[0], hsv[1], hsv[2]));
        host.rebuild();
    }

    public static View gradientPreview(SettingsActivity host) {
        int[] colours = new int[host.gradientColours.size()];
        for (int i = 0; i < colours.length; i ++) colours[i] = host.gradientColours.get(i);
        View bar = new View(host);
        android.graphics.drawable.GradientDrawable paint = new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                colours);
        paint.setCornerRadius(host.dp(10));
        bar.setBackground(paint);
        LinearLayout holder = new LinearLayout(host);
        holder.setPadding(host.dp(16), host.dp(14), host.dp(16), host.dp(6));
        holder.addView(bar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, host.dp(44)));
        return holder;
    }

    public static View gradientDots(SettingsActivity host) {
        LinearLayout line = new LinearLayout(host);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setPadding(host.dp(16), host.dp(6), host.dp(16), host.dp(10));
        for (int i = 0; i < host.gradientColours.size(); i ++) {
            final int at = i;
            Dot dot = new Dot(host, host.gradientColours.get(i), i == host.gradientSlot);
            dot.setOnClickListener(v -> {
                host.gradientSlot = at;
                host.rebuild();
            });
            line.addView(dot, new LinearLayout.LayoutParams(host.dp(36), host.dp(36), 1f));
        }
        return line;
    }

    public static String named(bk.soufit.badges.model.Badge badge) {
        if (badge.title != null && badge.title.length() > 0) return badge.title;
        return badge.text;
    }

    public static View mineHead(SettingsActivity host) {
        LinearLayout row = SettingsUI.row(host);
        row.addView(SettingsUI.icon(host, "favorite_border"));
        LinearLayout text = new LinearLayout(host);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(host, Text.MINE));
        text.addView(SettingsUI.detail(host, Text.MINE_NOTE));
        row.addView(text, SettingsUI.grow());
        row.addView(SettingsUI.disclosure(host, host.mineOpen, "mineOpen"));
        row.setOnClickListener(v -> {
            host.disclosureKey = "mineOpen";
            host.mineOpen = !host.mineOpen;
            if (host.mineOpen) {
                host.ordering = null;
                Mine.ask(host::rebuild);
            }
            host.rebuildAfterReveal(host.mineOpen);
        });
        return SettingsUI.sized(host, row, 60);
    }

    public static View mineRows(SettingsActivity host) {
        LinearLayout rows = new LinearLayout(host);
        rows.setOrientation(LinearLayout.VERTICAL);
        if (Mine.tookFresh() && !host.rearranged) host.ordering = null;
        if (host.ordering == null) host.ordering = Mine.held();
        rows.addView(proveRow(host));
        if (host.ordering.isEmpty()) {
            rows.addView(SettingsUI.quiet(host, Mine.everAsked() ? Text.MINE_NONE : Text.MINE_WAIT));
            return rows;
        }
        for (int i = 0; i < host.ordering.size(); i ++) {
            final int at = i;
            final Mine.Held one = host.ordering.get(i);
            if (i > 0) rows.addView(SettingsUI.line(host));
            LinearLayout row = SettingsUI.row(host);
            row.setPadding(host.dp(12), 0, host.dp(12), 0);
            bk.soufit.badges.model.Badge badge = Badges.byId(one.id);
            row.addView(badgeDot(host, badge));
            row.addView(SettingsUI.label(host, badge == null ? one.id : named(badge)), SettingsUI.grow());
            row.addView(mover(host, true, at > 0, () -> {
                java.util.Collections.swap(host.ordering, at, at - 1);
                host.rearranged = true;
                host.rebuild();
            }));
            row.addView(mover(host, false, at < host.ordering.size() - 1, () -> {
                java.util.Collections.swap(host.ordering, at, at + 1);
                host.rearranged = true;
                host.rebuild();
            }));
            M3Switch toggle = new M3Switch(host);
            toggle.colours(Accent.colour(), host.skin.muted(), host.skin.card);
            toggle.setChecked(one.shown, false);
            toggle.setOnChanged(checked -> {
                one.shown = checked;
                host.rearranged = true;
            });
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(host.dp(52), host.dp(32));
            size.leftMargin = host.dp(10);
            row.addView(toggle, size);
            rows.addView(SettingsUI.sized(host, row, 60));
        }
        rows.addView(SettingsUI.button(host, Text.MINE_SAVE, () -> Mine.save(host.ordering, (ok, trouble) -> {
            if (needsProof(host, ok, trouble)) return;
            Screen.say(ok ? Text.MINE_SAVED : Text.MINE_TOO_OFTEN);
            if (ok) {
                host.rearranged = false;
                host.ordering = null;
            }
            host.rebuild();
        })));
        return rows;
    }

    public static View mover(SettingsActivity host, boolean up, boolean can, final Runnable action) {
        android.widget.FrameLayout target = new android.widget.FrameLayout(host);
        Chevron arrow = new Chevron(host, can ? host.skin.text : host.skin.muted(), up, false);
        android.widget.FrameLayout.LayoutParams image = new android.widget.FrameLayout.LayoutParams(host.dp(24), host.dp(24),
                Gravity.CENTER);
        target.addView(arrow, image);
        target.setLayoutParams(new LinearLayout.LayoutParams(host.dp(48), host.dp(48)));
        target.setContentDescription(up ? Local.of("Up") : Local.of("Down"));
        target.setEnabled(can);
        if (can) {
            Motion.press(target);
            target.setOnClickListener(v -> action.run());
        }
        return target;
    }

    public static View badgeDot(SettingsActivity host, bk.soufit.badges.model.Badge badge) {
        android.widget.ImageView view = new android.widget.ImageView(host);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(host.dp(26), host.dp(26));
        size.rightMargin = host.dp(12);
        view.setLayoutParams(size);
        if (badge == null) return view;
        android.graphics.Bitmap picture = badge.image.length() == 0 ? null : Badges.picture(host, badge.image);
        if (picture != null) {
            view.setImageBitmap(picture);
        } else {
            view.setImageBitmap(Badges.note());
        }
        if (badge.colour != 0) {
            view.setColorFilter(badge.colour, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        return view;
    }
}

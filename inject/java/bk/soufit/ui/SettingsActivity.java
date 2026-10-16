package bk.soufit.ui;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import bk.soufit.core.Backup;
import bk.soufit.core.Cats;
import bk.soufit.core.DevMode;
import bk.soufit.core.Diagnostics;
import bk.soufit.core.Diary;
import bk.soufit.core.Flags;
import bk.soufit.core.Local;
import bk.soufit.core.Net;
import bk.soufit.core.Sites;
import bk.soufit.core.Text;
import bk.soufit.core.Unsaved;
import bk.soufit.core.Version;
import bk.soufit.core.soufiT;
import bk.soufit.feed.PlayerTools;
import bk.soufit.media.FavouriteStickers;
import bk.soufit.profile.AccountAppearance;
import bk.soufit.profile.Badges;
import bk.soufit.profile.Mine;
import bk.soufit.social.StreakAlarm;
import bk.soufit.social.Streaks;
import bk.soufit.theme.Accent;
import bk.soufit.theme.AppearanceColors;
import bk.soufit.theme.AvatarShape;
import bk.soufit.theme.EmojiPackManager;
import bk.soufit.theme.Fonts;
import bk.soufit.theme.Surfaces;
import bk.soufit.theme.Launcher;
import bk.soufit.theme.Skin;
import bk.soufit.theme.Themes;
import bk.soufit.ui.menus.AboutMenu;
import bk.soufit.ui.menus.AppearanceMenu;
import bk.soufit.ui.menus.DeveloperMenu;
import bk.soufit.ui.menus.FeedVideoMenu;
import bk.soufit.ui.menus.FriendsMenu;
import bk.soufit.ui.menus.PluginsMenu;
import bk.soufit.ui.menus.ProfileMenu;
import bk.soufit.ui.menus.SearchMenu;
import bk.soufit.ui.menus.ToolsMenu;
import java.util.List;
import java.util.Locale;
public class SettingsActivity extends Activity {
    public void accountAppearanceChanged() {
        skin = Skin.remembered(this);
        rebuild();
    }

    public Skin skin;
    private FrameLayout root;
    public final java.util.Map<String, View> settingTargets = new java.util.HashMap<>();
    public LinearLayout column;
    private View restartBar;
    private RestartSymbol restartSymbol;
    private TextView restartWhy, restartAction;
    private GradientDrawable restartBackground;
    private boolean rebuildQueued;
    public String disclosureKey, renderDisclosureKey;
    private boolean collapsing;
    public final java.util.Map<String, java.util.List<DisclosureBody>> disclosureBodies = new java.util.HashMap<>();
    public final java.util.Map<String, Chevron> disclosureArrows = new java.util.HashMap<>();
    private boolean revealAnimationRequested;
    public boolean revealAnimationUsed;
    private Runnable scrollRestore;
    private android.view.ViewTreeObserver.OnPreDrawListener scrollRestoreListener;
    private int requestedScrollY;
    private final Runnable delayedRebuild = new Runnable() {
        @Override public void run() {
            rebuildQueued = false;
            rebuild();
        }
    };

    private static boolean pending;
    public static final String FORUM = "https://t.me/margeletforum";
    private static final String OWNER_TELEGRAM = "https://t.me/narezany";
    private static final String OWNER_TIKTOK = "https://tiktok.com/@narezany?_r=1&_t=ZT-99hPDJ26hji_";
    public static final String CHANNEL_WRITE = "https://t.me/margytiktok?direct";
    private static final String TTCUZ = "https://t.me/ttcuzmod";
    public static final String GIT = "https://git.narez.xyz/narezany/MargyT";
    public static final String SOUFIT_GIT = "https://github.com/soufied/soufit";
    public static final String YOOMONEY = "https://yoomoney.ru/to/4100118196133693";
    public boolean countriesOpen;
    public boolean accentOpen;
    public boolean glassOpen;
    private Dot accentCustomSwatch;
    public boolean themeColoursOpen;
    public boolean fontOpen;
    public boolean iconOpen;
    public ScrollView page;
    public int settingsGroup;
    public boolean mineOpen;
    public android.widget.HorizontalScrollView badgeStrip;
    public android.widget.HorizontalScrollView gradientStrip;
    public int badgeScrollAt;
    public int gradientScrollAt;
    public java.util.List<Mine.Held> ordering;
    public boolean streakOpen;
    public boolean diaryOpen;
    public boolean rearranged;
    private final Runnable badgeUpdates = () -> {
        if (!isFinishing() && !isDestroyed()) rebuild();
    };

    public String searchQuery = "";
    public final java.util.Map<String, View> sectionTargets = new java.util.HashMap<String, View>();
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        soufiT.attach(this);
        skin = Skin.remembered(this);
        Unsaved.look(this);
        dressTheWindow();
        Badges.tell(badgeUpdates);
        Mine.ask(() -> {
            if (isFinishing() || isDestroyed()) return;
            rebuild();
        });
        ScrollView scroll = page = new ScrollView(this);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        scroll.setFillViewport(true);
        column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(0, statusBar(), 0, dp(32));
        scroll.addView(column, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root = new FrameLayout(this);
        root.setBackgroundColor(skin.page);
        root.addView(scroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        rebuild();
        if (getIntent().getBooleanExtra("soufit_tutorial", false)) root.postDelayed(() -> Tutorial.show(this), 350L);
    }

    public void tutorialStep(int step) {
        settingsGroup = step == 2 ? 1 : step == 3 ? 3 : step == 4 ? 2 : 0;
        if (step == 2) accentOpen = true;
        searchQuery = "";
        rebuild();
        String section = step == 2 ? Text.ACCENT : step == 3 ? BURN : step == 4 ? Text.PERKS : null;
        View target = section == null ? null : sectionTargets.get(section);
        if (target != null) page.post(() -> page.smoothScrollTo(0, Math.max(0, target.getTop() - dp(20))));
    }

    @Override protected void onResume() {
        super.onResume();
        if (Streaks.isEnabled()) {
            StreakAlarm.schedule(this, false);
            Streaks.round(this);
        }
        if (root != null && column != null) rebuild();
    }

    @Override protected void onPause() {
        navigating = false;
        if (column != null) {
            column.animate().withEndAction(null).cancel();
            column.setAlpha(1f);
            column.setTranslationX(0f);
        }
        super.onPause();
    }

    private void dressTheWindow() {
        try {
            getWindow().setStatusBarColor(skin.page);
            getWindow().setNavigationBarColor(skin.page);
            if (!skin.dark()) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } catch (Throwable ignored) {}
    }

    public void rebuild() {
        navigating = false;
        collapsing = false;
        column.animate().withEndAction(null).cancel();
        column.setAlpha(1f);
        column.setTranslationX(0f);
        revealAnimationUsed = false;
        boolean animateReveal = revealAnimationRequested;
        revealAnimationRequested = false;
        revealAnimationUsed = !animateReveal;
        final int previousScroll = page == null ? 0 : page.getScrollY();
        sectionTargets.clear();
        settingTargets.clear();
        searchHolder = null;
        disclosureBodies.clear();
        disclosureArrows.clear();
        renderDisclosureKey = null;
        column.removeAllViews();
        column.addView(backArrow());
        if (settingsGroup == DONATE_PAGE) {
            column.addView(title(Text.DONATE_BANNER));
            AboutMenu.donatePage(this);
            showRestartBar();
            restoreScroll(0);
            return;
        }
        if (settingsGroup == FLAGS_PAGE) {
            column.addView(title(Text.DEV_FLAGS));
            DeveloperMenu.flagsPage(this);
            showRestartBar();
            restoreScroll(previousScroll);
            return;
        }
        column.addView(title(settingsGroup == 0 ? "soufiT" : groupName(settingsGroup)));
        if (settingsGroup == 0) column.addView(forkLine());
        if (settingsGroup == 0 || searchQuery.length()>0) column.addView(SearchMenu.searchBox(this));
        if (searchQuery.length() > 0) {
            SearchMenu.searchResults(this);
            restoreScroll(previousScroll);
            return;
        }
        if (settingsGroup == 0) {
            if (Badges.freeStillOpen() && !Mine.hasFree()) column.addView(freeBanner());
            column.addView(groupNavigation());
            column.addView(SettingsUI.wrap(this, SettingsUI.actionRow(this, "info", Local.of("Tutorial"), null, () -> Tutorial.show(this))));
            column.addView(versions());
            showRestartBar();
            restoreScroll(0);
            return;
        }
        switch (settingsGroup) {
            case 1: AppearanceMenu.build(this, column);
            break;
            case 2: ProfileMenu.build(this, column);
            break;
            case 3: FeedVideoMenu.build(this, column);
            break;
            case 4: ToolsMenu.build(this, column);
            break;
            case 5: AboutMenu.build(this, column);
            break;
            case 6: PluginsMenu.build(this, column);
            break;
            case 7: FriendsMenu.build(this, column);
            break;
            case 8: DeveloperMenu.build(this, column);
            break;
            default: break;
        }
        column.addView(versions());
        showRestartBar();
        restoreScroll(previousScroll);
    }

    private void restoreScroll(final int scrollY) {
        Fonts.applyTree(root);
        if (page == null) return;
        requestedScrollY = Math.max(0, scrollY);
        if (scrollRestore != null) page.removeCallbacks(scrollRestore);
        if (scrollRestoreListener != null && page.getViewTreeObserver().isAlive()) {
            page.getViewTreeObserver().removeOnPreDrawListener(scrollRestoreListener);
            scrollRestoreListener = null;
        }
        if (requestedScrollY == 0) {
            page.scrollTo(0, 0);
            scrollRestore = null;
            return;
        }
        scrollRestoreListener = new android.view.ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                page.getViewTreeObserver().removeOnPreDrawListener(this);
                scrollRestoreListener = null;
                page.scrollTo(0, requestedScrollY);
                return true;
            }
        };
        page.getViewTreeObserver().addOnPreDrawListener(scrollRestoreListener);
    }

    private void showRestartBar() {
        if (pending && restartBar == null) {
            restartBar = restartBar();
            FrameLayout.LayoutParams floating = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);
            floating.setMargins(dp(12), 0, dp(12), dp(12) + navigationBar());
            root.addView(restartBar, floating);
            Motion.rise(restartBar);
        }
        if (pending && restartBar instanceof LinearLayout) {
            LinearLayout bar = (LinearLayout) restartBar;
            styleRestartBar(bar);
            restartSymbol.colour(Accent.colour());
            restartWhy.setTextColor(skin.text);
            restartAction.setTextColor(onAccent());
            restartBackground.setColor(Accent.colour());
        }
        if (!pending && restartBar != null) {
            restartBar.animate().withEndAction(null).cancel();
            root.removeView(restartBar);
            restartBar = null;
            restartSymbol = null;
            restartWhy = null;
            restartAction = null;
            restartBackground = null;
        }
        column.setPadding(0, statusBar(), 0, dp(32) + (pending ? dp(92) + navigationBar() : 0));
    }

    private void styleRestartBar(LinearLayout bar) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Expressive.mix(skin.card, Accent.colour(), 0.22f));
        shape.setCornerRadius(dp(28));
        bar.setBackground(shape);
        bar.setElevation(dp(6));
    }

    public static void restartOwed() {
        pending = true;
        Unsaved.other();
    }

    public void markChanged() {
        pending = Unsaved.after(this);
        showRestartBar();
    }

    @Override public boolean dispatchTouchEvent(android.view.MotionEvent event) {
        if (event != null && event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
            Unsaved.look(this);
        }
        return super.dispatchTouchEvent(event);
    }

    public java.util.List<Integer> gradientColours;
    public int gradientSlot;
    public static String fpsName(String which) {
        if (which == null || which.length() == 0) return Text.FPS_AUTO;
        return String.format(Text.FPS_LOCKED, which + " FPS");
    }

    private static final int DONATE_PAGE = 99;
    public void showDonations() {
        settingsGroup = DONATE_PAGE;
        searchQuery = "";
        rebuild();
    }

    public void rebuildAfterSwitchAnimation() {
        if (rebuildQueued) column.removeCallbacks(delayedRebuild);
        else rebuildQueued = true;
        column.postDelayed(delayedRebuild, M3Switch.ANIMATION_DURATION_MS);
    }

    public View switchRow() {
        LinearLayout row = SettingsUI.row(this);
        final View lead = SettingsUI.icon(this, "language", true, 40);
        if (lead instanceof Expressive.Badge)((Expressive.Badge) lead).set(soufiT.isEnabled(), false);
        row.addView(lead);
        row.addView(SettingsUI.label(this, Text.CHANGE_REGION), SettingsUI.grow());
        final M3Switch toggle = new M3Switch(this);
        toggle.colours(Accent.colour(), skin.muted(), skin.card);
        toggle.setChecked(soufiT.isEnabled());
        toggle.setOnChanged(checked -> {
            if (lead instanceof Expressive.Badge)((Expressive.Badge) lead).set(checked, true);
            soufiT.setEnabled(checked);
            markChanged();
            rebuildAfterSwitchAnimation();
        });
        row.addView(toggle);
        row.setOnClickListener(v -> {
            toggle.performClick();
        });
        return SettingsUI.sized(this, row, 52);
    }

    public View countryHead() {
        String[] current = soufiT.current();
        LinearLayout row = SettingsUI.row(this);
        row.addView(flag(current[soufiT.ISO]));
        row.setAlpha(soufiT.isEnabled() ? 1f : 0.4f);
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(this, current[soufiT.LABEL]));
        text.addView(SettingsUI.detail(this, current[soufiT.CARRIER] + "  ·  " + current[soufiT.MCCMNC] + "  ·  " + current[soufiT.ISO].toUpperCase(Locale.US)));
        row.addView(text, SettingsUI.grow());
        row.addView(SettingsUI.disclosure(this, countriesOpen, "countriesOpen"));
        if (soufiT.isEnabled()) {
            row.setOnClickListener(v -> {
                disclosureKey = "countriesOpen";
                countriesOpen = !countriesOpen;
                rebuildAfterReveal(false);
            });
        }
        return SettingsUI.sized(this, row, 60);
    }

    public View countryRow(final String[] country) {
        boolean selected = country[soufiT.ISO].equals(soufiT.iso());
        LinearLayout row = SettingsUI.row(this);
        row.addView(flag(country[soufiT.ISO]));
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(this, country[soufiT.LABEL]));
        text.addView(SettingsUI.detail(this, country[soufiT.CARRIER] + "  ·  " + country[soufiT.MCCMNC] + "  ·  " + country[soufiT.ISO].toUpperCase(Locale.US)));
        row.addView(text, SettingsUI.grow());
        if (selected) row.addView(new Check(this, Accent.colour()));
        row.setOnClickListener(v -> {
            soufiT.setIso(country[soufiT.ISO]);
            countriesOpen = false;
            markChanged();
            rebuild();
        });
        return SettingsUI.sized(this, row, 60);
    }

    private View flag(String iso) {
        TextView view = new TextView(this);
        StringBuilder out = new StringBuilder();
        String code = iso.toUpperCase(Locale.US);
        for (int i = 0; i < code.length() && i < 2; i ++) {
            int letter = code.charAt(i) - 'A';
            if (letter < 0 || letter > 25) return SettingsUI.spacer(this);
            out.appendCodePoint(0x1F1E6 + letter);
        }
        view.setText(out.toString());
        android.graphics.Typeface flags = Fonts.twemoji(this);
        if (flags != null) view.setTypeface(flags);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        view.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(34), ViewGroup.LayoutParams.WRAP_CONTENT);
        size.rightMargin = dp(10);
        view.setLayoutParams(size);
        return view;
    }

    public View glassHead() {
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "tune"));
        row.addView(SettingsUI.label(this, Text.LIQUID_GLASS), SettingsUI.grow());
        TextView now = SettingsUI.detail(this, Surfaces.glassCount() + " / " + Surfaces.PIECES.length);
        now.setTextColor(skin.text);
        row.addView(now);
        row.addView(SettingsUI.disclosure(this, glassOpen, "glassOpen"));
        row.setOnClickListener(v -> {
            disclosureKey = "glassOpen";
            glassOpen = !glassOpen;
            rebuildAfterReveal(glassOpen);
        });
        return SettingsUI.sized(this, row, 52);
    }

    public View glassChoices() {
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(4), 0, dp(4));
        String[] pieces = Surfaces.PIECES;
        String[] titles = {Text.NAV_CAPSULE, Text.MD3_PROFILE, Text.MD3_FEED, Text.MD3_INBOX};
        for (int i = 0; i < pieces.length; i ++) {
            if (i > 0) rows.addView(SettingsUI.line(this));
            rows.addView(glassRow(titles[i], pieces[i]));
        }
        rows.addView(SettingsUI.caption(this, Text.GLASS_NOTE));
        return rows;
    }

    private View glassRow(String title, final String piece) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(12), dp(16), dp(12));
        box.addView(SettingsUI.label(this, title));
        LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setPadding(dp(4), dp(4), dp(4), dp(4));
        GradientDrawable well = new GradientDrawable();
        well.setCornerRadius(dp(20));
        well.setColor(skin.page);
        track.setBackground(well);
        LinearLayout.LayoutParams tracked = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tracked.topMargin = dp(10);
        int now = Surfaces.choice(piece);
        String[] names = {Text.GLASS_DEFAULT, Text.GLASS_MD3, Text.GLASS_ON};
        for (int value = 0; value < names.length; value ++) {
            final int chosen = value;
            track.addView(glassSegment(names[value], now == value, () -> {
                Surfaces.set(piece, chosen);
                rebuild();
            }));
        }
        box.addView(track, tracked);
        return box;
    }

    private TextView glassSegment(String text, boolean chosen, Runnable action) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setTextColor(chosen ? accentInk() : skin.muted());
        view.setGravity(Gravity.CENTER);
        view.setSingleLine(true);
        view.setPadding(dp(6), dp(9), dp(6), dp(9));
        GradientDrawable pill = new GradientDrawable();
        pill.setCornerRadius(dp(16));
        pill.setColor(chosen ? accentTonal() : 0x00000000);
        if (chosen) pill.setStroke(dp(1), Accent.colour());
        view.setBackground(pill);
        view.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        view.setOnClickListener(v -> action.run());
        Motion.press(view);
        return view;
    }

    public View accentHead() {
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "palette"));
        row.addView(SettingsUI.label(this, Text.ACCENT_COLOUR), SettingsUI.grow());
        row.addView(new Dot(this, Accent.colour(), false));
        row.addView(SettingsUI.disclosure(this, accentOpen, "accentOpen"));
        row.setOnClickListener(v -> {
            disclosureKey = "accentOpen";
            accentOpen = !accentOpen;
            rebuildAfterReveal(accentOpen);
        });
        return SettingsUI.sized(this, row, 52);
    }

    public View themeColoursHead() {
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "tune"));
        row.addView(SettingsUI.label(this, Text.THEME_COLOURS), SettingsUI.grow());
        row.addView(SettingsUI.disclosure(this, themeColoursOpen, "themeColoursOpen"));
        row.setContentDescription(Text.THEME_COLOURS);
        row.setFocusable(true);
        row.setOnClickListener(v -> {
            disclosureKey = "themeColoursOpen";
            themeColoursOpen = !themeColoursOpen;
            rebuildAfterReveal(themeColoursOpen);
        });
        return SettingsUI.sized(this, row, 52);
    }

    private View freeBanner() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(Math.max(skin.radius, dp(18)));
        background.setColor(blend(0xFF40E0D0, skin.card, 0.82f));
        card.setBackground(background);
        TextView head = new TextView(this);
        head.setText(Text.FREE_BADGE);
        head.setTextColor(skin.text);
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(head);
        TextView body = new TextView(this);
        body.setText(Text.FREE_BADGE_TEXT);
        body.setTextColor(skin.text);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        body.setPadding(0, dp(4), 0, 0);
        card.addView(body);
        TextView take = new TextView(this);
        take.setText(Text.FREE_BADGE_TAKE);
        take.setTextColor(0xFF10221F);
        take.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        take.setTypeface(Typeface.DEFAULT_BOLD);
        take.setGravity(Gravity.CENTER);
        take.setPadding(0, dp(11), 0, dp(11));
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(0xFF40E0D0);
        pill.setCornerRadius(dp(14));
        take.setBackground(pill);
        LinearLayout.LayoutParams below = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        below.topMargin = dp(14);
        take.setOnClickListener(v -> {
            if (!Mine.everAsked()) {
                Mine.ask(() -> Mine.takeFree((p0, p1) -> ProfileMenu.tookFree(this, p0, p1)));
                return;
            }
            Mine.takeFree((p0, p1) -> ProfileMenu.tookFree(this, p0, p1));
        });
        card.addView(take, below);
        LinearLayout holder = new LinearLayout(this);
        holder.setPadding(dp(20), dp(14), dp(20), dp(2));
        holder.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return holder;
    }

    private static int blend(int colour, int into, float how) {
        int red = (int)(((colour >> 16) & 0xFF) * (1 - how) + ((into >> 16) & 0xFF) * how);
        int green = (int)(((colour >> 8) & 0xFF) * (1 - how) + ((into >> 8) & 0xFF) * how);
        int blue = (int)((colour & 0xFF) * (1 - how) + (into & 0xFF) * how);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    public View wallpaperRow() {
        final int colour = Accent.fromWallpaper();
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "wallpaper"));
        row.addView(SettingsUI.label(this, Text.ACCENT_WALLPAPER), SettingsUI.grow());
        row.addView(new Dot(this, colour, Accent.isMaterial()));
        row.setOnClickListener(v -> {
            Accent.setMaterial(true);
            markChanged();
            rebuild();
        });
        return SettingsUI.sized(this, row, 52);
    }

    public void rebuildAfterReveal(boolean expanded) {
        if (collapsing) return;
        java.util.List<DisclosureBody> bodies = disclosureBodies.get(disclosureKey);
        if (!expanded && bodies != null && !bodies.isEmpty() && Motion.enabled(this)) {
            collapsing = true;
            Chevron arrow = disclosureArrows.get(disclosureKey);
            if (arrow != null) arrow.animate().rotation(0f).setDuration(180L).start();
            final int[] remaining = {bodies.size()};
            for (DisclosureBody body : new java.util.ArrayList<>(bodies)) body.collapse(() -> {
                if (-- remaining[0] == 0) {
                    collapsing = false;
                    revealAnimationRequested = false;
                    disclosureKey = null;
                    rebuild();
                }
            });
            return;
        }
        revealAnimationRequested = expanded;
        rebuild();
        disclosureKey = null;
    }

    private static final int CREATE_BACKUP = 0x4D42;
    private static final int PICK_BACKUP = 0x4D49;
    public void createBackup() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "soufit-settings.json");
            startActivityForResult(intent, CREATE_BACKUP);
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    public void pickBackup() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(intent, PICK_BACKUP);
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    private void reviewBackup(final Uri source) {
        Screen.progress(Text.BACKUP_READING, 0);
        Net.away("backup", () -> {
            final Backup.Review review = Backup.review(this, source);
            if (review.fits()) Screen.progressDone();
            else Screen.progressGone();
            runOnUiThread(() -> {
                if (!review.fits()) {
                    Screen.say(Text.BACKUP_FAILED);
                    return;
                }
                new ModDialog.Builder(this, skin).setTitle(Text.BACKUP_IMPORT).setMessage(Text.backupPreview(review.count()))
                        .setNegativeButton(Text.CANCEL, null).setPositiveButton(Text.BACKUP_APPLY, (dialog, which) -> {
                    if (Backup.apply(this, review)) {
                        markChanged();
                        rebuild();
                        Screen.say(Text.BACKUP_DONE);
                    } else Screen.say(Text.BACKUP_FAILED);
                }).show();
            });
        });
    }

    public void pickResetGroup() {
        final String[] groups = {Text.RESET_APPEARANCE, Text.RESET_PLAYER, Text.RESET_FEED, Text.RESET_ALL};
        try {
            new ModDialog.Builder(this, skin).setTitle(Text.BACKUP_RESET).setMessage(Text.BACKUP_RESET_ASK).setItems(groups,
                    (dialog, chosen) -> askToReset(chosen + 1)).setNegativeButton(Text.CANCEL, null).show();
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    private void askToReset(final int group) {
        new ModDialog.Builder(this, skin).setTitle(Text.BACKUP_RESET).setMessage(Text.BACKUP_RESET_ASK).setNegativeButton(Text.CANCEL,
                null).setPositiveButton(Text.BACKUP_APPLY, (dialog, which) -> {
            Backup.reset(this, group);
            markChanged();
            rebuild();
        }).show();
    }

    public interface Chosen {
        void at(int value);
    }

    private static final int PICK_FONT = 0x4D46;
    private static final int PICK_EMOJI = 0x4D45;
    public View fontHead() {
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "text_fields"));
        row.addView(SettingsUI.label(this, Text.FONT), SettingsUI.grow());
        TextView now = SettingsUI.detail(this, fontName(Fonts.name()));
        now.setTextColor(skin.text);
        row.addView(now);
        row.addView(SettingsUI.disclosure(this, fontOpen, "fontOpen"));
        row.setOnClickListener(v -> {
            disclosureKey = "fontOpen";
            fontOpen = !fontOpen;
            rebuildAfterReveal(fontOpen);
        });
        return SettingsUI.sized(this, row, 52);
    }

    private static String fontName(String which) {
        if (Fonts.SANS.equals(which)) return Text.FONT_SANS;
        if (Fonts.SANS_LIGHT.equals(which)) return Text.FONT_SANS_LIGHT;
        if (Fonts.SANS_CONDENSED.equals(which)) return Text.FONT_SANS_CONDENSED;
        if (Fonts.SERIF.equals(which)) return Text.FONT_SERIF;
        if (Fonts.MONOSPACE.equals(which)) return Text.FONT_MONOSPACE;
        if (Fonts.CURSIVE.equals(which)) return Text.FONT_CURSIVE;
        if (Fonts.FILE.equals(which)) return Text.FONT_FILE;
        return Text.FONT_SYSTEM;
    }

    public View fontChoices() {
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        String now = Fonts.name();
        for (int i = 0; i < Fonts.PRESETS.length; i ++) {
            final String which = Fonts.PRESETS[i];
            if (i > 0) rows.addView(SettingsUI.line(this));
            rows.addView(SettingsUI.pickRow(this, fontName(which), which.equals(now), Fonts.SYSTEM.equals(which) ? null : which,
                    () -> {
                Fonts.choose(which);
                markChanged();
                rebuild();
            }));
        }
        rows.addView(SettingsUI.line(this));
        rows.addView(SettingsUI.actionRow(this, "download", Text.FONT_PICK, Fonts.FILE.equals(now) ? Text.FONT_FILE : null,
                this::pickFont));
        rows.addView(SettingsUI.line(this));
        rows.addView(SettingsUI.section(this, Text.EMOJI));
        String emoji = Fonts.emoji();
        List<EmojiPackManager.Pack> packs = EmojiPackManager.packs(this);
        boolean firstPack = true;
        for (EmojiPackManager.Pack pack : packs) {
            final String which = pack.id;
            if (Fonts.EMOJI_FILE.equals(which)) continue;
            if (!firstPack) rows.addView(SettingsUI.line(this));
            firstPack = false;
            rows.addView(SettingsUI.pickRow(this, emojiName(pack), which.equals(emoji), null, () -> {
                Fonts.chooseEmoji(this, which);
                markChanged();
                rebuild();
            }));
        }
        rows.addView(SettingsUI.line(this));
        rows.addView(SettingsUI.actionRow(this, "download", Text.EMOJI_FILE, Fonts.EMOJI_FILE.equals(emoji) ? Text.EMOJI_FILE
                : null, this::pickEmoji));
        TextView sample = new TextView(this);
        sample.setText(Text.EMOJI_PREVIEW);
        sample.setTextColor(skin.text);
        sample.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        sample.setGravity(Gravity.CENTER);
        sample.setPadding(dp(16), dp(12), dp(16), dp(12));
        android.graphics.Typeface emojiPreview = Fonts.preview(this, emoji);
        if (emojiPreview != null) sample.setTypeface(emojiPreview);
        rows.addView(sample, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));
        rows.addView(SettingsUI.quiet(this, Text.EMOJI_NOTE));
        return rows;
    }

    private String emojiName(EmojiPackManager.Pack pack) {
        if (Fonts.SYSTEM.equals(pack.id)) return Text.EMOJI_SYSTEM;
        return pack.name;
    }

    private void pickEmoji() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_EMOJI);
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    private void pickFont() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_FONT);
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    public View iconHead() {
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "image"));
        row.addView(SettingsUI.label(this, Text.ICON), SettingsUI.grow());
        TextView now = SettingsUI.detail(this, Launcher.nameOf(Launcher.chosen()));
        now.setTextColor(skin.text);
        row.addView(now);
        row.addView(SettingsUI.disclosure(this, iconOpen, "iconOpen"));
        row.setOnClickListener(v -> {
            disclosureKey = "iconOpen";
            iconOpen = !iconOpen;
            rebuildAfterReveal(iconOpen);
        });
        return SettingsUI.sized(this, row, 52);
    }

    public View iconChoices() {
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        String now = Launcher.chosen();
        String[] all = Launcher.all();
        for (int i = 0; i < all.length; i ++) {
            final String which = all[i];
            if (i > 0) rows.addView(SettingsUI.line(this));
            LinearLayout row = SettingsUI.row(this);
            row.setPadding(dp(12), 0, dp(12), 0);
            row.setGravity(Gravity.CENTER_VERTICAL);
            android.widget.ImageView shot = new android.widget.ImageView(this);
            android.graphics.Bitmap picture = Launcher.preview(which);
            if (picture != null) shot.setImageBitmap(picture);
            shot.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(32), dp(32));
            size.rightMargin = dp(14);
            row.addView(shot, size);
            row.addView(SettingsUI.label(this, Launcher.nameOf(which)), SettingsUI.grow());
            if (which.equals(now)) row.addView(new Check(this, Accent.colour()));
            row.setOnClickListener(v -> {
                Launcher.choose(this, which);
                rebuild();
            });
            rows.addView(SettingsUI.sized(this, row, 56));
        }
        rows.addView(SettingsUI.line(this));
        return rows;
    }

    public interface ColourPicked {
        void set(int colour);
    }

    public View appearanceColourRow(final String role) {
        final int selected = AppearanceColors.ACCENT.equals(role) ? Accent.colour() : AppearanceColors.get(role);
        LinearLayout row = SettingsUI.row(this);
        row.setPadding(dp(12), 0, dp(12), 0);
        row.addView(SettingsUI.label(this, AppearanceColors.label(role)), SettingsUI.grow());
        row.addView(new Dot(this, selected == 0 ? skin.muted() : selected, false));
        TextView value = SettingsUI.detail(this, selected == 0 ? "TikTok" : String.format(Locale.US, "#%06X", selected & 0xFFFFFF));
        value.setTextColor(skin.muted());
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.leftMargin = dp(10);
        row.addView(value, valueParams);
        row.setOnClickListener(v -> askColour(AppearanceColors.label(role), selected == 0 ? Accent.TIKTOK : selected, colour -> {
            AppearanceColors.set(role, colour);
            markChanged();
            rebuild();
        }));
        return SettingsUI.sized(this, row, 52);
    }

    private void askColour(String title, int initial, ColourPicked picked) {
        Panel panel = Panel.with(this, skin, title);
        EditText input = panel.field("#RRGGBB");
        input.setText(String.format(Locale.US, "#%06X", initial & 0xFFFFFF));
        panel.primaryKeepOpen(getString(android.R.string.ok), () -> {
            try {
                int colour = android.graphics.Color.parseColor(input.getText().toString().trim());
                picked.set(colour);
                panel.close();
            } catch (IllegalArgumentException error) {
                input.setError(Local.of("Enter the colour as #RRGGBB"));
            }
        });
        panel.quiet(Text.CANCEL, null);
        panel.show();
    }

    public void chooseThemePreset() {
        final String[] choices = {Text.THEME_PRESET_DARK, Text.THEME_PRESET_BLUE, Text.THEME_RESET};
        new ModDialog.Builder(this, skin).setTitle(Text.THEME_PRESETS).setItems(choices, (dialog, index) -> {
            if (index == 0) Themes.preset(0);
            else if (index == 1) Themes.preset(1);
            else Themes.reset();
            markChanged();
            rebuild();
        }).setNegativeButton(Text.CANCEL, null).show();
    }

    private void chooseAccentColour() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(8), dp(20), dp(8));
        final AccentColourPicker picker = new AccentColourPicker(this, Accent.colour());
        final View sample = new View(this);
        LinearLayout.LayoutParams sampleParams = new LinearLayout.LayoutParams(dp(56), dp(28));
        sampleParams.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(sample, sampleParams);
        content.addView(picker, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(238)));
        picker.onColourChanged(colour -> sample.setBackgroundColor(colour));
        sample.setBackgroundColor(Accent.colour());
        android.widget.HorizontalScrollView extras = new android.widget.HorizontalScrollView(this);
        extras.setHorizontalScrollBarEnabled(false);
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setPadding(0, dp(8), 0, dp(8));
        final java.util.List<Dot> extraDots = new java.util.ArrayList<>();
        for (int colour : new int[] {0xFFFFA726, 0xFF44D65B, 0xFFAA45EE, 0xFFF343C9, 0xFF405FE8, 0xFF35B8E5, 0xFFFA4654}) {
            Dot dot = new Dot(this, colour, colour == Accent.colour());
            extraDots.add(dot);
            dot.setContentDescription(String.format(Locale.ROOT, "#%06X", colour & 0xffffff));
            Motion.touch(dot);
            dot.setOnClickListener(v -> {
                picker.setColour(colour);
                for (Dot other : extraDots) other.choose(other == dot, true);
            });
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(44), dp(44));
            size.rightMargin = dp(8);
            strip.addView(dot, size);
        }
        extras.addView(strip);
        content.addView(extras);
        new ModDialog.Builder(this, skin).setTitle(Text.ACCENT_PICKER).setView(content).setNegativeButton(android.R.string.cancel,
                null).setPositiveButton(android.R.string.ok, (dialog, which) -> {
            Accent.setCustom(picker.colour());
            if (accentCustomSwatch != null) accentCustomSwatch.reveal(picker.colour());
            markChanged();
            if (rebuildQueued) column.removeCallbacks(delayedRebuild);
            rebuildQueued = true;
            column.postDelayed(delayedRebuild, 260L);
        }).show();
    }

    public View palette() {
        android.widget.HorizontalScrollView scroll = new android.widget.HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setPadding(dp(16), dp(12), dp(16), dp(16));
        int[] colours = Accent.palette();
        final java.util.List<Dot> swatches = new java.util.ArrayList<>();
        for (int colour : colours) {
            Dot dot = new Dot(this, colour, !Accent.isMaterial() && colour == Accent.colour());
            swatches.add(dot);
            Motion.touch(dot);
            dot.setContentDescription(String.format(Locale.US, "#%06X", colour & 0xFFFFFF));
            dot.setOnClickListener(v -> {
                Accent.set(colour);
                for (Dot swatch : swatches) swatch.choose(swatch.fill.getColor() == colour, true);
                if (accentCustomSwatch != null) accentCustomSwatch.choose(false, true);
                markChanged();
                rebuildAfterSwitchAnimation();
            });
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(48), dp(48));
            size.rightMargin = dp(8);
            strip.addView(dot, size);
        }
        int custom = Accent.custom();
        Dot picker = new Dot(this, custom == 0 ? Accent.colour() : custom, custom != 0 && !Accent.isMaterial() && custom == Accent.colour());
        if (custom == 0) picker.fill.setShader(new android.graphics.SweepGradient(dp(24), dp(24), new int[] {0xFFE53935, 0xFFFFB300,
                0xFF43A047, 0xFF00ACC1, 0xFF1E88E5, 0xFF8E24AA, 0xFFE53935}, null));
        accentCustomSwatch = picker;
        Motion.touch(picker);
        picker.setContentDescription(Text.ACCENT_PICKER);
        picker.setOnClickListener(v -> chooseAccentColour());
        strip.addView(picker, new LinearLayout.LayoutParams(dp(48), dp(48)));
        scroll.addView(strip);
        return scroll;
    }

    public interface Setting {
        void set(boolean on);
    }

    public View stickerHead() {
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "star"));
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(this, Text.STREAK_STICKER));
        int many = Streaks.offered().size();
        text.addView(SettingsUI.detail(this, many == 0 ? Text.STREAK_NOTHING : String.valueOf(many)));
        row.addView(text, SettingsUI.grow());
        row.addView(SettingsUI.disclosure(this, streakOpen, "streakOpen"));
        row.setOnClickListener(v -> {
            disclosureKey = "streakOpen";
            streakOpen = !streakOpen;
            rebuildAfterReveal(streakOpen);
        });
        return SettingsUI.sized(this, row, 60);
    }

    public View stickerChoices() {
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(dp(16), dp(10), dp(16), dp(14));
        FavouriteStickers.load(() -> {
            if (!isFinishing()) rebuild();
        });
        java.util.List<String> ids = Streaks.offered();
        if (ids.isEmpty()) {
            rows.addView(SettingsUI.quiet(this, Text.STREAK_NOTHING));
            return rows;
        }
        String chosen = Streaks.chosen();
        LinearLayout line = null;
        for (int i = 0; i < ids.size() && i < 120; i ++) {
            if (i % 5 == 0) {
                line = new LinearLayout(this);
                line.setOrientation(LinearLayout.HORIZONTAL);
                line.setPadding(0, dp(5), 0, dp(5));
                rows.addView(line);
            }
            final String id = ids.get(i);
            View one = stickerTile(id, id.equals(chosen));
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(52), dp(52), 1f);
            line.addView(one, size);
        }
        return rows;
    }

    private View stickerTile(final String id, boolean chosen) {
        ImageView view = new ImageView(this);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setPadding(dp(4), dp(4), dp(4), dp(4));
        if (chosen) {
            GradientDrawable ring = new GradientDrawable();
            ring.setColor(0x00000000);
            ring.setStroke(dp(2), Accent.colour());
            ring.setCornerRadius(dp(10));
            view.setBackground(ring);
        }
        final java.lang.ref.WeakReference<ImageView> target = new java.lang.ref.WeakReference<ImageView>(view);
        Bitmap picture = Streaks.thumbnail(this, id, () -> {
            ImageView tile = target.get();
            if (tile != null && tile.isAttachedToWindow()) {
                Bitmap loaded = Streaks.thumbnail(tile.getContext(), id);
                if (loaded != null) tile.setImageBitmap(loaded);
            }
        });
        if (picture != null) view.setImageBitmap(picture);
        view.setOnClickListener(v -> {
            Streaks.choose(id);
            rebuild();
        });
        return view;
    }

    private View versions() {
        TextView view = new TextView(this);
        view.setText("soufiT " + Version.MOD + "  ·  TikTok " + Version.TIKTOK + "\n" + Text.MADE_BY);
        view.setTextColor(skin.muted());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(20), dp(24), dp(20), dp(8));
        return view;
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null) return;
        Uri source = data.getData();
        if (source == null) return;
        if (request == CREATE_BACKUP) {
            Screen.progress(Text.BACKUP_WRITING, 0);
            Net.away("backup", () -> {
                boolean done = Backup.write(this, source);
                if (done) Screen.progressDone();
                else Screen.progressGone();
                runOnUiThread(() -> Screen.say(done ? Text.BACKUP_DONE : Text.BACKUP_FAILED));
            });
            return;
        }
        if (request == PICK_BACKUP) {
            reviewBackup(source);
            return;
        }
        if (request == PICK_FONT || request == PICK_EMOJI) {
            if (Fonts.take(this, source, request == PICK_EMOJI)) markChanged();
            else Toast.makeText(this, Text.FONT_FAILED, Toast.LENGTH_LONG).show();
            rebuild();
            return;
        }
        if (request != PluginsMenu.PICK_PLUGIN) return;
        PluginsMenu.reviewPlugin(this, source);
    }

    public View channelBanner() {
        ChannelBanner banner = new ChannelBanner(this);
        banner.setOnClickListener(v -> open(ChannelBanner.URL));
        LinearLayout box = new LinearLayout(this);
        box.setPadding(dp(12), dp(2), dp(12), dp(10));
        box.addView(banner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return box;
    }

    public View aboutIntro() {
        int accent = Accent.colour();
        int deeper = (accent & 0xFF000000) | ((accent >> 1) & 0x7F7F7F);
        int ink = accentInk();
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(22), dp(22), dp(22), dp(20));
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[] {accent, deeper});
        background.setCornerRadius(dp(28));
        hero.setBackground(background);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView head = new TextView(this);
        head.setText("soufiT");
        head.setTextColor(ink);
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        head.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        top.addView(head, SettingsUI.grow());
        top.addView(chip("v" + Version.MOD, ink));
        hero.addView(top);
        TextView kicker = new TextView(this);
        kicker.setText(Text.FORK_OF);
        kicker.setTextColor((ink & 0xFFFFFF) | 0xB3000000);
        kicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        kicker.setLetterSpacing(0.03f);
        kicker.setPadding(0, dp(2), 0, 0);
        hero.addView(kicker);
        TextView body = new TextView(this);
        body.setText(Text.ABOUT_INTRO);
        body.setTextColor((ink & 0xFFFFFF) | 0xE6000000);
        body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        body.setLineSpacing(dp(5), 1f);
        body.setPadding(0, dp(16), 0, 0);
        hero.addView(body);
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(0, dp(16), 0, 0);
        chips.addView(chip("@soufied", ink));
        chips.addView(chip("@narezany", ink));
        hero.addView(chips);
        TextView build = new TextView(this);
        build.setText(Text.ABOUT_TIKTOK_BUILD + " " + Version.TIKTOK);
        build.setTextColor((ink & 0xFFFFFF) | 0x80000000);
        build.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        build.setPadding(0, dp(14), 0, 0);
        hero.addView(build);
        return SettingsUI.wrap(this, hero);
    }

    private TextView chip(String text, int ink) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ink);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable shape = new GradientDrawable();
        shape.setColor((ink & 0xFFFFFF) | 0x26000000);
        shape.setCornerRadius(dp(14));
        view.setBackground(shape);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dp(8);
        view.setLayoutParams(params);
        return view;
    }

    private View forkLine() {
        TextView view = new TextView(this);
        view.setText(Text.FORK_OF);
        view.setTextColor(skin.muted());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        view.setPadding(dp(28), 0, dp(24), dp(12));
        return view;
    }

    public View countrySearch() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(4), dp(12), dp(4));
        GradientDrawable background = new GradientDrawable();
        background.setColor(skin.card);
        background.setCornerRadius(dp(24));
        row.setBackground(background);
        ImageView glass = new ImageView(this);
        glass.setImageDrawable(new Expressive.Magnifier(skin.muted()));
        LinearLayout.LayoutParams glassSize = new LinearLayout.LayoutParams(dp(22), dp(22));
        glassSize.rightMargin = dp(10);
        row.addView(glass, glassSize);
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(Text.COUNTRY_SEARCH);
        input.setHintTextColor(skin.muted());
        input.setTextColor(skin.text);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        row.addView(input, SettingsUI.grow());
        input.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {} @Override public void onTextChanged(CharSequence text,
                    int start, int before, int count) {
                filterCountries(text.toString());
            }
            @Override public void afterTextChanged(android.text.Editable text) {}
        });
        return SettingsUI.wrap(this, row);
    }

    private void filterCountries(String typed) {
        String wanted = typed.trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < countryRows.size(); i ++) {
            String[] country = countryRows.get(i);
            boolean shown = wanted.isEmpty() || (country[soufiT.LABEL] + " " + country[soufiT.ISO] + " " + country[soufiT.CARRIER])
                    .toLowerCase(Locale.ROOT).contains(wanted);
            countryViews.get(i).setVisibility(shown ? View.VISIBLE : View.GONE);
        }
    }

    public final java.util.List<String[]> countryRows = new java.util.ArrayList<String[]>();
    public final java.util.List<View> countryViews = new java.util.ArrayList<View>();
    public View thanks() {
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(6), 0, dp(10));
        rows.addView(SettingsUI.quiet(this, Text.THANKS_NOTE));
        rows.addView(owner());
        rows.addView(person("someone's mf", Text.THANKS_TTCUZ, TTCUZ));
        rows.addView(SettingsUI.line(this));
        TextView heading = new TextView(this);
        heading.setText(Text.DONATE);
        heading.setTextColor(skin.text);
        heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        heading.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        heading.setPadding(dp(16), dp(14), dp(16), dp(2));
        rows.addView(heading);
        rows.addView(SettingsUI.quiet(this, Text.DONATE_NOTE));
        rows.addView(SettingsUI.linkRow(this, "star", Text.YOOMONEY, Text.YOOMONEY_NOTE, YOOMONEY));
        return rows;
    }

    private View owner() {
        LinearLayout row = SettingsUI.row(this);
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(this, "@narezany"));
        text.addView(SettingsUI.detail(this, Text.THANKS_OWNER));
        row.addView(text, SettingsUI.grow());
        row.addView(SettingsUI.away(this));
        row.setOnClickListener(v -> {
            try {
                new ModDialog.Builder(this, skin).setTitle("@narezany").setItems(new CharSequence[] {"Telegram", "TikTok"},
                        (dialog, which) -> open(which == 0 ? OWNER_TELEGRAM : OWNER_TIKTOK)).setNegativeButton(Text.CANCEL,
                        null).show();
            } catch (Throwable error) {
                open(OWNER_TELEGRAM);
            }
        });
        return SettingsUI.sized(this, row, 60);
    }

    private View person(String name, String what, final String url) {
        LinearLayout row = SettingsUI.row(this);
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(SettingsUI.label(this, name));
        text.addView(SettingsUI.detail(this, what));
        row.addView(text, SettingsUI.grow());
        if (url != null) {
            row.addView(SettingsUI.away(this));
            row.setOnClickListener(v -> open(url));
        }
        return SettingsUI.sized(this, row, 60);
    }

    public void open(String url) {
        if (Sites.blocked(url)) {
            Toast.makeText(this, Text.SITE_BLOCKED, Toast.LENGTH_SHORT).show();
            return;
        }
        url = inApp(url);
        if (url.contains("tiktok.com") && openWith(url, getPackageName())) return;
        if (openWith(url, null)) return;
        Toast.makeText(this, Text.NO_BROWSER, Toast.LENGTH_SHORT).show();
    }

    private static String inApp(String url) {
        if (url.startsWith("https://tiktok.com/")) {
            return "https://www.tiktok.com/" + url.substring("https://tiktok.com/".length());
        }
        return url;
    }

    private boolean openWith(String url, String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (packageName != null) intent.setPackage(packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public void copy(String what, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(what, text));
            Toast.makeText(this, Text.COPIED, Toast.LENGTH_SHORT).show();
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    private void developerTap() {
        if (!DevMode.tapped()) return;
        if (DevMode.isOn()) {
            Toast.makeText(this, Text.DEV_ALREADY, Toast.LENGTH_SHORT).show();
            return;
        }
        DevMode.set(true);
        Diary.note("developer mode on");
        Toast.makeText(this, Text.DEV_ON, Toast.LENGTH_SHORT).show();
        rebuild();
    }

    public void addDeveloperRows(LinearLayout into) {
        into.addView(SettingsUI.line(this));
        into.addView(SettingsUI.actionRow(this, "tune", Text.DEV_FLAGS, Text.DEV_FLAGS_NOTE, this::showFlags));
        into.addView(SettingsUI.line(this));
        into.addView(SettingsUI.actionRow(this, "article", Text.DEV_FULL_REPORT, Text.DEV_FULL_NOTE, () -> copy("soufiT full report",
                Diagnostics.fullReport())));
        into.addView(SettingsUI.line(this));
        into.addView(SettingsUI.actionRow(this, "info", Text.DEV_DUMP_ALL, Text.DEV_DUMP_ALL_NOTE, this::dumpEveryMenu));
        into.addView(SettingsUI.line(this));
        into.addView(SettingsUI.toggleRow(this, "tune", Text.DEV_SWITCH, true, on -> {
            if (!on) {
                DevMode.set(false);
                rebuildAfterSwitchAnimation();
            }
        }));
    }

    private void dumpEveryMenu() {
        final int restore = settingsGroup;
        final int lastSection = 8;
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] section = {0};
        final Runnable[] next = new Runnable[1];
        Diary.note("menu dump: started");
        Diary.beginMenuDump();
        next[0] = new Runnable() {
            @Override public void run() {
                if (section[0] > lastSection) {
                    handler.postDelayed(new Runnable() {
                        @Override public void run() {
                            settingsGroup = restore;
                            rebuild();
                            List<String> lines = Diary.endMenuDump();
                            Diary.note("menu dump: finished, " + lines.size() + " lines");
                            StringBuilder text = new StringBuilder("soufiT menu dump\n");
                            for (String line : lines) text.append(line).append('\n');
                            copy("soufiT menu dump", text.toString());
                        }
                    }, 600);
                    return;
                }
                final int shown = section[0];
                settingsGroup = shown;
                rebuild();
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
                        Diary.snapshot(SettingsActivity.this, "section " + shown + " " + groupName(shown));
                        section[0] ++;
                        next[0].run();
                    }
                }, 900);
            }
        };
        next[0].run();
    }

    public void chooseAvatarShape() {
        final Panel panel = Panel.with(this, skin, Text.AVATAR_SHAPE);
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        final String now = AvatarShape.chosen();
        LinearLayout line = null;
        for (int i = 0; i < AvatarShape.ALL.length; i ++) {
            final String shape = AvatarShape.ALL[i];
            if (i % 4 == 0) {
                line = new LinearLayout(this);
                grid.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            LinearLayout tile = Panel.tile(this, skin, AvatarShape.picture(shape, dp(36), 0xFFFFFFFF), shape.equals(now) ? Accent.colour()
                    : skin.text, AvatarShape.nameOf(shape), shape.equals(now));
            tile.setOnClickListener(v -> {
                panel.close();
                if (shape.equals(now)) return;
                AvatarShape.choose(this, shape);
                markChanged();
                rebuild();
            });
            Motion.press(tile);
            LinearLayout.LayoutParams place = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            place.setMargins(dp(3), dp(3), dp(3), dp(3));
            line.addView(tile, place);
        }
        for (int i = AvatarShape.ALL.length; i % 4 != 0; i ++) {
            line.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
            ((LinearLayout.LayoutParams) line.getChildAt(line.getChildCount() - 1).getLayoutParams()).setMargins(dp(3), dp(3),
                    dp(3), dp(3));
        }
        panel.view(grid);
        panel.quiet(Text.CLOSE, null);
        panel.show();
    }

    public static final int FLAGS_PAGE = 98;
    public String flagQuery = "";
    public java.util.List<Flags.Known> flagList;
    public boolean flagsLoading;
    private void showFlags() {
        settingsGroup = FLAGS_PAGE;
        searchQuery = "";
        flagQuery = "";
        page.scrollTo(0, 0);
        rebuild();
    }

    private View fullReportView() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(8), dp(16), dp(12));
        TextView view = new TextView(this);
        view.setText(Diagnostics.fullReport());
        view.setTextColor(skin.muted());
        view.setTypeface(Typeface.MONOSPACE);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        view.setTextIsSelectable(true);
        box.addView(view);
        return box;
    }

    public View diaryHead() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = SettingsUI.row(this);
        row.addView(SettingsUI.icon(this, "article"));
        TextView title = SettingsUI.label(this, Text.DIARY_TITLE);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        row.addView(title, SettingsUI.grow());
        row.addView(SettingsUI.disclosure(this, diaryOpen, "diaryOpen"));
        row.setOnClickListener(v -> {
            disclosureKey = "diaryOpen";
            diaryOpen = !diaryOpen;
            rebuildAfterReveal(diaryOpen);
        });
        box.addView(SettingsUI.sized(this, row, 52));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(dp(16), 0, dp(16), dp(8));
        TextView copy = diaryAction(Text.COPY);
        copy.setOnClickListener(v -> copyDiary());
        actions.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView clear = diaryAction(Text.CLEAR);
        clear.setOnClickListener(v -> {
            Diary.clear();
            Toast.makeText(this, Text.CLEARED, Toast.LENGTH_SHORT).show();
            rebuild();
        });
        LinearLayout.LayoutParams clearWhere = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        clearWhere.leftMargin = dp(8);
        actions.addView(clear, clearWhere);
        box.addView(actions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return box;
    }

    private TextView diaryAction(String text) {
        TextView action = new TextView(this);
        action.setText(text);
        action.setTextColor(Accent.colour());
        action.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        action.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        action.setGravity(Gravity.CENTER);
        action.setSingleLine(true);
        action.setEllipsize(android.text.TextUtils.TruncateAt.END);
        action.setPadding(dp(12), dp(10), dp(12), dp(10));
        action.setMinHeight(dp(44));
        action.setClickable(true);
        action.setFocusable(true);
        GradientDrawable pill = new GradientDrawable();
        pill.setCornerRadius(dp(22));
        pill.setColor((Accent.colour() & 0xFFFFFF) | 0x1F000000);
        action.setBackground(pill);
        Motion.press(action);
        return action;
    }

    public View diaryLines() {
        if (DevMode.isOn()) return fullReportView();
        java.util.List<String> all = Diary.lines();
        StringBuilder text = new StringBuilder();
        for (String entry : all.subList(Math.max(0, all.size() - 300), all.size())) {
            text.append(entry).append('\n');
        }
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(8), dp(16), dp(12));
        TextView view = new TextView(this);
        view.setText(text.toString());
        view.setTextColor(skin.muted());
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        box.addView(view);
        return box;
    }

    private void restartTikTok() {
        try {
            android.content.Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent == null) return;
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            Runtime.getRuntime().exit(0);
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    private void copyDiary() {
        try {
            StringBuilder out = new StringBuilder("soufiT\n");
            if (DevMode.isOn()) out.append(Diagnostics.fullReport());
            else for (String line : Diary.lines()) out.append(line).append('\n');
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("soufiT", out.toString()));
            Toast.makeText(this, Text.COPIED, Toast.LENGTH_SHORT).show();
        } catch (Throwable error) {
            Toast.makeText(this, String.valueOf(error), Toast.LENGTH_LONG).show();
        }
    }

    private View backArrow() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(12), dp(8), dp(16), dp(4));
        View button = Expressive.roundButton(this, SettingsGlyph.make("arrow_forward", skin.text), skin.card, (Accent.colour() & 0xFFFFFF) | 0x33000000,
                skin.text, 48);
        ((ViewGroup) button).getChildAt(0).setScaleX(- 1f);
        bar.addView(button);
        TextView name = new TextView(this);
        name.setText(settingsGroup == 0 ? "TikTok" : Local.of("Settings"));
        name.setTextColor(skin.muted());
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        name.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        name.setPadding(dp(12), 0, 0, 0);
        bar.addView(name);
        bar.setContentDescription(settingsGroup == 0 ? "TikTok" : Local.of("Settings"));
        bar.setOnClickListener(v -> goBack());
        return bar;
    }

    private View title(String text) {
        final TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(skin.text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, settingsGroup == 0 ? 44 : 34);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setPadding(dp(28), dp(8), dp(24), dp(16));
        view.setOnClickListener(v -> {
            Cats.tapped(view);
            developerTap();
        });
        return view;
    }

    public static final long SEARCH_DELAY = 120L;
    public EditText searchField;
    public LinearLayout searchHolder;
    public boolean refocusSearch;
    public final Runnable liveSearch = new Runnable() {
        @Override public void run() {
            if (searchField != null) applySearch(searchField.getText().toString());
        }
    };

    public void applySearch(String typed) {
        String next = typed == null ? "" : typed.trim().toLowerCase(Locale.ROOT);
        if (next.equals(searchQuery)) return;
        boolean searching = searchQuery.length() > 0;
        searchQuery = next;
        if (searching && next.length() > 0 && searchHolder != null && searchHolder.isAttachedToWindow()) {
            searchHolder.removeAllViews();
            SearchMenu.fillSearch(this, searchHolder);
            return;
        }
        refocusSearch = next.length() > 0;
        rebuild();
    }

    public void focusSearchField() {
        final EditText field = searchField;
        if (field == null) return;
        field.post(() -> {
            field.requestFocus();
            field.setSelection(field.getText().length());
            android.view.inputmethod.InputMethodManager keys = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keys != null) keys.showSoftInput(field, 0);
        });
    }

    private String groupName(int group) {
        switch (group) {
            case 1: return Local.of("Appearance");
            case 2: return Local.of("Profile");
            case 3: return Local.of("Feed and videos");
            case 4: return Local.of("Tools");
            case 5: return Local.of("About");
            case 6: return Local.of("Plugins");
            case 7: return Local.of("Friends and chats");
            case 8: return Text.DEVELOPER_MENU;
            default: return "soufiT";
        }
    }

    private View groupNavigation() {
        LinearLayout list = SettingsUI.card(this);
        String[] icons = {"palette", "group", "play_circle", "tune", "info", "extension", "chat", "flare"};
        String[] notes = {Text.SHELF_LOOK, Text.SHELF_PROFILE, Text.SHELF_FEED, Text.SHELF_TOOLS, Text.SHELF_ABOUT, Text.SHELF_PLUGINS,
                Text.SHELF_FRIENDS, Text.DEVELOPER_NOTE};
        int[] shelf = DevMode.isOn() ? new int[] {1, 2, 3, 7, 4, 6, 5, 8} : new int[] {1, 2, 3, 7, 4, 6, 5};
        for (int n = 0; n < shelf.length; n ++) {
            final int i = shelf[n];
            final int chosen = i;
            if (n > 0) list.addView(SettingsUI.line(this));
            LinearLayout entry = SettingsUI.row(this);
            entry.setPadding(dp(16), dp(14), dp(20), dp(14));
            entry.addView(SettingsUI.icon(this, icons[i - 1], false, 52));
            LinearLayout words = new LinearLayout(this);
            words.setOrientation(LinearLayout.VERTICAL);
            TextView head = SettingsUI.label(this, groupName(i));
            head.setTextSize(19);
            head.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            words.addView(head);
            words.addView(SettingsUI.detail(this, notes[i - 1]));
            entry.setContentDescription(groupName(i) + ". " + notes[i - 1]);
            entry.addView(words, SettingsUI.grow());
            entry.addView(SettingsUI.away(this));
            entry.setMinimumHeight(dp(80));
            Motion.touch(entry);
            entry.setOnClickListener(v -> {
                if (navigating) return;
                navigating = true;
                Motion.navigate(column, 1f, () -> {
                    settingsGroup = chosen;
                    page.scrollTo(0, 0);
                    rebuild();
                    navigating = false;
                });
            });
            list.addView(entry);
        }
        return SettingsUI.wrap(this, list);
    }

    public int groupOf(String section) {
        if (section.equals(Text.FUN_SCREAMERS) || section.equals(Text.FUN_CAT) || section.equals(Text.DIAGNOSTICS) || section.equals(Text.DIARY)) return 8;
        if (section.equals(Text.PLUGINS)) return 6;
        if (section.equals(Text.SHOWCASE) || section.equals(Text.PERKS) || section.equals(Text.ACCOUNT) || section.equals(Local.of("Badge server"))) return 2;
        if (section.equals(Text.APPEARANCE) || section.equals(Text.ACCENT) || section.equals(Text.LIQUID_GLASS) || section.equals(Text.THEME)
                || section.equals(Text.FONT)) return 1;
        if (section.equals(Text.REGION) || section.equals(Text.COUNTRY) || section.equals(Text.FEED) || section.equals(Text.VIDEO)
                || section.equals(Text.RAIL) || section.equals(Text.COMMENTS) || section.equals(Text.TAGS) || section.equals(Text.FPS)
                || section.equals(Text.TIKTOK_FEATURES) || section.equals(BURN) || section.equals(Text.BLOCK_SITES)) return 3;
        if (section.equals(Text.PINNED_FRIENDS) || section.equals(Text.STREAKS) || section.equals(Text.NOTIFICATIONS) || section.equals(Text.SUGGESTED_ACCOUNTS)) return 7;
        if (section.equals(Text.THANKS) || section.equals(Text.CHANNEL) || section.equals(Text.LINKS)) return 5;
        return 4;
    }

    public static final String BURN = Local.of("Screen protection");
    private boolean navigating;
    private void goBack() {
        if (navigating) return;
        if (searchQuery.length() > 0) {
            searchQuery = "";
            rebuild();
        } else if (settingsGroup != 0) {
            final int to = settingsGroup == FLAGS_PAGE ? 8 : 0;
            navigating = true;
            Motion.navigate(column, - 1f, () -> {
                settingsGroup = to;
                rebuild();
                navigating = false;
            });
        } else finish();
    }

    @Override public void onBackPressed() {
        goBack();
    }

    @Override protected void onDestroy() {
        Badges.stopTelling(badgeUpdates);
        if (column != null) {
            column.removeCallbacks(delayedRebuild);
            column.animate().withEndAction(null).cancel();
        }
        if (page != null) {
            if (scrollRestore != null) page.removeCallbacks(scrollRestore);
            if (scrollRestoreListener != null && page.getViewTreeObserver().isAlive()) page.getViewTreeObserver().removeOnPreDrawListener(scrollRestoreListener);
        }
        super.onDestroy();
    }

    public int accentInk() {
        return skin.dark() ? Expressive.mix(Accent.colour(), 0xFFFFFFFF, 0.45f) : Expressive.mix(Accent.colour(), 0xFF000000,
                0.35f);
    }

    public int accentTonal() {
        return Expressive.mix(skin.card, Accent.colour(), 0.24f);
    }

    private View restartBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        styleRestartBar(bar);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(20), dp(10), dp(12), dp(10));
        RestartSymbol symbol = new RestartSymbol(this, Accent.colour());
        LinearLayout.LayoutParams symbolSize = new LinearLayout.LayoutParams(dp(24), dp(24));
        symbolSize.rightMargin = dp(12);
        content.addView(symbol, symbolSize);
        TextView why = new TextView(this);
        why.setText(Text.RESTART_PENDING);
        why.setTextColor(skin.text);
        why.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        content.addView(why, SettingsUI.grow());
        TextView button = new TextView(this);
        button.setText(Text.RESTART);
        button.setTextColor(onAccent());
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setIncludeFontPadding(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(16), 0, dp(16), 0);
        button.setMinWidth(dp(132));
        GradientDrawable pill = new GradientDrawable();
        pill.setColor(Accent.colour());
        pill.setCornerRadius(dp(24));
        button.setMinHeight(dp(48));
        button.setFocusable(true);
        button.setBackground(pill);
        button.setOnClickListener(v -> restartTikTok());
        LinearLayout.LayoutParams actionSize = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        actionSize.leftMargin = dp(12);
        content.addView(button, actionSize);
        why.setIncludeFontPadding(false);
        why.setMaxLines(2);
        why.setGravity(Gravity.CENTER_VERTICAL);
        restartSymbol = symbol;
        restartWhy = why;
        restartAction = button;
        restartBackground = pill;
        Fonts.applyTree(content);
        Motion.press(button);
        bar.addView(content);
        return bar;
    }

    public int onAccent() {
        int colour = Accent.colour();
        int red = (colour >> 16) & 0xFF, green = (colour >> 8) & 0xFF, blue = colour & 0xFF;
        int brightness = (red * 299 + green * 587 + blue * 114) / 1000;
        return brightness > 150 ? 0xFF1C2C24 : 0xFFFFFFFF;
    }

    private int navigationBar() {
        try {
            android.view.WindowInsets insets = getWindow().getDecorView().getRootWindowInsets();
            if (insets == null) return 0;
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                return insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom;
            }
            return insets.getStableInsetBottom();
        } catch (Throwable ignored) {} return 0;
    }

    private int statusBar() {
        try {
            android.view.WindowInsets insets = getWindow().getDecorView().getRootWindowInsets();
            int top = 0;
            if (insets != null) {
                top = android.os.Build.VERSION.SDK_INT >= 30 ? insets.getInsets(android.view.WindowInsets.Type.statusBars())
                        .top : insets.getStableInsetTop();
            }
            if (top > 0) return top;
        } catch (Throwable ignored) {} int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24);
    }

    public int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    public interface ColourChanged {
        void changed(int colour);
    }
}

package bk.soufit.ui.menus;
import bk.soufit.ui.SettingsUI;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import bk.soufit.core.Flags;
import bk.soufit.core.Local;
import bk.soufit.core.Text;
import bk.soufit.feed.Brightness;
import bk.soufit.feed.Comments;
import bk.soufit.feed.Dates;
import bk.soufit.feed.Dim;
import bk.soufit.feed.Feed;
import bk.soufit.feed.FeedBlacklist;
import bk.soufit.feed.FeedRail;
import bk.soufit.feed.LoginGate;
import bk.soufit.feed.RegionLabel;
import bk.soufit.media.Download;
import bk.soufit.profile.Account;
import bk.soufit.profile.Badges;
import bk.soufit.theme.Accent;
import bk.soufit.theme.Md3;
import bk.soufit.theme.NavBar;
import bk.soufit.theme.Themes;
import bk.soufit.ui.Expressive;
import bk.soufit.ui.Motion;
import bk.soufit.ui.Panel;
import bk.soufit.ui.Screen;
import bk.soufit.ui.SettingsActivity;
import java.util.Locale;
public final class SearchMenu {
    public static View searchBox(SettingsActivity host) {
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(host.dp(20), host.dp(4), host.dp(8), host.dp(4));
        GradientDrawable background = new GradientDrawable();
        background.setColor(host.skin.card);
        background.setCornerRadius(host.dp(28));
        row.setBackground(background);
        ImageView glass = new ImageView(host);
        glass.setImageDrawable(new Expressive.Magnifier(host.skin.muted()));
        LinearLayout.LayoutParams glassSize = new LinearLayout.LayoutParams(host.dp(24), host.dp(24));
        glassSize.rightMargin = host.dp(12);
        row.addView(glass, glassSize);
        final EditText input = new EditText(host);
        input.setSingleLine(true);
        input.setText(host.searchQuery);
        input.setSelectAllOnFocus(false);
        input.setHint(Text.SEARCH_HINT);
        input.setHintTextColor(host.skin.muted());
        input.setTextColor(host.skin.text);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setOnEditorActionListener((view, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                host.applySearch(input.getText().toString());
                return true;
            }
            return false;
        });
        input.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {} @Override public void onTextChanged(CharSequence text,
                    int start, int before, int count) {
                input.removeCallbacks(host.liveSearch);
                input.postDelayed(host.liveSearch, SettingsActivity.SEARCH_DELAY);
            }
            @Override public void afterTextChanged(android.text.Editable text) {}
        });
        host.searchField = input;
        row.addView(input, SettingsUI.grow());
        LinearLayout box = new LinearLayout(host);
        box.setPadding(host.dp(12), host.dp(4), host.dp(12), host.dp(8));
        box.addView(SettingsUI.sized(host, row, 56));
        return box;
    }

    public static void searchResults(SettingsActivity host) {
        host.searchHolder = new LinearLayout(host);
        host.searchHolder.setOrientation(LinearLayout.VERTICAL);
        host.column.addView(host.searchHolder);
        fillSearch(host, host.searchHolder);
        if (host.refocusSearch) {
            host.refocusSearch = false;
            host.focusSearchField();
        }
    }

    public static void fillSearch(SettingsActivity host, LinearLayout into) {
        into.addView(SettingsUI.section(host, Text.SEARCH_RESULTS));
        LinearLayout results = SettingsUI.card(host);
        int found = 0;
        found += searchControl(host, results, Text.FEED, Text.HIDE_ADS, Feed.isEnabled());
        found += searchControl(host, results, Text.FEED, Text.HIDE_LIVE, Feed.hides(Feed.KEY_LIVE));
        found += searchControl(host, results, Text.FEED, Text.HIDE_PHOTOS, Feed.hides(Feed.KEY_PHOTOS));
        found += searchControl(host, results, Text.FEED, Text.HIDE_STORIES, Feed.hides(Feed.KEY_STORIES));
        found += searchControl(host, results, Text.SUGGESTED_ACCOUNTS, Text.HIDE_SUGGESTED_ACCOUNTS, Feed.hides(Feed.KEY_SUGGESTIONS));
        found += searchControl(host, results, Text.FEED, Text.SHOW_REGION_LANG, RegionLabel.isEnabled());
        found += searchControl(host, results, Text.LIQUID_GLASS, Text.FULL_AMOLED, Themes.isAmoled());
        found += searchControl(host, results, Text.FEED, Text.SKIP_LOGIN_GATE, LoginGate.isEnabled());
        found += searchControl(host, results, Text.FEED, Text.SKIP_LOGIN_DOB, LoginGate.isDobEnabled());
        found += searchHit(host, results, Text.PINNED_FRIENDS, Text.PINNED_FRIENDS + " " + Text.MESSAGE_TIMES, () -> {});
        found += searchHit(host, results, Text.NOTIFICATIONS, Text.NOTIFY_MESSAGES + " " + Text.NOTIFY_PROMOS + " " + Text.NOTIFY_OTHER
                + " " + Text.NOTIFY_NO_PROMPTS + " " + Text.NOTIFY_SETTINGS, () -> {});
        found += searchHit(host, results, Text.VIDEO, Text.PLAYER_SPEED, () -> {});
        found += searchHit(host, results, Text.DOWNLOADS, Text.DL_QUALITY + " " + Text.FRAME_QUALITY, () -> {});
        String[] railNames = {Text.RAIL_LIKE, Text.RAIL_COMMENT, Text.RAIL_FAV, Text.RAIL_SHARE};
        for (int i = 0; i<4; i ++) found += searchControl(host, results, Text.RAIL, railNames[i], FeedRail.shown(i));
        if (matchesSearch(host, Text.HIDDEN_ITEMS)) {
            results.addView(SettingsUI.actionRow(host, "visibility_off", Text.HIDDEN_ITEMS, Text.HIDDEN_ITEMS_NOTE, (() -> showHiddenItems(host))));
            found ++;
        }
        found += searchControl(host, results, Text.DOWNLOADS, Text.NO_WATERMARK, Download.isEnabled());
        found += searchControl(host, results, Text.VIDEO, Text.ALWAYS_DATE, Dates.isEnabled());
        found += searchControl(host, results, SettingsActivity.BURN, Text.DIM, Dim.isConfigured());
        found += searchControl(host, results, Text.COMMENTS, Text.COMMENT_COPY_ON, Comments.isCopyEnabled());
        found += searchControl(host, results, Text.VIDEO, Text.OWN_BRIGHTNESS, Brightness.isEnabled());
        found += searchControl(host, results, Text.PERKS, Text.BADGES_ON, Badges.isEnabled());
        found += searchControl(host, results, Text.TIKTOK_FEATURES, Text.COMMENT_REPOST, Flags.isOn(Flags.KEY_COMMENT_REPOST));
        found += searchControl(host, results, Text.TIKTOK_FEATURES, Text.SELF_REPOST, Flags.isOn(Flags.KEY_SELF_REPOST));
        found += searchControl(host, results, Text.TIKTOK_FEATURES, Text.TRANSLATE_COMMENTS, Flags.isOn(Flags.KEY_TRANSLATE_COMMENTS));
        found += searchControl(host, results, Text.APPEARANCE, Text.NEW_PROFILE, Flags.isOn(Flags.KEY_NEW_PROFILE));
        found += searchHit(host, results, Text.LIQUID_GLASS, Text.LIQUID_GLASS, () -> {
            host.glassOpen = true;
        });
        found += searchControl(host, results, Text.APPEARANCE, Text.AUTO_HIDE_BAR, NavBar.isAutoHide());
        found += searchControl(host, results, Text.APPEARANCE, Text.MD3_ICONS, Md3.isOn(Md3.KEY_ICONS));
        found += searchHit(host, results, Text.APPEARANCE, Text.AVATAR_SHAPE, () -> {});
        found += searchHit(host, results, Text.FPS, Text.FPS, () -> {});
        found += searchHit(host, results, Text.TAGS, Text.TAGS + " " + Text.TAGS_ADD, () -> {});
        found += searchHit(host, results, Text.PERKS, Text.GRADIENT, () -> {});
        found += searchHit(host, results, Text.SHOWCASE, Text.SHOWCASE + " " + Text.SHOWCASE_BADGES + " " + Text.SHOWCASE_GRADIENTS,
                () -> {});
        if (found == 0) {
            found += searchHit(host, results, Text.REGION, Text.SEARCH_REGION, () -> {
                host.countriesOpen = true;
            });
            found += searchHit(host, results, Text.ACCENT, Text.SEARCH_ACCENT, () -> {
                host.accentOpen = true;
            });
            found += searchHit(host, results, Text.THEME, Text.SEARCH_THEME, () -> {
                host.themeColoursOpen = true;
            });
            found += searchHit(host, results, Text.FEED, Text.SEARCH_FEED, () -> {});
            found += searchHit(host, results, Text.VIDEO, Text.SEARCH_VIDEO, () -> {});
            found += searchHit(host, results, Text.COMMENTS, Text.SEARCH_COMMENTS, () -> {});
            found += searchHit(host, results, Text.DOWNLOADS, Text.SEARCH_DOWNLOADS, () -> {});
            found += searchHit(host, results, Text.BACKUP, Text.SEARCH_BACKUP, () -> {});
            found += searchHit(host, results, Text.FONT, Text.SEARCH_FONT, () -> {
                host.fontOpen = true;
            });
            found += searchHit(host, results, Text.PERKS, Text.SEARCH_BADGES, () -> {
                host.mineOpen = true;
            });
        }
        if (found == 0) results.addView(SettingsUI.caption(host, Text.SEARCH_NONE));
        into.addView(SettingsUI.wrap(host, results));
    }

    public static int searchControl(SettingsActivity host, LinearLayout into, String section, String title, boolean on) {
        if (!matchesSearch(host, title)) return 0;
        if (into.getChildCount()>0) into.addView(SettingsUI.line(host));
        into.addView(SettingsUI.actionRow(host, "search", title, on ? Local.of("On") : Local.of("Off"), () -> {
            host.searchQuery = "";
            host.settingsGroup = host.groupOf(section);
            host.rebuild();
            host.page.post(() -> {
                View target = host.settingTargets.get(title);
                if (target == null) return;
                int offset = 0;
                View cursor = target;
                while (cursor != host.column) {
                    offset += cursor.getTop();
                    if (!(cursor.getParent() instanceof View)) break;
                    cursor = (View) cursor.getParent();
                }
                host.page.smoothScrollTo(0, Math.max(0, offset - host.dp(16)));
                Motion.change(target);
            });
        }));
        return 1;
    }

    public static int searchHit(SettingsActivity host, LinearLayout into, String title, String words, final Runnable open) {
        if (!matchesSearch(host, title + " " + words)) return 0;
        if (into.getChildCount() > 0) into.addView(SettingsUI.line(host));
        into.addView(SettingsUI.actionRow(host, "search", title, currentValue(host, title), () -> {
            open.run();
            host.searchQuery = "";
            host.settingsGroup = host.groupOf(title);
            host.page.scrollTo(0, 0);
            host.rebuild();
            final View target = host.sectionTargets.get(title);
            if (target != null) host.page.post(() -> host.page.smoothScrollTo(0, target.getTop()));
        }));
        return 1;
    }

    public static String currentValue(SettingsActivity host, String title) {
        if (title.equals(Text.ACCENT)) return String.format(Locale.US, "#%06X", Accent.colour() & 0xFFFFFF);
        if (title.equals(Text.THEME)) return Themes.isEnabled() ? Local.of("On") : Local.of("Off");
        if (title.equals(Text.FEED)) return Feed.isEnabled() ? Local.of("Ads hidden") : Local.of("Ads allowed");
        if (title.equals(Text.DOWNLOADS)) return Download.isEnabled() ? Local.of("No watermark") : Local.of("Regular save");
        if (title.equals(Text.PROFILE_STYLE)) return Account.profileHandle() == null ? Local.of("Open your profile") : "@"
                + Account.profileHandle();
        return Local.of("Open settings");
    }

    public static boolean matchesSearch(SettingsActivity host, String words) {
        return words.toLowerCase(Locale.ROOT).contains(host.searchQuery);
    }

    public static void showHiddenItems(SettingsActivity host) {
        Panel panel = Panel.with(host, host.skin, Text.HIDDEN_ITEMS);
        LinearLayout rows = new LinearLayout(host);
        rows.setOrientation(LinearLayout.VERTICAL);
        String[] keys = {Feed.KEY, Feed.KEY_LIVE, Feed.KEY_PHOTOS, Feed.KEY_STORIES, Feed.KEY_SUGGESTIONS};
        String[] labels = {Text.HIDE_ADS, Text.HIDE_LIVE, Text.HIDE_PHOTOS, Text.HIDE_STORIES, Text.HIDE_SUGGESTIONS};
        int count = 0;
        for (int i = 0; i<keys.length; i ++) {
            final String key = keys[i];
            if (!(key.equals(Feed.KEY) ? Feed.isEnabled() : Feed.hides(key))) continue;
            count ++;
            rows.addView(SettingsUI.actionRow(host, "visibility_off", labels[i], Text.HIDDEN_RESTORE, () -> {
                if (key.equals(Feed.KEY)) Feed.setEnabled(false);
                else Feed.setHides(key, false);
                panel.close();
                host.rebuild();
                Screen.say(Text.HIDDEN_NEXT_PAGE);
            }));
        }
        String[] railNames = {Text.RAIL_LIKE, Text.RAIL_COMMENT, Text.RAIL_FAV, Text.RAIL_SHARE};
        for (int i = 0; i<4; i ++) if (!FeedRail.shown(i)) {
            final int kind = i;
            count ++;
            rows.addView(SettingsUI.actionRow(host, "visibility_off", railNames[i], Text.HIDDEN_RESTORE, () -> {
                FeedRail.setShown(kind, true);
                panel.close();
                host.rebuild();
            }));
        }
        for (java.util.Map.Entry<Integer, String> card : FeedBlacklist.cards().entrySet()) {
            final int type = card.getKey();
            count ++;
            rows.addView(SettingsUI.actionRow(host, "group", card.getValue(), Text.HIDDEN_RESTORE, () -> {
                FeedBlacklist.removeCard(type);
                panel.close();
                host.rebuild();
            }));
        }
        for (java.util.Map.Entry<String, String> author : FeedBlacklist.items().entrySet()) {
            final String uid = author.getKey();
            count ++;
            rows.addView(SettingsUI.actionRow(host, "group", author.getValue(), Text.HIDDEN_RESTORE, () -> {
                FeedBlacklist.remove(uid);
                panel.close();
                host.rebuild();
            }));
        }
        if (count == 0) rows.addView(SettingsUI.caption(host, Text.HIDDEN_EMPTY));
        else {
            rows.addView(SettingsUI.caption(host, Text.HIDDEN_NEXT_PAGE));
            panel.primary(Text.HIDDEN_RESTORE_ALL, () -> {
                Feed.setEnabled(false);
                Feed.setHides(Feed.KEY_LIVE, false);
                Feed.setHides(Feed.KEY_PHOTOS, false);
                Feed.setHides(Feed.KEY_STORIES, false);
                Feed.setHides(Feed.KEY_SUGGESTIONS, false);
                FeedBlacklist.clear();
                for (int i = 0; i<4; i ++) FeedRail.setShown(i, true);
                host.rebuild();
            });
        }
        panel.view(rows);
        panel.quiet(Text.CLOSE, null);
        panel.show();
    }
}

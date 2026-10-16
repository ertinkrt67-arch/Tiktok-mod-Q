package bk.soufit.ui.menus;
import bk.soufit.ui.SettingsUI;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import bk.soufit.core.Flags;
import bk.soufit.core.Local;
import bk.soufit.core.Sites;
import bk.soufit.core.Text;
import bk.soufit.core.soufiT;
import bk.soufit.feed.Brightness;
import bk.soufit.feed.Comments;
import bk.soufit.feed.Dates;
import bk.soufit.feed.Dim;
import bk.soufit.feed.Feed;
import bk.soufit.feed.FeedRail;
import bk.soufit.feed.LoginGate;
import bk.soufit.feed.PlayerTools;
import bk.soufit.feed.Rate;
import bk.soufit.feed.RegionLabel;
import bk.soufit.feed.Seekbar;
import bk.soufit.feed.Sound;
import bk.soufit.feed.Tags;
import bk.soufit.ui.Popup;
import bk.soufit.ui.SettingsActivity;
import java.util.List;
public final class FeedVideoMenu {
    public static void build(SettingsActivity host, LinearLayout column) {
        column.addView(SettingsUI.section(host, Text.REGION));
        LinearLayout head = SettingsUI.card(host);
        head.addView(host.switchRow());
        column.addView(SettingsUI.wrap(host, head));
        column.addView(SettingsUI.section(host, Text.COUNTRY));
        LinearLayout countries = SettingsUI.card(host);
        countries.addView(host.countryHead());
        if (host.countriesOpen) {
            countries.addView(SettingsUI.line(host));
            countries.addView(host.countrySearch());
            host.countryRows.clear();
            host.countryViews.clear();
            for (String[] country : soufiT.COUNTRIES) {
                View entry = SettingsUI.appear(host, host.countryRow(country));
                host.countryRows.add(country);
                host.countryViews.add(entry);
                countries.addView(entry);
            }
        }
        column.addView(SettingsUI.wrap(host, countries));
        column.addView(SettingsUI.section(host, Text.FEED));
        LinearLayout feed = SettingsUI.card(host);
        feed.addView(SettingsUI.toggleRow(host, "block", Text.HIDE_ADS, Feed.isEnabled(), Feed::setEnabled));
        feed.addView(SettingsUI.line(host));
        feed.addView(SettingsUI.toggleRow(host, "visibility_off", Text.HIDE_LIVE, Feed.hides(Feed.KEY_LIVE), on -> Feed.setHides(Feed.KEY_LIVE,
                on)));
        feed.addView(SettingsUI.line(host));
        feed.addView(SettingsUI.toggleRow(host, "image", Text.HIDE_PHOTOS, Feed.hides(Feed.KEY_PHOTOS), on -> Feed.setHides(Feed.KEY_PHOTOS,
                on)));
        feed.addView(SettingsUI.line(host));
        feed.addView(SettingsUI.liveToggleRow(host, "visibility_off", Text.HIDE_STORIES, Feed.hides(Feed.KEY_STORIES), on -> Feed.setHides(Feed.KEY_STORIES,
                on)));
        feed.addView(SettingsUI.liveToggleRow(host, "info", Text.SHOW_REGION_LANG, RegionLabel.isEnabled(), RegionLabel::setEnabled));
        feed.addView(SettingsUI.line(host));
        feed.addView(SettingsUI.toggleRow(host, "fingerprint", Text.SKIP_LOGIN_GATE, LoginGate.isEnabled(), LoginGate::setEnabled));
        feed.addView(SettingsUI.toggleRow(host, "cake", Text.SKIP_LOGIN_DOB, LoginGate.isDobEnabled(), LoginGate::setDobEnabled));
        column.addView(SettingsUI.wrap(host, feed));
        column.addView(SettingsUI.section(host, Text.BLOCK_SITES));
        LinearLayout sites = SettingsUI.card(host);
        sites.addView(SettingsUI.toggleRow(host, "link", Text.BLOCK_YZEWE, Sites.isOn(), on -> {
            Sites.setOn(on);
            host.markChanged();
        }));
        sites.addView(SettingsUI.quiet(host, Text.BLOCK_YZEWE_NOTE));
        column.addView(SettingsUI.wrap(host, sites));
        column.addView(SettingsUI.section(host, Text.TAGS));
        LinearLayout tags = SettingsUI.card(host);
        tags.addView(SettingsUI.betaRow(host, "tag", Text.TAGS, Tags.isEnabled(), Tags::setEnabled));
        java.util.List<String> blocked = Tags.all();
        if (blocked.isEmpty()) {
            tags.addView(SettingsUI.caption(host, Text.TAGS_NONE));
        } else {
            for (final String tag : blocked) {
                tags.addView(SettingsUI.line(host));
                tags.addView(SettingsUI.actionRow(host, "block", "#" + tag, null, () -> {
                    Tags.remove(tag);
                    host.rebuild();
                }));
            }
        }
        tags.addView(SettingsUI.line(host));
        tags.addView(SettingsUI.actionRow(host, "tag", Text.TAGS_ADD, null, () -> Popup.write(host, Text.TAGS_ADD, "", Text.TAGS_ADD,
                said -> {
            if (!Tags.add(said)) {
                Toast.makeText(host, Text.TAGS_FULL, Toast.LENGTH_SHORT).show();
            }
            host.rebuild();
        })));
        tags.addView(SettingsUI.caption(host, Text.TAGS_NOTE));
        column.addView(SettingsUI.wrap(host, tags));
        column.addView(SettingsUI.section(host, Text.RAIL));
        LinearLayout rail = SettingsUI.card(host);
        String[] railNames = {Text.RAIL_LIKE, Text.RAIL_COMMENT, Text.RAIL_FAV, Text.RAIL_SHARE};
        String[] railIcons = {"favorite_border", "chat", "star", "share"};
        for (int i = 0; i<railNames.length; i ++) {
            final int kind = i;
            rail.addView(SettingsUI.liveToggleRow(host, railIcons[i], railNames[i], FeedRail.shown(i), on -> FeedRail.setShown(kind,
                    on)));
        }
        column.addView(SettingsUI.wrap(host, rail));
        column.addView(SettingsUI.section(host, Text.VIDEO));
        LinearLayout video = SettingsUI.card(host);
        video.addView(SettingsUI.actionRow(host, "speed", Text.PLAYER_SPEED, Float.toString(PlayerTools.speed()) + "×", () -> PlayerTools.choose(host,
                host::rebuild)));
        video.addView(SettingsUI.toggleRow(host, "lock", Text.SPEED_LOCK, Flags.isOn(Flags.KEY_SPEED_LOCK), on -> Flags.set(Flags.KEY_SPEED_LOCK,
                on)));
        video.addView(SettingsUI.toggleRow(host, "volume_up", Text.SOUND, Sound.isEnabled(), Sound::setEnabled));
        video.addView(SettingsUI.line(host));
        video.addView(SettingsUI.liveToggleRow(host, "timeline", Text.SEEKBAR, Seekbar.isEnabled(), Seekbar::setEnabled));
        video.addView(SettingsUI.line(host));
        video.addView(SettingsUI.liveToggleRow(host, "info", Text.ALWAYS_DATE, Dates.isEnabled(), Dates::setEnabled));
        video.addView(SettingsUI.line(host));
        video.addView(SettingsUI.toggleRow(host, "hdr_off", Text.OWN_BRIGHTNESS, Brightness.isEnabled(), on -> {
            Brightness.setEnabled(on);
            host.markChanged();
        }));
        video.addView(SettingsUI.caption(host, Text.OWN_BRIGHTNESS_NOTE));
        column.addView(SettingsUI.wrap(host, video));
        column.addView(SettingsUI.section(host, Text.FPS));
        LinearLayout fps = SettingsUI.card(host);
        fps.addView(SettingsUI.quiet(host, Text.FPS_ABOUT));
        String rate = Rate.name();
        for (int i = 0; i < Rate.CHOICES.length; i ++) {
            final String which = Rate.CHOICES[i];
            if (i > 0) fps.addView(SettingsUI.line(host));
            fps.addView(SettingsUI.pickRow(host, SettingsActivity.fpsName(which), which.equals(rate), null, () -> {
                Rate.choose(which);
                host.markChanged();
            }));
        }
        if (!Rate.reachable(host)) fps.addView(SettingsUI.quiet(host, Text.FPS_UNSUPPORTED));
        column.addView(SettingsUI.wrap(host, fps));
        column.addView(SettingsUI.section(host, Text.COMMENTS));
        LinearLayout comments = SettingsUI.card(host);
        comments.addView(SettingsUI.liveToggleRow(host, "article", Text.COMMENT_COPY_ON, Comments.isCopyEnabled(), Comments::setCopyEnabled));
        column.addView(SettingsUI.wrap(host, comments));
        column.addView(SettingsUI.section(host, Text.TIKTOK_FEATURES));
        LinearLayout features = SettingsUI.card(host);
        features.addView(SettingsUI.quiet(host, Text.TIKTOK_FEATURES_NOTE));
        features.addView(SettingsUI.toggleRow(host, "repeat", Text.COMMENT_REPOST, Flags.isOn(Flags.KEY_COMMENT_REPOST), on -> Flags.set(Flags.KEY_COMMENT_REPOST,
                on)));
        features.addView(SettingsUI.line(host));
        features.addView(SettingsUI.toggleRow(host, "repeat", Text.SELF_REPOST, Flags.isOn(Flags.KEY_SELF_REPOST), on -> Flags.set(Flags.KEY_SELF_REPOST,
                on)));
        features.addView(SettingsUI.line(host));
        features.addView(SettingsUI.toggleRow(host, "language", Text.TRANSLATE_COMMENTS, Flags.isOn(Flags.KEY_TRANSLATE_COMMENTS),
                on -> Flags.set(Flags.KEY_TRANSLATE_COMMENTS, on)));
        column.addView(SettingsUI.wrap(host, features));
        column.addView(SettingsUI.section(host, SettingsActivity.BURN));
        LinearLayout burn = SettingsUI.card(host);
        burn.addView(SettingsUI.liveToggleRow(host, "eye_open", Text.DIM, Dim.isConfigured(), on -> {
            Dim.setEnabled(on);
            host.rebuildAfterSwitchAnimation();
        }));
        if (Dim.isConfigured()) {
            burn.addView(SettingsUI.line(host));
            burn.addView(SettingsUI.slider(host, "contrast", Text.DIM_HOW, Dim.strength(), 90, 0, "%", value -> Dim.setStrength(value)));
        }
        burn.addView(SettingsUI.quiet(host, Local.of("The eye in the feed and on videos turns the dimming on and off on the fly. The comment input is not dimmed. When dimmed, the bottom bar hides and comes back for a few seconds if you tap where it was.")));
        column.addView(SettingsUI.wrap(host, burn));
    }
}

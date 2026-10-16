package bk.soufit.theme;
import bk.soufit.profile.AccountAppearance;
import bk.soufit.core.Diary;
import bk.soufit.core.Probe;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.widget.TextView;
import java.io.File;
public final class Fonts {
    private Fonts() {} public static final String KEY = "font";
    public static final String KEY_EMOJI = "font_emoji";
    public static final String TWEMOJI = "twemoji";
    public static final String NOTO = "noto";
    public static final String BLOBMOJI = "blobmoji";
    public static final String EMOJI_FILE = "emoji_file";
    public static final String TWEMOJI_ASSET = "soufit/fonts/twemoji.ttf";
    private static String packAsset(Context context, String which) {
        return EmojiPackManager.asset(context, which);
    }

    public static final String SYSTEM = "";
    public static final String SANS = "sans-serif";
    public static final String SANS_LIGHT = "sans-serif-light";
    public static final String SANS_CONDENSED = "sans-serif-condensed";
    public static final String SERIF = "serif";
    public static final String MONOSPACE = "monospace";
    public static final String CURSIVE = "cursive";
    public static final String FILE = "file";
    public static final String[] EMOJI_PACKS = {SYSTEM, TWEMOJI, NOTO, BLOBMOJI};
    public static final String[] PRESETS = {SYSTEM, SANS, SANS_LIGHT, SANS_CONDENSED, SERIF, MONOSPACE, CURSIVE,};
    private static volatile String chosen;
    private static volatile String chosenEmoji;
    private static volatile Typeface face;
    private static volatile boolean looked;
    public static String name() {
        String known = chosen;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        if (prefs == null) return SYSTEM;
        String value = prefs.getString(KEY, SYSTEM);
        chosen = value;
        return value;
    }

    public static void choose(String value) {
        if (value == null) value = SYSTEM;
        chosen = value;
        forget();
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putString(KEY, value).apply();
        new android.os.Handler(android.os.Looper.getMainLooper()).post(Themes::repaintVisible);
    }

    public static String emoji() {
        String known = chosenEmoji;
        if (known != null) return known;
        SharedPreferences prefs = prefs();
        if (prefs == null) return SYSTEM;
        String value = prefs.getString(KEY_EMOJI, SYSTEM);
        chosenEmoji = value;
        return value;
    }

    public static void chooseEmoji(Context context, String value) {
        if (value == null) value = SYSTEM;
        if (context == null) context = soufiT.context();
        if (EmojiPackManager.find(context, value) == null) value = SYSTEM;
        chosenEmoji = value;
        forget();
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().putString(KEY_EMOJI, value).apply();
    }

    private static void forget() {
        face = null;
        looked = false;
        styleBase = null;
        styled = new Typeface[4];
    }

    public static void reload() {
        chosen = null;
        chosenEmoji = null;
        forget();
    }

    public static boolean emojiReady(Context context) {
        return EmojiPackManager.ready(context, emoji());
    }

    public static Typeface twemoji(Context context) {
        Typeface known = bundled;
        if (known != null) return known;
        try {
            String asset = packAsset(context, TWEMOJI);
            if (asset == null) return null;
            known = Typeface.createFromAsset(context.getAssets(), asset);
            bundled = known;
            return known;
        } catch (Throwable error) {
            Diary.note("twemoji: " + error);
            return null;
        }
    }

    private static volatile Typeface bundled;
    public static File file(Context context) {
        return AccountAppearance.file(context, "font");
    }

    public static boolean take(Context context, android.net.Uri uri) {
        return take(context, uri, false);
    }

    public static boolean take(Context context, android.net.Uri uri, boolean forEmoji) {
        final SharedPreferences destination = AccountAppearance.prefs(context);
        final String owner = AccountAppearance.scope(context);
        File temporary = null;
        try {
            File out = forEmoji ? emojiFile(context, EMOJI_FILE) : file(context);
            File parent = out.getParentFile();
            if (parent != null) parent.mkdirs();
            temporary = new File(parent, out.getName() + ".tmp");
            java.io.InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) return false;
            java.io.FileOutputStream sink = new java.io.FileOutputStream(temporary);
            try {
                byte[] buffer = new byte[16384];
                int read;
                long total = 0;
                while ((read = in.read(buffer)) > 0) {
                    total += read;
                    if (total > 24L * 1024 * 1024) return false;
                    sink.write(buffer, 0, read);
                }
            } finally {
                sink.close();
                in.close();
            }
            Typeface test = Typeface.createFromFile(temporary);
            if (test == null) return false;
            if (!temporary.renameTo(out)) return false;
            destination.edit().putString(forEmoji ? KEY_EMOJI : KEY, forEmoji ? EMOJI_FILE : FILE).apply();
            if (owner.equals(AccountAppearance.scope(context))) reload();
            return true;
        } catch (Throwable error) {
            Diary.note("font: " + error);
            return false;
        } finally {
            if (temporary != null && temporary.isFile()) temporary.delete();
        }
    }

    public static File emojiFile(Context context, String which) {
        if (EMOJI_FILE.equals(which)) return AccountAppearance.file(context, "emoji-custom");
        return new File(context.getFilesDir(), "soufit/emoji-" + which);
    }

    public static Typeface chosenFace() {
        if (looked) return face;
        return lookedUp();
    }

    private static synchronized Typeface lookedUp() {
        if (looked) return face;
        face = build();
        looked = true;
        return face;
    }

    private static Typeface build() {
        Context context = soufiT.context();
        return build(context, name(), emoji());
    }

    public static Typeface preview(Context context, String emojiPack) {
        return build(context, name(), emojiPack);
    }

    private static Typeface build(Context context, String letters, String emoji) {
        Typeface plain = null;
        try {
            if (FILE.equals(letters)) {
                File font = context == null ? null : file(context);
                if (font != null && font.isFile()) plain = Typeface.createFromFile(font);
            } else if (!SYSTEM.equals(letters)) {
                File font = systemFont(letters);
                if (font != null) plain = Typeface.createFromFile(font);
                if (plain == null) plain = Typeface.create(letters, Typeface.NORMAL);
            }
        } catch (Throwable error) {
            Diary.note("font: " + error);
        }
        if (context == null || SYSTEM.equals(emoji) || android.os.Build.VERSION.SDK_INT < 29) {
            if (context == null) looked = false;
            return plain;
        }
        String asset = packAsset(context, emoji);
        if (asset != null) return hybrid(context, letters, null, asset);
        File pack = emojiFile(context, emoji);
        if (!pack.isFile()) return plain;
        return hybrid(context, letters, pack, null);
    }

    private static Typeface hybrid(Context context, String letters, File pack, String asset) {
        try {
            File base = FILE.equals(letters) ? file(context) : systemFont(letters);
            if (base == null || !base.isFile()) return null;
            android.graphics.fonts.FontFamily letterFamily = new android.graphics.fonts.FontFamily.Builder(new android.graphics.fonts.Font.Builder(base)
                    .build()).build();
            android.graphics.fonts.Font.Builder emojiFont = asset != null ? new android.graphics.fonts.Font.Builder(context.getAssets(),
                    asset) : new android.graphics.fonts.Font.Builder(pack);
            android.graphics.fonts.FontFamily emojiFamily = new android.graphics.fonts.FontFamily.Builder(emojiFont.build())
                    .build();
            return new Typeface.CustomFallbackBuilder(letterFamily).addCustomFallback(emojiFamily).build();
        } catch (Throwable error) {
            Diary.note("emoji: " + error);
            return null;
        }
    }

    private static File systemFont(String family) {
        for (String path : filesFor(family)) {
            File file = new File(path);
            if (file.isFile()) return file;
        }
        for (String path : filesFor(SANS)) {
            File file = new File(path);
            if (file.isFile()) return file;
        }
        return null;
    }

    private static String[] filesFor(String family) {
        if (MONOSPACE.equals(family)) {
            return new String[] {"/system/fonts/DroidSansMono.ttf", "/system/fonts/RobotoMono-Regular.ttf", "/system/fonts/CutiveMono.ttf",
                    };
        }
        if (SERIF.equals(family)) {
            return new String[] {"/system/fonts/NotoSerif-Regular.ttf", "/system/fonts/Tinos-Regular.ttf", "/system/fonts/DroidSerif-Regular.ttf",
                    };
        }
        if (CURSIVE.equals(family)) {
            return new String[] {"/system/fonts/DancingScript-Regular.ttf", "/system/fonts/NotoSerif-Italic.ttf", "/system/fonts/Roboto-Italic.ttf",
                    };
        }
        if (SANS_LIGHT.equals(family)) {
            return new String[] {"/system/fonts/Roboto-Light.ttf", "/system/fonts/NotoSans-Light.ttf",};
        }
        if (SANS_CONDENSED.equals(family)) {
            return new String[] {"/system/fonts/RobotoCondensed-Regular.ttf", "/system/fonts/NotoSansCondensed-Regular.ttf",
                    };
        }
        return new String[] {"/system/fonts/Roboto-Regular.ttf", "/system/fonts/NotoSans-Regular.ttf", "/system/fonts/DroidSans.ttf",
                };
    }

    private static final class OwnedFont {
        public Typeface original, applied;
        public OwnedFont(Typeface original) {
            this.original = original;
        }
    }

    private static final java.util.Map<TextView, OwnedFont> ownedFonts = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<TextView,
            OwnedFont>());
    private static Typeface styleBase;
    private static Typeface[] styled = new Typeface[4];
    public static void applyTree(android.view.View root) {
        if (chosenFace() == null && ownedFonts.isEmpty()) return;
        long probe = Probe.start();
        applyTree(root, 0, new int[] {4000});
        Probe.stop(Probe.FONTS, probe);
    }

    private static final java.util.concurrent.ConcurrentHashMap<Class<?>, Boolean> glyphs = new java.util.concurrent.ConcurrentHashMap<Class<
            ?>, Boolean>();
    private static boolean drawsGlyphs(android.view.View view) {
        Class<?> type = view.getClass();
        Boolean known = glyphs.get(type);
        if (known == null) {
            String kind = type.getName().toLowerCase(java.util.Locale.ROOT);
            known = Boolean.valueOf(kind.contains("icon") || kind.contains("emoji"));
            glyphs.put(type, known);
        }
        return known.booleanValue();
    }

    private static void applyTree(android.view.View root, int depth, int[] budget) {
        if (root == null || depth>40 || -- budget[0]<0) return;
        if (root instanceof TextView) apply((TextView) root);
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) root;
            for (int i = 0; i<group.getChildCount() && budget[0]>0; i ++) applyTree(group.getChildAt(i), depth + 1, budget);
        }
    }

    public static void apply(TextView view) {
        if (view == null) return;
        Typeface wanted = chosenFace();
        if (wanted == null && ownedFonts.isEmpty()) return;
        if (drawsGlyphs(view)) return;
        try {
            Typeface had = view.getTypeface();
            OwnedFont mine = ownedFonts.get(view);
            if (wanted == null) {
                if (mine != null) {
                    if (had == mine.applied) view.setTypeface(mine.original);
                    ownedFonts.remove(view);
                }
                return;
            }
            if (mine == null) {
                mine = new OwnedFont(had);
                ownedFonts.put(view, mine);
            } else if (had != mine.applied) mine.original = had;
            Typeface out = keep(wanted, mine.original);
            if (out != had) view.setTypeface(out);
            mine.applied = out;
        } catch (Throwable ignored) {}
    }

    public static void setText(TextView view, int resource) {
        view.setText(resource);
        apply(view);
    }

    public static void setHint(TextView view, int resource) {
        view.setHint(resource);
        apply(view);
    }

    public static void setHint(TextView view, CharSequence text) {
        view.setHint(text);
        apply(view);
    }

    public static void setTextAppearance(TextView view, int resource) {
        view.setTextAppearance(resource);
        apply(view);
    }

    public static void setTextAppearance(TextView view, Context context, int resource) {
        view.setTextAppearance(context, resource);
        apply(view);
    }

    public static void setTypeface(TextView view, Typeface face) {
        Typeface wanted = chosenFace();
        if (wanted == null && ownedFonts.isEmpty()) {
            view.setTypeface(face);
            return;
        }
        boolean replace = wanted != null && !drawsGlyphs(view);
        view.setTypeface(replace ? keep(wanted, face) : face);
        if (replace) {
            OwnedFont mine = new OwnedFont(face);
            mine.applied = view.getTypeface();
            ownedFonts.put(view, mine);
        } else ownedFonts.remove(view);
    }

    public static void setTypeface(TextView view, Typeface face, int style) {
        Typeface wanted = chosenFace();
        if (wanted == null && ownedFonts.isEmpty()) {
            view.setTypeface(face, style);
            return;
        }
        boolean replace = wanted != null && !drawsGlyphs(view);
        view.setTypeface(replace ? wanted : face, style);
        if (replace) {
            OwnedFont mine = new OwnedFont(Typeface.create(face, style));
            mine.applied = view.getTypeface();
            ownedFonts.put(view, mine);
        } else ownedFonts.remove(view);
    }

    public static Typeface setTypeface(android.graphics.Paint paint, Typeface face) {
        Typeface wanted = chosenFace();
        return paint.setTypeface(wanted == null ? face : keep(wanted, face));
    }

    private static Typeface keep(Typeface wanted, Typeface theirs) {
        try {
            int style = theirs == null ? Typeface.NORMAL : theirs.getStyle();
            if (style == Typeface.NORMAL) return wanted;
            if (style<0 || style >= 4) return Typeface.create(wanted, style);
            synchronized (Fonts.class) {
                if (styleBase != wanted) {
                    styleBase = wanted;
                    styled = new Typeface[4];
                }
                if (styled[style] == null) styled[style] = Typeface.create(wanted, style);
                return styled[style];
            }
        } catch (Throwable ignored) {
            return wanted;
        }
    }

    private static SharedPreferences prefs() {
        Context context = soufiT.context();
        if (context == null) return null;
        return AccountAppearance.prefs(context);
    }
}

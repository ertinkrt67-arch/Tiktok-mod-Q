package bk.soufit.feed;
import bk.soufit.core.Anchors;
import bk.soufit.core.Diary;
import bk.soufit.theme.Fonts;
import bk.soufit.core.Names;
import bk.soufit.core.NativeRead;
import bk.soufit.ui.Panel;
import bk.soufit.ui.Screen;
import bk.soufit.theme.Skin;
import bk.soufit.media.Stickers;
import bk.soufit.core.Text;
import bk.soufit.core.soufiT;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ss.android.ugc.aweme.base.model.UrlModel;
import com.ss.android.ugc.aweme.comment.model.CommentImageStruct;
import com.ss.android.ugc.aweme.comment.model.CommentStickerStruct;
import com.ss.android.ugc.aweme.im.common.model.StickerItem;
import kotlin.jvm.functions.Function0;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public final class Comments {
    public static void reloadSettings() {
        copyEnabled = null;
    }

    private Comments() {} public static void mark(View cell) {
        if (cell != null) cell.setTag(0x5454434D, Boolean.TRUE);
    }

    public static View findViewById(View cell, int id) {
        mark(cell);
        View result = cell.findViewById(id);
        if (result != null) result.setTag(0x5454434D, Boolean.TRUE);
        return result;
    }

    public static final String KEY_COPY = "comment_copy";
    private static volatile Boolean copyEnabled;
    public static boolean isCopyEnabled() {
        Boolean known = copyEnabled;
        if (known != null) return known.booleanValue();
        try {
            Context context = soufiT.context();
            if (context == null) return true;
            boolean on = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).getBoolean(KEY_COPY, true);
            copyEnabled = Boolean.valueOf(on);
            return on;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void setCopyEnabled(boolean enabled) {
        copyEnabled = Boolean.valueOf(enabled);
        try {
            Context context = soufiT.context();
            if (context != null) context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_COPY,
                    enabled).apply();
        } catch (Throwable ignored) {}
    }

    public static void stickerTapped(View view, CommentStickerStruct sticker, boolean flag, String from, Map extras, String where) {
        try {
            if (sticker != null && Stickers.isEnabled()) Stickers.sheet(sticker);
        } catch (Throwable error) {
            Diary.note("comment sticker: " + error);
        }
        onwards(view, sticker, flag, from, extras, where);
    }

    private static void onwards(Object ... args) {
        try {
            Class<?> owner = Class.forName(Anchors.COMMENT_STICKER_TAPPED);
            for (java.lang.reflect.Method method : owner.getDeclaredMethods()) {
                if (!method.getName().equals(Anchors.COMMENT_STICKER_TAPPED_METHOD)) continue;
                if (method.getParameterTypes().length != args.length) continue;
                method.setAccessible(true);
                method.invoke(null, args);
                return;
            }
            Diary.note("comment sticker tap: nothing to hand it back to");
        } catch (Throwable error) {
            Diary.note("comment sticker tap: " + error);
        }
    }

    public static void stickerSheet(Object helper, String where, StickerItem sticker, View view, boolean saved, String from,
            Map extras, Function0 onShare, Function0 onSave, Function0 onUse) {
        try {
            java.lang.reflect.Method their = null;
            for (java.lang.reflect.Method method : helper.getClass().getMethods()) {
                if (method.getName().equals(Anchors.COMMENT_STICKER_SHEET_METHOD) && method.getParameterTypes().length == 9) {
                    their = method;
                    break;
                }
            }
            if (their != null) {
                their.setAccessible(true);
                their.invoke(helper, where, sticker, view, Boolean.valueOf(saved), from, extras, onShare, onSave, onUse);
            } else {
                Diary.note("comment sticker sheet: nothing to hand it back to");
            }
        } catch (Throwable error) {
            Diary.note("comment sticker sheet: " + error);
        }
        try {
            if (sticker != null && Stickers.isEnabled()) Stickers.sheet(sticker);
        } catch (Throwable error) {
            Diary.note("comment sticker: " + error);
        }
    }

    public static CommentStickerStruct getStickerStruct(Object comment) {
        CommentStickerStruct sticker = null;
        try {
            java.lang.reflect.Method method = comment.getClass().getMethod("getStickerStruct");
            method.setAccessible(true);
            sticker = (CommentStickerStruct) method.invoke(comment);
        } catch (Throwable error) {
            Diary.note("comment sticker: " + error);
            return null;
        }
        if (sticker != null) lastBound = sticker;
        return sticker;
    }

    private static volatile CommentStickerStruct lastBound;
    public static void setOnLongClickListener(View view, final View.OnLongClickListener theirs) {
        if (view == null) return;
        view.setTag(0x5454434D, Boolean.TRUE);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View pressed) {
                String comment = isCopyEnabled() ? CommentText.from(theirs) : null;
                if (isCopyEnabled() && comment == null) comment = visibleComment(pressed);
                if (!TextUtils.isEmpty(comment) && showCommentActions(pressed, comment, theirs)) return true;
                try {
                    if (Stickers.isEnabled()) {
                        CommentStickerStruct sticker = hunt(theirs, new HashSet<Object>(), 0);
                        if (sticker == null) sticker = lastBound;
                        if (sticker != null) Stickers.seen(sticker);
                    }
                } catch (Throwable error) {
                    Diary.note("comment sticker: " + error);
                }
                return theirs != null && theirs.onLongClick(pressed);
            }
        });
    }

    private static boolean showCommentActions(final View pressed, final String comment, final View.OnLongClickListener theirs) {
        return showCommentActions(pressed.getContext(), comment, () -> nativeLongPress(theirs, pressed));
    }

    private static final ThreadLocal<Boolean> nativeMenu = new ThreadLocal<Boolean>();
    public static boolean openNativeActions(Object cell) {
        return false;
    }

    public static void bindCell(Object cell) {
        try {
            View root = (View) cell.getClass().getField("itemView").get(cell);
            if (root == null) return;
            mark(root);
            scanForFeelingsBar(root);
            if (!isCopyEnabled()) return;
            String body = CommentText.from(cell);
            if (TextUtils.isEmpty(body)) return;
            View.OnLongClickListener listener = v -> {
                if (openNativeActions(cell)) return true;
                nativeMenu.set(Boolean.TRUE);
                try {
                    cell.getClass().getMethod("k8").invoke(cell);
                    return true;
                } catch (Throwable ignored) {
                    return false;
                } finally {
                    nativeMenu.remove();
                }
            };
            root.setOnLongClickListener(listener);
            bindBody(root, body, listener, 0, new int[] {128});
        } catch (Throwable error) {
            Diary.note("comment body actions: " + error);
        }
    }

    private static void scanForFeelingsBar(View cell) {
        try {
            android.view.ViewParent parent = cell.getParent();
            ViewGroup recycler = null;
            for (int depth = 0; depth < 8 && parent instanceof ViewGroup; depth ++) {
                String name = parent.getClass().getName();
                if (name.contains("RecyclerView") || name.contains("ListView")) {
                    recycler = (ViewGroup) parent;
                    break;
                }
                parent = ((ViewGroup) parent).getParent();
            }
            if (recycler == null) return;
            String rName = recycler.getClass().getName().toLowerCase(java.util.Locale.ROOT);
            android.view.ViewParent rParent = recycler.getParent();
            String rpName = rParent == null ? "" : rParent.getClass().getName().toLowerCase(java.util.Locale.ROOT);
            if (!rName.contains("comment") && !rpName.contains("comment")) return;
            hideFeelingsBar(recycler);
        } catch (Throwable ignored) {}
    }

    private static void bindBody(View view, String body, View.OnLongClickListener listener, int depth, int[] remaining) {
        if (view == null || depth>12 || remaining[0] -- <= 0) return;
        if (view instanceof TextView && ((TextView) view).getText() != null && body.contentEquals(((TextView) view).getText())) view.setOnLongClickListener(listener);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i<group.getChildCount(); i ++) bindBody(group.getChildAt(i), body, listener, depth + 1, remaining);
        }
    }

    private static boolean showCommentActions(Context context, String comment, Runnable nativeAction) {
        return false;
    }

    public static List nativeItems(Object fragment, List items) {
        if (!isCopyEnabled() || items == null) return items;
        try {
            Object comment = NativeRead.field(fragment, "LLJJL");
            String body = NativeRead.string(NativeRead.get(comment, "getText"));
            if (TextUtils.isEmpty(body)) return items;
            Context context = (Context) NativeRead.get(fragment, "getContext");
            if (context == null) return items;
            int after = items.size();
            Integer icon = null;
            for (int i = 0; i<items.size(); i ++) {
                Object item = items.get(i);
                String key = NativeRead.string(NativeRead.field(item, "LIZIZ"));
                if ("soufit_select_comment".equals(key)) return items;
                String label = NativeRead.string(NativeRead.field(item, "LIZLLL")).toLowerCase(java.util.Locale.ROOT);
                if (key.toLowerCase(java.util.Locale.ROOT).contains("copy") || label.contains("копир") || label.equals("copy")) {
                    after = i + 1;
                    Object art = NativeRead.field(item, "LIZJ");
                    if (art instanceof Integer) icon = (Integer) art;
                }
            }
            Class<?> type = Class.forName("X.0oi8");
            Object action = type.getConstructor().newInstance();
            type.getField("LIZIZ").set(action, "soufit_select_comment");
            type.getField("LIZLLL").set(action, Text.COMMENT_SELECT_COPY);
            type.getField("LIZJ").set(action, icon);
            type.getField("LJ").set(action, (Function0<Object>)() -> {
                try {
                    fragment.getClass().getMethod("dismissAllowingStateLoss").invoke(fragment);
                } catch (Throwable ignored) {} selectComment(context, body);
                return null;
            });
            ArrayList result = new ArrayList(items);
            result.add(after, action);
            return result;
        } catch (Throwable error) {
            Diary.note("native comment selection: " + error);
            return items;
        }
    }

    private static boolean nativeLongPress(View.OnLongClickListener theirs, View pressed) {
        nativeMenu.set(Boolean.TRUE);
        try {
            return theirs != null && theirs.onLongClick(pressed);
        } catch (Throwable error) {
            Diary.note("comment long press: " + error);
            return false;
        } finally {
            nativeMenu.remove();
        }
    }

    private static String visibleComment(View root) {
        if (root == null || root.getVisibility() != View.VISIBLE) return null;
        if (root instanceof TextView) {
            try {
                String id = Names.of(root);
                if (id.equals("comment_text") || id.equals("comment_content") || id.equals("comment_body")) {
                    CharSequence body = ((TextView) root).getText();
                    if (body != null && body.toString().trim().length()>0) return body.toString();
                }
            } catch (Throwable ignored) {}
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i<group.getChildCount(); i ++) {
                String body = visibleComment(group.getChildAt(i));
                if (body != null) return body;
            }
        }
        return null;
    }

    private static void copyComment(Context context, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) return;
            clipboard.setPrimaryClip(ClipData.newPlainText("TikTok comment", text));
            Screen.say(Text.COMMENT_COPIED);
        } catch (Throwable error) {
            Diary.note("comment copy: " + error);
        }
    }

    private static void selectComment(Context context, String text) {
        try {
            Skin skin = Skin.remembered(context);
            TextView body = new TextView(context);
            body.setText(text);
            body.setTextColor(skin.text);
            body.setTextSize(16);
            body.setTextIsSelectable(true);
            body.setFocusable(true);
            body.setFocusableInTouchMode(true);
            body.setLineSpacing(0, 1.12f);
            Fonts.apply(body);
            int pad = (int)(context.getResources().getDisplayMetrics().density * 8f + .5f);
            body.setPadding(pad, pad, pad, pad);
            Panel panel = Panel.with(context, skin, Text.COMMENT_SELECT);
            panel.view(body);
            panel.primaryKeepOpen(Text.COMMENT_COPY_SELECTED, () -> {
                String selected = CommentSelection.part(text, body.getSelectionStart(), body.getSelectionEnd());
                if (selected == null) {
                    Screen.say(Text.COMMENT_SELECT_HINT);
                    return;
                }
                copyComment(context, selected);
                panel.close();
            });
            panel.quiet(Text.CLOSE, null);
            panel.show();
        } catch (Throwable error) {
            Diary.note("comment selection: " + error);
        }
    }

    private static CommentStickerStruct hunt(Object thing, Set<Object> seen, int depth) {
        if (thing == null || depth > 3) return null;
        if (thing instanceof CommentStickerStruct) return (CommentStickerStruct) thing;
        Class<?> type = thing.getClass();
        String name = type.getName();
        if (name.startsWith("java.") || name.startsWith("android.")) return null;
        if (!seen.add(thing)) return null;
        while (type != null && type != Object.class) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                try {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    CommentStickerStruct found = hunt(field.get(thing), seen, depth + 1);
                    if (found != null) return found;
                } catch (Throwable ignored) {}
            }
            type = type.getSuperclass();
        }
        return null;
    }

    public static UrlModel getCropUrl(CommentImageStruct image) {
        return image == null ? null : image.getCropUrl();
    }

    public static UrlModel getOriginUrl(CommentImageStruct image) {
        return image == null ? null : image.getOriginUrl();
    }

    private static final String[] FEELINGS_SIGNALS = {"how do you feel", "how do you feel about", "feel about these comments",
            "feel about the comments", "what do you think of these comments", "\u0427\u0442\u043e \u0434\u0443\u043c\u0430\u0435\u0448\u044c",
            "\u0429\u043e \u0434\u0443\u043c\u0430\u0454\u0448", "que piensas de", "que pensez-vous", "was denkst du", "cosa ne pensi",
            "o que voc\u00ea acha",};
    public static void hideFeelingsBar(View root) {
        if (root == null) return;
        try {
            hideFeelingsBarInner(root);
        } catch (Throwable error) {
            Diary.note("feelings bar: " + error);
        }
    }

    private static void hideFeelingsBarInner(View view) {
        if (view == null || view.getVisibility() == View.GONE) return;
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null && text.length() > 4) {
                String lower = text.toString().toLowerCase(java.util.Locale.ROOT);
                for (String signal : FEELINGS_SIGNALS) {
                    if (lower.contains(signal)) {
                        hideContainerOf(view);
                        return;
                    }
                }
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i ++) {
                hideFeelingsBarInner(group.getChildAt(i));
            }
        }
    }

    private static void hideContainerOf(View view) {
        View candidate = view;
        android.view.ViewParent parent = view.getParent();
        while (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            String pName = group.getClass().getName();
            if (pName.contains("RecyclerView") || pName.contains("ListView")) {
                if (candidate.getVisibility() != View.GONE) {
                    candidate.setVisibility(View.GONE);
                    Diary.note("feelings bar: hid container " + candidate.getClass().getSimpleName());
                }
                return;
            }
            candidate = group;
            parent = group.getParent();
        }
        android.view.ViewParent direct = view.getParent();
        if (direct instanceof View) {
            View directView = (View) direct;
            if (directView.getVisibility() != View.GONE) {
                directView.setVisibility(View.GONE);
                Diary.note("feelings bar: hid fallback parent " + directView.getClass().getSimpleName());
            }
        }
    }
}

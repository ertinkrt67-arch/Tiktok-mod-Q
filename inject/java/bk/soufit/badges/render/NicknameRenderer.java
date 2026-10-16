package bk.soufit.badges.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import bk.soufit.badges.model.Badge;
import bk.soufit.core.Diary;
import bk.soufit.ui.Popup;

public final class NicknameRenderer {
    private static final int MINT = 0xFF8DD1B0;
    private static final int PAINTED = 0x4D61726C;
    private static final int LISTENING = 0x4D617268;
    private static final int WATCHED = 0x4D61726B;

    private NicknameRenderer() {}

    public static CharSequence render(TextView view, CharSequence text) {
        clearGradient(view);
        if (text == null || !hasMarks(text)) return text;
        try {
            SpannableStringBuilder out = new SpannableStringBuilder(text);
            boolean drew = false;
            int[] gradient = null;
            for (int i = out.length() - 1; i >= 0; i--) {
                char c = out.charAt(i);
                if (Marks.isGradientMark(c)) {
                    if (gradient == null) gradient = Marks.coloursAt(c);
                    out.delete(i, i + 1);
                    continue;
                }
                if (!Marks.isBadgeMark(c)) continue;
                Badge badge = Marks.badgeAt(c);
                Drawable picture = badge == null ? null : picture(view, badge);
                if (picture == null) {
                    out.delete(i, i + 1);
                    continue;
                }
                out.setSpan(new ImageSpan(picture, ImageSpan.ALIGN_BOTTOM), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                out.setSpan(new Tap(badge), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                drew = true;
            }
            if (gradient != null) paintGradient(view, gradient);
            if (!drew) return tidy(out.toString());
            listen(view);
            return out;
        } catch (Throwable error) {
            Diary.fail("nickname", error);
            return strip(text.toString());
        }
    }

    public static Drawable picture(TextView view, Badge badge) {
        Drawable drawable = drawable(view.getResources(), IconCache.request(view.getContext(), badge.image, null), badge);
        if (drawable == null) return null;
        int size = Math.round(view.getTextSize());
        if (size <= 0) size = Math.round(14 * view.getResources().getDisplayMetrics().density);
        int wide = drawable.getIntrinsicWidth();
        int tall = drawable.getIntrinsicHeight();
        int across = wide > 0 && tall > 0 ? Math.round(size * (float) wide / tall) : size;
        drawable.setBounds(0, 0, Math.max(1, across), size);
        return drawable;
    }

    public static Drawable popupIcon(Context context, Badge badge) {
        return drawable(context.getResources(), IconCache.request(context, badge.image, null), badge);
    }

    public static void open(View anchor, Badge badge) {
        String head = badge.title.isEmpty() ? "soufiT" : badge.title;
        Popup.show(anchor.getContext(), head, badge.text, badge.button, popupIcon(anchor.getContext(), badge));
    }

    public static void paintGradient(TextView view, int[] colours) {
        if (view == null || colours == null || colours.length < 2) return;
        try {
            float width = view.getPaint().measureText(view.getText().toString());
            if (width <= 0) width = view.getWidth();
            if (width <= 0) return;
            view.getPaint().setShader(new LinearGradient(0, 0, width, 0, colours, null, Shader.TileMode.CLAMP));
            view.setTag(PAINTED, Boolean.TRUE);
            view.invalidate();
        } catch (Throwable ignored) {}
    }

    public static void clearGradient(TextView view) {
        if (view == null) return;
        try {
            if (view.getTag(PAINTED) == null) return;
            view.getPaint().setShader(null);
            view.setTag(PAINTED, null);
        } catch (Throwable ignored) {}
    }

    public static String strip(String plain) {
        boolean any = false;
        for (int i = 0; i < plain.length() && !any; i++) {
            any = Marks.isPrivate(plain.charAt(i));
        }
        if (!any) return plain;
        StringBuilder out = new StringBuilder(plain.length());
        for (int i = 0; i < plain.length(); i++) {
            char c = plain.charAt(i);
            if (!Marks.isPrivate(c)) out.append(c);
        }
        return tidy(out.toString());
    }

    public static CharSequence typed(TextView view, CharSequence text) {
        watch(view);
        if (text == null) return null;
        String plain = text.toString();
        String cleaned = strip(plain);
        return cleaned.equals(plain) ? text : cleaned;
    }

    private static Drawable drawable(android.content.res.Resources resources, Bitmap bitmap, Badge badge) {
        Bitmap source = bitmap != null ? bitmap : IconCache.emblem();
        if (source == null) return null;
        BitmapDrawable drawable = new BitmapDrawable(resources, source);
        int tint = badge.colour != 0 ? badge.colour : (bitmap == null ? MINT : 0);
        if (tint != 0) drawable.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    private static boolean hasMarks(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Marks.isPrivate(c) && (Marks.isBadgeMark(c) || Marks.isGradientMark(c))) return true;
        }
        return false;
    }

    private static String tidy(String name) {
        if (name.endsWith("\u2009") || name.endsWith(" ")) return name.substring(0, name.length() - 1);
        return name;
    }

    private static void watch(TextView view) {
        if (Boolean.TRUE.equals(view.getTag(WATCHED))) return;
        try {
            view.setTag(WATCHED, Boolean.TRUE);
            view.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable text) {
                    for (int i = text.length() - 1; i >= 0; i--) {
                        if (Marks.isPrivate(text.charAt(i))) text.delete(i, i + 1);
                    }
                }
            });
        } catch (Throwable ignored) {}
    }

    private static void listen(TextView view) {
        if (Boolean.TRUE.equals(view.getTag(LISTENING))) return;
        try {
            view.setTag(LISTENING, Boolean.TRUE);
            view.setOnTouchListener(new Touch());
        } catch (Throwable ignored) {}
    }

    private static final class Tap extends ClickableSpan {
        private final Badge badge;

        Tap(Badge badge) {
            this.badge = badge;
        }

        @Override public void onClick(View widget) {
            open(widget, badge);
        }

        @Override public void updateDrawState(TextPaint paint) {}
    }

    private static final class Touch implements View.OnTouchListener {
        private boolean pressed;

        @Override public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                pressed = span(v, event) != null;
                return pressed;
            }
            if (!pressed) return false;
            if (action == MotionEvent.ACTION_UP) {
                pressed = false;
                ClickableSpan tapped = span(v, event);
                if (tapped != null) tapped.onClick(v);
                return true;
            }
            if (action == MotionEvent.ACTION_CANCEL) pressed = false;
            return true;
        }

        private ClickableSpan span(View v, MotionEvent event) {
            try {
                TextView view = (TextView) v;
                CharSequence text = view.getText();
                if (!(text instanceof Spanned)) return null;
                Layout layout = view.getLayout();
                if (layout == null) return null;
                int x = (int) event.getX() - view.getTotalPaddingLeft() + view.getScrollX();
                int y = (int) event.getY() - view.getTotalPaddingTop() + view.getScrollY();
                int line = layout.getLineForVertical(y);
                if (x < layout.getLineLeft(line) || x > layout.getLineRight(line)) return null;
                int at = layout.getOffsetForHorizontal(line, x);
                ClickableSpan[] found = ((Spanned) text).getSpans(at, at, ClickableSpan.class);
                return found.length == 0 ? null : found[0];
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}

package bk.soufit.ui;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import bk.soufit.core.DevMode;
import bk.soufit.core.Diary;
import bk.soufit.core.Flags;
import bk.soufit.core.Text;
import bk.soufit.theme.Accent;
public final class DevPill {
    public static final String KEY = "dev_pill";
    private static final int TAG = 0x7e0d0341;
    private static final int MAX_CHARS = 400000;
    private DevPill() {}
    public static boolean isOn() {
        return Flags.isOn(KEY);
    }

    public static void setOn(boolean on) {
        Flags.set(KEY, on);
        attach(Screen.now());
    }

    public static void attach(Activity activity) {
        if (activity == null) return;
        try {
            View decor = activity.getWindow().getDecorView();
            View old = (View) decor.getTag(TAG);
            if (!(isOn() && DevMode.isOn())) {
                if (old != null) {
                    if (old.getParent() instanceof ViewGroup) ((ViewGroup) old.getParent()).removeView(old);
                    decor.setTag(TAG, null);
                }
                return;
            }
            if (old != null || !(decor instanceof ViewGroup)) return;
            FrameLayout.LayoutParams where = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.BOTTOM);
            where.rightMargin = dp(activity, 16);
            where.bottomMargin = dp(activity, 120);
            View pill = build(activity);
            ((ViewGroup) decor).addView(pill, where);
            decor.setTag(TAG, pill);
        } catch (Throwable error) {
            Diary.fail("dev pill", error);
        }
    }

    private static View build(final Activity activity) {
        final TextView pill = new TextView(activity);
        int accent = Accent.colour();
        pill.setText(Text.DEV_PILL_LABEL);
        pill.setContentDescription(Text.DEV_PILL);
        pill.setTextColor(isLight(accent) ? 0xFF000000 : 0xFFFFFFFF);
        pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        pill.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        pill.setPadding(dp(activity, 16), dp(activity, 9), dp(activity, 16), dp(activity, 9));
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(accent);
        shape.setCornerRadius(dp(activity, 20));
        pill.setBackground(shape);
        pill.setClickable(true);
        final float slop = ViewConfiguration.get(activity).getScaledTouchSlop();
        pill.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY, baseX, baseY;
            private boolean moved;
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        baseX = view.getTranslationX();
                        baseY = view.getTranslationY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - startX, dy = event.getRawY() - startY;
                        if (!moved && Math.hypot(dx, dy) > slop) moved = true;
                        if (moved) {
                            view.setTranslationX(baseX + dx);
                            view.setTranslationY(baseY + dy);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) view.performClick();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                    default:
                        return false;
                }
            }
        });
        pill.setOnClickListener(v -> copyScreen(activity, pill));
        return pill;
    }

    private static void copyScreen(Activity activity, View pill) {
        try {
            String text = ScreenDump.capture(activity, pill);
            if (text.length() > MAX_CHARS) text = text.substring(0, MAX_CHARS) + "\n(cut to fit the clipboard)\n";
            ClipboardManager board = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (board == null) {
                Screen.say(Text.DEV_PILL_NO_CLIPBOARD);
                return;
            }
            board.setPrimaryClip(ClipData.newPlainText("soufiT dump", text));
            Screen.say(Text.DEV_PILL_COPIED);
        } catch (Throwable error) {
            Diary.fail("dev pill dump", error);
            Screen.say(Text.DEV_PILL_FAILED);
        }
    }

    private static boolean isLight(int colour) {
        double red = ((colour >> 16) & 0xFF) / 255.0, green = ((colour >> 8) & 0xFF) / 255.0, blue = (colour & 0xFF) / 255.0;
        return 0.299 * red + 0.587 * green + 0.114 * blue > 0.6;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}

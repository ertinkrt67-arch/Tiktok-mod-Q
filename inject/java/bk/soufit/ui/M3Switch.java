package bk.soufit.ui;
import bk.soufit.theme.Accent;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
public final class M3Switch extends View {
    public static final long ANIMATION_DURATION_MS = 300L;
    public interface OnChanged {
        void onChanged(boolean checked);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean lightSurface;
    private int trackOff, trackOn, thumbOff, thumbOn, outlineOff;
    {
        palette(false);
    }

    private void palette(boolean light) {
        lightSurface = light;
        if (light) {
            trackOff = 0xFFE4E4E7;
            outlineOff = 0xFF8A8A92;
            thumbOff = 0xFF6E6E76;
            trackOn = 0xFF111111;
            thumbOn = 0xFFFFFFFF;
        } else {
            trackOff = 0xFF181818;
            outlineOff = 0xFFBDBDBD;
            thumbOff = 0xFFFFFFFF;
            trackOn = 0xFFFFFFFF;
            thumbOn = 0xFF000000;
        }
    }

    private boolean checked;
    private float position;
    private OnChanged listener;
    private ValueAnimator animator;
    private boolean interruptingAnimation;
    public M3Switch(Context context) {
        super(context);
        setClickable(true);
        setFocusable(true);
    }

    public void colours(int accent, int off, int surface) {
        float luma = (0.299f * Color.red(surface) + 0.587f * Color.green(surface) + 0.114f * Color.blue(surface)) / 255f;
        boolean light = luma > 0.55f;
        if (light != lightSurface) palette(light);
        invalidate();
    }

    public void setChecked(boolean value) {
        setChecked(value, false);
    }

    public void setChecked(boolean value, boolean animate) {
        if (checked == value && (position == (value ? 1f : 0f))) return;
        if (animator != null) {
            interruptingAnimation = true;
            animator.cancel();
            animator = null;
            interruptingAnimation = false;
        }
        checked = value;
        if (!animate || !Motion.enabled(getContext())) {
            position = value ? 1f : 0f;
            invalidate();
            return;
        }
        animator = ValueAnimator.ofFloat(position, value ? 1f : 0f);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f));
        animator.addUpdateListener(a -> {
            position = (Float) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                if (!interruptingAnimation && animation == animator) {
                    position = checked ? 1f : 0f;
                    animator = null;
                    invalidate();
                }
            }
        });
        animator.start();
    }

    public boolean isChecked() {
        return checked;
    }

    public void setOnChanged(OnChanged listener) {
        this.listener = listener;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: setPressed(true);
            return true;
            case MotionEvent.ACTION_CANCEL: setPressed(false);
            return true;
            case MotionEvent.ACTION_UP: boolean inside = event.getX() >= 0 && event.getX() < getWidth() && event.getY() >= 0
                    && event.getY() < getHeight();
            setPressed(false);
            if (inside) performClick();
            return true;
            default: return true;
        }
    }

    @Override public boolean performClick() {
        if (!isEnabled()) return false;
        setChecked(!checked, true);
        if (listener != null) listener.onChanged(checked);
        super.performClick();
        return true;
    }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.CheckBox");
        info.setCheckable(true);
        info.setChecked(checked);
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredDimension(dp(28), dp(28));
    }

    private final android.graphics.Path tick = new android.graphics.Path();
    private final android.graphics.Path tickPart = new android.graphics.Path();
    private final android.graphics.PathMeasure measure = new android.graphics.PathMeasure();
    private final android.graphics.Path blob = new android.graphics.Path();
    private void cookie(float cx, float cy, float radius, float amount, float turn) {
        final int lobes = 8, steps = 128;
        float depth = 0.09f * amount;
        double start = Math.toRadians(turn) - Math.PI / 2.0;
        blob.reset();
        for (int i = 0; i < steps; i ++) {
            double t = Math.PI * 2.0 * i / steps;
            double reach = radius * (1.0 - depth * (1.0 - Math.cos(lobes * (t - start))));
            float x = (float)(cx + reach * Math.cos(t)), y = (float)(cy + reach * Math.sin(t));
            if (i == 0) blob.moveTo(x, y);
            else blob.lineTo(x, y);
        }
        blob.close();
    }

    @Override protected void onDraw(Canvas canvas) {
        float cx = getWidth() / 2f, cy = getHeight() / 2f, r = dp(12f);
        int accent = Accent.colour();
        int ink = (Color.red(accent) * 299 + Color.green(accent) * 587 + Color.blue(accent) * 114) / 1000 > 150 ? 0xFF111111
                : 0xFFFFFFFF;
        float eased = position * position * (3f - 2f * position);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2f));
        paint.setColor(withAlpha(outlineOff, Math.round(190 * (1f - eased))));
        canvas.drawCircle(cx, cy, r - dp(1f), paint);
        if (position > 0f) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withAlpha(accent, Math.round(255 * eased)));
            cookie(cx, cy, r * (0.82f + 0.18f * eased), eased, 45f * eased);
            canvas.drawPath(blob, paint);
            tick.reset();
            tick.moveTo(cx - dp(5.2f), cy + dp(0.6f));
            tick.lineTo(cx - dp(1.7f), cy + dp(4f));
            tick.lineTo(cx + dp(5.2f), cy - dp(3.4f));
            measure.setPath(tick, false);
            tickPart.reset();
            measure.getSegment(0f, measure.getLength() * Math.max(0f, Math.min(1f, (position - 0.25f) / 0.75f)), tickPart,
                    true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2.2f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(ink);
            canvas.drawPath(tickPart, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private static int withAlpha(int colour, int alpha) {
        return (colour & 0x00FFFFFF) | (Math.max(0, Math.min(255, alpha)) << 24);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}

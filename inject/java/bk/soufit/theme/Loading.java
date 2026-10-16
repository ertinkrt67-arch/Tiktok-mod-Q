package bk.soufit.theme;
import bk.soufit.core.Diary;
import bk.soufit.core.soufiT;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import java.lang.reflect.Method;
public final class Loading {
    private Loading() {} public static final String KEY = "loading_shapes";
    private static volatile Boolean cached;
    public static void reloadSettings() {
        cached = null;
    }

    public static boolean isEnabled() {
        Boolean known = cached;
        if (known != null) return known;
        try {
            android.content.Context context = soufiT.context();
            if (context == null) return true;
            boolean on = context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE).getBoolean(KEY,
                    true);
            cached = on;
            return on;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void setEnabled(boolean enabled) {
        cached = enabled;
        try {
            android.content.Context context = soufiT.context();
            if (context == null) return;
            context.getSharedPreferences(soufiT.PREFS, android.content.Context.MODE_PRIVATE).edit().putBoolean(KEY, enabled)
                    .apply();
        } catch (Throwable ignored) {}
    }

    private static boolean asking;
    private static boolean complained;
    public static boolean draw(View view, Canvas canvas) {
        if (asking) return false;
        try {
            if (view == null || canvas == null || !isEnabled()) return false;
            if (Looper.myLooper() != Looper.getMainLooper()) return false;
            int width = view.getWidth();
            int height = view.getHeight();
            if (width <= 0 || height <= 0) return false;
            if (!wouldDraw(view, width, height)) return true;
            shape(canvas, width, height);
            if (view.isShown()) view.postInvalidateOnAnimation();
            return true;
        } catch (Throwable error) {
            if (!complained) {
                complained = true;
                Diary.note("loading: " + error);
            }
            return false;
        }
    }

    private static final int PROBE = 24;
    private static Bitmap probe;
    private static Canvas probeCanvas;
    private static final int[] seen = new int[PROBE * PROBE];
    private static Method onDraw;
    private static boolean wouldDraw(View view, int width, int height) throws Exception {
        if (probe == null) {
            probe = Bitmap.createBitmap(PROBE, PROBE, Bitmap.Config.ARGB_8888);
            probeCanvas = new Canvas(probe);
            onDraw = View.class.getDeclaredMethod("onDraw", Canvas.class);
            onDraw.setAccessible(true);
        }
        probe.eraseColor(0);
        int saved = probeCanvas.save();
        probeCanvas.scale(PROBE / (float) width, PROBE / (float) height);
        asking = true;
        try {
            onDraw.invoke(view, probeCanvas);
        } finally {
            asking = false;
            probeCanvas.restoreToCount(saved);
        }
        probe.getPixels(seen, 0, PROBE, 0, 0, PROBE, PROBE);
        for (int pixel : seen) {
            if ((pixel >>> 24) > 24) return true;
        }
        return false;
    }

    private static final long STEP = 650;
    private static final int SHAPES = 7;
    private static final int POINTS = 96;
    private static final float TURN = 50f;
    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Path path = new Path();
    private static void shape(Canvas canvas, int width, int height) {
        long now = SystemClock.uptimeMillis();
        float along = (now % (STEP * SHAPES)) / (float) STEP;
        int from = (int) along;
        int to = (from + 1) % SHAPES;
        float eased = settle(along - from);
        long steps = now / STEP;
        float angle = (float) Math.toRadians(((steps % 360) + eased) * TURN);
        float centreX = width / 2f;
        float centreY = height / 2f;
        float size = Math.min(width, height) * 0.46f;
        path.rewind();
        for (int i = 0; i < POINTS; i ++) {
            double around = 2 * Math.PI * i / POINTS;
            float one = radius(from, around);
            float radius = (one + (radius(to, around) - one) * eased) * size;
            float x = centreX + radius * (float) Math.cos(around + angle);
            float y = centreY + radius * (float) Math.sin(around + angle);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Accent.colour() | 0xFF000000);
        canvas.drawPath(path, paint);
    }

    private static float settle(float t) {
        if (t <= 0f) return 0f;
        if (t >= 1f) return 1f;
        return 1f - (float)(Math.exp(- 6.0 * t) * Math.cos(8.0 * t));
    }

    private static float radius(int shape, double around) {
        switch (shape) {
            case 0: return 0.89f + 0.11f * (float) Math.cos(10 * around);
            case 1: return 0.91f + 0.09f * (float) Math.cos(9 * around);
            case 2: return sided(5, around);
            case 3: return oval(0.56f, around);
            case 4: return 0.86f + 0.14f * (float) Math.cos(8 * around);
            case 5: return 0.83f + 0.17f * (float) Math.cos(4 * around);
            default: return oval(0.74f, around);
        }
    }

    private static float sided(int sides, double around) {
        double slice = 2 * Math.PI / sides;
        double within = around % slice;
        if (within < 0) within += slice;
        double flat = Math.cos(Math.PI / sides) / Math.cos(within - slice / 2);
        double cap = 0.92;
        double sharp = 36.0;
        double soft = - Math.log(Math.exp(- sharp * flat) + Math.exp(- sharp * cap)) / sharp;
        return (float)(soft / cap);
    }

    private static float oval(float narrow, double around) {
        double x = narrow * Math.cos(around);
        double y = Math.sin(around);
        return (float)(narrow / Math.sqrt(x * x + y * y));
    }
}

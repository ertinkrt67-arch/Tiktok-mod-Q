package bk.soufit.theme;
import bk.soufit.core.Text;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
public final class AvatarShape {
    private AvatarShape() {} public static final String KEY = "avatar_shape";
    public static final String[] ALL = {"circle", "rounded", "square", "cookie", "clover", "burst", "sun", "pentagon", "hexagon",
            "heart"};
    private static final int POINTS = 144;
    private static volatile int chosen = - 1;
    private static final Path[] UNIT = new Path[ALL.length];
    private static final Matrix PLACE = new Matrix();
    public static String chosen() {
        return ALL[index()];
    }

    public static void choose(Context context, String shape) {
        int found = 0;
        for (int i = 0; i < ALL.length; i ++) if (ALL[i].equals(shape)) found = i;
        chosen = found;
        context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putString(KEY, ALL[found]).apply();
    }

    public static void reloadSettings() {
        chosen = - 1;
    }

    private static int index() {
        int known = chosen;
        if (known >= 0) return known;
        Context context = soufiT.context();
        if (context == null) return 0;
        int found = 0;
        try {
            SharedPreferences prefs = context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
            String name = prefs.getString(KEY, ALL[0]);
            for (int i = 0; i < ALL.length; i ++) if (ALL[i].equals(name)) found = i;
        } catch (Throwable ignored) {} chosen = found;
        return found;
    }

    public static void addCircle(Path path, float x, float y, float radius, Path.Direction direction) {
        int shape = index();
        if (shape > 0 && radius > 0) {
            try {
                Path unit = unit(shape);
                synchronized (PLACE) {
                    PLACE.setScale(radius, radius);
                    PLACE.postTranslate(x, y);
                    path.addPath(unit, PLACE);
                }
                return;
            } catch (Throwable error) {}
        }
        path.addCircle(x, y, radius, direction);
    }

    private static final Path RING = new Path();
    private static final Path PART = new Path();
    private static final Matrix RING_PLACE = new Matrix();
    public static void drawArc(Canvas canvas, RectF oval, float start, float sweep, boolean useCentre, Paint paint) {
        int shape = index();
        if (shape > 0 && !useCentre && oval != null && sweep != 0) {
            try {
                synchronized (RING) {
                    RING.rewind();
                    RING_PLACE.setScale(oval.width() / 2f, oval.height() / 2f);
                    RING_PLACE.postTranslate(oval.centerX(), oval.centerY());
                    RING.addPath(unit(shape), RING_PLACE);
                    if (Math.abs(sweep) >= 359.5f) {
                        canvas.drawPath(RING, paint);
                        return;
                    }
                    android.graphics.PathMeasure measure = new android.graphics.PathMeasure(RING, true);
                    float length = measure.getLength();
                    float from = start + (sweep < 0 ? sweep : 0) + 90f;
                    from = ((from % 360f) + 360f) % 360f / 360f * length;
                    float span = Math.min(Math.abs(sweep), 360f) / 360f * length;
                    PART.rewind();
                    if (from + span <= length) {
                        measure.getSegment(from, from + span, PART, true);
                    } else {
                        measure.getSegment(from, length, PART, true);
                        measure.getSegment(0, from + span - length, PART, true);
                    }
                    canvas.drawPath(PART, paint);
                    return;
                }
            } catch (Throwable error) {}
        }
        canvas.drawArc(oval, start, sweep, useCentre, paint);
    }

    public static Path chosenUnit() {
        return unit(index());
    }

    public static Path unit(int shape) {
        Path known = UNIT[shape];
        if (known != null) return known;
        Path made = new Path();
        switch (ALL[shape]) {
            case "rounded": made.addRoundRect(new RectF(- 1, - 1, 1, 1), 0.44f, 0.44f, Path.Direction.CW);
            break;
            case "square": made.addRect(- 1, - 1, 1, 1, Path.Direction.CW);
            break;
            case "heart": made.arcTo(new RectF(- 1f, - 1f, 0.1f, 0.1f), - 35.1f, - 174.9f, true);
            made.cubicTo(- 0.70f, 0.25f, - 0.28f, 0.70f, 0f, 1f);
            made.cubicTo(0.28f, 0.70f, 0.70f, 0.25f, 0.926f, - 0.175f);
            made.arcTo(new RectF(- 0.1f, - 1f, 1f, 0.1f), 30f, - 174.9f, false);
            made.close();
            break;
            case "circle": made.addCircle(0, 0, 1, Path.Direction.CW);
            break;
            default: for (int i = 0; i < POINTS; i ++) {
                double around = 2 * Math.PI * i / POINTS - Math.PI / 2;
                float reach = reach(ALL[shape], around + Math.PI / 2);
                float px = (float)(reach * Math.cos(around)), py = (float)(reach * Math.sin(around));
                if (i == 0) made.moveTo(px, py);
                else made.lineTo(px, py);
            }
            made.close();
        }
        UNIT[shape] = made;
        return made;
    }

    public static float reach(String shape, double around) {
        switch (shape) {
            case "cookie": return 0.92f + 0.08f * (float) Math.cos(9 * around);
            case "clover": return 0.84f + 0.16f * (float) Math.cos(4 * around + Math.PI);
            case "burst": return 0.90f + 0.10f * (float) Math.cos(12 * around);
            case "sun": return 0.87f + 0.13f * (float) Math.cos(8 * around);
            case "pentagon": return sided(5, around);
            case "hexagon": return sided(6, around);
            default: return 1f;
        }
    }

    private static float sided(int sides, double around) {
        double slice = 2 * Math.PI / sides;
        double within = around % slice;
        if (within < 0) within += slice;
        double flat = Math.cos(Math.PI / sides) / Math.cos(within - slice / 2);
        double cap = 0.93, sharp = 36.0;
        double soft = - Math.log(Math.exp(- sharp * flat) + Math.exp(- sharp * cap)) / sharp;
        return (float)(soft / cap);
    }

    public static Bitmap picture(String shape, int size, int colour) {
        int which = 0;
        for (int i = 0; i < ALL.length; i ++) if (ALL[i].equals(shape)) which = i;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colour);
        Path placed = new Path();
        Matrix place = new Matrix();
        place.setScale(size / 2f - 1, size / 2f - 1);
        place.postTranslate(size / 2f, size / 2f);
        placed.addPath(unit(which), place);
        canvas.drawPath(placed, paint);
        return bitmap;
    }

    public static String nameOf(String shape) {
        switch (shape) {
            case "rounded": return Text.SHAPE_ROUNDED;
            case "square": return Text.SHAPE_SQUARE;
            case "cookie": return Text.SHAPE_COOKIE;
            case "clover": return Text.SHAPE_CLOVER;
            case "burst": return Text.SHAPE_BURST;
            case "sun": return Text.SHAPE_SUN;
            case "pentagon": return Text.SHAPE_PENTAGON;
            case "hexagon": return Text.SHAPE_HEXAGON;
            case "heart": return Text.SHAPE_HEART;
            default: return Text.SHAPE_CIRCLE;
        }
    }
}

package bk.soufit.ui;
import bk.soufit.core.Text;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
public final class ChannelBanner extends View {
    public static final String URL = "https://t.me/margytiktok";
    private static final float W = 1107f, H = 300f;
    private final Paint ink = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path plane = new Path(), fold = new Path(), card = new Path();
    private final RectF box = new RectF(), pill = new RectF();
    private Shader background;
    private float backgroundWidth;
    public ChannelBanner(Context context) {
        super(context);
        setClickable(true);
        setFocusable(true);
        setContentDescription(Text.CHANNEL_TITLE + " MargyT. " + Text.CHANNEL_NEWS_A + Text.CHANNEL_NEWS_B + ". " + Text.CHANNEL_SUBSCRIBE);
        plane.moveTo(- 1f, 0.12f);
        plane.lineTo(1f, - 0.78f);
        plane.lineTo(0.32f, 0.86f);
        plane.lineTo(- 0.02f, 0.3f);
        plane.lineTo(- 0.26f, 0.5f);
        plane.lineTo(- 0.3f, 0.22f);
        plane.close();
        fold.moveTo(- 0.3f, 0.22f);
        fold.lineTo(0.78f, - 0.5f);
        fold.lineTo(- 0.02f, 0.3f);
        fold.lineTo(- 0.26f, 0.5f);
        fold.close();
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        if (width <= 0) width = Math.round(340 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(width, Math.round(width * H / W));
    }

    @Override protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), s = w / W;
        if (w <= 0) return;
        ink.setShader(null);
        ink.setStyle(Paint.Style.FILL);
        box.set(0, 0, w, H * s);
        card.reset();
        card.addRoundRect(box, 40 * s, 40 * s, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(card);
        if (background == null || backgroundWidth != w) {
            background = new LinearGradient(0, 0, w, H * s, 0xFF43C0F8, 0xFF2B82C8, Shader.TileMode.CLAMP);
            backgroundWidth = w;
        }
        ink.setShader(background);
        canvas.drawRect(box, ink);
        ink.setShader(null);
        paint(ink, argb(0x26, 0xFFFFFF));
        canvas.drawCircle(1000 * s, - 10 * s, 230 * s, ink);
        paint(ink, argb(0x33, 0xFFFFFF));
        canvas.drawCircle(158 * s, 150 * s, 78 * s, ink);
        canvas.save();
        canvas.translate(158 * s - 4 * s, 150 * s + 3 * s);
        canvas.scale(44 * s, 44 * s);
        paint(ink, 0xFFFFFFFF);
        canvas.drawPath(plane, ink);
        paint(ink, argb(0x77, 0xA9D7F7));
        canvas.drawPath(fold, ink);
        canvas.restore();
        canvas.restore();
        ink.setTextAlign(Paint.Align.LEFT);
        ink.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint(ink, argb(0xB8, 0xFFFFFF));
        ink.setTextSize(38 * s);
        canvas.drawText(Text.CHANNEL_TITLE, 268 * s, 96 * s, ink);
        ink.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        paint(ink, 0xFFFFFFFF);
        ink.setTextSize(68 * s);
        canvas.drawText("MargyT", 268 * s, 166 * s, ink);
        ink.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint(ink, argb(0xE6, 0xFFFFFF));
        ink.setTextSize(40 * s);
        canvas.drawText(Text.CHANNEL_NEWS_A + Text.CHANNEL_NEWS_B, 268 * s, 232 * s, ink);
        pill.set(790 * s, 104 * s, 1057 * s, 196 * s);
        paint(ink, isPressed() ? 0xEBEFF7FF : 0xFFFFFFFF);
        canvas.drawRoundRect(pill, 46 * s, 46 * s, ink);
        paint(ink, 0xFF2B82C8);
        ink.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        ink.setTextAlign(Paint.Align.CENTER);
        float size = 40 * s;
        ink.setTextSize(size);
        float room = pill.width() * 0.84f, need = ink.measureText(Text.CHANNEL_SUBSCRIBE);
        if (need > room) ink.setTextSize(size * room / need);
        Paint.FontMetrics m = ink.getFontMetrics();
        canvas.drawText(Text.CHANNEL_SUBSCRIBE, pill.centerX(), pill.centerY() - (m.ascent + m.descent) / 2f, ink);
        ink.setTextAlign(Paint.Align.LEFT);
    }

    private static void paint(Paint brush, int colour) {
        brush.setARGB((colour >>> 24) & 0xFF, (colour >> 16) & 0xFF, (colour >> 8) & 0xFF, colour & 0xFF);
    }

    private static int argb(int alpha, int rgb) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }
}

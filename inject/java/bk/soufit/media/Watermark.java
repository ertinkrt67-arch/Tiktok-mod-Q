package bk.soufit.media;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
public final class Watermark {
    private Watermark() {} public static void drawBitmap(Canvas canvas, Bitmap bitmap, float left, float top, Paint paint) {
        if (Download.isEnabled()) return;
        canvas.drawBitmap(bitmap, left, top, paint);
    }
}

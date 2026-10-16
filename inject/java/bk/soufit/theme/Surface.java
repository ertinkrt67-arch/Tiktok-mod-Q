package bk.soufit.theme;
import android.graphics.Canvas;
import android.graphics.RectF;
public interface Surface {
    void paint(Canvas canvas, RectF box, float round, float unit, int ground);
}

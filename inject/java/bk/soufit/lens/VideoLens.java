package bk.soufit.lens;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RenderEffect;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public final class VideoLens {
    static final long FRAME_GAP_MS = 33L;
    private static final int SLOTS = 3;

    private final View video;
    private final float left, top, width, height, radius;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean effectOn;
    private Snapshot overlay;
    private ViewTreeObserver.OnPreDrawListener follow;

    private VideoLens(View video, float left, float top, float width, float height, float radius) {
        this.video = video;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    public static VideoLens attach(View video, float left, float top, float width, float height, float radius) {
        if (Build.VERSION.SDK_INT < 33 || video == null || width <= 0f || height <= 0f) return null;
        VideoLens lens = new VideoLens(video, left, top, width, height, radius);
        boolean ok;
        if (video instanceof TextureView) ok = lens.attachEffect();
        else if (video instanceof SurfaceView && video.getParent() instanceof ViewGroup) ok = lens.attachOverlay();
        else ok = false;
        return ok ? lens : null;
    }

    private boolean attachEffect() {
        RuntimeShader shader = Lens.create();
        if (shader == null) return false;
        Lens.bind(shader, left, top, width, height, radius, 0f, 0f, true);
        video.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, Lens.VIDEO_CHILD));
        effectOn = true;
        return true;
    }

    private boolean attachOverlay() {
        RuntimeShader shader = Lens.create();
        if (shader == null) return false;
        ViewGroup host = (ViewGroup) video.getParent();
        overlay = new Snapshot(video.getContext(), shader);
        host.addView(overlay, host.indexOfChild(video) + 1,
                new ViewGroup.LayoutParams(Math.round(width), Math.round(height)));
        follow = () -> {
            place();
            return true;
        };
        video.getViewTreeObserver().addOnPreDrawListener(follow);
        place();
        overlay.start();
        return true;
    }

    private void place() {
        if (overlay == null) return;
        overlay.setX(video.getX() + left);
        overlay.setY(video.getY() + top);
        overlay.setVisibility(video.getVisibility());
    }

    public void stop() {
        if (overlay != null) overlay.stop();
    }

    public void start() {
        if (overlay != null) overlay.start();
    }

    public void detach() {
        if (effectOn) {
            video.setRenderEffect(null);
            effectOn = false;
        }
        if (overlay != null) {
            overlay.stop();
            video.getViewTreeObserver().removeOnPreDrawListener(follow);
            if (overlay.getParent() instanceof ViewGroup) ((ViewGroup) overlay.getParent()).removeView(overlay);
            overlay = null;
            follow = null;
        }
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }

    private final class Snapshot extends View {
        private final RuntimeShader shader;
        private final Paint paint = new Paint();
        private final Bitmap[] slots = new Bitmap[SLOTS];
        private final Runnable next = this::copy;
        private int shown = -1, previous = -1;
        private boolean hasFrame, pending, running;
        private float offsetX, offsetY;

        Snapshot(Context context, RuntimeShader shader) {
            super(context);
            this.shader = shader;
            setWillNotDraw(false);
        }

        void start() {
            if (running) return;
            running = true;
            handler.post(next);
        }

        void stop() {
            running = false;
            handler.removeCallbacks(next);
        }

        private void schedule() {
            if (running) handler.postDelayed(next, FRAME_GAP_MS);
        }

        private void copy() {
            if (!running || pending) return;
            int vw = video.getWidth(), vh = video.getHeight();
            if (!video.isShown() || vw == 0 || vh == 0) {
                schedule();
                return;
            }
            float pad = Lens.padding(VideoLens.this.width, VideoLens.this.height);
            int x0 = clamp(Math.round(VideoLens.this.left - pad), vw), y0 = clamp(Math.round(VideoLens.this.top - pad), vh);
            int x1 = clamp(Math.round(VideoLens.this.left + VideoLens.this.width + pad), vw);
            int y1 = clamp(Math.round(VideoLens.this.top + VideoLens.this.height + pad), vh);
            int w = x1 - x0, h = y1 - y0;
            if (w <= 0 || h <= 0) {
                schedule();
                return;
            }
            int slot = 0;
            while (slot == shown || slot == previous) slot++;
            Bitmap target = slots[slot];
            if (target == null || target.getWidth() != w || target.getHeight() != h) {
                if (target != null) target.recycle();
                target = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                slots[slot] = target;
            }
            final float ox = VideoLens.this.left - x0, oy = VideoLens.this.top - y0;
            final int chosen = slot;
            pending = true;
            PixelCopy.request((SurfaceView) video, new Rect(x0, y0, x1, y1), target, result -> {
                pending = false;
                if (!running) return;
                if (result == PixelCopy.SUCCESS) show(chosen, ox, oy);
                schedule();
            }, handler);
        }

        private void show(int slot, float ox, float oy) {
            previous = shown;
            shown = slot;
            shader.setInputShader(Lens.VIDEO_CHILD,
                    new BitmapShader(slots[slot], Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            offsetX = ox;
            offsetY = oy;
            hasFrame = true;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            if (!hasFrame) return;
            Lens.bind(shader, 0f, 0f, VideoLens.this.width, VideoLens.this.height, VideoLens.this.radius,
                    offsetX, offsetY, false);
            paint.setShader(shader);
            canvas.drawRect(0f, 0f, VideoLens.this.width, VideoLens.this.height, paint);
        }
    }
}

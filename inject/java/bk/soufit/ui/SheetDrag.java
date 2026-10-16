package bk.soufit.ui;
public final class SheetDrag {
    private float down, last, distance, velocity;
    private long time;
    public void start(float y, long now) {
        down = last = y;
        time = now;
        distance = velocity = 0;
    }

    public float move(float y, long now) {
        long dt = now - time;
        if (dt>0) velocity = (y - last) * 1000f / dt;
        last = y;
        time = now;
        distance = Math.max(0, y - down);
        return distance;
    }

    public boolean release(float height, float density) {
        return release(height, density, time);
    }

    public boolean release(float height, float density, long now) {
        return distance >= Math.max(64 * density, height * 0.28f) || (now - time<100 && distance>24 * density && velocity>900 * density);
    }
}

package bk.soufit.media;
public final class MediaQueue {
    private static final java.util.concurrent.ThreadPoolExecutor workers = new java.util.concurrent.ThreadPoolExecutor(2,
            2, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, new java.util.concurrent.ArrayBlockingQueue<Runnable>(32),
            r -> {
        Thread thread = new Thread(r, "soufit-media");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    public static boolean add(Runnable work) {
        try {
            workers.execute(work);
            return true;
        } catch (java.util.concurrent.RejectedExecutionException e) {
            return false;
        }
    }
}

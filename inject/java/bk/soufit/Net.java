package bk.soufit;
public final class Net {
    private Net() {} public static void away(String what, Runnable work) {
        bk.soufit.core.Net.away(what, work);
    }

    public static boolean download(String url, java.io.File into, bk.soufit.core.Net.Along along) {
        return bk.soufit.core.Net.download(url, into, along);
    }

    public static boolean download(String url, java.io.File into, bk.soufit.core.Net.Along along, bk.soufit.media.DownloadTasks.Control control) {
        return bk.soufit.core.Net.download(url, into, along, control);
    }

    public static byte[] bytes(String url) {
        return bk.soufit.core.Net.bytes(url);
    }
}

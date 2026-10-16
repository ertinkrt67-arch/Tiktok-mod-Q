package bk.soufit;
public final class Popup {
    private Popup() {} public static void show(android.content.Context context, String message) {
        bk.soufit.ui.Popup.show(context, message);
    }

    public static void show(android.content.Context context, String title, String message) {
        bk.soufit.ui.Popup.show(context, title, message);
    }
}

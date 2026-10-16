package bk.soufit.core;
public final class Sites {
    public static final String KEY = "block_yzewe";
    public static final String DOMAIN = "yzewe.ru";
    private Sites() {} public static boolean isOn() {
        return Flags.isOn(KEY);
    }

    public static void setOn(boolean on) {
        Flags.set(KEY, on);
    }

    public static boolean blocked(String address) {
        if (address == null || !isOn()) return false;
        String text = address.trim();
        if (!text.contains("://")) text = "https://" + text;
        try {
            String host = new java.net.URI(text).getHost();
            if (host == null) return false;
            host = host.toLowerCase(java.util.Locale.ROOT);
            return host.equals(DOMAIN) || host.endsWith("." + DOMAIN);
        } catch (Throwable invalid) {
            return false;
        }
    }
}

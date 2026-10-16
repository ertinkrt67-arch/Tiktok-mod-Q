package bk.soufit.plugins;
public final class PluginChecks {
    private PluginChecks() {} public static boolean id(String id) {
        return id != null && id.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,79}");
    }

    public static boolean digest(String digest) {
        return digest != null && digest.matches("[a-fA-F0-9]{64}");
    }
}

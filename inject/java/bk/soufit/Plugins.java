package bk.soufit;
public final class Plugins {
    private Plugins() {} public static void choose(android.app.Activity activity, String title, String[] options, cat.narezany.margyt.plugin.PluginContext.Selected selected) {
        bk.soufit.plugins.Plugins.choose(activity, title, options, selected);
    }

    public static void addRow(String plugin, String title, String detail, cat.narezany.margyt.plugin.PluginContext.Tapped action) {
        bk.soufit.plugins.Plugins.addRow(plugin, title, detail, action);
    }

    public static void addSection(String plugin, String title) {
        bk.soufit.plugins.Plugins.addSection(plugin, title);
    }

    public static void addToggle(String plugin, String title, String detail, String key, boolean def) {
        bk.soufit.plugins.Plugins.addToggle(plugin, title, detail, key, def);
    }

    public static void addChoice(String plugin, String title, String detail, String key, String[] options, int def) {
        bk.soufit.plugins.Plugins.addChoice(plugin, title, detail, key, options, def);
    }

    public static void addSlider(String plugin, String title, String detail, String key, int min, int max, int def) {
        bk.soufit.plugins.Plugins.addSlider(plugin, title, detail, key, min, max, def);
    }
}

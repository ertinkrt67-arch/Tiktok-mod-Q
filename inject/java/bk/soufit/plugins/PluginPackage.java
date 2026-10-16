package bk.soufit.plugins;
import bk.soufit.core.Local;
import java.io.*;
import java.util.*;
import java.util.zip.*;
public final class PluginPackage {
    private PluginPackage() {} public static void unpack(InputStream input, File folder) throws IOException {
        if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException(Local.of("Could not prepare the plugin"));
        Set<String> seen = new HashSet<String>();
        long total = 0;
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || !Arrays.asList("manifest.json", "classes.dex", "icon.png").contains(name) || !seen.add(name)) throw new IOException(Local.of("The plugin contains extra, nested or duplicate files"));
                int limit = name.equals("classes.dex") ? PluginAudit.MAX_DEX : name.equals("icon.png") ? 1024 * 1024 : 65536;
                File out = new File(folder, name);
                if (!out.getCanonicalPath().startsWith(folder.getCanonicalPath() + File.separator)) throw new IOException(Local.of("Invalid plugin path"));
                try (OutputStream sink = new FileOutputStream(out)) {
                    int n, count = 0;
                    while ((n = zip.read(buffer)) != - 1) {
                        count += n;
                        total += n;
                        if (count>limit || total>PluginAudit.MAX_DEX + 1024 * 1024 + 65536) throw new IOException(Local.of("The plugin exceeds the size limit"));
                        sink.write(buffer, 0, n);
                    }
                }
            }
        }
        if (!seen.contains("manifest.json") || !seen.contains("classes.dex")) throw new IOException(Local.of("The plugin has no manifest.json or classes.dex"));
    }
}

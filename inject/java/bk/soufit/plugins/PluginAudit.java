package bk.soufit.plugins;
import bk.soufit.core.Local;
import bk.soufit.core.Text;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.Adler32;
public final class PluginAudit {
    private PluginAudit() {} public static final int MAX_DEX = 16 * 1024 * 1024;
    public static final int LOW = 0, MEDIUM = 1, HIGH = 2;
    public static final class Report {
        public final Set<String> findings = new LinkedHashSet<String>();
        public final Map<String, Integer> levels = new HashMap<String, Integer>();
        public int risk = LOW;
        public boolean blocked;
        public String digest = "";
        public String pluginId = "";
        public void add(String text, int level) {
            findings.add(text);
            Integer before = levels.get(text);
            if (before == null || before<level) levels.put(text, level);
            if (level>risk) risk = level;
        }
        public boolean highRisk() {
            return risk >= HIGH;
        }
        public String summary() {
            StringBuilder text = new StringBuilder(Text.PLUGIN_AUDIT_HEAD);
            text.append("\n").append(Local.of("Risk level: ")).append(label(risk));
            for (String item : findings) {
                Integer level = levels.get(item);
                text.append("\n").append(mark(level == null ? LOW : level)).append(item);
            }
            if (findings.isEmpty()) text.append("\n").append(Text.PLUGIN_AUDIT_CLEAN);
            return text.append("\n\n").append(Text.PLUGIN_AUDIT_NOTE).toString();
        }
    }

    private static String label(int level) {
        return level >= HIGH ? Local.of("high") : level == MEDIUM ? Local.of("medium") : Local.of("low");
    }

    private static String mark(int level) {
        return level >= HIGH ? "\u26A0 " : level == MEDIUM ? "\u25B2 " : "\u2022 ";
    }

    public static byte[] read(File file, int limit) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            int n, total = 0;
            while ((n = in.read(b)) != - 1) {
                total += n;
                if (total>limit) throw new IOException(Local.of("Plugin file size exceeded"));
                out.write(b, 0, n);
            }
            return out.toByteArray();
        }
    }

    public static String sha256(byte[] bytes) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static String hex(byte[] bytes) {
        StringBuilder b = new StringBuilder();
        for (byte v : bytes) b.append(String.format(java.util.Locale.ROOT, "%02x", v & 255));
        return b.toString();
    }

    public static Report folder(File folder) throws Exception {
        byte[] dex = read(new File(folder, "classes.dex"), MAX_DEX);
        Report report = dexWithEntry(dex, entryOf(folder));
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        for (String name : new String[] {"manifest.json", "classes.dex", "icon.png"}) {
            hash.update(name.getBytes("UTF-8"));
            File file = new File(folder, name);
            if (file.isFile()) hash.update(MessageDigest.getInstance("SHA-256").digest(read(file, name.equals("classes.dex")
                    ? MAX_DEX : name.equals("icon.png") ? 1024 * 1024 : 65536)));
        }
        report.digest = hex(hash.digest());
        return report;
    }

    public static Report dex(byte[] data) throws Exception {
        return dexWithEntry(data, null);
    }

    private static Report dexWithEntry(byte[] data, String entry) throws Exception {
        try {
            return read(data, entry);
        } catch (IOException unreadable) {
            Report report = new Report();
            report.add(Local.of("The plugin's code could not be parsed: ") + unreadable.getMessage(), LOW);
            report.digest = sha256(data);
            return report;
        }
    }

    private static String entryOf(File folder) {
        try {
            String text = new String(read(new File(folder, "manifest.json"), 65536), "UTF-8");
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"entry\"\\s*:\\s*\"([^\"]+)\"").matcher(text);
            return m.find() ? m.group(1) : null;
        } catch (Exception unreadable) {
            return null;
        }
    }

    private static final String[] SENSITIVE_CLASSES = {"Runtime", "ProcessBuilder", "ClassLoader", "DexFile", "Telephony",
            "ContactsContract", "AccountManager", "Clipboard", "WebView", "Cipher", "Socket", "Contacts", "Method", "Field"};
    private static final java.util.regex.Pattern FRAMEWORK_NAME = java.util.regex.Pattern.compile("(android|java|javax|dalvik)(\\.[A-Za-z_][A-Za-z0-9_]*)+\\.[A-Z][A-Za-z0-9_$]*");
    private static final java.util.regex.Pattern ENCODED = java.util.regex.Pattern.compile("[A-Za-z0-9+/=_-]+");
    private static boolean framework(String type) {
        return type.startsWith("Landroid/") || type.startsWith("Ljava/") || type.startsWith("Ljavax/") || type.startsWith("Ldalvik/")
                || type.startsWith("Lkotlin/") || type.startsWith("Lorg/") || type.startsWith("Lcom/google/") || type.startsWith("Landroidx/");
    }

    private static String simpleName(String type) {
        String t = type.endsWith(";") ? type.substring(0, type.length() - 1) : type;
        return t.substring(t.lastIndexOf('/') + 1);
    }

    private static double entropy(String s) {
        int[] count = new int[256];
        for (int i = 0; i<s.length(); i ++) count[s.charAt(i) & 255] ++;
        double bits = 0;
        for (int n : count) if (n>0) {
            double p = (double) n / s.length();
            bits -= p * Math.log(p) / Math.log(2);
        }
        return bits;
    }

    private static final String B64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static byte[] unbase64(String s) {
        String t = s.replace('-', '+').replace('_', '/');
        int end = t.length();
        while (end>0 && t.charAt(end - 1) == '=') end --;
        if (end == 0 || end % 4 == 1) return null;
        byte[] out = new byte[end * 3 / 4];
        int bits = 0, value = 0, n = 0;
        for (int i = 0; i<end; i ++) {
            int v = B64.indexOf(t.charAt(i));
            if (v<0) return null;
            value = (value<<6) | v;
            bits += 6;
            if (bits >= 8) {
                bits -= 8;
                if (n<out.length) out[n ++] = (byte)((value>>bits) & 255);
            }
        }
        return Arrays.copyOf(out, n);
    }

    private static boolean readable(byte[] bytes) {
        if (bytes == null || bytes.length<16) return false;
        int text = 0;
        for (byte b : bytes) {
            int c = b & 255;
            if ((c >= 0x20 && c<0x7f) || c == '\n' || c == '\r' || c == '\t') text ++;
        }
        return text * 10 >= bytes.length * 9;
    }

    private static String hostOf(String url) {
        int start = url.indexOf("://") + 3;
        int end = url.length();
        for (int k = start; k<url.length(); k ++) {
            char c = url.charAt(k);
            if (c == '/' || c == '?' || c == ':') {
                end = k;
                break;
            }
        }
        return url.substring(start, end);
    }

    private static Report read(byte[] data, String entry) throws Exception {
        Report report = new Report();
        Dex d = new Dex(data);
        Set<String> types = new HashSet<String>();
        int plugin = 0, shortNames = 0;
        for (int i = 0; i<d.typeCount; i ++) {
            String type = d.type(i);
            types.add(type);
            if (type.startsWith("Ldalvik/system/") && (type.contains("ClassLoader") || type.contains("DexFile"))) report.add(Local.of("Loads additional executable code"),
                    HIGH);
            if (type.startsWith("Ljava/lang/reflect/") || type.startsWith("Ljava/lang/invoke/")) report.add(Local.of("Reflection: some actions cannot be determined statically"),
                    MEDIUM);
            if (type.startsWith("Ljava/net/") || type.startsWith("Lokhttp") || type.startsWith("Lretrofit")) report.add(Local.of("Network access"),
                    MEDIUM);
            if (type.equals("Landroid/webkit/WebView;")) report.add(Local.of("WebView and possible JavaScript execution"),
                    MEDIUM);
            if (type.startsWith("Landroid/telephony/") || type.startsWith("Landroid/provider/Contacts") || type.startsWith("Landroid/accounts/")) report.add(Local.of("Access to personal device data"),
                    MEDIUM);
            if (type.equals("Landroid/content/ClipboardManager;")) report.add(Local.of("Clipboard access"), LOW);
            if (type.startsWith("Ljava/io/") || type.startsWith("Ljava/nio/file/")) report.add(Local.of("Direct access to the app's files"),
                    LOW);
            if (type.startsWith("L") && !framework(type)) {
                plugin ++;
                if (simpleName(type).length() <= 2) shortNames ++;
            }
        }
        if (plugin >= 8 && shortNames * 10 >= plugin * 6) report.add(Local.of("Class names are obfuscated: ") + shortNames
                + " of " + plugin, MEDIUM);
        if (entry != null && entry.length()>0 && !types.contains("L" + entry.replace('.', '/') + ";")) report.add(Local.of("Plugin entry point not found in the code: ")
                + entry, HIGH);
        for (int i = 0; i<d.methodCount; i ++) {
            int p = d.methodOff + i * 8;
            String owner = d.type(d.u16(p));
            String name = d.string(d.u32(p + 4));
            if ((owner.equals("Ljava/lang/Runtime;") && name.equals("exec")) || (owner.equals("Ljava/lang/ProcessBuilder;")
                    && name.equals("start"))) report.add(Local.of("Runs system commands"), HIGH);
            if ((owner.equals("Ljava/lang/System;") || owner.equals("Ljava/lang/Runtime;")) && (name.equals("load") || name.equals("loadLibrary"))) report.add(Local.of("Loads native libraries"),
                    HIGH);
            if (owner.equals("Ljava/lang/Class;") && name.equals("forName")) report.add(Local.of("Class lookup by name: API may be loaded covertly"),
                    MEDIUM);
            if ((owner.equals("Lcat/narezany/margyt/plugin/PluginContext;") && (name.equals("fetch") || name.equals("download")))
                    || owner.equals("Lbk/soufit/Net;")) report.add(Local.of("Network through the mod API"), MEDIUM);
            if ((owner.equals("Lcat/narezany/margyt/plugin/PluginContext;") && name.equals("context")) || (owner.equals("Lbk/soufit/soufiT;")
                    && name.equals("context"))) report.add(Local.of("Access to the TikTok context"), LOW);
            if (owner.equals("Ljava/lang/ClassLoader;") && name.equals("loadClass")) report.add(Local.of("Loads classes by name"),
                    MEDIUM);
        }
        List<String> hosts = new ArrayList<String>();
        int encoded = 0;
        for (int i = 0; i<d.stringCount; i ++) {
            String s = d.string(i);
            if (s.startsWith("http://") || s.startsWith("https://")) {
                String host = hostOf(s);
                if (host.length()>0 && hosts.size()<5 && !hosts.contains(host)) hosts.add(host);
            }
            if (s.length() >= 32 && s.indexOf(' ')<0 && s.indexOf('.')<0 && ENCODED.matcher(s).matches() && readable(unbase64(s))) encoded ++;
            if (FRAMEWORK_NAME.matcher(s).matches()) {
                String simple = s.substring(s.lastIndexOf('.') + 1);
                for (String word : SENSITIVE_CLASSES) if (simple.contains(word)) {
                    report.add(Local.of("A system class name is written as a string: the call is hidden from review"), HIGH);
                    break;
                }
            }
            if (s.equals("/system/bin/su") || s.contains("busybox") || s.contains("su -c")) report.add(Local.of("Searches for root access"),
                    HIGH);
        }
        if (encoded >= 2) report.add(Local.of("Hidden text: encoded strings that read as text or code: ")
                + encoded + Local.of(" items."), MEDIUM);
        if (!hosts.isEmpty()) {
            StringBuilder list = new StringBuilder();
            for (String host : hosts) {
                if (list.length()>0) list.append(", ");
                list.append(host);
            }
            report.add(Local.of("Addresses in the code: ") + list, LOW);
        }
        int count = d.u32(96), off = d.u32(100);
        d.table(off, count, 32);
        for (int i = 0; i<count; i ++) {
            int at = d.u32(off + i * 32 + 24);
            if (at == 0) continue;
            int[] cursor = {at};
            int sf = d.uleb(cursor), inf = d.uleb(cursor), dm = d.uleb(cursor), vm = d.uleb(cursor);
            if ((long) sf + inf + dm + vm>1000000) throw new IOException(Local.of("Invalid DEX table"));
            for (int j = 0; j<sf + inf; j ++) {
                d.uleb(cursor);
                d.uleb(cursor);
            }
            for (int j = 0; j<dm + vm; j ++) {
                d.uleb(cursor);
                int flags = d.uleb(cursor);
                d.uleb(cursor);
                if ((flags & 256) != 0) report.add(Local.of("Native methods outside the reach of Java checks"), HIGH);
            }
        }
        report.digest = sha256(data);
        return report;
    }

    private static final class Dex {
        public final byte[] b;
        private final Map<Integer, String> decoded = new HashMap<Integer, String>();
        private int textBytes, ulebReads;
        public final int stringCount, stringOff, typeCount, typeOff, methodCount, methodOff;
        public Dex(byte[] bytes) throws Exception {
            b = bytes;
            if (b.length<112 || b.length>MAX_DEX || b[0] != 'd' || b[1] != 'e' || b[2] != 'x' || b[3] != 10 || b[7] != 0) throw new IOException(Local.of("Invalid DEX"));
            String version = new String(b, 4, 3, "US-ASCII");
            if (!Arrays.asList("035", "037", "038", "039", "040").contains(version)) throw new IOException(Local.of("Unsupported DEX version"));
            if (u32(32) != b.length || u32(36) != 112 || u32(40) != 0x12345678) throw new IOException(Local.of("Invalid DEX header"));
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(b, 32, b.length - 32);
            if (!Arrays.equals(sha.digest(), Arrays.copyOfRange(b, 12, 32))) throw new IOException(Local.of("DEX damaged: SHA-1"));
            Adler32 checksum = new Adler32();
            checksum.update(b, 12, b.length - 12);
            if (checksum.getValue() != (raw32(8) & 0xffffffffL)) throw new IOException(Local.of("DEX damaged: checksum"));
            stringCount = u32(56);
            stringOff = u32(60);
            typeCount = u32(64);
            typeOff = u32(68);
            methodCount = u32(88);
            methodOff = u32(92);
            table(stringOff, stringCount, 4);
            table(typeOff, typeCount, 4);
            table(methodOff, methodCount, 8);
            for (int i = 0; i<stringCount; i ++) string(i);
        }
        public int raw32(int p) throws IOException {
            range(p, 4);
            return (b[p] & 255) | ((b[p + 1] & 255)<<8) | ((b[p + 2] & 255)<<16) | ((b[p + 3] & 255)<<24);
        }
        public int u32(int p) throws IOException {
            int value = raw32(p);
            if (value<0) throw new IOException(Local.of("DEX value too large"));
            return value;
        }
        public int u16(int p) throws IOException {
            range(p, 2);
            return (b[p] & 255) | ((b[p + 1] & 255)<<8);
        }
        public void range(int p, long n) throws IOException {
            if (p<0 || n<0 || (long) p + n>b.length) throw new IOException(Local.of("DEX table out of bounds"));
        }
        public void table(int off, int count, int size) throws IOException {
            if (count>100000 || (count == 0) != (off == 0)) throw new IOException(Local.of("Invalid DEX table"));
            range(off, (long) count * size);
        }
        public int uleb(int[] pos) throws IOException {
            if (++ ulebReads>1000000) throw new IOException(Local.of("DEX too complex to check"));
            long value = 0;
            for (int i = 0; i<5; i ++) {
                range(pos[0], 1);
                int v = b[pos[0] ++] & 255;
                value |=(long)(v & 127)<<(i * 7);
                if ((v & 128) == 0) {
                    if (value>Integer.MAX_VALUE) throw new IOException(Local.of("DEX overflow"));
                    return (int) value;
                }
            }
            throw new IOException(Local.of("Invalid ULEB128"));
        }
        public String string(int index) throws IOException {
            if (index<0 || index >= stringCount) throw new IOException(Local.of("DEX string index"));
            int offset = u32(stringOff + index * 4);
            String ready = decoded.get(offset);
            if (ready != null) return ready;
            int[] pos = {offset};
            uleb(pos);
            int start = pos[0];
            while (true) {
                range(pos[0], 1);
                if (b[pos[0] ++] == 0) break;
                if (++ textBytes>MAX_DEX) throw new IOException(Local.of("DEX strings too large"));
            }
            ready = new String(b, start, pos[0] - start - 1, java.nio.charset.StandardCharsets.UTF_8);
            decoded.put(offset, ready);
            return ready;
        }
        public String type(int index) throws IOException {
            if (index<0 || index >= typeCount) throw new IOException(Local.of("DEX type index"));
            return string(u32(typeOff + index * 4));
        }
    }
}

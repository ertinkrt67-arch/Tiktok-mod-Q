import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
LABEL = ROOT / "inject" / "java" / "bk" / "soufit" / "feed" / "RegionLabel.java"

STUBS = {
    "bk/soufit/core/DevMode.java": """package bk.soufit.core;
public final class DevMode { public static boolean on = true; public static boolean isOn() { return on; } }""",
    "bk/soufit/core/Diary.java": """package bk.soufit.core;
public final class Diary {
    public static final java.util.List<String> lines = new java.util.ArrayList<String>();
    public static void note(String line) { lines.add(line); }
    public static void detail(String line) { lines.add(line); }
    public static void fail(String where, Throwable error) { lines.add(where); }
}""",
    "bk/soufit/core/Flags.java": """package bk.soufit.core;
public final class Flags {
    private static final java.util.Map<String, Boolean> values = new java.util.HashMap<String, Boolean>();
    public static boolean isOn(String key) { return values.containsKey(key) && values.get(key); }
    public static void set(String key, boolean on) { values.put(key, on); }
}""",
    "bk/soufit/core/NativeRead.java": """package bk.soufit.core;
public final class NativeRead {
    public static Object get(Object object, String name) {
        if (object == null) return null;
        try { return object.getClass().getMethod(name).invoke(object); } catch (Throwable error) { return null; }
    }
    public static String string(Object object) { return object instanceof String ? (String) object : ""; }
}""",
    "bk/soufit/feed/Dates.java": """package bk.soufit.feed;
public final class Dates { public static boolean enabled; public static boolean isEnabled() { return enabled; } }""",
    "bk/soufit/feed/DateOverlay.java": """package bk.soufit.feed;
public final class DateOverlay { public static void update() {} }""",
    "android/os/Looper.java": """package android.os;
public class Looper { public static Looper getMainLooper() { return null; } }""",
    "android/os/Handler.java": """package android.os;
public class Handler {
    public Handler(Looper looper) {}
    public void post(Runnable task) { task.run(); }
}""",
}

CHECK = r"""
package bk.soufit;
import bk.soufit.core.Diary;
import bk.soufit.feed.Dates;
import bk.soufit.feed.RegionLabel;
import java.util.Locale;

public class RegionLabelCheck {
    public static class Author {
        String region, language;
        public Author(String region, String language) { this.region = region; this.language = language; }
        public String getRegion() { return region; }
        public String getLanguage() { return language; }
    }

    public static class Post {
        Author author;
        String region, language;
        public Post(Author author) { this.author = author; }
        public Post(Author author, String region, String language) { this.author = author; this.region = region; this.language = language; }
        public Author getAuthor() { return author; }
        public String getRegion() { return region; }
        public String getDescLanguage() { return language; }
    }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        Locale.setDefault(new Locale("de", "DE"));

        Post russian = new Post(new Author("RU", "ru"));
        Post american = new Post(new Author("us", "en"));

        check("".equals(RegionLabel.of(null)), "no post, no label");
        check("3 days ago".equals(RegionLabel.with("3 days ago", russian)), "off leaves the date alone");

        RegionLabel.setEnabled(true);
        Dates.enabled = true;
        check("3 days ago · RU · ru".equals(RegionLabel.with("3 days ago", russian)),
                "date and line together, got " + RegionLabel.with("3 days ago", russian));

        check("3 days ago · US · en".equals(RegionLabel.with("3 days ago", american)),
                "each post shows its own profile, got " + RegionLabel.with("3 days ago", american));

        check(!"DE · de".equals(RegionLabel.of(russian)) && !"DE · de".equals(RegionLabel.of(american)),
                "the phone's locale is not used");

        Dates.enabled = false;
        check("RU · ru".equals(RegionLabel.with("3 days ago", russian)), "line alone when the date is off");

        Dates.enabled = true;
        check("RU · ru".equals(RegionLabel.with(null, russian)), "line alone when there is no date");

        check("BR · ja".equals(RegionLabel.of(new Post(new Author(null, "ja"), "br", "xx"))),
                "author language used, author has no region so the video's region is used, got " + RegionLabel.of(new Post(new Author(null, "ja"), "br", "xx")));

        Diary.lines.clear();
        check("".equals(RegionLabel.of(new Post(new Author("Russia", "russian-language-name")))),
                "values that are not a code are ignored");
        check(Diary.lines.size() == 1 && Diary.lines.get(0).startsWith("region: none found"),
                "the missing accessors are reported, got " + Diary.lines);
        check("3 days ago".equals(RegionLabel.with("3 days ago", new Post(new Author(null, null)))),
                "nothing known: the date alone");
        check(Diary.lines.size() == 1, "reported only once");

        RegionLabel.setEnabled(false);
        check("3 days ago".equals(RegionLabel.with("3 days ago", russian)), "switched off again");
        System.out.println("region label ok");
    }
}
"""


class RegionLabelRuntimeTest(unittest.TestCase):
    def test_line_is_the_selected_profiles_region_and_language(self):
        work = pathlib.Path(tempfile.mkdtemp(prefix="region-label-"))
        try:
            src = work / "src"
            files = dict(STUBS)
            files["bk/soufit/feed/RegionLabel.java"] = LABEL.read_text(encoding="utf-8")
            files["bk/soufit/RegionLabelCheck.java"] = CHECK
            paths = []
            for name, text in files.items():
                path = src / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text, encoding="utf-8")
                paths.append(str(path))
            out = work / "out"
            out.mkdir()
            built = subprocess.run(["javac", "-nowarn", "-d", str(out)] + paths, capture_output=True, text=True)
            self.assertEqual(0, built.returncode, built.stderr)
            ran = subprocess.run(["java", "-cp", str(out), "bk.soufit.RegionLabelCheck"], capture_output=True, text=True)
            self.assertEqual(0, ran.returncode, ran.stdout + ran.stderr)
            self.assertIn("region label ok", ran.stdout)
        finally:
            shutil.rmtree(work)


if __name__ == "__main__":
    unittest.main()

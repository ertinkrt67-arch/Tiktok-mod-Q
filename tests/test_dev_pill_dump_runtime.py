import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
DUMP = ROOT / "inject" / "java" / "bk" / "soufit" / "ui" / "ScreenDump.java"

STUBS = {
    "android/content/res/Resources.java": """package android.content.res;
public class Resources {
    public String getResourceEntryName(int id) {
        if (id == 0x7f0a0001) return "feed_root";
        throw new RuntimeException("no name for " + id);
    }
}""",
    "android/view/View.java": """package android.view;
public class View {
    public static final int GONE = 8, INVISIBLE = 4;
    public int id, width, height, left, top, visibility;
    public float alpha = 1f;
    public boolean clickable;
    public CharSequence description;
    public android.content.res.Resources resources;
    public int getId() { return id; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLeft() { return left; }
    public int getTop() { return top; }
    public int getVisibility() { return visibility; }
    public float getAlpha() { return alpha; }
    public boolean isClickable() { return clickable; }
    public CharSequence getContentDescription() { return description; }
    public android.content.res.Resources getResources() { return resources; }
}""",
    "android/view/ViewGroup.java": """package android.view;
public class ViewGroup extends View {
    public final java.util.List<View> children = new java.util.ArrayList<View>();
    public int getChildCount() { return children.size(); }
    public View getChildAt(int index) { return children.get(index); }
}""",
    "android/widget/TextView.java": """package android.widget;
public class TextView extends android.view.View {
    public CharSequence text = "";
    public CharSequence getText() { return text; }
}""",
    "android/view/Window.java": """package android.view;
public class Window {
    public View decor;
    public View getDecorView() { return decor; }
}""",
    "android/app/Activity.java": """package android.app;
public class Activity {
    public android.view.Window window;
    public android.view.Window getWindow() { return window; }
}""",
    "bk/soufit/core/Diagnostics.java": """package bk.soufit.core;
public final class Diagnostics { public static String fullReport() { return "== FULL REPORT STUB ==\\n"; } }""",
}

CHECK = r"""
package bk.soufit;
import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import bk.soufit.ui.ScreenDump;

public class DumpCheck {
    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static Activity screen(ViewGroup root) {
        Activity activity = new Activity();
        Window window = new Window();
        window.decor = root;
        activity.window = window;
        return activity;
    }

    public static void main(String[] args) {
        Resources resources = new Resources();

        ViewGroup root = new ViewGroup();
        root.width = 1080;
        root.height = 2400;
        root.resources = resources;

        ViewGroup row = new ViewGroup();
        row.resources = resources;
        TextView title = new TextView();
        title.id = 0x7f0a0001;
        title.resources = resources;
        title.width = 100;
        title.height = 40;
        title.clickable = true;
        title.text = "Hello\nworld";
        row.children.add(title);
        root.children.add(row);

        TextView unnamed = new TextView();
        unnamed.id = 0x7f0a0002;
        unnamed.text = "";
        unnamed.visibility = View.GONE;
        unnamed.description = "Close";
        root.children.add(unnamed);

        TextView pill = new TextView();
        pill.resources = resources;
        pill.text = "Dump";
        root.children.add(pill);

        String out = ScreenDump.capture(screen(root), pill);
        check(out.startsWith("== soufiT screen dump =="), "header first, got " + out.substring(0, 40));
        check(out.contains("screen: android.app.Activity\n"), "screen name");
        check(out.contains("window: 1080x2400\n"), "window size");
        check(out.contains("\n== view tree ==\n"), "tree section");
        check(out.contains("ViewGroup 1080x2400 at 0,0\n"), "root line, got " + out);
        check(out.contains("\n  ViewGroup 0x0 at 0,0\n"), "row indented one level");
        check(out.contains("\n    TextView #feed_root 100x40 at 0,0 clickable text=\"Hello world\"\n"),
                "named clickable text view, got " + out);
        check(out.contains("\n  TextView #7f0a0002 0x0 at 0,0 gone desc=\"Close\"\n"),
                "gone view shows its id as hex when no name exists, got " + out);
        check(!out.contains("text=\"Dump\""), "the pill that asked for the dump is left out");
        check(out.endsWith("== FULL REPORT STUB ==\n"), "diagnostics appended last, got " + out.substring(out.length() - 40));

        ViewGroup wide = new ViewGroup();
        wide.resources = resources;
        TextView wordy = new TextView();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 300; i ++) text.append('x');
        wordy.text = text.toString();
        wide.children.add(wordy);
        String cut = ScreenDump.capture(screen(wide), null);
        check(cut.contains("text=\"" + repeat('x', 200) + "…\""), "long text is cut at 200 characters");

        ViewGroup crowd = new ViewGroup();
        crowd.resources = resources;
        for (int i = 0; i < 1600; i ++) crowd.children.add(new TextView());
        String many = ScreenDump.capture(screen(crowd), null);
        check(many.contains("(stopped after 1500 views)"), "budget message");
        check(count(many, "\n") < 1500 + 25, "no more than the budget is written");

        String none = ScreenDump.capture(null, null);
        check(none.contains("screen: none\n") && none.endsWith("== FULL REPORT STUB ==\n"), "no screen still dumps the report");

        System.out.println("dump checks ok");
    }

    static String repeat(char c, int n) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i ++) out.append(c);
        return out.toString();
    }

    static int count(String text, String part) {
        int found = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + 1)) found ++;
        return found;
    }
}
"""


class DevPillDumpRuntimeTest(unittest.TestCase):
    def test_dump_lists_the_screen_and_leaves_out_the_pill(self):
        work = pathlib.Path(tempfile.mkdtemp(prefix="screen-dump-"))
        try:
            src = work / "src"
            files = dict(STUBS)
            files["bk/soufit/ui/ScreenDump.java"] = DUMP.read_text(encoding="utf-8")
            files["bk/soufit/DumpCheck.java"] = CHECK
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
            ran = subprocess.run(["java", "-cp", str(out), "bk.soufit.DumpCheck"], capture_output=True, text=True)
            self.assertEqual(0, ran.returncode, ran.stdout + ran.stderr)
            self.assertIn("dump checks ok", ran.stdout)
        finally:
            shutil.rmtree(work)


if __name__ == "__main__":
    unittest.main()

import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE = ROOT / "inject" / "java" / "bk" / "soufit" / "profile" / "ProfileRegion.java"

STUBS = {
    "android/util/TypedValue.java": """package android.util;
public class TypedValue { public static final int COMPLEX_UNIT_PX = 0; }""",
    "android/content/Context.java": """package android.content;
public class Context {}""",
    "android/view/ViewParent.java": """package android.view;
public interface ViewParent {}""",
    "android/view/View.java": """package android.view;
public class View {
    public static final int VISIBLE = 0, GONE = 8;
    public ViewParent parent;
    java.util.Map<Integer, Object> tags = new java.util.HashMap<Integer, Object>();
    public ViewParent getParent() { return parent; }
    public void setTag(int key, Object value) { tags.put(key, value); }
    public Object getTag(int key) { return tags.get(key); }
    public android.content.Context getContext() { return new android.content.Context(); }
    public void setAlpha(float alpha) {}
    public int getVisibility() { return VISIBLE; }
}""",
    "android/view/ViewGroup.java": """package android.view;
public class ViewGroup extends View implements ViewParent {
    final java.util.List<View> children = new java.util.ArrayList<View>();
    public int getChildCount() { return children.size(); }
    public View getChildAt(int index) { return children.get(index); }
    public void addView(View child, int index) {
        child.parent = this;
        children.add(index, child);
    }
    public void removeView(View child) {
        children.remove(child);
        child.parent = null;
    }
}""",
    "android/widget/LinearLayout.java": """package android.widget;
public class LinearLayout extends android.view.ViewGroup {}""",
    "android/widget/TextView.java": """package android.widget;
public class TextView extends android.view.View {
    CharSequence text = "";
    int color = 0xFF000000;
    float size = 20f;
    public TextView() {}
    public TextView(android.content.Context context) {}
    public void setText(CharSequence value) { text = value; }
    public CharSequence getText() { return text; }
    public void setSingleLine(boolean value) {}
    public void setTextColor(int value) { color = value; }
    public int getCurrentTextColor() { return color; }
    public void setTextSize(int unit, float value) { size = value; }
    public float getTextSize() { return size; }
}""",
    "bk/soufit/feed/RegionLabel.java": """package bk.soufit.feed;
public final class RegionLabel {
    public static boolean on = true;
    public static String value = "US · en";
    public static boolean isEnabled() { return on; }
    public static String of(Object post) { return value; }
}""",
    "bk/soufit/profile/Account.java": """package bk.soufit.profile;
public final class Account {
    public static String region = "US · en";
    public static String handle = "sillysoufi";
    public static String profileRegion() { return region; }
    public static boolean isOwnHandle(String text) {
        if (text == null) return false;
        String shown = text.trim();
        if (shown.startsWith("@")) shown = shown.substring(1);
        return shown.equals(handle);
    }
}""",
}

CHECK = r"""
package bk.soufit.profile;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import bk.soufit.feed.RegionLabel;

public class ProfileRegionCheck {
    static final int TAG = 0x7e0c0412;

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static int labels(ViewGroup column) {
        int count = 0;
        for (int i = 0; i < column.getChildCount(); i ++) if (column.getChildAt(i).getTag(TAG) != null) count ++;
        return count;
    }

    static String labelText(ViewGroup column) {
        for (int i = 0; i < column.getChildCount(); i ++) {
            View child = column.getChildAt(i);
            if (child.getTag(TAG) != null) return String.valueOf(((TextView) child).getText());
        }
        return null;
    }

    static TextView text(String value) {
        TextView view = new TextView();
        view.setText(value);
        return view;
    }

    public static void main(String[] args) {
        ViewGroup header = new ViewGroup();
        LinearLayout column = new LinearLayout();
        header.addView(column, 0);
        column.addView(text("soufi 1"), 0);
        LinearLayout row = new LinearLayout();
        TextView handle = text("@sillysoufi");
        row.addView(handle, 0);
        column.addView(row, 1);
        column.addView(text("206"), 2);

        ProfileRegion.attach(header);
        check(column.getChildCount() == 4, "the region line is added under the handle row");
        check(column.getChildAt(2).getTag(TAG) != null, "the region line sits right after the handle row");
        check("US · en".equals(labelText(column)), "the region line shows the region and language");
        check(column.getChildAt(3) instanceof TextView && "206".equals(String.valueOf(((TextView) column.getChildAt(3)).getText())),
                "the stats row still follows the handle");

        ProfileRegion.attach(header);
        ProfileRegion.attach(header);
        check(labels(column) == 1 && column.getChildCount() == 4, "repeated layout passes do not add more lines");

        Account.region = "DE · de";
        ProfileRegion.attach(header);
        check("DE · de".equals(labelText(column)), "a changed region is shown on the next pass");
        Account.region = "US · en";

        RegionLabel.on = false;
        ProfileRegion.attach(header);
        check(labels(column) == 0 && column.getChildCount() == 3, "turning the setting off removes the line");
        RegionLabel.on = true;
        ProfileRegion.attach(header);
        check(labels(column) == 1, "turning the setting back on brings the line back");

        handle.setText("@someone_else");
        ProfileRegion.attach(header);
        check(labels(column) == 0, "another user's profile gets no region line");
        handle.setText("@sillysoufi");

        Account.region = "";
        ProfileRegion.attach(header);
        check(labels(column) == 0, "no region means no line");
        Account.region = "US · en";
        ProfileRegion.attach(header);
        check(labels(column) == 1, "the line returns once the region is known");

        System.out.println("profile region checks ok");
    }
}
"""


class ProfileRegionRuntimeTest(unittest.TestCase):
    def test_region_line_under_own_handle(self):
        work = pathlib.Path(tempfile.mkdtemp(prefix="profile-region-"))
        try:
            src = work / "src"
            files = dict(STUBS)
            files["bk/soufit/profile/ProfileRegion.java"] = SOURCE.read_text(encoding="utf-8")
            files["bk/soufit/profile/ProfileRegionCheck.java"] = CHECK
            paths = []
            for name, text in files.items():
                path = src / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text, encoding="utf-8")
                paths.append(str(path))
            out = work / "out"
            out.mkdir()
            built = subprocess.run(["javac", "-encoding", "UTF-8", "-nowarn", "-d", str(out)] + paths, capture_output=True, text=True)
            self.assertEqual(0, built.returncode, built.stderr)
            ran = subprocess.run(["java", "-cp", str(out), "bk.soufit.profile.ProfileRegionCheck"], capture_output=True, text=True)
            self.assertEqual(0, ran.returncode, ran.stdout + ran.stderr)
            self.assertIn("profile region checks ok", ran.stdout)
        finally:
            shutil.rmtree(work)


if __name__ == "__main__":
    unittest.main()

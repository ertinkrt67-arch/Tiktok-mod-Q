import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE = ROOT / "inject" / "java" / "bk" / "soufit" / "feed" / "SuggestedAccounts.java"

STUBS = {
    "android/content/res/Resources.java": """package android.content.res;
public class Resources {
    public static final java.util.Map<Integer, String> NAMES = new java.util.HashMap<Integer, String>();
    public static int id(String name) {
        int id = 0x7f0a1000 + NAMES.size() + 1;
        NAMES.put(id, name);
        return id;
    }
    public String getResourceEntryName(int id) {
        String name = NAMES.get(id);
        if (name == null) throw new RuntimeException("no name for " + id);
        return name;
    }
}""",
    "android/view/ViewParent.java": """package android.view;
public interface ViewParent {}""",
    "android/view/ViewTreeObserver.java": """package android.view;
public class ViewTreeObserver {
    public interface OnGlobalLayoutListener { void onGlobalLayout(); }
    public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {}
}""",
    "android/view/View.java": """package android.view;
public class View {
    public static final int VISIBLE = 0, INVISIBLE = 4, GONE = 8;
    int id;
    int visibility = VISIBLE;
    ViewParent parent;
    public int getId() { return id; }
    public void setId(int value) { id = value; }
    public int getVisibility() { return visibility; }
    public void setVisibility(int value) { visibility = value; }
    public ViewParent getParent() { return parent; }
    public android.content.res.Resources getResources() { return new android.content.res.Resources(); }
    ViewGroup.MarginLayoutParams layout = new ViewGroup.MarginLayoutParams(220);
    public ViewGroup.LayoutParams getLayoutParams() { return layout; }
    public static int relayouts = 0;
    public void setLayoutParams(ViewGroup.LayoutParams value) {
        layout = (ViewGroup.MarginLayoutParams) value;
        relayouts ++;
    }
    public Object getTag(int key) { return null; }
    public void setTag(int key, Object value) {}
    public void postDelayed(Runnable task, long delay) { task.run(); }
    public ViewTreeObserver getViewTreeObserver() { return new ViewTreeObserver(); }
}""",
    "android/view/ViewGroup.java": """package android.view;
public class ViewGroup extends View implements ViewParent {
    final java.util.List<View> children = new java.util.ArrayList<View>();
    public static class LayoutParams {
        public int height;
        public LayoutParams(int height) { this.height = height; }
    }
    public static class MarginLayoutParams extends LayoutParams {
        public int topMargin, bottomMargin;
        public MarginLayoutParams(int height) { super(height); }
    }
    public int getChildCount() { return children.size(); }
    public View getChildAt(int index) { return children.get(index); }
    public void add(View child) {
        child.parent = this;
        children.add(child);
    }
}""",
    "android/widget/LinearLayout.java": """package android.widget;
public class LinearLayout extends android.view.ViewGroup {}""",
    "android/widget/FrameLayout.java": """package android.widget;
public class FrameLayout extends android.view.ViewGroup {}""",
    "android/widget/TextView.java": """package android.widget;
public class TextView extends android.view.View {
    CharSequence text = "";
    public void setText(CharSequence value) { text = value; }
    public CharSequence getText() { return text; }
}""",
    "android/os/SystemClock.java": """package android.os;
public class SystemClock {
    public static long now = 0;
    public static long uptimeMillis() { return now; }
}""",
    "android/view/Window.java": """package android.view;
public class Window {
    public View decor = new View();
    public View getDecorView() { return decor; }
}""",
    "android/app/Activity.java": """package android.app;
public class Activity {
    public android.view.Window window = new android.view.Window();
    public android.view.Window getWindow() { return window; }
}""",
    "bk/soufit/core/Diary.java": """package bk.soufit.core;
public final class Diary {
    public static void note(String line) {}
    public static void detail(String line) {}
    public static void fail(String where, Throwable error) { throw new RuntimeException(where, error); }
}""",
    "bk/soufit/feed/Feed.java": """package bk.soufit.feed;
public final class Feed {
    public static final String KEY_SUGGESTIONS = "hide_suggestions";
    public static boolean on;
    public static boolean hides(String key) { return on; }
}""",
}

CHECK = r"""
package bk.soufit;
import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import bk.soufit.feed.Feed;
import bk.soufit.feed.SuggestedAccounts;

public class SuggestedCheck {
    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static View named(View view, String name) {
        view.setId(Resources.id(name));
        return view;
    }

    static boolean gone(View view) { return view.getVisibility() == View.GONE; }

    static LinearLayout inboxRow(String label) {
        LinearLayout row = new LinearLayout();
        row.add(named(new View(), "x8w"));
        row.add(named(new View(), "ip9"));
        LinearLayout text = new LinearLayout();
        TextView caption = new TextView();
        caption.setText(label);
        text.add(named(caption, "u9g"));
        row.add(text);
        row.add(named(new TextView(), "u96"));
        row.add(named(new View(), "fy3"));
        return row;
    }

    public static void main(String[] args) {
        ViewGroup decor = new ViewGroup();

        ViewGroup feedRow = new ViewGroup();
        decor.add(feedRow);
        View feedRowMulti = new View();
        feedRow.add(feedRowMulti);
        feedRow.add(new View());

        ViewGroup strip = new FrameLayout();
        View header = named(new LinearLayout(), "q3l");
        ((ViewGroup) header).add(named(new TextView(), "u1d"));
        ((ViewGroup) header).add(named(new TextView(), "l_x"));
        strip.add(header);
        View stub = named(new View(), "pwq");
        strip.add(stub);
        feedRow.add(strip);

        LinearLayout[] cards = new LinearLayout[6];
        for (int i = 0; i < cards.length; i ++) {
            cards[i] = new LinearLayout();
            if (i < 5) {
                cards[i].add(named(new View(), "x8w"));
                cards[i].add(named(new View(), "ip9"));
                cards[i].add(named(new View(), "p3w"));
                cards[i].add(named(new TextView(), "u96"));
                cards[i].add(named(new View(), "fy3"));
            } else {
                cards[i].add(named(new View(), "x8w"));
                cards[i].add(named(new View(), "ip9"));
            }
            feedRow.add(cards[i]);
        }
        LinearLayout otherRow = new LinearLayout();
        otherRow.add(named(new View(), "x8w"));
        feedRow.add(otherRow);
        LinearLayout laterCard = new LinearLayout();
        laterCard.add(named(new TextView(), "u96"));
        feedRow.add(laterCard);

        LinearLayout profileFollowers = new LinearLayout();
        profileFollowers.add(named(new TextView(), "u96"));
        decor.add(profileFollowers);

        LinearLayout findPage = new LinearLayout();
        decor.add(findPage);
        View title = named(new TextView(), "wm3");
        View list = named(new FrameLayout(), "wlx");
        findPage.add(title);
        findPage.add(list);
        View findHeading = named(new TextView(), "pt3");
        findPage.add(findHeading);

        LinearLayout suggestedRow = inboxRow("People you may know");
        LinearLayout followedRow = inboxRow("Followed by  ");
        LinearLayout inbox = new LinearLayout();
        inbox.add(suggestedRow);
        inbox.add(followedRow);
        decor.add(inbox);

        Activity activity = new Activity();
        activity.window.decor = decor;

        Feed.on = true;
        SuggestedAccounts.sweep(decor);
        check(gone(suggestedRow), "an inbox row labelled People you may know is hidden");
        check(suggestedRow.getLayoutParams().height == 0, "the hidden inbox row takes no height");
        int settled = View.relayouts;
        SuggestedAccounts.sweep(decor);
        SuggestedAccounts.sweep(decor);
        check(View.relayouts == settled, "a sweep that changes nothing does not ask for a new layout");
        check(!gone(followedRow), "an inbox row labelled Followed by is left alone");
        check(gone(strip), "the strip header and its wrapper are hidden");
        for (int i = 0; i < 5; i ++) check(gone(cards[i]), "card " + i + " is hidden");
        check(gone(cards[5]), "the card with only an avatar is hidden too");
        check(!gone(otherRow), "a row that is not a card stops the run");
        check(!gone(laterCard), "a card after a break is left alone");
        check(!gone(feedRowMulti), "other feed rows are left alone");
        check(!gone(profileFollowers), "a Follow button with no strip is left alone");
        check(gone(title) && gone(list), "the Find friends list and title are hidden");
        check(!gone(findHeading), "the Find friends heading is left alone");

        Feed.on = false;
        SuggestedAccounts.sweep(decor);
        check(!gone(strip) && !gone(cards[0]) && !gone(cards[5]) && !gone(title) && !gone(list) && !gone(suggestedRow),
                "turning the setting off brings everything back");
        check(suggestedRow.getLayoutParams().height == 220, "the inbox row gets its height back");

        Feed.on = true;
        SuggestedAccounts.sweep(decor);
        cards[2].setVisibility(View.VISIBLE);
        SuggestedAccounts.sweep(decor);
        check(gone(cards[2]), "a card the feed shows again is hidden on the next sweep");

        View stubGone = named(new View(), "pwq");
        stubGone.setVisibility(View.GONE);
        feedRow.add(stubGone);
        Feed.on = false;
        SuggestedAccounts.sweep(decor);
        check(gone(stubGone), "a view that was already gone stays gone after restoring");

        Feed.on = false;
        SuggestedAccounts.sweep(decor);
        System.out.println("suggested accounts checks ok");
    }
}
"""


class SuggestedAccountsRuntimeTest(unittest.TestCase):
    def test_strip_and_friends_list_hide_and_restore(self):
        work = pathlib.Path(tempfile.mkdtemp(prefix="suggested-accounts-"))
        try:
            src = work / "src"
            files = dict(STUBS)
            files["bk/soufit/feed/SuggestedAccounts.java"] = SOURCE.read_text(encoding="utf-8")
            files["bk/soufit/SuggestedCheck.java"] = CHECK
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
            ran = subprocess.run(["java", "-cp", str(out), "bk.soufit.SuggestedCheck"], capture_output=True, text=True)
            self.assertEqual(0, ran.returncode, ran.stdout + ran.stderr)
            self.assertIn("suggested accounts checks ok", ran.stdout)
        finally:
            shutil.rmtree(work)


if __name__ == "__main__":
    unittest.main()

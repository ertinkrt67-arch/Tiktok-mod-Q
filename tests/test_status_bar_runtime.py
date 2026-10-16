import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE = ROOT / "inject" / "java" / "bk" / "soufit" / "theme" / "StatusBar.java"

STUBS = {
    "android/os/Build.java": """package android.os;
public class Build { public static class VERSION { public static int SDK_INT = 30; } }""",
    "android/view/WindowInsets.java": """package android.view;
public class WindowInsets { public static class Type { public static int statusBars() { return 1; } } }""",
    "android/view/WindowInsetsController.java": """package android.view;
public class WindowInsetsController {
    public static final int BEHAVIOR_SHOW_TRANSIENT_BY_SWIPE = 2;
    public int hides, shows, behaviour;
    public void hide(int types) { hides ++; }
    public void show(int types) { shows ++; }
    public void setSystemBarsBehavior(int behavior) { behaviour = behavior; }
}""",
    "android/view/ViewTreeObserver.java": """package android.view;
public class ViewTreeObserver {
    public interface OnWindowFocusChangeListener { void onWindowFocusChanged(boolean hasFocus); }
    public final java.util.List<OnWindowFocusChangeListener> focus = new java.util.ArrayList<OnWindowFocusChangeListener>();
    public void addOnWindowFocusChangeListener(OnWindowFocusChangeListener listener) { focus.add(listener); }
}""",
    "android/view/View.java": """package android.view;
public class View {
    public static final int SYSTEM_UI_FLAG_FULLSCREEN = 4;
    public static final java.util.List<Runnable> POSTED = new java.util.ArrayList<Runnable>();
    public WindowInsetsController controller;
    public int uiVisibility;
    private final ViewTreeObserver observer = new ViewTreeObserver();
    public WindowInsetsController getWindowInsetsController() { return controller; }
    public ViewTreeObserver getViewTreeObserver() { return observer; }
    public int getSystemUiVisibility() { return uiVisibility; }
    public void setSystemUiVisibility(int flags) { uiVisibility = flags; }
    public boolean post(Runnable task) { POSTED.add(task); return true; }
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
    "bk/soufit/core/Flags.java": """package bk.soufit.core;
public final class Flags {
    private static final java.util.Map<String, Boolean> values = new java.util.HashMap<String, Boolean>();
    public static boolean isOn(String key) { return values.containsKey(key) && values.get(key); }
    public static void set(String key, boolean on) { values.put(key, on); }
}""",
    "bk/soufit/core/Diary.java": """package bk.soufit.core;
public final class Diary {
    public static void note(String line) {}
    public static void fail(String where, Throwable error) { throw new RuntimeException(where, error); }
}""",
}

CHECK = r"""
package bk.soufit;
import android.app.Activity;
import android.view.View;
import android.view.WindowInsetsController;
import bk.soufit.core.Flags;
import bk.soufit.theme.StatusBar;
import java.util.ArrayList;
import java.util.List;

public class StatusCheck {
    public static class Mine extends Activity {}

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static void runPosted() {
        while (!View.POSTED.isEmpty()) {
            List<Runnable> batch = new ArrayList<Runnable>(View.POSTED);
            View.POSTED.clear();
            for (Runnable task : batch) task.run();
        }
    }

    static Activity activity(View decor) {
        Activity activity = new Activity();
        activity.window.decor = decor;
        return activity;
    }

    public static void main(String[] args) {
        Flags.set(StatusBar.KEY, true);

        View early = new View();
        Activity first = activity(early);
        StatusBar.watch(first);
        check(View.POSTED.size() == 1, "the window is not ready: one retry is queued");
        WindowInsetsController controller = new WindowInsetsController();
        early.controller = controller;
        runPosted();
        check(controller.hides == 1 && controller.behaviour == 2, "hidden once the window is ready, got hides=" + controller.hides);

        for (Object listener : early.getViewTreeObserver().focus) ((android.view.ViewTreeObserver.OnWindowFocusChangeListener) listener).onWindowFocusChanged(true);
        check(controller.hides == 2, "gaining focus hides it again");

        StatusBar.setOn(false);
        check(controller.shows == 1, "turning it off shows it");
        StatusBar.setOn(true);
        check(controller.hides == 3, "turning it on hides it at once");

        View never = new View();
        StatusBar.watch(activity(never));
        check(View.POSTED.size() == 1, "a window that never gets insets is retried once");
        runPosted();
        check(View.POSTED.isEmpty(), "and not retried forever");

        View.POSTED.clear();
        View own = new View();
        Activity mine = new Mine();
        mine.window.decor = own;
        StatusBar.watch(mine);
        check(View.POSTED.isEmpty() && own.controller == null, "the mod's own screens are left alone");
        System.out.println("status bar checks ok");
    }
}
"""


class StatusBarRuntimeTest(unittest.TestCase):
    def test_status_bar_waits_for_the_window_and_follows_the_toggle(self):
        work = pathlib.Path(tempfile.mkdtemp(prefix="status-bar-"))
        try:
            src = work / "src"
            files = dict(STUBS)
            files["bk/soufit/theme/StatusBar.java"] = SOURCE.read_text(encoding="utf-8")
            files["bk/soufit/StatusCheck.java"] = CHECK
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
            ran = subprocess.run(["java", "-cp", str(out), "bk.soufit.StatusCheck"], capture_output=True, text=True)
            self.assertEqual(0, ran.returncode, ran.stdout + ran.stderr)
            self.assertIn("status bar checks ok", ran.stdout)
        finally:
            shutil.rmtree(work)


if __name__ == "__main__":
    unittest.main()

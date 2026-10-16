import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
STUBS = {
    'android/content/Intent.java': 'package android.content; public class Intent {public Intent(Context c,Class<?> t){}public Intent putExtra(String k,boolean b){return this;}}',
    'android/content/Context.java': '''package android.content; public class Context {
        public static int MODE_PRIVATE=0; public static Prefs prefs=new Prefs();
        public Prefs getSharedPreferences(String s,int m){return prefs;}
        public android.content.res.Resources getResources(){return new android.content.res.Resources();}
        public static class Prefs {public boolean done;
        public boolean getBoolean(String k,boolean d){return done;}
        public Prefs edit(){return this;} public Prefs putBoolean(String k,boolean b){done=b;return this;} public void apply(){} } }''',
    'android/content/res/Resources.java': '''package android.content.res; public class Resources {
        public static class Metrics {public float density=1;}
        public Metrics getDisplayMetrics(){return new Metrics();} }''',
    'android/content/DialogInterface.java': '''package android.content; public interface DialogInterface {
        interface Listener {void onDialog(DialogInterface d);} }''',
    'android/graphics/Typeface.java': '''package android.graphics; public class Typeface {
        public static Typeface create(String n,int s){return new Typeface();} }''',
    'android/view/View.java': '''package android.view; public class View {
        public static int GONE=8,VISIBLE=0,ACCESSIBILITY_LIVE_REGION_POLITE=1;
        public boolean focus=true; public int visibility;
        public java.util.List<Runnable> queue=new java.util.ArrayList<>();
        public interface Click {void onClick(View v);} public Click click;
        public void postDelayed(Runnable r,long n){queue.add(r);}
        public boolean hasWindowFocus(){return focus;}
        public void flush(){for(Runnable r:new java.util.ArrayList<>(queue))r.run();queue.clear();}
        public void setOnClickListener(Click c){click=c;} public void tap(){if(click!=null)click.onClick(this);}
        public void setVisibility(int v){visibility=v;} }''',
    'android/view/Window.java': '''package android.view; public class Window {
        public View decor=new View(); public View getDecorView(){return decor;} }''',
    'android/app/Activity.java': '''package android.app; public class Activity extends android.content.Context {
        public void startActivity(android.content.Intent i){} public boolean finishing; public android.view.Window window=new android.view.Window();
        public boolean isFinishing(){return finishing;} public boolean isDestroyed(){return false;}
        public android.view.Window getWindow(){return window;} }''',
    'android/app/Dialog.java': '''package android.app; public class Dialog implements android.content.DialogInterface {
        public boolean showing; public Listener cancel,dismiss;
        public boolean isShowing(){return showing;} public void setOnCancelListener(Listener l){cancel=l;}
        public void setOnDismissListener(Listener l){dismiss=l;}
        public void dismiss(){showing=false;if(dismiss!=null)dismiss.onDialog(this);}
        public void cancel(){if(cancel!=null)cancel.onDialog(this);dismiss();} }''',
    'android/widget/TextView.java': '''package android.widget; public class TextView extends android.view.View {
        public String text; public TextView(android.content.Context c){}
        public void setText(String t){text=t;} public void setTextColor(int i){} public void setTextSize(int n){}
        public void setTypeface(android.graphics.Typeface t){} public void setPadding(int a,int b,int c,int d){}
        public void setLineSpacing(int a,float b){} public void setAccessibilityLiveRegion(int n){} }''',
    'android/widget/LinearLayout.java': '''package android.widget; public class LinearLayout extends android.view.View {
        public static int VERTICAL=1; public java.util.List<android.view.View> children=new java.util.ArrayList<>();
        public LinearLayout(android.content.Context c){} public void setOrientation(int n){}
        public void addView(android.view.View v){children.add(v);} }''',
    'com/ss/android/ugc/aweme/main/MainActivity.java': 'package com.ss.android.ugc.aweme.main; public class MainActivity extends android.app.Activity {}',
    'bk/soufit/ui/SettingsActivity.java': 'package bk.soufit.ui; public class SettingsActivity extends android.app.Activity {public void tutorialStep(int i){}}',
    'bk/soufit/ui/SettingsRow.java': 'package bk.soufit.ui; public class SettingsRow {public static String SETTINGS_ACTIVITY="settings";}',
    'bk/soufit/core/soufiT.java': 'package bk.soufit.core; public class soufiT {public static String PREFS="mod";}',
    'bk/soufit/theme/Skin.java': '''package bk.soufit;import bk.soufit.theme.Skin; class Skin {int text=-1;
        static Skin remembered(android.content.Context c){return new Skin();} }''',
    'bk/soufit/theme/Accent.java': 'package bk.soufit.theme; public class Accent {public static int colour(){return -1;}}',
    'bk/soufit/ui/Screen.java': '''package bk.soufit;import bk.soufit.ui.Screen; class Screen {
        static android.app.Activity current;static android.app.Activity now(){return current;} }''',
    'bk/soufit/ui/Motion.java': 'package bk.soufit.ui; public class Motion {public static void enter(android.view.View v,float d) {}}',
    'bk/soufit/core/Text.java': '''package bk.soufit;import bk.soufit.core.Text; class Text {
        static final String TUTORIAL_NEXT="Next",TUTORIAL_BACK="Back",TUTORIAL_DONE="Done",TUTORIAL_SKIP="Skip";
        static final String[] TUTORIAL_TITLES={"a","b","c","d","e"},TUTORIAL_WORDS={"a","b","c","d","e"}; }''',
    'bk/soufit/ui/Panel.java': '''package bk.soufit;import bk.soufit.ui.Panel;import bk.soufit.theme.Skin; class Panel {
        interface Click {void on();} static Panel last;static int created;
        android.app.Dialog dialog=new android.app.Dialog(); android.content.Context context;
        java.util.List<android.widget.TextView> buttons=new java.util.ArrayList<>();
        static Panel with(android.content.Context c,Skin s,String t){last=new Panel();last.context=c;created++;return last;}
        Panel view(android.view.View v){return this;}
        android.widget.TextView action(String label,boolean main){android.widget.TextView b=new android.widget.TextView(context);b.setText(label);buttons.add(b);return b;}
        Panel quiet(String label,Click c){action(label,false).setOnClickListener(v->{close();c.on();});return this;}
        android.app.Dialog dialog(){return dialog;} void show(){dialog.showing=true;} void close(){dialog.dismiss();} }''',
}
HARNESS = '''package bk.soufit;import bk.soufit.ui.Panel;import bk.soufit.ui.Screen;import bk.soufit.ui.SettingsActivity;import bk.soufit.ui.Tutorial;
public class TutorialHarness {
    static void check(boolean v,String s){if(!v)throw new AssertionError(s);}
    public static void main(String[] args) {
        android.app.Activity main=new SettingsActivity();Screen.current=main;
        Tutorial.maybeShow(main);Tutorial.maybeShow(main);main.window.decor.flush();
        check(Panel.created==1,"Only one startup tutorial");
        Panel sheet=Panel.last;check(sheet.buttons.get(1).visibility==8,"Back hidden on first step");
        sheet.buttons.get(0).tap();sheet.buttons.get(1).tap();
        check(sheet.buttons.get(1).visibility==8,"Back returns to first step");
        for(int i=0;i<4;i++)sheet.buttons.get(0).tap();
        check(!android.content.Context.prefs.done,"No completion until final action");
        sheet.buttons.get(0).tap();check(android.content.Context.prefs.done&&!sheet.dialog.showing,"Finish persisted");
        Tutorial.maybeShow(main);main.window.decor.flush();check(Panel.created==1,"No automatic replay after completion");
        Tutorial.show(main);check(Panel.created==2,"Manual replay remains available");
        Panel.last.dialog.cancel();
        android.content.Context.prefs.done=false;
        Tutorial.maybeShow(new android.app.Activity());check(Panel.created==2,"No login or splash tutorial");
        main.window.decor.focus=false;Tutorial.maybeShow(main);main.window.decor.flush();check(Panel.created==2,"No overlay while unfocused");
        main.window.decor.focus=true;Tutorial.maybeShow(main);main.window.decor.flush();
        Tutorial.paused(main);check(!Panel.last.dialog.showing&&!android.content.Context.prefs.done,"Pausing does not complete tutorial");
        Tutorial.maybeShow(main);main.window.decor.flush();Panel.last.buttons.get(2).tap();
        check(android.content.Context.prefs.done&&!Panel.last.dialog.showing,"Skip persists and closes");
        android.content.Context.prefs.done=false;int before=Panel.created;
        Tutorial.maybeShow(main);main.window.decor.flush();
        check(Panel.created==before+1,"Tutorial starts straight away on first run");
        Panel.last.dialog.cancel();
        android.content.Context.prefs.done=true;int seen=Panel.created;
        Tutorial.maybeShow(main);main.window.decor.flush();
        check(Panel.created==seen,"A seen tutorial is not repeated");
        System.out.println("First-run, completion, skip, replay and lifecycle scenarios passed");
    }
}'''


@unittest.skipUnless(shutil.which('javac') and shutil.which('java'), 'JDK required')
class TutorialRuntimeTest(unittest.TestCase):
    def test_first_run_skip_replay_and_pause(self):
        with tempfile.TemporaryDirectory() as directory:
            work = pathlib.Path(directory)
            sources = dict(STUBS)
            sources['bk/soufit/ui/Tutorial.java'] = (
                ROOT / 'inject/java/bk/soufit/ui/Tutorial.java').read_text(encoding='utf-8')
            sources['bk/soufit/TutorialHarness.java'] = HARNESS
            __import__("android_doubles").with_mod(sources)
            for name, content in sources.items():
                path = work / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(content, encoding='utf-8')
            compiled = subprocess.run(['javac', '-source', '8', '-target', '8', '-encoding', 'UTF-8', '-d', str(work)] +
                                      [str(work / name) for name in sources], capture_output=True, text=True)
            self.assertEqual(0, compiled.returncode, compiled.stderr)
            result = subprocess.run(['java', '-cp', str(work), 'bk.soufit.TutorialHarness'],
                                    capture_output=True, text=True)
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertIn('First-run, completion, skip, replay and lifecycle scenarios passed', result.stdout)

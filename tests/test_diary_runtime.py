from android_doubles import mod_file, mod_key
import pathlib, shutil, subprocess, tempfile, unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / 'inject/java/bk/soufit'

STUBS = {
'android/os/SystemClock.java': 'package android.os;public class SystemClock {public static long now=1000;public static long uptimeMillis(){return now;}}',
'android/os/Build.java': 'package android.os;public class Build {public static String MANUFACTURER="Test",MODEL="Phone";public static class VERSION {public static String RELEASE="14";public static int SDK_INT=34;}}',
'android/content/res/Resources.java': 'package android.content.res;public class Resources {public String getResourceEntryName(int id){return "res"+id;}}',
'android/content/Context.java': 'package android.content;public class Context {private final java.io.File dir;public Context(java.io.File d){dir=d;}public java.io.File getFilesDir(){return dir;}public android.content.res.Resources getResources(){return new android.content.res.Resources();}}',
'android/view/View.java': 'package android.view;public class View {public static final int VISIBLE=0,INVISIBLE=4,GONE=8;public int id=-1,visibility=0,left,top=0,width=10,height=10;public int getId(){return id;}public int getWidth(){return width;}public int getHeight(){return height;}public int getLeft(){return left;}public int getTop(){return top;}public int getVisibility(){return visibility;}public float alpha=1f;public float getAlpha(){return alpha;}public boolean post(Runnable r){r.run();return true;}}',
'android/view/ViewGroup.java': 'package android.view;import java.util.*;public class ViewGroup extends View {public ArrayList<View> kids=new ArrayList<View>();public int getChildCount(){return kids.size();}public View getChildAt(int i){return kids.get(i);}}',
'android/widget/TextView.java': 'package android.widget;public class TextView extends android.view.View {public CharSequence text="";public CharSequence getText(){return text;}}',
'android/view/Window.java': 'package android.view;public class Window {public View decor=new View();public View getDecorView(){return decor;}}',
'android/app/Activity.java': 'package android.app;public class Activity extends android.content.Context {public android.view.Window window=new android.view.Window();public Activity(java.io.File d){super(d);}public android.view.Window getWindow(){return window;}}',
'bk/soufit/core/soufiT.java': 'package bk.soufit.core;public class soufiT {public static android.content.Context ctx;public static android.content.Context context(){return ctx;}}',
'bk/soufit/core/DevMode.java': 'package bk.soufit.core;public class DevMode {public static boolean isOn(){return true;}}',
}

MAIN = r'''package bk.soufit;import bk.soufit.core.Diary;import bk.soufit.core.soufiT;
import java.util.*;import java.io.File;
import android.app.Activity;import android.view.View;import android.view.ViewGroup;import android.widget.TextView;
public class DiaryCheck {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static int count(List<String> lines,String part){int n=0;for(String l:lines)if(l.contains(part))n++;return n;}
 public static void main(String[] a) throws Exception {
  File dir=new File(a[0]);
  soufiT.ctx=new android.content.Context(dir);
  android.os.SystemClock.now=1000;
  Diary.note("hello diary");
  List<String> lines=Diary.lines();
  check(lines.get(0).startsWith("== soufiT "),"header first");
  check(count(lines,"hello diary")==1,"line kept once");
  Diary.settle();
  check(new File(new File(dir,"soufit"),"diary.log").isFile(),"written to the file");

  for(int i=0;i<20;i++)Diary.note("tick 5");
  check(count(Diary.lines(),"tick 5")==6,"a burst is written up to the limit");
  android.os.SystemClock.now+=61000;
  Diary.note("tick 9");
  lines=Diary.lines();
  check(count(lines,"14 more like")==1,"the left-out count is written when the minute ends");
  check(count(lines,"tick 9")==1,"the line after the minute is written");

  Diary.fail("boom",new RuntimeException("bad"));
  lines=Diary.lines();
  String boom=null;for(String l:lines)if(l.contains("boom: "))boom=l;
  check(boom!=null && boom.contains("java.lang.RuntimeException: bad") && boom.contains("["),"error has its place: "+boom);

  Activity act=new Activity(dir);
  ViewGroup root=new ViewGroup();root.id=1;
  TextView row=new TextView();row.id=5;row.text="Mod row";root.kids.add(row);
  act.getWindow().decor=root;
  Diary.layout(act);Diary.layout(act);
  lines=Diary.lines();
  check(count(lines,"layout of ")==1,"a screen is written once");
  row.alpha=0.5f;Diary.layout(new Activity(dir));
  check(count(lines,"TextView #res5")==1 && count(lines,"\"Mod row\"")==1,"its views and text are written");

  for(int i=0;i<5000;i++){StringBuilder s=new StringBuilder();int x=i;do{s.append((char)('a'+x%26));x/=26;}while(x>0);Diary.note("filler "+s+" "+"x".repeat(200));}
  Diary.settle();
  check(new File(new File(dir,"soufit"),"diary.log.1").isFile(),"the file rolls over past a megabyte");
  check(Diary.lines().size()<=3004,"what is shown is bounded (3000 lines plus a 4-line header)");

  Diary.clear();
  lines=Diary.lines();
  check(count(lines,"hello diary")==0 && count(lines,"filler")==0,"clear removes the diary");
  System.out.println("diary ok");
 }
}
'''


class DiaryRuntimeTest(unittest.TestCase):
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'), 'JDK required')
    def test_diary_keeps_bursts_errors_layouts_and_rolls_over(self):
        sources = dict(STUBS)
        sources['bk/soufit/core/Diary.java'] = (mod_file('Diary')).read_text(encoding='utf-8')
        sources['bk/soufit/core/Version.java'] = (mod_file('Version')).read_text(encoding='utf-8')
        sources['bk/soufit/DiaryCheck.java'] = MAIN
        with tempfile.TemporaryDirectory() as folder:
            out = pathlib.Path(folder) / 'classes'
            out.mkdir()
            files = []
            for name, source in sources.items():
                path = pathlib.Path(folder) / 'src' / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(source, encoding='utf-8')
                files.append(str(path))
            built = subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(out), *files],
                                   capture_output=True, text=True)
            self.assertEqual(0, built.returncode, built.stderr)
            run = subprocess.run(['java', '-cp', str(out), 'bk.soufit.DiaryCheck',
                                  str(pathlib.Path(folder) / 'phone')],
                                 capture_output=True, text=True, timeout=120)
            self.assertEqual(0, run.returncode, run.stdout + run.stderr)
            self.assertIn('diary ok', run.stdout)


if __name__ == '__main__':
    unittest.main()

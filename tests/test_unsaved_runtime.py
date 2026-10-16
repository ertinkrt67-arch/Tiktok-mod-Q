import pathlib, shutil, subprocess, tempfile, unittest
ROOT = pathlib.Path(__file__).resolve().parents[1]
STUBS = {
 'android/content/pm/ApplicationInfo.java': 'package android.content.pm;public class ApplicationInfo{public String dataDir="/nowhere";}',
 'android/content/SharedPreferences.java': 'package android.content;public interface SharedPreferences{java.util.Map<String,?> getAll();}',
 'android/content/Context.java': '''package android.content;public class Context{public static final int MODE_PRIVATE=0;
  public java.util.Map<String,Store> files=new java.util.HashMap<>();
  public static class Store implements SharedPreferences{public java.util.Map<String,Object> values=new java.util.HashMap<>();public java.util.Map<String,?> getAll(){return new java.util.HashMap<>(values);}}
  public android.content.pm.ApplicationInfo getApplicationInfo(){return new android.content.pm.ApplicationInfo();}
  public SharedPreferences getSharedPreferences(String name,int mode){Store s=files.get(name);if(s==null){s=new Store();files.put(name,s);}return s;}
  public Store of(String name){return (Store)getSharedPreferences(name,0);}}''',
 'bk/soufit/core/soufiT.java': 'package bk.soufit.core;public class soufiT{public static final String PREFS="soufit";}',
 'bk/soufit/core/Diary.java': 'package bk.soufit.core;public class Diary{public static void note(String s){throw new AssertionError(s);}}',
 'bk/soufit/profile/AccountAppearance.java': 'package bk.soufit.profile;import android.content.*;public class AccountAppearance{public static SharedPreferences prefs(Context c){return c.getSharedPreferences("look",0);}public static String scope(Context c){return "1";}}',
}
HARNESS = '''package bk.soufit;import bk.soufit.core.Unsaved;import android.content.Context;
public class UnsavedHarness{
 static void check(boolean ok,String what){if(!ok)throw new AssertionError(what);}
 public static void main(String[] a){
  Context c=new Context();java.util.Map<String,Object> main=c.of("soufit").values,look=c.of("look").values;
  main.put("ads",true);main.put("visits",3);look.put("accent",7);
  Unsaved.look(c);
  Unsaved.look(c);main.put("ads",false);check(Unsaved.after(c),"a changed switch owes a restart");
  Unsaved.look(c);main.put("ads",true);check(!Unsaved.after(c),"put back, it owes nothing");
  Unsaved.look(c);main.put("hdr",false);check(Unsaved.after(c),"first write of a switch");
  Unsaved.look(c);main.put("hdr",true);check(!Unsaved.after(c),"and back to what it showed");
  Unsaved.look(c);main.put("ads",false);check(Unsaved.after(c),"one");
  Unsaved.look(c);look.put("accent",9);check(Unsaved.after(c),"two");
  Unsaved.look(c);main.put("ads",true);check(Unsaved.after(c),"one back is not both");
  Unsaved.look(c);look.put("accent",7);check(!Unsaved.after(c),"both back");
  main.put("visits",4);
  Unsaved.look(c);main.put("ads",false);check(Unsaved.after(c),"changed");
  Unsaved.look(c);main.put("ads",true);check(!Unsaved.after(c),"a counter that moved by itself does not keep the bar up");
  Unsaved.look(c);main.put("fps",60);check(Unsaved.after(c),"new number");
  Unsaved.look(c);main.put("fps",30);check(Unsaved.after(c),"another number is not the start");
  Unsaved.look(c);main.remove("fps");check(!Unsaved.after(c),"gone again is the start");
  Unsaved.look(c);check(Unsaved.after(c),"nothing moved here, so it moved elsewhere");
  Unsaved.look(c);main.put("ads",false);Unsaved.after(c);
  Unsaved.look(c);main.put("ads",true);check(Unsaved.after(c),"and after that the bar stays");
  System.out.println("restart bookkeeping passed");
 }
}'''
class UnsavedRuntimeTest(unittest.TestCase):
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'), 'JDK required')
    def test_putting_things_back_takes_the_bar_away(self):
        with tempfile.TemporaryDirectory() as directory:
            work = pathlib.Path(directory); sources = dict(STUBS)
            sources['bk/soufit/core/Unsaved.java'] = (ROOT / 'inject/java/bk/soufit/core/Unsaved.java').read_text(encoding='utf-8')
            sources['bk/soufit/UnsavedHarness.java'] = HARNESS
            __import__("android_doubles").with_mod(sources)
            for name, content in sources.items():
                target = work / name; target.parent.mkdir(parents=True, exist_ok=True); target.write_text(content, encoding='utf-8')
            built = subprocess.run(['javac', '-source', '8', '-target', '8', '-encoding', 'UTF-8', '-d', str(work)] + [str(work / name) for name in sources], capture_output=True, text=True)
            self.assertEqual(0, built.returncode, built.stderr)
            run = subprocess.run(['java', '-cp', str(work), 'bk.soufit.UnsavedHarness'], capture_output=True, text=True)
            self.assertEqual(0, run.returncode, run.stderr)

if __name__ == '__main__':
    unittest.main()

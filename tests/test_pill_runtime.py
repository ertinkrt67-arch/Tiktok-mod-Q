from android_doubles import mod_file, mod_key
import pathlib, shutil, subprocess, tempfile, unittest
from test_screen_runtime import STUBS as SCREEN_STUBS
ROOT = pathlib.Path(__file__).resolve().parents[1]

HARNESS = '''package bk.soufit;import bk.soufit.ui.Screen;
import android.app.*;import android.view.*;import android.widget.*;
public class PillHarness {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void runNext(){android.os.Handler.delayed.remove(0).run();}
 static Dialog showing()throws Exception{java.lang.reflect.Field f=Screen.class.getDeclaredField("showing");f.setAccessible(true);return (Dialog)f.get(null);}
 public static void main(String[] args)throws Exception{
  Activity act=new Activity();Screen.at(act);
  final int[] ran=new int[3];
  android.os.Handler.delayed.clear();
  Screen.offer("Save",0.5f,new Runnable(){public void run(){ran[0]++;}});
  runNext();
  Dialog first=showing();
  check(first!=null&&first.showing,"the offer is shown");
  check(android.os.Handler.delayed.size()==1,"a safety timer is set for it");
  check(first.content instanceof TextView,"the offer is just the pill, with no close button beside it");

  android.os.Handler.delayed.clear();
  Screen.offer("Save",0.5f,new Runnable(){public void run(){ran[1]++;}});
  runNext();
  check(showing()==first && first.showing,"a repeat of the same offer keeps its window");

  first.content.click.onClick(first.content);
  check(ran[0]==0&&ran[1]==1,"tapping runs the newest action, once");
  check(!first.showing && showing()==null,"tapping takes the offer away");

  android.os.Handler.delayed.clear();
  Screen.offer("Other",0.5f,new Runnable(){public void run(){ran[2]++;}});
  runNext();
  Dialog second=showing();
  check(second!=first && second.showing,"a different offer gets its own window");
  check(android.os.Handler.delayed.size()==1,"and its own timer");
  android.os.Handler.delayed.remove(0).run();
  check(!second.showing && showing()==null,"an untouched offer leaves when its timer runs");
  check(ran[2]==0,"the timer does not run the action");
  System.out.println("pill checks passed");
 }
}
'''


class PillRuntimeTest(unittest.TestCase):
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'), 'JDK required')
    def test_pill_offer_repeats_taps_and_timeout(self):
        with tempfile.TemporaryDirectory() as directory:
            work = pathlib.Path(directory)
            sources = dict(SCREEN_STUBS)
            sources['android/view/View.java'] = sources['android/view/View.java'].replace('public void setPadding(int a,int b,int c,int d){}','public void setPadding(int a,int b,int c,int d){}public void setTranslationY(float y){}')
            sources['android/widget/TextView.java'] = sources['android/widget/TextView.java'].replace('public String text;','public String text;public void setMaxLines(int n){}')
            for name in ('Screen', 'ProgressJobs', 'DownloadMorph'):
                sources['bk/soufit/' + name + '.java'] = (
                    mod_file(name)).read_text(encoding='utf-8')
            sources['bk/soufit/PillHarness.java'] = HARNESS
            __import__("android_doubles").with_mod(sources)
            for name, content in sources.items():
                target = work / name
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(content, encoding='utf-8')
            built = subprocess.run(['javac', '-source', '8', '-target', '8', '-encoding', 'UTF-8', '-d', str(work)]
                                   + [str(work / name) for name in sources], capture_output=True, text=True)
            self.assertEqual(0, built.returncode, built.stderr)
            run = subprocess.run(['java', '-cp', str(work), 'bk.soufit.PillHarness'],
                                 capture_output=True, text=True)
            self.assertEqual(0, run.returncode, run.stdout + run.stderr)
            self.assertIn('pill checks passed', run.stdout)


if __name__ == '__main__':
    unittest.main()

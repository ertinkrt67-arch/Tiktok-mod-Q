from android_doubles import mod_file, mod_key
import pathlib,shutil,subprocess,tempfile,unittest
import test_account_appearance_runtime as base
ROOT=pathlib.Path(__file__).resolve().parents[1]
def method(file,signature):
 text=mod_file(file[:-5]).read_text(encoding='utf-8');a=text.index(signature);b=text.index('{',a);depth=1;i=b+1
 while depth:
  if text[i]=='{':depth+=1
  if text[i]=='}':depth-=1
  i+=1
 return text[a:i]
class ScheduleTest(unittest.TestCase):
 @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK required')
 def test_chosen_minute_is_the_moment_the_alarm_fires(self):
  sources=dict(base.STUBS)
  sources['bk/soufit/social/StreakSchedule.java']='package bk.soufit.social;import java.util.Calendar;public class StreakSchedule{'
  sources['bk/soufit/social/StreakSchedule.java']+=('static final String KEY="streak_send_minute";'
   +method('StreakSchedule.java','static int minute()').replace('AccountAppearance.prefs(soufiT.context())','soufiT.prefs()')
   +method('StreakSchedule.java','static boolean due(')
   +method('StreakSchedule.java','static long delay(')
   +method('StreakSchedule.java','static long next(')
   +method('StreakSchedule.java','static boolean sameDay(')
   +method('StreakSchedule.java','static int parse(')
   +method('StreakSchedule.java','static void set(').replace('static void set(int minute)','static void store(int minute)').replace('AccountAppearance.prefs(soufiT.context())','soufiT.prefs()').replace('android.content.Context c=soufiT.context();if(c!=null){StreakAlarm.schedule(c,false);Streaks.round(c);}','')
   +'}')
  sources['bk/soufit/core/soufiT.java']='package bk.soufit.core;public class soufiT{public static String PREFS="ttcuz";public static android.content.Context ctx;public static android.content.Context context(){return ctx;}public static android.content.SharedPreferences p;public static android.content.SharedPreferences prefs(){if(p==null)p=ctx.getSharedPreferences(PREFS,0);return p;}}'
  sources['bk/soufit/Test.java']=HARNESS
  with tempfile.TemporaryDirectory() as directory:
   work=pathlib.Path(directory)
   for name,source in sources.items():p=work/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(source,encoding='utf-8')
   result=subprocess.run(['javac','-encoding','UTF-8','-source','8','-target','8','-d',str(work)]+[str(p) for p in work.rglob('*.java')],capture_output=True,text=True)
   self.assertEqual(0,result.returncode,result.stderr)
   result=subprocess.run(['java','-cp',str(work),'bk.soufit.Test'],capture_output=True,text=True)
   self.assertEqual(0,result.returncode,result.stderr+result.stdout)
HARNESS=r'''package bk.soufit;import bk.soufit.social.StreakSchedule;import bk.soufit.core.soufiT;import java.util.*;
public class Test{
 static void check(boolean ok,String what){if(!ok)throw new AssertionError(what);}
 static long at(int year,int month,int day,int hour,int minute){
  Calendar c=Calendar.getInstance();
  c.set(year,month-1,day,hour,minute,0);c.set(Calendar.MILLISECOND,0);
  return c.getTimeInMillis();
 }
 public static void main(String[] args)throws Exception{
  android.content.Context context=new android.content.Context();
  soufiT.ctx=context;
  long cadence=15*60*1000L;

  StreakSchedule.store(20*60+30);
  check(StreakSchedule.minute()==1230,"the chosen minute is remembered");
  long now=at(2026,10,6,18,0);
  check(StreakSchedule.next(now,1230,false,cadence)==at(2026,10,6,20,30),"today's time is used before it passes");
  check(!StreakSchedule.due(now,1230),"18:00 is not yet due for a 20:30 send");

  check(StreakSchedule.due(at(2026,10,6,20,30),1230),"the chosen minute is due");
  check(StreakSchedule.due(at(2026,10,6,20,31),1230),"a minute past is still due");

  check(StreakSchedule.next(at(2026,10,6,21,0),1230,false,cadence)==at(2026,10,7,20,30),"after it passes, tomorrow");
  check(StreakSchedule.next(at(2026,10,6,21,0),1230,true,cadence)==at(2026,10,6,21,5),"a pending send retries in five minutes");
  check(StreakSchedule.next(at(2026,10,6,23,30),1230,true,cadence)==at(2026,10,7,20,30),"the retry window closes after three hours");

  StreakSchedule.store(-1);
  check(StreakSchedule.minute()==-1,"no schedule reads as none");
  check(StreakSchedule.next(now,-1,false,cadence)==now+cadence,"without a schedule the cadence stands");
  check(StreakSchedule.due(now,-1),"without a schedule nothing is held back for a time");

  StreakSchedule.store(0);
  check(StreakSchedule.next(at(2026,10,6,23,0),0,false,cadence)==at(2026,10,7,0,0),"00:00 from 23:00 is tomorrow");
  check(StreakSchedule.next(at(2026,10,6,0,0),0,false,cadence)==at(2026,10,7,0,0),"00:00 at 00:00 is the next day, not now");
  check(StreakSchedule.due(at(2026,10,7,0,0),0),"00:00 is due at midnight");

  StreakSchedule.store(20*60+30);
  check(StreakSchedule.delay(at(2026,10,6,20,29),cadence)==60000,"a minute before, the wait is a minute");
  check(StreakSchedule.delay(at(2026,10,6,20,29),30000L)==30000L,"the cadence caps a long wait");
  check(StreakSchedule.delay(at(2026,10,6,21,0),cadence)==cadence,"past the minute, the cadence");

  check(StreakSchedule.parse("20:30")==1230,"a typed time is read");
  check(StreakSchedule.parse("7:05")==425,"a typed time with one digit");
  check(StreakSchedule.parse("24:00")==-2,"an impossible hour is refused");
  check(StreakSchedule.parse("20:60")==-2,"an impossible minute is refused");
  check(StreakSchedule.parse("nonsense")==-2,"nonsense is refused");
  check(StreakSchedule.parse("")==-2,"empty is refused");
 }
}
'''

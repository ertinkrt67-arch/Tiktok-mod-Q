package bk.soufit.feed;
import bk.soufit.core.Diary;
import bk.soufit.ui.Screen;
import bk.soufit.core.Text;
import bk.soufit.core.soufiT;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
public final class LoginGate {
    private LoginGate() {} public static final String KEY = "skip_login_gate";
    public static final String KEY_DOB = "skip_login_dob";
    private static volatile int cached;
    private static volatile int cachedDob;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Random RNG = new Random();
    private static final int SCAN_DELAY_MS = 420;
    private static final String[] GATE_ACTIVITY_PATTERNS = {"ForceLoginActivity", "ForceLandActivity", "ForceSignActivity",
            "LoginConfirmActivity", "login.ForceLogin", "login.ForceLand", "authorize.AuthorizeActivity",};
    private static final String[] SOFT_LOGIN_PATTERNS = {"LoginContainerActivity", "LoginEntryActivity", "LoginSwitchActivity",
            "aweme.login", "musically.login", "LoginMainActivity", "LoginActivity", "SignUpOrLoginActivity", "AccountAuthActivity",
            "I18nSignUpActivity",};
    private static final String[] BIRTHDAY_PATTERNS = {"BirthdayActivity", "AgeGateActivity", "BirthdayInputActivity", "SignUpBirthdayActivity",
            "RegisterBirthdayActivity", "birthday.Birthday", "birthday.AgeGate", "AgeVerifyActivity",};
    private static final String PERMISSION_ACTIVITY = "GrantPermissionsActivity";
    private static final String JOURNEY_ACTIVITY = "NewUserJourneyActivity";
    private static final String[] GATE_FRAGMENT_PATTERNS = {"ForceLoginFragment", "ForceLandFragment", "ForceSignFragment",
            "LoginDialogFragment", "LoginConfirmFragment", "ForceLoginBottomSheet", "login.Force",};
    private static final String[] AGREE_LABELS = {"agree and continue", "agree & continue", "i agree", "accept and continue",
            "accept & continue", "accept all", "agree", "akzeptieren", "accepter", "aceptar", "accetta", "concordo", "принять",
            "согласен", "同意并继续", "同意",};
    private static final String[] GENDER_SCREEN_SIGNALS = {"how do you identify", "how do you identify?", "gender", "tell us about yourself",
            "i identify as", "wie identifizierst du dich", "comment vous identifiez",};
    private static final String[] PREFER_NOT_LABELS = {"prefer not to say", "prefer not to answer", "rather not say", "not specified",
            "nicht angeben", "ne pas préciser", "prefiero no decir", "preferisco non", "não informar", "не указывать", "不透露",
            };
    private static final String[] INTERESTS_SCREEN_SIGNALS = {"what do you like", "what do you enjoy", "choose your interests",
            "select your interests", "pick your interests", "topics you like", "comedy", "entertainment",};
    private static final String[] DENY_LABELS = {"don't allow", "dont allow", "deny", "not now", "no thanks", "skip", "nicht zulassen",
            "ne pas autoriser", "no permitir", "non consentire", "não permitir", "запретить", "不允许",};
    private static final String[] NEXT_LABELS = {"next", "continue", "confirm", "done", "ok", "weiter", "suivant", "siguiente",
            "avanti", "далее", "продолжить", "下一步", "继续",};
    public static boolean isEnabled() {
        int k = cached;
        if (k != 0) return k == 2;
        boolean on = pref(KEY, true);
        cached = on ? 2 : 1;
        return on;
    }

    public static void setEnabled(boolean on) {
        cached = on ? 2 : 1;
        setPref(KEY, on);
    }

    public static boolean isDobEnabled() {
        int k = cachedDob;
        if (k != 0) return k == 2;
        boolean on = pref(KEY_DOB, true);
        cachedDob = on ? 2 : 1;
        return on;
    }

    public static void setDobEnabled(boolean on) {
        cachedDob = on ? 2 : 1;
        setPref(KEY_DOB, on);
    }

    public static void onActivityResumed(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        String name = activity.getClass().getName();
        if (isEnabled() && matches(name, GATE_ACTIVITY_PATTERNS)) {
            Diary.note("login gate: finishing " + simple(name));
            activity.finish();
            return;
        }
        if (isEnabled() && name.contains(PERMISSION_ACTIVITY)) {
            Diary.note("login gate: permission dialog — denying");
            MAIN.postDelayed(() -> autoDenyPermission(activity), SCAN_DELAY_MS);
            return;
        }
        if (isEnabled() && name.contains(JOURNEY_ACTIVITY)) {
            MAIN.postDelayed(() -> handleJourneyStep(activity), SCAN_DELAY_MS);
            return;
        }
        if (isDobEnabled() && matches(name, BIRTHDAY_PATTERNS)) {
            Diary.note("login gate: legacy birthday activity");
            MAIN.postDelayed(() -> {
                ViewGroup root = root(activity);
                if (root != null) {
                    fillBirthday(activity, root);
                }
            }, SCAN_DELAY_MS);
            return;
        }
        if (isEnabled() && matches(name, SOFT_LOGIN_PATTERNS)) {
            Diary.note("login gate: skip pill on " + simple(name));
            Screen.offer(Text.SKIP_LOGIN_PILL, 0.88f, () -> activity.finish());
        }
        if (isEnabled()) {
            MAIN.post(() -> {
                if (!isEnabled()) return;
                try {
                    dismissGateFragments(activity);
                } catch (Throwable e) {
                    Diary.note("login gate fragment: " + e);
                }
            });
        }
    }

    private static void handleJourneyStep(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        ViewGroup root = root(activity);
        if (root == null) return;
        List<String> texts = collectVisibleTexts(root);
        String joined = join(texts).toLowerCase();
        Diary.note("login gate: journey texts = " + joined.substring(0, Math.min(joined.length(), 200)));
        if (containsAny(joined, INTERESTS_SCREEN_SIGNALS)) {
            Diary.note("login gate: interests screen — leaving alone");
            return;
        }
        if (isDobEnabled() && hasBirthdayWidget(root)) {
            Diary.note("login gate: birthday widget detected — auto-filling");
            fillBirthday(activity, root);
            return;
        }
        if (containsAny(joined, GENDER_SCREEN_SIGNALS)) {
            Diary.note("login gate: gender screen — clicking prefer not to say");
            autoSelectGender(root);
            return;
        }
        if (containsAny(joined, AGREE_LABELS)) {
            Diary.note("login gate: ToS screen — auto-agreeing");
            autoAgree(root);
            return;
        }
        Diary.note("login gate: unknown journey step — offering skip pill");
        Screen.offer(Text.SKIP_LOGIN_PILL, 0.88f, () -> activity.finish());
    }

    private static void autoAgree(ViewGroup root) {
        TextView btn = findButtonByLabels(root, AGREE_LABELS);
        if (btn != null) {
            Diary.note("login gate: tapping agree: " + btn.getText());
            btn.performClick();
        }
    }

    private static void fillBirthday(Activity activity, ViewGroup root) {
        try {
            Calendar lo = Calendar.getInstance();
            lo.set(1990, Calendar.JANUARY, 1);
            Calendar hi = Calendar.getInstance();
            hi.set(2004, Calendar.JANUARY, 1);
            long range = (hi.getTimeInMillis() - lo.getTimeInMillis()) / 86400000L;
            long offset = (long)(RNG.nextDouble() * range);
            Calendar dob = Calendar.getInstance();
            dob.setTimeInMillis(lo.getTimeInMillis() + offset * 86400000L);
            int year = dob.get(Calendar.YEAR);
            int month = dob.get(Calendar.MONTH);
            int day = dob.get(Calendar.DAY_OF_MONTH);
            Diary.note("login gate: DOB " + year + "-" + (month + 1) + "-" + day);
            DatePicker dp = findView(root, DatePicker.class);
            if (dp != null) {
                dp.updateDate(year, month, day);
            } else {
                List<NumberPicker> pickers = findAll(root, NumberPicker.class);
                NumberPicker yearPicker = null, monthPicker = null, dayPicker = null;
                for (NumberPicker p : pickers) {
                    if (p.getVisibility() != View.VISIBLE) continue;
                    int min = p.getMinValue(), max = p.getMaxValue();
                    if (max - min >= 50 && min >= 1900 && max <= 2030) {
                        yearPicker = p;
                    } else if (max - min >= 10 && min <= 1 && max <= 31) {
                        if (max <= 12) monthPicker = p;
                        else dayPicker = p;
                    }
                }
                if (yearPicker != null) yearPicker.setValue(year);
                if (monthPicker != null) monthPicker.setValue(Math.min(month + 1, monthPicker.getMaxValue()));
                if (dayPicker != null) dayPicker.setValue(Math.min(day, dayPicker.getMaxValue()));
            }
            MAIN.postDelayed(() -> clickNext(root), 150);
        } catch (Throwable e) {
            Diary.note("login gate: DOB fill failed: " + e);
        }
    }

    private static boolean hasBirthdayWidget(ViewGroup root) {
        if (findView(root, DatePicker.class) != null) return true;
        List<NumberPicker> pickers = findAll(root, NumberPicker.class);
        int visible = 0;
        for (NumberPicker p : pickers) {
            if (p.getVisibility() == View.VISIBLE) visible ++;
        }
        return visible >= 3;
    }

    private static void autoSelectGender(ViewGroup root) {
        TextView pref = findButtonByLabels(root, PREFER_NOT_LABELS);
        if (pref != null) {
            Diary.note("login gate: tapping prefer not to say");
            pref.performClick();
            MAIN.postDelayed(() -> clickNext(root), 150);
        } else {
            clickNext(root);
        }
    }

    private static void autoDenyPermission(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        ViewGroup root = root(activity);
        if (root == null) return;
        TextView deny = findButtonByLabels(root, DENY_LABELS);
        if (deny != null) {
            Diary.note("login gate: denying permission: " + deny.getText());
            deny.performClick();
        }
    }

    private static void clickNext(ViewGroup root) {
        try {
            TextView btn = findButtonByLabels(root, NEXT_LABELS);
            if (btn != null) {
                Diary.note("login gate: clicking next: " + btn.getText());
                btn.performClick();
            }
        } catch (Throwable e) {
            Diary.note("login gate: next click: " + e);
        }
    }

    private static TextView findButtonByLabels(ViewGroup root, String[] labels) {
        java.util.ArrayDeque<View> queue = new java.util.ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            View v = queue.poll();
            if (v instanceof TextView && v.isClickable() && v.getVisibility() == View.VISIBLE) {
                String text = ((TextView) v).getText().toString().trim().toLowerCase();
                for (String label : labels) {
                    if (text.equals(label) || text.contains(label)) {
                        return (TextView) v;
                    }
                }
            }
            if (v instanceof ViewGroup) {
                ViewGroup g = (ViewGroup) v;
                for (int i = 0; i < g.getChildCount(); i ++) queue.add(g.getChildAt(i));
            }
        }
        return null;
    }

    private static List<String> collectVisibleTexts(ViewGroup root) {
        List<String> out = new ArrayList<>();
        collectTextsInto(root, out);
        return out;
    }

    private static void collectTextsInto(ViewGroup root, List<String> out) {
        for (int i = 0; i < root.getChildCount(); i ++) {
            View child = root.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            if (child instanceof TextView) {
                String t = ((TextView) child).getText().toString().trim();
                if (!t.isEmpty()) out.add(t);
            }
            if (child instanceof ViewGroup) collectTextsInto((ViewGroup) child, out);
        }
    }

    @SuppressWarnings("unchecked") private static <T extends View> T findView(ViewGroup root, Class<T> type) {
        for (int i = 0; i < root.getChildCount(); i ++) {
            View child = root.getChildAt(i);
            if (type.isInstance(child)) return (T) child;
            if (child instanceof ViewGroup) {
                T found = findView((ViewGroup) child, type);
                if (found != null) return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked") private static <T extends View> List<T> findAll(ViewGroup root, Class<T> type) {
        List<T> out = new ArrayList<>();
        findAllInto(root, type, out);
        return out;
    }

    @SuppressWarnings("unchecked") private static <T extends View> void findAllInto(ViewGroup root, Class<T> type, List<T> out) {
        for (int i = 0; i < root.getChildCount(); i ++) {
            View child = root.getChildAt(i);
            if (type.isInstance(child)) out.add((T) child);
            if (child instanceof ViewGroup) findAllInto((ViewGroup) child, type, out);
        }
    }

    private static ViewGroup root(Activity activity) {
        try {
            return (ViewGroup) activity.getWindow().getDecorView();
        } catch (Throwable e) {
            return null;
        }
    }

    private static void dismissGateFragments(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        try {
            FragmentManager fm = activity.getFragmentManager();
            if (popGateFragment(fm)) return;
        } catch (Throwable ignored) {} try {
            Method get = activity.getClass().getMethod("getSupportFragmentManager");
            Object sfm = get.invoke(activity);
            if (sfm == null) return;
            Method getFragments = sfm.getClass().getMethod("getFragments");
            Object result = getFragments.invoke(sfm);
            if (!(result instanceof List)) return;
            for (Object frag : (List<?>) result) {
                if (frag == null) continue;
                if (matches(frag.getClass().getName(), GATE_FRAGMENT_PATTERNS)) {
                    Diary.note("login gate: dismissing fragment " + simple(frag.getClass().getName()));
                    tryDismiss(frag);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }

    private static boolean popGateFragment(FragmentManager fm) {
        if (fm == null) return false;
        try {
            List<Fragment> fragments = fm.getFragments();
            if (fragments == null) return false;
            for (Fragment frag : fragments) {
                if (frag == null) continue;
                if (matches(frag.getClass().getName(), GATE_FRAGMENT_PATTERNS)) {
                    Diary.note("login gate: popping fragment " + simple(frag.getClass().getName()));
                    fm.beginTransaction().remove(frag).commitAllowingStateLoss();
                    return true;
                }
            }
        } catch (Throwable ignored) {} return false;
    }

    private static void tryDismiss(Object frag) {
        try {
            frag.getClass().getMethod("dismissAllowingStateLoss").invoke(frag);
            return;
        } catch (Throwable ignored) {} try {
            frag.getClass().getMethod("dismiss").invoke(frag);
        } catch (Throwable ignored) {}
    }

    private static boolean containsAny(String haystack, String[] needles) {
        for (String n : needles) if (haystack.contains(n)) return true;
        return false;
    }

    private static String join(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s.toLowerCase()).append(' ');
        return sb.toString();
    }

    private static boolean pref(String key, boolean def) {
        Context context = soufiT.context();
        if (context == null) return def;
        try {
            return context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).getBoolean(key, def);
        } catch (Throwable e) {
            return def;
        }
    }

    private static void setPref(String key, boolean on) {
        Context context = soufiT.context();
        if (context == null) return;
        try {
            context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE).edit().putBoolean(key, on).apply();
        } catch (Throwable ignored) {}
    }

    private static boolean matches(String name, String[] patterns) {
        if (name == null) return false;
        for (String p : patterns) if (name.contains(p)) return true;
        return false;
    }

    private static String simple(String name) {
        if (name == null) return "?";
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(dot + 1);
    }
}

package bk.soufit.profile;
import bk.soufit.core.Diary;
import bk.soufit.core.Net;
import bk.soufit.core.Text;
import bk.soufit.core.soufiT;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
public final class Proof {
    private Proof() {} private static final String KEY_PROVED = "proved_uid";
    private static final String KEY_CODE = "proof_code";
    private static final String KEY_HOLDER = "proof_holder";
    private static final String KEY_FOR = "proof_for";
    private static volatile String code = "";
    private static volatile String holder = "";
    private static volatile boolean needsName;
    public static boolean wantsName() {
        return needsName;
    }

    public interface Then {
        void then(boolean ok, String trouble);
    }

    public static boolean proved() {
        String uid = Account.id();
        if (uid == null || uid.length() == 0) return false;
        SharedPreferences prefs = prefs();
        if (prefs == null) return false;
        return uid.equals(prefs.getString(KEY_PROVED, "")) && Mine.token().length() > 0;
    }

    public static void heard(boolean isProved) {
        if (isProved) return;
        forget();
    }

    public static void lost() {
        forget();
    }

    private static void forget() {
        SharedPreferences prefs = prefs();
        if (prefs != null) prefs.edit().remove(KEY_PROVED).apply();
    }

    public static String waiting() {
        if (code.length() == 0) remember();
        return code;
    }

    private static synchronized void remember() {
        if (code.length() > 0) return;
        String uid = Account.id();
        SharedPreferences prefs = prefs();
        if (uid == null || prefs == null) return;
        if (!uid.equals(prefs.getString(KEY_FOR, ""))) return;
        code = prefs.getString(KEY_CODE, "");
        holder = prefs.getString(KEY_HOLDER, "");
    }

    private static void keep() {
        String uid = Account.id();
        SharedPreferences prefs = prefs();
        if (uid == null || prefs == null) return;
        prefs.edit().putString(KEY_FOR, uid).putString(KEY_CODE, code).putString(KEY_HOLDER, holder).apply();
    }

    public static void want(final Then then) {
        final String uid = Account.id();
        if (uid == null || uid.length() == 0) {
            answer(then, false, Text.PROVE_NO_ACCOUNT);
            return;
        }
        remember();
        Net.away("proof: code", new Runnable() {
            @Override public void run() {
                Net.Said said = Net.talk(Badges.base() + "/prove", json("uid", uid, "holder", holder));
                if (!said.ok()) {
                    answer(then, false, trouble(said));
                    return;
                }
                try {
                    JSONObject told = new JSONObject(said.body);
                    code = told.optString("code", "");
                    String key = told.optString("holder", "");
                    if (key.length() > 0) holder = key;
                    keep();
                } catch (Throwable error) {
                    Diary.note("proof: " + error);
                }
                answer(then, code.length() > 0, code.length() > 0 ? "" : Text.PROVE_FAILED);
            }
        });
    }

    public static void check(final String name, final Then then) {
        final String uid = Account.id();
        if (uid == null || uid.length() == 0) {
            answer(then, false, Text.PROVE_NO_ACCOUNT);
            return;
        }
        final String asked = clean(name);
        remember();
        Net.away("proof: check", new Runnable() {
            @Override public void run() {
                Net.Said said = asked.length() > 0 ? Net.talk(Badges.base() + "/prove/check", json("uid", uid, "holder", holder,
                        "name", asked)) : Net.talk(Badges.base() + "/prove/check", json("uid", uid, "holder", holder));
                if (!said.ok()) {
                    mint(uid);
                    answer(then, false, trouble(said));
                    return;
                }
                boolean ok = false;
                try {
                    JSONObject told = new JSONObject(said.body);
                    String token = told.optString("token", "");
                    if (token.length() > 0) {
                        Mine.gotKey(uid, token);
                        SharedPreferences prefs = prefs();
                        if (prefs != null) {
                            prefs.edit().putString(KEY_PROVED, uid).apply();
                        }
                        Mine.read(told);
                        code = "";
                        holder = "";
                        needsName = false;
                        keep();
                        ok = true;
                    }
                } catch (Throwable error) {
                    Diary.note("proof: " + error);
                }
                answer(then, ok, ok ? "" : Text.PROVE_FAILED);
            }
        });
    }

    public static void fresh(final Then then) {
        final String uid = Account.id();
        if (uid == null || uid.length() == 0) {
            answer(then, false, Text.PROVE_NO_ACCOUNT);
            return;
        }
        Net.away("proof: fresh", new Runnable() {
            @Override public void run() {
                try {
                    Net.Said said = Net.talk(Badges.base() + "/prove", new JSONObject().put("uid", uid).put("holder", holder)
                            .put("fresh", true).toString());
                    if (!said.ok()) {
                        answer(then, false, trouble(said));
                        return;
                    }
                    JSONObject told = new JSONObject(said.body);
                    String made = told.optString("code", "");
                    String key = told.optString("holder", "");
                    if (made.length() > 0) code = made;
                    if (key.length() > 0) holder = key;
                    keep();
                    needsName = false;
                    answer(then, made.length() > 0, made.length() > 0 ? "" : Text.PROVE_FAILED);
                } catch (Throwable error) {
                    Diary.note("proof: " + error);
                    answer(then, false, Text.PROVE_FAILED);
                }
            }
        });
    }

    private static void mint(String uid) {
        try {
            Net.Said said = Net.talk(Badges.base() + "/prove", json("uid", uid, "holder", holder));
            if (!said.ok()) return;
            JSONObject told = new JSONObject(said.body);
            String fresh = told.optString("code", "");
            if (fresh.length() > 0) code = fresh;
            String key = told.optString("holder", "");
            if (key.length() > 0) holder = key;
            keep();
        } catch (Throwable error) {
            Diary.note("proof: " + error);
        }
    }

    public static String clean(String name) {
        if (name == null) return "";
        String out = name.trim();
        int slash = out.lastIndexOf('/');
        if (slash >= 0) out = out.substring(slash + 1);
        while (out.startsWith("@")) out = out.substring(1);
        int query = out.indexOf('?');
        if (query >= 0) out = out.substring(0, query);
        return out.trim();
    }

    private static String trouble(Net.Said said) {
        String what = "";
        try {
            if (said.body.length() > 0) {
                what = new JSONObject(said.body).optString("error", "");
            }
        } catch (Throwable ignored) {} needsName = "tell me the name".equals(what);
        if (needsName) return Text.PROVE_NEED_NAME;
        if ("that is not the code's owner".equals(what)) return Text.PROVE_NOT_YOURS;
        if (said.code == 0) return Text.PROVE_NO_SERVER;
        if ("the code is not in that profile yet".equals(what)) return Text.PROVE_NOT_THERE;
        if ("that name belongs to another account".equals(what)) return Text.PROVE_OTHER_NAME;
        if ("tiktok did not answer".equals(what)) return Text.PROVE_NO_TIKTOK;
        if (said.code == 410) return Text.PROVE_STALE;
        if (said.code == 429) return Text.PROVE_TOO_OFTEN;
        return Text.PROVE_FAILED;
    }

    private static String json(String ... pairs) {
        try {
            JSONObject out = new JSONObject();
            for (int i = 0; i + 1 < pairs.length; i += 2) out.put(pairs[i], pairs[i + 1]);
            return out.toString();
        } catch (Throwable error) {
            return "{}";
        }
    }

    private static void answer(final Then then, final boolean ok, final String trouble) {
        if (then == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() {
                then.then(ok, trouble);
            }
        });
    }

    private static SharedPreferences prefs() {
        Context context = soufiT.context();
        if (context == null) return null;
        try {
            return context.getSharedPreferences(soufiT.PREFS, Context.MODE_PRIVATE);
        } catch (Throwable ignored) {
            return null;
        }
    }
}

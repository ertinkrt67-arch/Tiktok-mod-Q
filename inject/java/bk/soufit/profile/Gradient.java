package bk.soufit.profile;
import bk.soufit.core.Diary;
import bk.soufit.core.Net;
import bk.soufit.core.Text;
import android.graphics.Color;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public final class Gradient {
    public static final int FEWEST = 2;
    public static final int MOST = 5;
    private static final int[] SUGGESTED = {0xFFFF6FA5, 0xFF7B61FF};
    private Gradient() {} public interface Said {
        void said(boolean ok, String trouble);
    }

    public static int[] mine() {
        String uid = Account.id();
        int[] have = uid == null ? null : Looks.of(uid);
        return have == null ? null : have.clone();
    }

    public static List<Integer> starting() {
        List<Integer> out = new ArrayList<Integer>();
        int[] have = mine();
        if (have != null) {
            for (int colour : have) out.add(colour);
        } else {
            for (int colour : SUGGESTED) out.add(colour);
        }
        return out;
    }

    public static void save(final List<Integer> colours, final Said then) {
        final String uid = Account.id();
        final String token = Mine.token();
        if (uid == null || token.length() == 0) {
            if (then != null) then.said(false, Mine.PROVE);
            return;
        }
        Net.away("gradient: save", new Runnable() {
            @Override public void run() {
                boolean ok = false;
                String trouble = "";
                try {
                    JSONArray list = new JSONArray();
                    for (Integer colour : colours) list.put(hex(colour));
                    Net.Said said = Net.talk(Badges.base() + "/gradient", new JSONObject().put("uid", uid).put("token", token)
                            .put("colours", list).toString());
                    if (said.ok()) {
                        ok = true;
                        Badges.refresh();
                    } else {
                        trouble = Mine.asksForProof(said) ? Mine.PROVE : Text.GRADIENT_REFUSED;
                    }
                } catch (Throwable error) {
                    trouble = String.valueOf(error);
                    Diary.note("gradient: " + error);
                }
                answer(then, ok, trouble);
            }
        });
    }

    public static void drop(final Said then) {
        final String uid = Account.id();
        final String token = Mine.token();
        if (uid == null || token.length() == 0) {
            if (then != null) then.said(false, Mine.PROVE);
            return;
        }
        Net.away("gradient: drop", new Runnable() {
            @Override public void run() {
                boolean ok = false;
                String trouble = Text.GRADIENT_REFUSED;
                try {
                    Net.Said said = Net.talk(Badges.base() + "/gradient", new JSONObject().put("uid", uid).put("token", token)
                            .toString());
                    ok = said.ok();
                    if (ok) Badges.refresh();
                    else if (Mine.asksForProof(said)) trouble = Mine.PROVE;
                } catch (Throwable error) {
                    Diary.note("gradient: " + error);
                }
                answer(then, ok, ok ? "" : trouble);
            }
        });
    }

    public static String hex(int colour) {
        return String.format("%06X", colour & 0xFFFFFF);
    }

    public static int from(int hue, int saturation, int value) {
        return Color.HSVToColor(new float[] {hue % 360, saturation / 100f, value / 100f});
    }

    public static int[] hsv(int colour) {
        float[] out = new float[3];
        Color.colorToHSV(colour, out);
        return new int[] { (int) out[0], Math.round(out[1] * 100), Math.round(out[2] * 100)};
    }

    private static void answer(final Said then, final boolean ok, final String trouble) {
        if (then == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
            @Override public void run() {
                then.said(ok, trouble);
            }
        });
    }
}

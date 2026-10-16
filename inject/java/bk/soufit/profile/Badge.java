package bk.soufit.profile;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import bk.soufit.badges.render.NicknameRenderer;
import bk.soufit.core.Probe;
import bk.soufit.plugins.Plugins;
import bk.soufit.theme.Fonts;
import com.ss.android.ugc.aweme.profile.model.User;
import com.ss.android.ugc.profile.platform.base.data.UserProfileInfo;

public final class Badge {
    private static final int DEEP = 40;
    private static final int BUDGET = 4000;
    private static final long RECENT = 3000;
    private static volatile String lastName;
    private static volatile String lastUid;
    private static volatile long lastAt;

    private Badge() {}

    public static String getNickname(User user) {
        if (user == null) return null;
        String name = user.getNickname();
        try {
            return marked(name, user.getUid(), false);
        } catch (Throwable ignored) {
            return name;
        }
    }

    public static String getNickname(UserProfileInfo user) {
        if (user == null) return null;
        String name = user.getNickname();
        try {
            return marked(name, user.getUid(), true);
        } catch (Throwable ignored) {
            return name;
        }
    }

    public static String getDisplayName(Object contact) {
        return chatName(contact, "getDisplayName");
    }

    public static String getNickName(Object contact) {
        return chatName(contact, "getNickName");
    }

    private static String chatName(Object contact, String method) {
        if (contact == null) return null;
        String name = null;
        try {
            Object said = contact.getClass().getMethod(method).invoke(contact);
            name = said instanceof String ? (String) said : null;
            Object uid = contact.getClass().getMethod("getUid").invoke(contact);
            if (name == null || !(uid instanceof String)) return name;
            String id = (String) uid;
            return id.matches("[0-9]{1,24}") ? marked(name, id, false) : name;
        } catch (Throwable ignored) {
            return name;
        }
    }

    private static String marked(String name, String uid, boolean fromProfile) {
        if (name == null || name.isEmpty()) return name;
        if (!Badges.isEnabled() && !Plugins.anyRunning()) return name;
        try {
            String own = Plugins.name(uid, NicknameRenderer.strip(name));
            remember(uid, own, fromProfile);
            String marks = Badges.marksFor(uid) + Looks.mark(uid);
            return marks.isEmpty() ? own : own + '\u2009' + marks;
        } catch (Throwable ignored) {
            return name;
        }
    }

    private static void remember(String uid, String name, boolean fromProfile) {
        if (!fromProfile || uid == null || name == null || name.isEmpty()) return;
        try {
            if (Badges.marksFor(uid).isEmpty() && Looks.of(uid) == null) return;
            lastName = name;
            lastUid = uid;
            lastAt = android.os.SystemClock.uptimeMillis();
        } catch (Throwable ignored) {}
    }

    public static void rewrite(View root) {
        String name = lastName;
        String uid = lastUid;
        if (root == null || name == null || uid == null) return;
        if (android.os.SystemClock.uptimeMillis() - lastAt > RECENT) return;
        try {
            walk(root, 0, name, uid, new int[] {0});
        } catch (Throwable ignored) {}
    }

    private static void walk(View view, int depth, String name, String uid, int[] seen) {
        if (view == null || depth > DEEP || ++seen[0] > BUDGET) return;
        if (inAList(view)) return;
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            CharSequence showing = text.getText();
            if (showing != null && name.contentEquals(showing)) {
                String out = marked(showing.toString(), uid, false);
                if (!out.equals(showing.toString())) setText(text, out);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                walk(group.getChildAt(i), depth + 1, name, uid, seen);
            }
        }
    }

    private static boolean inAList(View view) {
        String name = view.getClass().getName();
        return name.contains("RecyclerView") || name.contains("ListView") || name.contains("ViewPager");
    }

    public static void setText(TextView view, CharSequence text) {
        setText(view, text, null);
    }

    public static void setText(TextView view, CharSequence text, TextView.BufferType type) {
        long probe = Probe.start();
        Fonts.apply(view);
        if (view instanceof EditText) {
            CharSequence cleaned = NicknameRenderer.typed(view, text);
            if (type == null) view.setText(cleaned);
            else view.setText(cleaned, type);
            return;
        }
        CharSequence out = NicknameRenderer.render(view, text);
        Probe.stop(Probe.TEXT, probe);
        TextView.BufferType chosen = out != text ? TextView.BufferType.SPANNABLE
                : (type == null ? TextView.BufferType.NORMAL : type);
        view.setText(out, chosen);
    }

    public static Drawable picture(TextView view, bk.soufit.badges.model.Badge badge) {
        return NicknameRenderer.picture(view, badge);
    }
}

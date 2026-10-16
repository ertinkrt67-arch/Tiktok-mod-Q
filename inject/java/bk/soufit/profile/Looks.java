package bk.soufit.profile;

import android.widget.TextView;
import bk.soufit.badges.model.BadgeGradient;
import bk.soufit.badges.render.Marks;
import bk.soufit.badges.render.NicknameRenderer;
import bk.soufit.badges.store.BadgeRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Looks {
    private Looks() {}

    public static int[] of(String uid) {
        BadgeGradient gradient = BadgeRepository.get().gradient(uid);
        return gradient == null ? null : gradient.colours();
    }

    public static int[][] all() {
        List<int[]> out = new ArrayList<>();
        Set<BadgeGradient> seen = new HashSet<>();
        for (BadgeGradient gradient : BadgeRepository.get().snapshot().gradients.values()) {
            if (seen.add(gradient)) out.add(gradient.colours());
        }
        return out.toArray(new int[0][]);
    }

    public static String mark(String uid) {
        return Marks.gradientMark(uid);
    }

    public static boolean isMark(char c) {
        return Marks.isGradientMark(c);
    }

    public static int[] colours(char c) {
        return Marks.coloursAt(c);
    }

    public static void paint(TextView view, int[] colours) {
        NicknameRenderer.paintGradient(view, colours);
    }

    public static void clear(TextView view) {
        NicknameRenderer.clearGradient(view);
    }
}

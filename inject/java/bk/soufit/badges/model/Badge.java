package bk.soufit.badges.model;

import java.util.Comparator;

public final class Badge {
    public enum Origin { CREATOR, SOURCE_ONE, SOURCE_TWO, SOURCE_THREE }

    public static final Comparator<Badge> ORDER = new Comparator<Badge>() {
        @Override public int compare(Badge a, Badge b) {
            if (a.priority != b.priority) return a.priority > b.priority ? -1 : 1;
            return a.id.compareTo(b.id);
        }
    };

    public final String id;
    public final Origin origin;
    public final String image;
    public final int colour;
    public final String title;
    public final String text;
    public final String button;
    public final BadgeGradient gradient;
    public final int priority;
    public final boolean immutable;

    public Badge(String id, Origin origin, String image, int colour, String title, String text, String button,
                 BadgeGradient gradient, int priority, boolean immutable) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("badge id is required");
        this.id = id;
        this.origin = origin == null ? Origin.SOURCE_ONE : origin;
        this.image = image == null ? "" : image;
        this.colour = colour;
        this.title = title == null ? "" : title;
        this.text = text == null ? "" : text;
        this.button = button == null ? "" : button;
        this.gradient = gradient;
        this.priority = priority;
        this.immutable = immutable;
    }

    public boolean isCreator() { return origin == Origin.CREATOR; }
}

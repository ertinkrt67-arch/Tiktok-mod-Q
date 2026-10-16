package bk.soufit.badges.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;
import android.view.ViewGroup;
import bk.soufit.badges.model.Badge;
import bk.soufit.badges.model.BadgeGradient;
import bk.soufit.badges.model.UserBadgeProfile;
import java.util.List;

public final class BadgeFlow extends ViewGroup {
    private static final int GAP_DP = 6;
    private static final int CHIP_DP = 22;
    private static final int CORNER_DP = 8;

    private final float density;
    private final int gap;
    private int chip;

    public BadgeFlow(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        gap = dp(GAP_DP);
        chip = dp(CHIP_DP);
    }

    public void setProfile(UserBadgeProfile profile) {
        setBadges(profile == null ? null : profile.badges());
    }

    public void setBadges(List<Badge> badges) {
        removeAllViews();
        if (badges != null) {
            for (Badge badge : badges) addView(chip(badge));
        }
        requestLayout();
    }

    public void setChipSize(int px) {
        if (px <= 0 || px == chip) return;
        chip = px;
        requestLayout();
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int usable = width - getPaddingLeft() - getPaddingRight();
        int exact = MeasureSpec.makeMeasureSpec(chip, MeasureSpec.EXACTLY);
        int row = 0;
        int cursor = 0;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(exact, exact);
            if (cursor > 0 && cursor + gap + chip > usable) {
                row++;
                cursor = 0;
            } else if (cursor > 0) {
                cursor += gap;
            }
            cursor += chip;
        }
        int rows = getChildCount() == 0 ? 0 : row + 1;
        int height = rows * chip + Math.max(0, rows - 1) * gap + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, resolveSize(height, heightSpec));
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int usable = (right - left) - getPaddingLeft() - getPaddingRight();
        int row = 0;
        int cursor = 0;
        for (int i = 0; i < getChildCount(); i++) {
            if (cursor > 0 && cursor + gap + chip > usable) {
                row++;
                cursor = 0;
            } else if (cursor > 0) {
                cursor += gap;
            }
            int x = getPaddingLeft() + cursor;
            int y = getPaddingTop() + row * (chip + gap);
            getChildAt(i).layout(x, y, x + chip, y + chip);
            cursor += chip;
        }
    }

    private ImageView chip(final Badge badge) {
        ImageView view = new ImageView(getContext());
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setContentDescription(badge.title);
        view.setBackground(background(badge));
        Bitmap ready = IconCache.request(getContext(), badge.image, loaded -> view.setImageBitmap(loaded));
        view.setImageBitmap(ready != null ? ready : IconCache.emblem());
        view.setOnClickListener(v -> NicknameRenderer.open(v, badge));
        return view;
    }

    private Drawable background(Badge badge) {
        BadgeGradient gradient = badge.gradient;
        if (gradient == null && badge.colour == 0) return null;
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dp(CORNER_DP));
        if (gradient == null) {
            shape.setColor(badge.colour);
            return shape;
        }
        shape.setColors(gradient.colours());
        switch (gradient.type()) {
            case RADIAL:
                shape.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                shape.setGradientRadius(dp(CHIP_DP) / 2f);
                break;
            case SWEEP:
                shape.setGradientType(GradientDrawable.SWEEP_GRADIENT);
                break;
            default:
                shape.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                shape.setOrientation(orientation(gradient.angle()));
        }
        return shape;
    }

    private static GradientDrawable.Orientation orientation(float angle) {
        switch (Math.round(angle / 45f) % 8) {
            case 1: return GradientDrawable.Orientation.BL_TR;
            case 2: return GradientDrawable.Orientation.BOTTOM_TOP;
            case 3: return GradientDrawable.Orientation.BR_TL;
            case 4: return GradientDrawable.Orientation.RIGHT_LEFT;
            case 5: return GradientDrawable.Orientation.TR_BL;
            case 6: return GradientDrawable.Orientation.TOP_BOTTOM;
            case 7: return GradientDrawable.Orientation.TL_BR;
            default: return GradientDrawable.Orientation.LEFT_RIGHT;
        }
    }

    private int dp(int value) {
        return Math.round(value * density);
    }
}

package bk.soufit.profile;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import bk.soufit.feed.RegionLabel;

public final class ProfileRegion {
    private static final int TAG = 0x7e0c0412;

    private ProfileRegion() {}

    public static void attach(View header) {
        if (!(header instanceof ViewGroup)) return;
        ViewGroup root = (ViewGroup) header;
        TextView handle = ownHandleIn(root);
        if (handle == null || !(handle.getParent() instanceof ViewGroup)) {
            removeLabels(root);
            return;
        }
        ViewGroup row = (ViewGroup) handle.getParent();
        if (!(row.getParent() instanceof ViewGroup)) return;
        ViewGroup column = (ViewGroup) row.getParent();
        String line = RegionLabel.isEnabled() ? Account.profileRegion() : "";
        TextView label = labelIn(column);
        if (line.isEmpty()) {
            if (label != null) column.removeView(label);
            return;
        }
        if (label == null) {
            label = new TextView(handle.getContext());
            label.setTag(TAG, Boolean.TRUE);
            label.setSingleLine(true);
            label.setAlpha(0.6f);
            label.setTextColor(handle.getCurrentTextColor());
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, handle.getTextSize() * 0.85f);
            column.addView(label, indexOf(column, row) + 1);
        }
        if (!line.equals(String.valueOf(label.getText()))) label.setText(line);
    }

    private static TextView ownHandleIn(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i ++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                CharSequence text = ((TextView) child).getText();
                if (text != null && Account.isOwnHandle(text.toString())) return (TextView) child;
            } else if (child instanceof ViewGroup) {
                TextView found = ownHandleIn((ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static TextView labelIn(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i ++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView && Boolean.TRUE.equals(child.getTag(TAG))) return (TextView) child;
        }
        return null;
    }

    private static void removeLabels(ViewGroup group) {
        for (int i = group.getChildCount() - 1; i >= 0; i --) {
            View child = group.getChildAt(i);
            if (child instanceof TextView && Boolean.TRUE.equals(child.getTag(TAG))) {
                group.removeView(child);
            } else if (child instanceof ViewGroup) {
                removeLabels((ViewGroup) child);
            }
        }
    }

    private static int indexOf(ViewGroup group, View child) {
        for (int i = 0; i < group.getChildCount(); i ++) if (group.getChildAt(i) == child) return i;
        return group.getChildCount() - 1;
    }
}

package bk.soufit.ui;
import bk.soufit.theme.Skin;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
public final class ModDialog {
    public static final class Builder {
        private final Context context;
        private final Skin skin;
        private CharSequence title, message, positive, negative;
        private View content;
        private CharSequence[] choices;
        private int selected = - 1;
        private DialogInterface.OnClickListener onPositive, onNegative, onChoice;
        public Builder(Context context) {
            this(context, Skin.remembered(context));
        }
        public Builder(Context context, Skin skin) {
            this.context = context;
            this.skin = skin;
        }
        public Builder setTitle(CharSequence v) {
            title = v;
            return this;
        }
        public Builder setMessage(CharSequence v) {
            message = v;
            return this;
        }
        public Builder setView(View v) {
            content = v;
            return this;
        }
        public Builder setPositiveButton(CharSequence v, DialogInterface.OnClickListener listener) {
            positive = v;
            onPositive = listener;
            return this;
        }
        public Builder setPositiveButton(int v, DialogInterface.OnClickListener listener) {
            return setPositiveButton(context.getString(v), listener);
        }
        public Builder setNegativeButton(CharSequence v, DialogInterface.OnClickListener listener) {
            negative = v;
            onNegative = listener;
            return this;
        }
        public Builder setNegativeButton(int v, DialogInterface.OnClickListener listener) {
            return setNegativeButton(context.getString(v), listener);
        }
        public Builder setItems(CharSequence[] v, DialogInterface.OnClickListener listener) {
            choices = v;
            onChoice = listener;
            return this;
        }
        public Builder setSingleChoiceItems(CharSequence[] v, int index, DialogInterface.OnClickListener listener) {
            choices = v;
            selected = index;
            onChoice = listener;
            return this;
        }
        public android.app.Dialog show() {
            Panel panel = Panel.with(context, skin, title == null ? null : title.toString());
            if (message != null) panel.text(message.toString());
            if (content != null) panel.view(content);
            if (choices != null) {
                panel.section(null);
                for (int i = 0; i < choices.length; i ++) {
                    final int index = i;
                    panel.option(String.valueOf(choices[i]), selected == i, selected >= 0, () -> {
                        if (onChoice != null) onChoice.onClick(panel.dialog(), index);
                    });
                }
            }
            if (positive != null) panel.primary(positive.toString(), () -> {
                if (onPositive != null) onPositive.onClick(panel.dialog(), DialogInterface.BUTTON_POSITIVE);
            });
            if (negative != null) panel.quiet(negative.toString(), () -> {
                if (onNegative != null) onNegative.onClick(panel.dialog(), DialogInterface.BUTTON_NEGATIVE);
            });
            panel.show();
            return panel.dialog();
        }
    }
}

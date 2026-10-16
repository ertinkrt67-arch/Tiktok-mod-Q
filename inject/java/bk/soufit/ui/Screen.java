package bk.soufit.ui;
import bk.soufit.theme.Accent;
import bk.soufit.core.Diary;
import bk.soufit.media.DownloadIsland;
import bk.soufit.media.DownloadMark;
import bk.soufit.media.DownloadMorph;
import bk.soufit.media.DownloadTasks;
import bk.soufit.theme.Fonts;
import bk.soufit.core.Local;
import bk.soufit.feed.PlayerTools;
import bk.soufit.core.ProgressJobs;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import java.lang.ref.WeakReference;
public final class Screen {
    private Screen() {} private static volatile WeakReference<Activity> current = new WeakReference<Activity>(null);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final long LINGER = 120000;
    private static final long AFTER = 550;
    public static void at(Activity activity) {
        current = new WeakReference<Activity>(activity);
        MAIN.post(() -> {
            if (progressLine != null) progressWindow();
        });
    }

    public static void gone(Activity activity) {
        PlayerTools.cancelSmooth();
        if (current.get() == activity) current = new WeakReference<Activity>(null);
        if (barOwner.get() == activity) hideProgressWindow();
        closeOffers();
    }

    private static volatile Dialog showing;
    private static final java.util.List<Dialog> OFFERS = new java.util.ArrayList<Dialog>();
    private static void forget(Dialog dialog) {
        OFFERS.remove(dialog);
        if (showing == dialog) showing = null;
        close(dialog);
    }

    private static void closeOffers() {
        for (Dialog old : new java.util.ArrayList<Dialog>(OFFERS)) forget(old);
        showing = null;
    }

    public static Activity now() {
        return current.get();
    }

    public static void say(final String message) {
        MAIN.post(new Runnable() {
            @Override public void run() {
                Activity activity = current.get();
                if (activity == null || activity.isFinishing()) return;
                Popup.show(activity, null, message, null);
            }
        });
    }

    public static void offer(String label, Runnable action) {
        offer(label, 0.52f, action);
    }

    private static volatile String offerLabel;
    private static volatile Runnable offerAction;
    public static void offer(final String label, final float where, final Runnable action) {
        MAIN.postDelayed(new Runnable() {
            @Override public void run() {
                try {
                    final Activity activity = current.get();
                    if (activity == null || activity.isFinishing()) return;
                    Dialog open = showing;
                    if (open != null && open.isShowing() && label.equals(offerLabel)) {
                        offerAction = action;
                        return;
                    }
                    closeOffers();
                    final Dialog dialog = new Dialog(activity);
                    Window window = dialog.getWindow();
                    if (window == null) return;
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setDimAmount(0f);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    dialog.setCanceledOnTouchOutside(false);
                    offerLabel = label;
                    offerAction = action;
                    TextView pillView = pill(activity, label, new Runnable() {
                        @Override public void run() {
                            Runnable chosen = offerAction;
                            forget(dialog);
                            if (chosen == null) return;
                            try {
                                chosen.run();
                            } catch (Throwable error) {
                                Diary.note("offer: " + error);
                            }
                        }
                    });
                    if (Motion.enabled(activity)) {
                        pillView.setAlpha(0f);
                        pillView.animate().alpha(1f).setDuration(180L).start();
                    }
                    dialog.setContentView(pillView);
                    dialog.show();
                    showing = dialog;
                    OFFERS.add(dialog);
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    params.y = (int)(activity.getResources().getDisplayMetrics().heightPixels * where);
                    window.setAttributes(params);
                    MAIN.postDelayed(new Runnable() {
                        @Override public void run() {
                            if (showing == dialog) forget(dialog);
                        }
                    }, LINGER);
                } catch (Throwable error) {
                    Diary.note("offer: " + error);
                }
            }
        }, AFTER);
    }

    public static TextView pill(Activity activity, String label, final Runnable action) {
        TextView pill = new TextView(activity);
        pill.setText(label);
        pill.setTextColor(onAccent());
        pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        pill.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(activity, 22), dp(activity, 11), dp(activity, 22), dp(activity, 11));
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Accent.colour());
        shape.setCornerRadius(dp(activity, 22));
        pill.setBackground(shape);
        pill.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                action.run();
            }
        });
        return pill;
    }

    private static void close(Dialog dialog) {
        try {
            if (dialog.isShowing()) dialog.dismiss();
        } catch (Throwable ignored) {}
    }

    private static volatile Dialog bar;
    private static volatile TextView barText;
    private static volatile android.widget.ProgressBar barProgress;
    private static volatile DownloadMark barSurface;
    private static DownloadIsland barIsland;
    private static android.animation.ValueAnimator completionMotion;
    private static String progressLine;
    private static Dialog noteDialog;
    private static volatile String doneNote;
    private static final ProgressJobs progressJobs = new ProgressJobs();
    private static WeakReference<Activity> barOwner = new WeakReference<Activity>(null);
    private static void hideProgressWindow() {
        if (noteDialog != null) {
            Dialog n = noteDialog;
            noteDialog = null;
            close(n);
        }
        if (barIsland != null) {
            barIsland.dispose();
            barIsland = null;
        }
        if (completionMotion != null) {
            completionMotion.cancel();
            completionMotion = null;
        }
        Dialog old = bar;
        bar = null;
        barText = null;
        barProgress = null;
        barSurface = null;
        barOwner = new WeakReference<Activity>(null);
        if (old != null) close(old);
    }

    public static void progress(final String what, final int percent) {
        progress("default", what, percent);
    }

    public static void progress(final String job, final String what, final int percent) {
        MAIN.post(new Runnable() {
            @Override public void run() {
                DownloadTasks.observe(job, what, percent);
                progressJobs.update(job, what, percent);
                progressLine = progressJobs.line();
                progressWindow();
            }
        });
    }

    private static void progressWindow() {
        try {
            String line = progressLine;
            if (line == null) return;
            Activity activity = current.get();
            if (activity == null || activity.isFinishing()) return;
            TextView already = barText;
            if (already != null && bar != null && barOwner.get() == activity && bar.isShowing()) {
                if (!line.equals(already.getContentDescription())) already.setContentDescription(line);
                updateProgress();
                return;
            }
            hideProgressWindow();
            Dialog dialog = new Dialog(activity);
            Window window = dialog.getWindow();
            if (window == null) return;
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0f);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            DownloadMark content = new DownloadMark(activity, onAccent());
            content.setOrientation(android.widget.LinearLayout.VERTICAL);
            content.setPadding(dp(activity, 16), dp(activity, 10), dp(activity, 16), dp(activity, 12));
            TextView text = new TextView(activity);
            text.setText(progressJobs.line() == null ? Local.of("Tasks") : Local.of("Downloading"));
            text.setGravity(Gravity.CENTER);
            text.setPadding(0, 0, 0, 0);
            text.setContentDescription(line);
            text.setTextColor(0xFFF2F0F5);
            text.setTextSize(13);
            text.setSingleLine(true);
            text.setEllipsize(android.text.TextUtils.TruncateAt.END);
            Fonts.apply(text);
            content.addView(text, new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            android.widget.ProgressBar progress = new android.widget.ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
            progress.setMax(100);
            GradientDrawable track = new GradientDrawable();
            track.setColor((Accent.colour() & 0xFFFFFF) | 0x33000000);
            track.setCornerRadius(dp(activity, 4));
            GradientDrawable fill = new GradientDrawable();
            fill.setColor(Accent.colour());
            fill.setCornerRadius(dp(activity, 4));
            android.graphics.drawable.ClipDrawable clip = new android.graphics.drawable.ClipDrawable(fill, Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
            android.graphics.drawable.LayerDrawable layers = new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[] {track,
                    clip});
            layers.setId(0, android.R.id.background);
            layers.setId(1, android.R.id.progress);
            progress.setProgressDrawable(layers);
            progress.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Accent.colour()));
            android.widget.LinearLayout.LayoutParams meter = new android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(activity, 10));
            meter.topMargin = dp(activity, 8);
            content.addView(progress, meter);
            content.bind(progress);
            dialog.setContentView(content);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = Math.min(dp(activity, 260), activity.getResources().getDisplayMetrics().widthPixels - dp(activity,
                    32));
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = dp(activity, 56);
            window.setAttributes(params);
            bar = dialog;
            barText = text;
            barSurface = content;
            barProgress = progress;
            updateProgress();
            barIsland = new DownloadIsland(activity, dialog, content);
            text.setOnClickListener(v -> {
                if (barIsland != null) barIsland.toggle();
            });
            barOwner = new WeakReference<Activity>(activity);
        } catch (Throwable error) {
            Diary.note("progress: " + error);
        }
    }

    public static void showTasks() {
        MAIN.post(() -> {
            if (bar != null && barText == null) hideProgressWindow();
            if (bar == null) {
                progressLine = Local.of("Tasks");
                progressWindow();
                progressLine = progressJobs.line();
                if (barSurface != null) barSurface.progress(100);
            }
            if (barIsland != null && !barIsland.expanded()) barIsland.toggle();
        });
    }

    public static void tasksCollapsed() {
        if (progressLine == null) hideProgressWindow();
    }

    public static void progressGone() {
        progressGone("default");
    }

    private static void updateProgress() {
        android.widget.ProgressBar meter = barProgress;
        if (meter == null) return;
        int percent = progressJobs.percent();
        meter.setIndeterminate(false);
        if (percent >= 0) meter.setProgress(percent);
        DownloadMark surface = barSurface;
        if (surface != null) surface.progress(percent);
    }

    public static void progressGone(final String job) {
        endProgress(job, false);
    }

    public static void progressDone(final String job) {
        endProgress(job, true);
    }

    public static void progressDone() {
        endProgress("default", true);
    }

    public static void progressDone(final String job, final String where) {
        doneNote = where;
        endProgress(job, true);
    }

    private static void endProgress(final String job, final boolean completed) {
        MAIN.post(new Runnable() {
            @Override public void run() {
                DownloadTasks.endObserved(job, completed);
                progressJobs.remove(job);
                progressLine = progressJobs.line();
                if (progressLine == null) {
                    if (!completed) doneNote = null;
                    if (completed) finishProgress();
                    else hideProgressWindow();
                } else {
                    doneNote = null;
                    progressWindow();
                }
            }
        });
    }

    private static void finishProgress() {
        if (barIsland != null && barIsland.expanded()) {
            barIsland.collapse(() -> {
                if (progressLine == null) finishProgress();
            });
            return;
        }
        final Dialog finished = bar;
        final Activity owner = barOwner.get();
        if (finished == null || owner == null || owner.isFinishing() || !finished.isShowing()) {
            hideProgressWindow();
            return;
        }
        final DownloadMark mark = barSurface;
        if (mark == null || barText == null || barProgress == null) {
            hideProgressWindow();
            return;
        }
        Window window = finished.getWindow();
        if (window == null) {
            hideProgressWindow();
            return;
        }
        WindowManager.LayoutParams params = window.getAttributes();
        final int width = params.width;
        if (barIsland != null) {
            barIsland.dispose();
            barIsland = null;
        }
        mark.setOnClickListener(null);
        mark.begin(width, barText, barProgress);
        final int height = mark.initialHeight();
        barText = null;
        barProgress = null;
        if (Motion.enabled(owner)) {
            android.animation.ValueAnimator morph = android.animation.ValueAnimator.ofFloat(0, 1);
            completionMotion = morph;
            morph.setDuration(620L);
            morph.setInterpolator(new android.view.animation.PathInterpolator(0.4f, 0f, 0.2f, 1f));
            morph.addUpdateListener(a -> {
                if (bar == finished && finished.isShowing()) {
                    float value = (Float) a.getAnimatedValue();
                    mark.morph(value);
                    WindowManager.LayoutParams p = window.getAttributes();
                    p.width = Math.round(DownloadMorph.mix(width, dp(owner, 56), value));
                    p.height = Math.round(DownloadMorph.mix(height, dp(owner, 56), value));
                    window.setAttributes(p);
                }
            });
            morph.start();
        } else {
            mark.morph(1);
            params.width = dp(owner, 56);
            params.height = dp(owner, 56);
            window.setAttributes(params);
        }
        final String note = doneNote;
        doneNote = null;
        if (note != null && note.length()>0) showNote(owner, finished, note);
        final long hold = note != null && note.length()>0 ? 3800L : 2400L;
        MAIN.postDelayed(() -> {
            if (bar == finished && progressLine == null) {
                if (Motion.enabled(owner)) {
                    Dialog n = noteDialog;
                    if (n != null && n.getWindow() != null) n.getWindow().getDecorView().animate().alpha(0).setDuration(180L)
                            .start();
                    mark.animate().alpha(0).setDuration(180L).withEndAction(() -> {
                        if (bar == finished && progressLine == null) hideProgressWindow();
                    }).start();
                } else hideProgressWindow();
            }
        }, hold);
    }

    private static void showNote(final Activity owner, final Dialog finished, final String where) {
        try {
            Dialog dialog = new Dialog(owner);
            Window window = dialog.getWindow();
            if (window == null) return;
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0f);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            TextView text = new TextView(owner);
            text.setText(Local.of("Saved to ") + where);
            text.setTextColor(0xFFF2F0F5);
            text.setTextSize(12);
            text.setGravity(Gravity.CENTER);
            text.setMaxLines(2);
            text.setEllipsize(android.text.TextUtils.TruncateAt.END);
            Fonts.apply(text);
            text.setPadding(dp(owner, 14), dp(owner, 8), dp(owner, 14), dp(owner, 8));
            GradientDrawable pill = new GradientDrawable();
            pill.setColor(0xFF24262B);
            pill.setCornerRadius(dp(owner, 16));
            text.setBackground(pill);
            dialog.setContentView(text);
            dialog.show();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = dp(owner, 56) + dp(owner, 56) + dp(owner, 8);
            window.setAttributes(params);
            if (Motion.enabled(owner)) {
                text.setAlpha(0f);
                text.setTranslationY(- dp(owner, 6));
                text.animate().alpha(1f).translationY(0).setStartDelay(260L).setDuration(260L).start();
            }
            noteDialog = dialog;
        } catch (Throwable error) {
            Diary.note("note: " + error);
        }
    }

    private static int onAccent() {
        int colour = Accent.colour();
        int red = (colour >> 16) & 0xFF, green = (colour >> 8) & 0xFF, blue = colour & 0xFF;
        return (red * 299 + green * 587 + blue * 114) / 1000 > 150 ? 0xFF1C2C24 : 0xFFFFFFFF;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}

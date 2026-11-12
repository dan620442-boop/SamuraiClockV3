package com.samuraiclock.v3;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private FrameLayout preview;
    private TextView previewDate, previewClock;
    private float density;

    private static final String[] FONTS = {"Condensed Bold", "Roboto Condensed", "Sans Serif", "Serif", "Monospace"};
    private static final String[] TIMES = {"24-hour (23:52)", "12-hour (11:52 PM)"};
    private static final String[] DATES = {"dd/MM/yyyy", "dd MMM yyyy", "MMMM dd, yyyy", "EEE, dd MMM", "dd MMMM", "yyyy-MM-dd"};
    private static final String[] STYLES = {"Samurai Red / Black", "Crimson / White", "White / Red", "All Black", "All White"};

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("samurai_clock_settings", MODE_PRIVATE);
        density = getResources().getDisplayMetrics().density;
        getWindow().setStatusBarColor(Color.rgb(12,12,14));
        getWindow().setNavigationBarColor(Color.rgb(12,12,14));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(22), dp(18), dp(28));
        page.setBackgroundColor(Color.rgb(12,12,14));
        scroll.addView(page);

        TextView title = text("SAMURAI CLOCK", 27, Color.WHITE, true);
        page.addView(title);
        TextView subtitle = text("CUSTOMISE YOUR LIVE WALLPAPER", 11, 0xFFFF5265, true);
        subtitle.setLetterSpacing(.16f);
        addGap(page, 4);
        page.addView(subtitle);
        addGap(page, 18);

        TextView previewLabel = text("LIVE PREVIEW", 12, 0xFFB8B8BE, true);
        page.addView(previewLabel);
        preview = new FrameLayout(this);
        GradientDrawable previewBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFF7F6F3, 0xFFE9E8E5});
        previewBg.setCornerRadius(dp(18));
        preview.setBackground(previewBg);
        preview.setClipToPadding(false);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, dp(230));
        pp.topMargin = dp(9);
        page.addView(preview, pp);

        previewDate = text("12/11/2026", 19, Color.BLACK, true);
        previewDate.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        FrameLayout.LayoutParams dateLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        dateLp.topMargin = dp(24);
        preview.addView(previewDate, dateLp);

        previewClock = text("23:52", 78, Color.BLACK, true);
        previewClock.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams clockLp = new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER);
        preview.addView(previewClock, clockLp);
        preview.post(this::updatePreview);
        addGap(page, 18);

        section(page, "CLOCK");
        addSeek(page, "Horizontal position", "clock_x", 0, 100, 61);
        addSeek(page, "Vertical position", "clock_y", 0, 100, 49);
        addSeek(page, "Vertical size", "clock_size_y", 50, 160, 100);
        addSeek(page, "Horizontal size", "clock_size_x", 50, 160, 100);
        addSpinner(page, "Time format", "time_format", TIMES, 0);
        addSpinner(page, "Font", "font", FONTS, 0);
        addSpinner(page, "Clock colour style", "style", STYLES, 0);
        addGap(page, 10);

        section(page, "DATE");
        addSpinner(page, "Date format", "date_format", DATES, 0);
        addSeek(page, "Date horizontal position", "date_x", 0, 100, 50);
        addSeek(page, "Date vertical position", "date_y", 0, 100, 19);
        addSeek(page, "Date size", "date_size", 50, 160, 100);

        addGap(page, 20);
        Button reset = button("RESET DEFAULTS", false);
        page.addView(reset, fullWidth());
        reset.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            recreate();
        });
        addGap(page, 10);
        Button apply = button("PREVIEW & SET WALLPAPER", true);
        page.addView(apply, fullWidth());
        apply.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new android.content.ComponentName(this, SamuraiWallpaperService.class));
                startActivity(intent);
            } catch (Exception e) {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            }
        });
        TextView hint = text("Changes save automatically. Set the wallpaper again to see them applied.", 12, 0xFFAAAAAA, false);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hp = fullWidth();
        hp.topMargin = dp(12);
        page.addView(hint, hp);
        setContentView(scroll);
    }

    private void addSeek(LinearLayout parent, String label, String key, int min, int max, int def) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setPadding(0, dp(7), 0, dp(5));
        TextView name = text(label + "  ·  " + prefs.getInt(key, def), 14, Color.WHITE, false);
        group.addView(name);
        SeekBar seek = new SeekBar(this);
        seek.setMax(max - min);
        seek.setProgress(Math.max(0, Math.min(max-min, prefs.getInt(key, def)-min)));
        seek.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFE51B35));
        seek.setThumbTintList(android.content.res.ColorStateList.valueOf(0xFFFF5265));
        group.addView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean user) {
                int value = p + min;
                name.setText(label + "  ·  " + value);
                if (user) {
                    prefs.edit().putInt(key, value).apply();
                    updatePreview();
                }
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        parent.addView(group);
    }

    private void addSpinner(LinearLayout parent, String label, String key, String[] choices, int def) {
        TextView name = text(label, 14, Color.WHITE, false);
        LinearLayout.LayoutParams np = fullWidth();
        np.topMargin = dp(9);
        parent.addView(name, np);
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices);
        spinner.setAdapter(adapter);
        spinner.setSelection(prefs.getInt(key, def));
        spinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE51B35));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (first) { first = false; return; }
                prefs.edit().putInt(key, pos).apply();
                updatePreview();
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        parent.addView(spinner, fullWidth());
    }

    private void updatePreview() {
        if (previewClock == null) return;
        int font = prefs.getInt("font", 0);
        Typeface typeface = clockTypeface(font);
        previewClock.setTypeface(typeface);
        previewDate.setTypeface(typeface);
        int style = prefs.getInt("style", 0);
        int hourColor = style == 2 ? Color.WHITE : (style == 3 ? Color.BLACK : (style == 4 ? Color.WHITE : 0xFFE50920));
        int minuteColor = style == 1 ? Color.WHITE : (style == 2 ? 0xFFE50920 : (style == 4 ? Color.WHITE : Color.BLACK));
        SpannableString clock = new SpannableString("23:52");
        clock.setSpan(new ForegroundColorSpan(hourColor), 0, 2, 0);
        clock.setSpan(new ForegroundColorSpan(0xFFE50920), 2, 3, 0);
        clock.setSpan(new ForegroundColorSpan(minuteColor), 3, 5, 0);
        previewClock.setText(clock);
        previewClock.setScaleX(prefs.getInt("clock_size_x",100)/100f);
        previewClock.setScaleY(prefs.getInt("clock_size_y",100)/100f);
        FrameLayout.LayoutParams cp = (FrameLayout.LayoutParams) previewClock.getLayoutParams();
        cp.gravity = Gravity.TOP | Gravity.LEFT;
        cp.leftMargin = (int)(preview.getWidth() * prefs.getInt("clock_x",61) / 100f);
        cp.topMargin = (int)(preview.getHeight() * prefs.getInt("clock_y",49) / 100f) - dp(42);
        previewClock.setLayoutParams(cp);
        previewDate.setText(DATES[Math.max(0,Math.min(DATES.length-1,prefs.getInt("date_format",0)))]
                .replace("dd","12").replace("MM","11").replace("yyyy","2026").replace("MMM","Nov").replace("MMMM","November").replace("EEE","Thu"));
        previewDate.setTextSize(14f * prefs.getInt("date_size",100)/100f);
        FrameLayout.LayoutParams dp = (FrameLayout.LayoutParams) previewDate.getLayoutParams();
        dp.gravity = Gravity.TOP | Gravity.LEFT;
        dp.leftMargin = (int)(preview.getWidth() * prefs.getInt("date_x",50)/100f);
        dp.topMargin = (int)(preview.getHeight() * prefs.getInt("date_y",19)/100f);
        previewDate.setLayoutParams(dp);
    }

    private Typeface clockTypeface(int index) {
        switch(index) {
            case 1: return Typeface.create("sans-serif-condensed", Typeface.BOLD);
            case 2: return Typeface.create("sans-serif", Typeface.BOLD);
            case 3: return Typeface.create("serif", Typeface.BOLD);
            case 4: return Typeface.create("monospace", Typeface.BOLD);
            default: return Typeface.create("sans-serif-condensed", Typeface.BOLD);
        }
    }
    private void section(LinearLayout p, String title) {
        TextView t = text(title, 13, 0xFFFF5265, true);
        t.setLetterSpacing(.13f);
        LinearLayout.LayoutParams lp = fullWidth(); lp.topMargin = dp(5); lp.bottomMargin = dp(4); p.addView(t, lp);
    }
    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private Button button(String label, boolean primary) {
        Button b = new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                primary ? new int[]{0xFFB90F2A,0xFFE02A3E} : new int[]{0xFF303036,0xFF45454D});
        bg.setCornerRadius(dp(14)); b.setBackground(bg); return b;
    }
    private LinearLayout.LayoutParams fullWidth() { return new LinearLayout.LayoutParams(-1,-2); }
    private void addGap(LinearLayout p, int height) { View v = new View(this); p.addView(v,new LinearLayout.LayoutParams(1,dp(height))); }
    private int dp(float n) { return (int)(n*density+.5f); }
}

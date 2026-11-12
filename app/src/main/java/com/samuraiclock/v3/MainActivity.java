package com.samuraiclock.v3;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.getDecorView().setSystemUiVisibility(0);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        float d = getResources().getDisplayMetrics().density;
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.samurai_wallpaper);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(background, new FrameLayout.LayoutParams(-1, -1));

        View shade = new View(this);
        GradientDrawable overlay = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xCC000000, 0x11000000, 0x22000000, 0xEE000000});
        shade.setBackground(overlay);
        root.addView(shade, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding((int)(28*d), (int)(46*d), (int)(28*d), (int)(30*d));
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(-1, -1);
        root.addView(content, contentParams);

        TextView eyebrow = new TextView(this);
        eyebrow.setText("LIVE WALLPAPER  /  V3");
        eyebrow.setTextColor(0xFFFF5364);
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.22f);
        eyebrow.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        content.addView(eyebrow);

        TextView title = new TextView(this);
        title.setText("SAMURAI\nCLOCK");
        title.setTextColor(Color.WHITE);
        title.setTextSize(42);
        title.setLineSpacing(0, 0.92f);
        title.setLetterSpacing(0.06f);
        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = (int)(10*d);
        content.addView(title, titleParams);

        TextView description = new TextView(this);
        description.setText("A quiet moment. A living clock.\nCrimson hours, ink-black minutes, and a moving second hand.");
        description.setTextColor(0xFFE7E3E1);
        description.setTextSize(14);
        description.setGravity(Gravity.CENTER);
        description.setLineSpacing((int)(5*d), 1f);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(-1, -2);
        descriptionParams.topMargin = (int)(14*d);
        content.addView(description, descriptionParams);

        View spacer = new View(this);
        content.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1f));

        Button setWallpaper = new Button(this);
        setWallpaper.setText("PREVIEW  &  SET WALLPAPER");
        setWallpaper.setTextColor(Color.WHITE);
        setWallpaper.setTextSize(14);
        setWallpaper.setLetterSpacing(0.08f);
        setWallpaper.setAllCaps(false);
        setWallpaper.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        setWallpaper.setPadding((int)(20*d), (int)(16*d), (int)(20*d), (int)(16*d));
        GradientDrawable buttonBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFFB90F2A, 0xFFE02A3E});
        buttonBg.setCornerRadius(18*d);
        setWallpaper.setBackground(buttonBg);
        setWallpaper.setOnClickListener(v -> IntentHelper.openWallpaper(this));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2);
        buttonParams.bottomMargin = (int)(16*d);
        content.addView(setWallpaper, buttonParams);

        TextView note = new TextView(this);
        note.setText("Choose Home screen, Lock screen, or both in Android's wallpaper preview.");
        note.setTextColor(0xFFDDDDDD);
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        content.addView(note);

        setContentView(root);
    }
}

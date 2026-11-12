package com.samuraiclock.v3;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);

        TextView title = new TextView(this);
        title.setText("SAMURAI CLOCK V3");
        title.setTextSize(26);

        Button button = new Button(this);
        button.setText("Set Live Wallpaper");

        button.setOnClickListener(v -> {
            WallpaperManager manager =
                    WallpaperManager.getInstance(this);

            try {
                IntentHelper.openWallpaper(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        layout.addView(title);
        layout.addView(button);

        setContentView(layout);
    }
}

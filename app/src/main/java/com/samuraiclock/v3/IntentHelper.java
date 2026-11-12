package com.samuraiclock.v3;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;

public class IntentHelper {

    public static void openWallpaper(Activity activity) {
        Intent intent = new Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        );

        intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(
                        activity,
                        SamuraiWallpaperService.class
                )
        );

        activity.startActivity(intent);
    }
}

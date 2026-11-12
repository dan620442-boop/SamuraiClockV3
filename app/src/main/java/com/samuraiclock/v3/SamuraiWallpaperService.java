package com.samuraiclock.v3;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SamuraiWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new SamuraiEngine();
    }

    private class SamuraiEngine extends Engine {

        private final Handler handler = new Handler();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean visible = false;

        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                drawFrame();
            }
        };

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;

            if (visible) {
                drawFrame();
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceChanged(
                SurfaceHolder holder,
                int format,
                int width,
                int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            visible = false;
            handler.removeCallbacks(drawRunner);
            super.onSurfaceDestroyed(holder);
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas != null) {
                    drawClock(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(drawRunner);

            if (visible) {
                handler.postDelayed(drawRunner, 1000);
            }
        }

        private void drawClock(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();

            canvas.drawColor(Color.BLACK);

            paint.setTypeface(Typeface.create(
                    "sans-serif-condensed",
                    Typeface.BOLD
            ));

            paint.setTextAlign(Paint.Align.RIGHT);

            String hour = new SimpleDateFormat(
                    "HH", Locale.getDefault()
            ).format(new Date());

            String minute = new SimpleDateFormat(
                    "mm", Locale.getDefault()
            ).format(new Date());

            String date = new SimpleDateFormat(
                    "EEEE, dd MMMM",
                    Locale.getDefault()
            ).format(new Date());

            float x = width * 0.90f;
            float y = height * 0.30f;

            paint.setColor(Color.RED);
            paint.setTextSize(height * 0.14f);
            canvas.drawText(hour, x, y, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(height * 0.14f);
            canvas.drawText(minute, x, y + height * 0.14f, paint);

            paint.setTextSize(height * 0.025f);
            canvas.drawText(date, x, y - height * 0.08f, paint);
        }
    }
}

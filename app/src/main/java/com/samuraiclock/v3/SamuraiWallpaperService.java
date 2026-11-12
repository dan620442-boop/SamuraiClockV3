package com.samuraiclock.v3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
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

    private final class SamuraiEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Runnable drawRunner = this::drawFrame;
        private final Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint hoursPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        private final Paint minutesPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        private final Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Bitmap artwork;
        private boolean visible;

        SamuraiEngine() {
            artwork = BitmapFactory.decodeResource(getResources(), R.drawable.samurai_wallpaper);

            Typeface digits = Typeface.create("sans-serif-condensed", Typeface.BOLD);
            hoursPaint.setTypeface(digits);
            minutesPaint.setTypeface(digits);
            hoursPaint.setTextAlign(Paint.Align.LEFT);
            minutesPaint.setTextAlign(Paint.Align.LEFT);
            hoursPaint.setShadowLayer(5f, 0f, 2f, 0x55000000);
            minutesPaint.setShadowLayer(5f, 0f, 2f, 0x55000000);

            datePaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            datePaint.setTextAlign(Paint.Align.CENTER);
            datePaint.setColor(0xFF292929);

            accentPaint.setStyle(Paint.Style.STROKE);
            accentPaint.setStrokeWidth(3f);
            accentPaint.setStrokeCap(Paint.Cap.ROUND);
            accentPaint.setColor(0xFFE11D35);
        }

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;
            handler.removeCallbacks(drawRunner);
            if (visible) drawFrame();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            visible = false;
            handler.removeCallbacks(drawRunner);
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onDestroy() {
            visible = false;
            handler.removeCallbacks(drawRunner);
            if (artwork != null && !artwork.isRecycled()) {
                artwork.recycle();
                artwork = null;
            }
            super.onDestroy();
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    int w = canvas.getWidth();
                    int h = canvas.getHeight();
                    canvas.drawColor(Color.BLACK);
                    drawArtwork(canvas, w, h);
                    drawClock(canvas, w, h);
                    drawBottomFade(canvas, w, h);
                }
            } catch (Exception ignored) {
                // Android can destroy the wallpaper surface while a frame is being drawn.
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }

            handler.removeCallbacks(drawRunner);
            if (visible) {
                // The small seconds hand moves once per second; the digits remain stable.
                long delay = 1000L - (System.currentTimeMillis() % 1000L) + 12L;
                handler.postDelayed(drawRunner, delay);
            }
        }

        private void drawArtwork(Canvas canvas, int w, int h) {
            if (artwork == null) return;
            float scale = Math.max((float) w / artwork.getWidth(), (float) h / artwork.getHeight());
            float dw = artwork.getWidth() * scale;
            float dh = artwork.getHeight() * scale;
            float left = (w - dw) / 2f;
            float top = (h - dh) / 2f;
            canvas.drawBitmap(artwork, null, new RectF(left, top, left + dw, top + dh), imagePaint);
        }

        private void drawClock(Canvas canvas, int w, int h) {
            android.content.SharedPreferences cfg = getSharedPreferences("samurai_clock_settings", MODE_PRIVATE);
            Date now = new Date();
            int fontIndex = cfg.getInt("font", 0);
            Typeface digitTypeface;
            switch (fontIndex) {
                case 2: digitTypeface = Typeface.create("sans-serif", Typeface.BOLD); break;
                case 3: digitTypeface = Typeface.create("serif", Typeface.BOLD); break;
                case 4: digitTypeface = Typeface.create("monospace", Typeface.BOLD); break;
                default: digitTypeface = Typeface.create("sans-serif-condensed", Typeface.BOLD); break;
            }

            String timePattern = cfg.getBoolean("use_12_hour", false) ? "hh:mm" : "HH:mm";
            String time = new SimpleDateFormat(timePattern, Locale.getDefault()).format(now);
            String[] parts = time.split(":");
            String hours = parts[0];
            String minutes = parts[1];
            int dateIndex = cfg.getInt("date_format", 0);
            String[] datePatterns = {"dd/MM/yyyy", "dd MMM yyyy", "MMMM dd, yyyy", "EEE, dd MMM", "dd MMMM", "yyyy-MM-dd"};
            String date = new SimpleDateFormat(datePatterns[Math.max(0, Math.min(datePatterns.length - 1, dateIndex))],
                    Locale.getDefault()).format(now);

            float sizeY = cfg.getInt("clock_size_y", 100) / 100f;
            float sizeX = cfg.getInt("clock_size_x", 100) / 100f;
            float size = Math.min(w * 0.98f, h * 0.38f) * sizeY;
            float centerX = w * cfg.getInt("clock_x", 61) / 100f;
            float baseline = h * cfg.getInt("clock_y", 49) / 100f;

            int style = cfg.getInt("style", 0);
            int hourColor = style == 2 ? Color.WHITE : (style == 3 ? Color.BLACK : (style == 4 ? Color.WHITE : 0xFFE50920));
            int minuteColor = style == 1 ? Color.WHITE : (style == 2 ? 0xFFE50920 : (style == 4 ? Color.WHITE : Color.BLACK));
            int colonColor = 0xFFE50920;

            hoursPaint.setTypeface(digitTypeface);
            minutesPaint.setTypeface(digitTypeface);
            hoursPaint.setTextAlign(Paint.Align.LEFT);
            minutesPaint.setTextAlign(Paint.Align.LEFT);
            hoursPaint.setTextSize(size);
            minutesPaint.setTextSize(size);
            hoursPaint.setTextScaleX(sizeX);
            minutesPaint.setTextScaleX(sizeX);
            hoursPaint.setColor(hourColor);
            minutesPaint.setColor(minuteColor);
            hoursPaint.setShadowLayer(2f, 0f, 1f, 0x22000000);
            minutesPaint.setShadowLayer(2f, 0f, 1f, 0x22000000);

            Paint colonPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            colonPaint.setTypeface(digitTypeface);
            colonPaint.setTextSize(size * 0.82f);
            colonPaint.setTextAlign(Paint.Align.LEFT);
            colonPaint.setTextScaleX(sizeX);
            colonPaint.setColor(colonColor);

            float hoursWidth = hoursPaint.measureText(hours);
            float colonWidth = colonPaint.measureText(":");
            float minutesWidth = minutesPaint.measureText(minutes);
            float gap = size * 0.012f * sizeX;
            float totalWidth = hoursWidth + colonWidth + minutesWidth + gap * 2f;
            float x = centerX - totalWidth * 0.5f;

            int dateSize = cfg.getInt("date_size", 100);
            datePaint.setTypeface(digitTypeface);
            datePaint.setTextAlign(Paint.Align.CENTER);
            datePaint.setTextSize(Math.min(w * 0.075f, h * 0.032f) * dateSize / 100f);
            datePaint.setTextScaleX(0.88f * sizeX);
            datePaint.setColor(0xFF111111);
            datePaint.setShadowLayer(1f, 0f, 1f, 0x22000000);
            float dateX = w * cfg.getInt("date_x", 50) / 100f;
            float dateY = h * cfg.getInt("date_y", 19) / 100f;
            canvas.drawText(date, dateX, dateY, datePaint);

            hoursPaint.setShader(new LinearGradient(0, baseline - size * 0.78f, 0, baseline + size * 0.10f,
                    hourColor, 0x00FFFFFF & hourColor, Shader.TileMode.CLAMP));
            minutesPaint.setShader(new LinearGradient(0, baseline - size * 0.78f, 0, baseline + size * 0.10f,
                    minuteColor, 0x00FFFFFF & minuteColor, Shader.TileMode.CLAMP));

            canvas.drawText(hours, x, baseline, hoursPaint);
            x += hoursWidth + gap;
            canvas.drawText(":", x, baseline - size * 0.01f, colonPaint);
            x += colonWidth + gap;
            canvas.drawText(minutes, x, baseline, minutesPaint);

            hoursPaint.setShader(null);
            minutesPaint.setShader(null);
        }

        private void drawBottomFade(Canvas canvas, int w, int h) {
            gradientPaint.setShader(new LinearGradient(0, h * 0.73f, 0, h,
                    new int[]{0x00000000, 0x5505000A, 0xD9000000},
                    new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, h * 0.73f, w, h, gradientPaint);
            gradientPaint.setShader(new LinearGradient(0, h * 0.80f, w * 0.75f, h,
                    new int[]{0x00D10B2B, 0x553D0010, 0x00000000},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, h * 0.80f, w, h, gradientPaint);
            gradientPaint.setShader(null);
        }
    }
}

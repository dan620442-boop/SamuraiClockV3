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
            datePaint.setLetterSpacingSafe();
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
            Date now = new Date();
            String hours = new SimpleDateFormat("HH", Locale.getDefault()).format(now);
            String minutes = new SimpleDateFormat("mm", Locale.getDefault()).format(now);
            String date = new SimpleDateFormat("EEEE  •  dd MMMM yyyy", Locale.getDefault()).format(now);
            int seconds = Integer.parseInt(new SimpleDateFormat("ss", Locale.US).format(now));

            float size = Math.min(w * 0.19f, h * 0.088f);
            float centerX = w * 0.61f;
            float baseline = h * 0.315f;
            hoursPaint.setTextSize(size);
            minutesPaint.setTextSize(size);
            datePaint.setTextSize(Math.max(15f, w * 0.027f));

            float hoursWidth = hoursPaint.measureText(hours);
            float minutesWidth = minutesPaint.measureText(minutes);
            float dialGap = size * 0.25f;
            float totalWidth = hoursWidth + dialGap + minutesWidth;
            float startX = centerX - totalWidth / 2f;

            // The date floats above the clock; no translucent panel obscures the artwork.
            datePaint.setColor(0xFF292929);
            canvas.drawText(date.toUpperCase(Locale.getDefault()), centerX, baseline - size * 1.28f, datePaint);

            Paint rule = new Paint(Paint.ANTI_ALIAS_FLAG);
            rule.setColor(0x99D51B32);
            rule.setStrokeWidth(Math.max(2f, w * 0.0022f));
            canvas.drawLine(centerX - w * 0.12f, baseline - size * 1.12f,
                    centerX + w * 0.12f, baseline - size * 1.12f, rule);

            // Each half has its own vertical fade: crimson hours and ink-black minutes.
            hoursPaint.setShader(new LinearGradient(0, baseline - size, 0, baseline + size * 0.10f,
                    0xFFE51B35, 0x00E51B35, Shader.TileMode.CLAMP));
            minutesPaint.setShader(new LinearGradient(0, baseline - size, 0, baseline + size * 0.10f,
                    0xFF171717, 0x00171717, Shader.TileMode.CLAMP));

            canvas.drawText(hours, startX, baseline, hoursPaint);
            float dialCenterX = startX + hoursWidth + dialGap / 2f;
            float dialCenterY = baseline - size * 0.40f;
            float radius = size * 0.105f;

            // A tiny analogue seconds dial is built into the separator.
            accentPaint.setStyle(Paint.Style.STROKE);
            accentPaint.setStrokeWidth(Math.max(2f, size * 0.012f));
            canvas.drawCircle(dialCenterX, dialCenterY, radius, accentPaint);
            double angle = Math.toRadians(seconds * 6.0 - 90.0);
            float handX = dialCenterX + (float) Math.cos(angle) * radius * 0.78f;
            float handY = dialCenterY + (float) Math.sin(angle) * radius * 0.78f;
            accentPaint.setStrokeWidth(Math.max(2f, size * 0.014f));
            canvas.drawLine(dialCenterX, dialCenterY, handX, handY, accentPaint);
            accentPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(dialCenterX, dialCenterY, Math.max(2f, size * 0.018f), accentPaint);

            canvas.drawText(minutes, startX + hoursWidth + dialGap, baseline, minutesPaint);
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

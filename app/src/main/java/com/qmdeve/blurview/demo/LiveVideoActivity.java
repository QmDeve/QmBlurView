package com.qmdeve.blurview.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;

/**
 * "Live Video" demo - simulates video playback by rendering frames continuously.
 * This looks like a video but doesn't require any video files or network.
 * Perfect for demonstrating blur over video-like content.
 */
public class LiveVideoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private VideoSimulator mVideoSimulator;
    private TextView mStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        mSurfaceView = findViewById(R.id.surfaceView);
        mStatusText = findViewById(R.id.statusText);

        // Set z-order for proper blur rendering
        mSurfaceView.setZOrderMediaOverlay(true);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

        updateStatus("✓ Ready to play");
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        updateStatus("✓ Playing simulated video");
        mVideoSimulator = new VideoSimulator(holder);
        mVideoSimulator.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Handle size changes
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (mVideoSimulator != null) {
            mVideoSimulator.stop();
            try {
                mVideoSimulator.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mVideoSimulator = null;
        }
    }

    private void updateStatus(final String message) {
        runOnUiThread(() -> {
            if (mStatusText != null) {
                mStatusText.setText(message);
            }
        });
    }

    /**
     * Simulates video playback by rendering dynamic frames
     * This creates content that looks like a video playing
     */
    private static class VideoSimulator extends Thread {
        private final SurfaceHolder holder;
        private volatile boolean running = true;
        private long frameCount = 0;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public VideoSimulator(SurfaceHolder holder) {
            this.holder = holder;
            textPaint.setTextSize(60);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setShadowLayer(8, 0, 0, Color.BLACK);
        }

        public void stopPlayback() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        renderFrame(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }

                // Target 30 FPS
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    break;
                }

                frameCount++;
            }
        }

        private void renderFrame(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();

            float time = frameCount * 0.05f;

            // Create dynamic gradient background (simulates video)
            for (int i = 0; i < height; i++) {
                float ratio = (float) i / height;
                float wave = (float) Math.sin(time + ratio * 3) * 0.3f + 0.5f;

                // Create HSV color that changes over time
                float hue = (time * 10 + ratio * 120) % 360;
                float[] hsv = {hue, 0.6f + wave * 0.3f, 0.7f + wave * 0.2f};
                int color = Color.HSVToColor(hsv);

                paint.setColor(color);
                canvas.drawRect(0, i, width, i + 2, paint);
            }

            // Draw animated shapes (simulates video objects)
            paint.setColor(Color.WHITE);
            paint.setAlpha(200);

            // Moving circles
            float x1 = width * 0.3f + (float) Math.sin(time * 0.5) * width * 0.2f;
            float y1 = height * 0.3f + (float) Math.cos(time * 0.7) * height * 0.15f;
            canvas.drawCircle(x1, y1, 60, paint);

            float x2 = width * 0.7f + (float) Math.cos(time * 0.6) * width * 0.2f;
            float y2 = height * 0.7f + (float) Math.sin(time * 0.4) * height * 0.15f;
            canvas.drawCircle(x2, y2, 80, paint);

            // Pulsating rectangle
            float pulse = (float) Math.abs(Math.sin(time * 0.8)) * 50 + 80;
            canvas.drawRect(
                    width / 2f - pulse,
                    height / 2f - pulse,
                    width / 2f + pulse,
                    height / 2f + pulse,
                    paint
            );

            // Draw "video" info
            textPaint.setTextSize(50);
            canvas.drawText("SIMULATED VIDEO", width / 2f, height / 2f - 150, textPaint);

            textPaint.setTextSize(35);
            canvas.drawText("Frame: " + frameCount, width / 2f, height / 2f + 180, textPaint);

            textPaint.setTextSize(25);
            canvas.drawText("30 FPS • No network needed", width / 2f, height / 2f + 220, textPaint);
        }
    }
}

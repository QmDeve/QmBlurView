package com.qmdeve.blurview.demo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;

/**
 * Simple animated SurfaceView demo (no video) to test blur functionality.
 * This uses Canvas drawing instead of MediaPlayer to ensure it always works.
 */
public class SimpleSurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private RenderThread mRenderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_surface_view);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        mSurfaceView = findViewById(R.id.surfaceView);

        // Set z-order for proper blur rendering
        mSurfaceView.setZOrderMediaOverlay(true);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mRenderThread = new RenderThread(holder);
        mRenderThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Handle size changes if needed
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (mRenderThread != null) {
            mRenderThread.stopRendering();
            try {
                mRenderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mRenderThread = null;
        }
    }

    private static class RenderThread extends Thread {
        private final SurfaceHolder mHolder;
        private volatile boolean mRunning = true;
        private float hue = 0;

        public RenderThread(SurfaceHolder holder) {
            mHolder = holder;
        }

        public void stopRendering() {
            mRunning = false;
        }

        @Override
        public void run() {
            while (mRunning) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        draw(canvas);
                    } finally {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        private void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();

            // Create animated gradient background
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Animated color gradient
            hue += 0.5f;
            if (hue > 360) hue = 0;

            float[] hsv = {hue, 0.7f, 0.9f};
            int color1 = Color.HSVToColor(hsv);
            hsv[0] = (hue + 60) % 360;
            int color2 = Color.HSVToColor(hsv);
            hsv[0] = (hue + 120) % 360;
            int color3 = Color.HSVToColor(hsv);

            // Draw gradient rectangles
            paint.setColor(color1);
            canvas.drawRect(0, 0, width, height / 3f, paint);

            paint.setColor(color2);
            canvas.drawRect(0, height / 3f, width, 2 * height / 3f, paint);

            paint.setColor(color3);
            canvas.drawRect(0, 2 * height / 3f, width, height, paint);

            // Draw animated circles
            paint.setColor(Color.WHITE);
            float time = System.currentTimeMillis() / 1000f;
            float x = width / 2f + (float) Math.sin(time) * width / 4f;
            float y = height / 2f + (float) Math.cos(time * 1.3f) * height / 4f;
            canvas.drawCircle(x, y, 80, paint);

            // Draw text
            paint.setTextSize(60);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setShadowLayer(5, 2, 2, Color.BLACK);
            canvas.drawText("Animated Content", width / 2f, height / 2f, paint);
        }
    }
}

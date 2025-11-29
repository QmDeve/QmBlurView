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

public class SurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private RenderThread mRenderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        mSurfaceView = findViewById(R.id.surfaceView);

        // NOTE: No need to call setZOrderMediaOverlay(true) manually!
        // BlurView automatically detects and configures SurfaceView for proper blur rendering

        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mRenderThread = new RenderThread(holder);
        mRenderThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Handle size change if needed
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

        private float x = 0;
        private float y = 0;
        private float dx = 10;
        private float dy = 10;
        private final Paint paint = new Paint();

        private void draw(Canvas canvas) {
            canvas.drawColor(Color.DKGRAY);
            paint.setColor(Color.GREEN); // Different color for SurfaceView demo
            paint.setAntiAlias(true);

            int width = canvas.getWidth();
            int height = canvas.getHeight();

            x += dx;
            y += dy;

            if (x < 0 || x > width) dx = -dx;
            if (y < 0 || y > height) dy = -dy;

            canvas.drawCircle(x, y, 100, paint);
            
            paint.setColor(Color.MAGENTA);
            canvas.drawRect(width / 2f - 100, height / 2f - 100, width / 2f + 100, height / 2f + 100, paint);
        }
    }
}

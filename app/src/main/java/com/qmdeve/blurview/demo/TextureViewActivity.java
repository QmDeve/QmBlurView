package com.qmdeve.blurview.demo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;

public class TextureViewActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private TextureView mTextureView;
    private RenderThread mRenderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_view);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        mTextureView = findViewById(R.id.textureView);
        mTextureView.setOpaque(false);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        mRenderThread = new RenderThread(surface);
        mRenderThread.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        // Handle size change if needed
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        if (mRenderThread != null) {
            mRenderThread.stopRendering();
            try {
                mRenderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mRenderThread = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        // Update logic if needed
    }

    private static class RenderThread extends Thread {
        private final Surface mSurface;
        private volatile boolean mRunning = true;

        public RenderThread(Surface surface) {
            mSurface = surface;
        }

        public void stopRendering() {
            mRunning = false;
        }

        @Override
        public void run() {
            while (mRunning) {
                Canvas canvas = mSurface.lockCanvas(null);
                if (canvas != null) {
                    try {
                        draw(canvas);
                    } finally {
                        mSurface.unlockCanvasAndPost(canvas);
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
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);

            int width = canvas.getWidth();
            int height = canvas.getHeight();

            x += dx;
            y += dy;

            if (x < 0 || x > width) dx = -dx;
            if (y < 0 || y > height) dy = -dy;

            canvas.drawCircle(x, y, 100, paint);

            paint.setColor(Color.BLUE);
            canvas.drawRect(width / 2f - 100, height / 2f - 100, width / 2f + 100, height / 2f + 100, paint);
        }
    }
}

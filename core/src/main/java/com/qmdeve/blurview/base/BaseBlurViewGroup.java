/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Donny Yale
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ===========================================
 * Project: QmBlurView
 * Created Date: 2025-10-21
 * Author: Donny Yale
 * GitHub: https://github.com/QmDeve/QmBlurView
 * Website: https://blurview.qmdeve.com
 * ===========================================
 */

package com.qmdeve.blurview.base;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.PixelCopy;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.tracing.Trace;

import com.qmdeve.blurview.Blur;
import com.qmdeve.blurview.BlurNative;
import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

public class BaseBlurViewGroup {
    private int mOverlayColor;
    private float mBlurRadius;
    private float mDownsampleFactor = 0f;
    private final Blur mBlur;
    private boolean mDirty = true;
    private Bitmap mBitmapToBlur, mBlurredBitmap;
    private Canvas mBlurringCanvas;
    private boolean mIsRendering;
    private float mCornerRadius;
    private float mTopLeftCornerRadius = mCornerRadius;
    private float mTopRightCornerRadius = mCornerRadius;
    private float mBottomLeftCornerRadius = mCornerRadius;
    private float mBottomRightCornerRadius = mCornerRadius;
    private final RectF mClipRect = new RectF();
    private final Path mG3Path = new Path();
    private View mDecorView;
    private boolean mDifferentRoot;
    private View mHostView;
    private boolean mFirstDraw = true;
    private boolean mForceRedraw = false;
    private boolean mSkipNextPreDraw = false;
    private boolean mIsPixelCopyPending = false;
    private int mPixelCopyTraceCookie = 0;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mPixelCopyThread;
    private Handler mPixelCopyHandler;

    public BaseBlurViewGroup(Context context, AttributeSet attrs) {
        mBlur = new BlurNative();
        initPixelCopyThread();
        initAttributes(context, attrs);
    }

    private void initPixelCopyThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPixelCopyThread = new HandlerThread("BlurViewGroupPixelCopy");
            mPixelCopyThread.start();
            mPixelCopyHandler = new Handler(mPixelCopyThread.getLooper());
        }
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurView);
        mBlurRadius = a.getDimension(
                R.styleable.BlurView_blurRadius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics())
        );
        mOverlayColor = a.getColor(R.styleable.BlurView_overlayColor, 0xAAFFFFFF);
        mCornerRadius = a.getDimension(R.styleable.BlurView_cornerRadius, 0);

        mTopLeftCornerRadius = a.getDimension(R.styleable.BlurView_topLeftCornerRadius, mCornerRadius);
        mTopRightCornerRadius = a.getDimension(R.styleable.BlurView_topRightCornerRadius, mCornerRadius);
        mBottomLeftCornerRadius = a.getDimension(R.styleable.BlurView_bottomLeftCornerRadius, mCornerRadius);
        mBottomRightCornerRadius = a.getDimension(R.styleable.BlurView_bottomRightCornerRadius, mCornerRadius);

        mDownsampleFactor = a.getFloat(R.styleable.BlurView_downsampleFactor, 0f);
        a.recycle();
    }

    public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            mDirty = true;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    /**
     * Set the number of blur rounds (iterations) for BlurNative
     * More rounds = stronger blur effect
     * @param rounds Number of blur rounds (1-10)
     */
    public void setBlurRounds(int rounds) {
        if (mBlur instanceof com.qmdeve.blurview.BlurNative) {
            ((com.qmdeve.blurview.BlurNative) mBlur).setBlurRounds(rounds);
            mDirty = true;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    /**
     * Get the current number of blur rounds
     * @return Current blur rounds, or -1 if not using BlurNative
     */
    public int getBlurRounds() {
        if (mBlur instanceof com.qmdeve.blurview.BlurNative) {
            return ((com.qmdeve.blurview.BlurNative) mBlur).getBlurRounds();
        }
        return -1;
    }

    public void setDownsampleFactor(float factor) {
        if (mDownsampleFactor != factor && factor >= 0) {
            mDownsampleFactor = factor;
            mDirty = true;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    public void setOverlayColor(int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    public void setCornerRadius(float radius) {
        if (radius >= 0 && (mCornerRadius != radius
                || mTopLeftCornerRadius != radius
                || mTopRightCornerRadius != radius
                || mBottomLeftCornerRadius != radius
                || mBottomRightCornerRadius != radius)) {
            mCornerRadius = radius;
            mTopLeftCornerRadius = radius;
            mTopRightCornerRadius = radius;
            mBottomLeftCornerRadius = radius;
            mBottomRightCornerRadius = radius;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    public void setTopLeftCornerRadius(float radius) {
        if (mTopLeftCornerRadius != radius && radius >= 0) {
            mTopLeftCornerRadius = radius;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    public void setTopRightCornerRadius(float radius) {
        if (mTopRightCornerRadius != radius && radius >= 0) {
            mTopRightCornerRadius = radius;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    public void setBottomLeftCornerRadius(float radius) {
        if (mBottomLeftCornerRadius != radius && radius >= 0) {
            mBottomLeftCornerRadius = radius;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    public void setBottomRightCornerRadius(float radius) {
        if (mBottomRightCornerRadius != radius && radius >= 0) {
            mBottomRightCornerRadius = radius;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
    }

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public float getTopLeftCornerRadius() {
        return mTopLeftCornerRadius;
    }

    public float getTopRightCornerRadius() {
        return mTopRightCornerRadius;
    }

    public float getBottomLeftCornerRadius() {
        return mBottomLeftCornerRadius;
    }

    public float getBottomRightCornerRadius() {
        return mBottomRightCornerRadius;
    }

    public Bitmap getBlurredBitmap() {
        return mBlurredBitmap;
    }

    public int getOverlayColor() {
        return mOverlayColor;
    }

    private void releaseBitmap() {
        if (mBitmapToBlur != null) {
            mBitmapToBlur.recycle();
            mBitmapToBlur = null;
        }
        if (mBlurredBitmap != null) {
            mBlurredBitmap.recycle();
            mBlurredBitmap = null;
        }
        mBlurringCanvas = null;
    }

    public void release() {
        releaseBitmap();
        mBlur.release();
        if (mPixelCopyThread != null) {
            mPixelCopyThread.quitSafely();
            mPixelCopyThread = null;
            mPixelCopyHandler = null;
        }
    }

    private boolean prepare(int width, int height) {
        Trace.beginSection("BlurViewGroup.prepare");
        try {
        if (mBlurRadius <= 0) {
            release();
            return false;
        }

        float downsampleFactor = mDownsampleFactor > 0 ? mDownsampleFactor : 2.52f;
        float radius = mBlurRadius / downsampleFactor;

        if (mDownsampleFactor <= 0 && radius > 25) {
            downsampleFactor *= radius / 25;
            radius = 25;
        }

        if (width == 0 || height == 0) return false;

        int scaledWidth = Math.max(1, Math.round(width / downsampleFactor));
        int scaledHeight = Math.max(1, Math.round(height / downsampleFactor));

        boolean dirty = mDirty;

        if (mBlurredBitmap == null || mBlurredBitmap.getWidth() != scaledWidth || mBlurredBitmap.getHeight() != scaledHeight) {
            dirty = true;
            releaseBitmap();

            try {
                mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                mBlurringCanvas = new Canvas(mBitmapToBlur);
                mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

                // Ensure software bitmaps for compatibility
                mBitmapToBlur = Utils.ensureSoftwareBitmap(mBitmapToBlur);
                mBlurredBitmap = Utils.ensureSoftwareBitmap(mBlurredBitmap);
            } catch (OutOfMemoryError | IllegalArgumentException e) {
                release();
                return false;
            }
        }

        if (dirty && mBlur.prepare(mBitmapToBlur, radius)) {
            mDirty = false;
        }

        return true;
        } finally {
            Trace.endSection();
        }
    }

    private android.view.Window getActivityWindow() {
        if (mHostView == null) return null;
        Context ctx = mHostView.getContext();
        for (int i = 0; i < 4 && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return (ctx instanceof Activity) ? ((Activity) ctx).getWindow() : null;
    }

    private void performPixelCopyBlur(int width, int height) {
        if (mIsPixelCopyPending || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        android.view.Window window = getActivityWindow();
        if (window == null) return;

        // Marks that the API>=O PixelCopy fallback branch is active (vs. the software captureDecorView branch).
        Trace.beginSection("BlurViewGroup.pixelCopyRequest");
        try {
            int[] locWindow = new int[2];
            mHostView.getLocationInWindow(locWindow);

            Rect rect = new Rect(locWindow[0], locWindow[1], locWindow[0] + width, locWindow[1] + height);

            mIsPixelCopyPending = true;

            // Async span: PixelCopy is non-blocking, so measure request -> copy-complete across threads.
            final int cookie = ++mPixelCopyTraceCookie;
            Trace.beginAsyncSection("BlurViewGroup.pixelCopyAsync", cookie);

            try {
                Handler handler = mPixelCopyHandler != null ? mPixelCopyHandler : mHandler;
                PixelCopy.request(window, rect, mBitmapToBlur, copyResult -> {
                    // Runs on the PixelCopy handler thread once the copy finishes.
                    Trace.endAsyncSection("BlurViewGroup.pixelCopyAsync", cookie);
                    mHandler.post(() -> {
                        mIsPixelCopyPending = false;
                        if (copyResult == PixelCopy.SUCCESS) {
                            Trace.beginSection("BlurViewGroup.pixelCopyBlur");
                            try {
                                blur(mBitmapToBlur, mBlurredBitmap);
                                if (mHostView != null) mHostView.invalidate();
                            } finally {
                                Trace.endSection();
                            }
                        } else {
                            Log.w(Utils.TAG, "PixelCopy fallback failed: " + copyResult);
                        }
                    });
                }, handler);
            } catch (IllegalArgumentException e) {
                mIsPixelCopyPending = false;
                Trace.endAsyncSection("BlurViewGroup.pixelCopyAsync", cookie);
                Log.e(Utils.TAG, "PixelCopy fallback exception: " + e.getMessage());
            }
        } finally {
            Trace.endSection();
        }
    }

    private void blur(Bitmap input, Bitmap output) {
        try {
            // Ensure input is software bitmap
            Bitmap softwareInput = Utils.ensureSoftwareBitmap(input);
            mBlur.blur(softwareInput, output);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null &&
                    e.getMessage().contains("Software rendering doesn't support hardware bitmaps")) {
                Log.e(Utils.TAG, "Hardware bitmap error detected, converting and retrying");
                // Force conversion and retry
                Bitmap softwareInput = input.copy(Bitmap.Config.ARGB_8888, false);
                Bitmap softwareOutput = output.copy(Bitmap.Config.ARGB_8888, false);
                if (softwareInput != null && softwareOutput != null) {
                    mBlur.blur(softwareInput, softwareOutput);
                } else {
                    throw new RuntimeException("Failed to convert hardware bitmaps for blur processing", e);
                }
            } else {
                throw e;
            }
        }
    }

    public boolean performBlurSync(int width, int height) {
        if (mHostView == null || !mHostView.isShown() || mDecorView == null) {
            return false;
        }

        Trace.beginSection("BlurViewGroup.performBlurSync");
        try {
        if (!prepare(width, height)) {
            return false;
        }

        if (mBlurredBitmap == null || mBitmapToBlur == null || mBlurringCanvas == null) {
            return false;
        }

        int[] locDecor = new int[2];
        int[] locSelf = new int[2];
        mDecorView.getLocationOnScreen(locDecor);
        mHostView.getLocationOnScreen(locSelf);

        int offsetX = locSelf[0] - locDecor[0];
        int offsetY = locSelf[1] - locDecor[1];

        mBitmapToBlur.eraseColor(0);

        int saveCount = mBlurringCanvas.save();
        mIsRendering = true;
        Utils.sIsGlobalCapturing = true;
        try {
            float scaleX = 1f * mBitmapToBlur.getWidth() / width;
            float scaleY = 1f * mBitmapToBlur.getHeight() / height;
            mBlurringCanvas.scale(scaleX, scaleY);
            mBlurringCanvas.translate(-offsetX, -offsetY);

            Trace.beginSection("BlurViewGroup.captureDecorView");
            try {
                mDecorView.draw(mBlurringCanvas);
            } catch (IllegalArgumentException e) {
                if (e.getMessage() != null &&
                        e.getMessage().contains("Software rendering doesn't support hardware bitmaps")) {
                    Log.w(Utils.TAG, "Hardware bitmap detected during draw, converting and retrying");
                    // Convert hardware bitmaps in the view hierarchy
                    Utils.disableHardwareBitmapsInView(mDecorView);
                    // Retry the draw
                    try {
                        mBlurringCanvas.restoreToCount(saveCount);
                        saveCount = mBlurringCanvas.save();
                        mBlurringCanvas.scale(scaleX, scaleY);
                        mBlurringCanvas.translate(-offsetX, -offsetY);
                        mDecorView.draw(mBlurringCanvas);
                    } catch (Exception retryError) {
                        Log.e(Utils.TAG, "Retry after hardware bitmap conversion failed: " + retryError.getMessage() + ". Switching to PixelCopy fallback.");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            performPixelCopyBlur(width, height);
                            return false;
                        }
                    }
                } else {
                    throw e;
                }
            } catch (IndexOutOfBoundsException e) {
                Log.w(Utils.TAG, "View hierarchy changed during blur operation: " + e.getMessage());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    performPixelCopyBlur(width, height);
                    return false;
                }
                return false;
            } finally {
                Trace.endSection();
            }
        } finally {
            mIsRendering = false;
            Utils.sIsGlobalCapturing = false;
            try {
                if (mBlurringCanvas != null && saveCount >= 0) {
                    mBlurringCanvas.restoreToCount(saveCount);
                }
            } catch (Exception e) {
                Log.w(Utils.TAG, "error: " + e.getMessage());
            }
        }

        Trace.beginSection("BlurViewGroup.blur");
        try {
            blur(mBitmapToBlur, mBlurredBitmap);
        } finally {
            Trace.endSection();
        }

        return mDifferentRoot || mForceRedraw;
        } finally {
            Trace.endSection();
        }
    }

    public void ensureBlurReady(int width, int height) {
        if (mFirstDraw || mForceRedraw) {
            performBlurSync(width, height);
            mFirstDraw = false;
            mForceRedraw = false;
        }
    }

    private final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (mHostView == null || !mHostView.isShown()) return true;

            if (mSkipNextPreDraw) {
                mSkipNextPreDraw = false;
                return true;
            }

            if (performBlurSync(mHostView.getWidth(), mHostView.getHeight())) {
                mHostView.postInvalidateOnAnimation();
            }

            return true;
        }
    };

    private View getActivityDecorView() {
        if (mHostView == null) return null;
        Context ctx = mHostView.getContext();
        for (int i = 0; i < 4 && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return (ctx instanceof Activity) ? ((Activity) ctx).getWindow().getDecorView() : null;
    }

    public void onAttachedToWindow(View hostView) {
        this.mHostView = hostView;
        mDecorView = getActivityDecorView();
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            mDifferentRoot = mDecorView.getRootView() != hostView.getRootView();
            mFirstDraw = true;
            mForceRedraw = true;
        }
    }

    public void onDetachedFromWindow() {
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            mDecorView = null;
        }
        release();
        mHostView = null;
    }

    public boolean isRendering() {
        return mIsRendering;
    }

    private boolean hasCornerRadius() {
        return mTopLeftCornerRadius > 0 || mTopRightCornerRadius > 0 ||
                mBottomLeftCornerRadius > 0 || mBottomRightCornerRadius > 0;
    }

    private void updatePath(RectF rect) {
        Utils.roundedRectPath(rect,
                mTopLeftCornerRadius, mTopRightCornerRadius,
                mBottomLeftCornerRadius, mBottomRightCornerRadius,
                mG3Path);
    }

    public void drawBlurredBitmap(Canvas canvas, int width, int height) {
        ensureBlurReady(width, height);

        Trace.beginSection("BlurViewGroup.drawResult");
        try {
        if (mBlurredBitmap != null) {
            Rect srcRect = new Rect(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());
            Rect dstRect = new Rect(0, 0, width, height);

            if (hasCornerRadius()) {
                canvas.save();
                mClipRect.set(dstRect);
                updatePath(mClipRect);
                canvas.clipPath(mG3Path);
                canvas.drawBitmap(mBlurredBitmap, srcRect, dstRect, null);
                canvas.restore();
            } else {
                canvas.drawBitmap(mBlurredBitmap, srcRect, dstRect, null);
            }
        }

        Paint paint = new Paint();
        paint.setColor(mOverlayColor);

        Rect dstRect = new Rect(0, 0, width, height);
        if (hasCornerRadius()) {
            canvas.save();
            mClipRect.set(dstRect);
            updatePath(mClipRect);
            canvas.clipPath(mG3Path);
            canvas.drawRect(dstRect, paint);
            canvas.restore();
        } else {
            canvas.drawRect(dstRect, paint);
        }
        } finally {
            Trace.endSection();
        }
    }

    public void drawPreviewBackground(Canvas canvas, int width, int height) {
        if (width == 0 || height == 0) return;

        Paint previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStyle(Paint.Style.FILL);
        int previewColor = mOverlayColor;
        previewPaint.setColor(previewColor);
        if (hasCornerRadius()) {
            mClipRect.set(0, 0, width, height);
            updatePath(mClipRect);
            canvas.drawPath(mG3Path, previewPaint);
        } else {
            canvas.drawRect(0, 0, width, height, previewPaint);
        }
    }

    public void clipCanvasWithRoundedCorner(Canvas canvas, int width, int height) {
        mClipRect.set(0, 0, width, height);
        updatePath(mClipRect);
        canvas.clipPath(mG3Path);
    }

    public void updateBlurImmediately() {
        if (mHostView != null) {
            mForceRedraw = true;
            mSkipNextPreDraw = true;
            mHostView.invalidate();
        }
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Donny Yang
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
 * Author: Donny Yang
 * GitHub: https://github.com/QmDeve/QmBlurView
 * Website: https://blurview.qmdeve.com
 * ===========================================
 */

package com.qmdeve.blurview.engine;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.tracing.Trace;

import com.qmdeve.blurview.Blur;
import com.qmdeve.blurview.BlurNative;
import com.qmdeve.blurview.util.Utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class AbsBlurEngine implements BlurEngine {

    protected static final String TAG = "BlurEngine";
    protected static final float DEFAULT_DOWNSAMPLE_FACTOR = 2.52f;
    protected static final float MAX_BLUR_RADIUS = 25f;

    protected final View mHost;
    protected final Handler mHandler = new Handler(Looper.getMainLooper());

    protected int mOverlayColor = 0xAAFFFFFF;
    protected float mBlurRadius;
    protected float mDownsampleFactor;
    protected final Blur mBlur = new BlurNative();
    protected int mMaxFps;
    protected int mBlurRounds = 2;

    protected float mCornerRadius;
    protected float mTopLeftCornerRadius;
    protected float mTopRightCornerRadius;
    protected float mBottomLeftCornerRadius;
    protected float mBottomRightCornerRadius;

    protected Bitmap mBitmapToBlur;
    protected Canvas mBlurringCanvas;

    protected View mDecorView;
    protected boolean mDifferentRoot;
    protected boolean mDirty = true;
    protected boolean mIsCapturing;
    protected boolean mFirstDraw = true;
    protected boolean mForceRedraw;
    protected boolean mSkipNextPreDraw;
    protected long mLastSyncTime;

    protected boolean mUsePixelCopyFallback;
    protected boolean mIsPixelCopyPending;
    protected int mPixelCopyTraceCookie;
    protected HandlerThread mPixelCopyThread;
    protected Handler mPixelCopyHandler;

    protected final Map<SurfaceView, Bitmap> mSurfaceViewBitmaps = new WeakHashMap<>();
    protected final Map<SurfaceView, Boolean> mPendingPixelCopies = new WeakHashMap<>();
    protected final Set<SurfaceView> mConfiguredSurfaceViews =
            Collections.newSetFromMap(new WeakHashMap<SurfaceView, Boolean>());
    protected boolean mSurfaceViewWarningLogged;

    protected final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    protected final Paint mPreviewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Rect mRectSrc = new Rect();
    protected final Rect mRectDst = new Rect();
    protected final RectF mClipRect = new RectF();
    protected final Path mClipPath = new Path();
    protected final int[] mLocDecor = new int[2];
    protected final int[] mLocSelf = new int[2];
    protected final float[] mBlurParams = new float[2]; // [0] = downsample, [1] = radius

    private final ViewTreeObserver.OnPreDrawListener mPreDrawListener =
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    View host = mHost;
                    if (host == null || !host.isShown()) {
                        return true;
                    }
                    if (mSkipNextPreDraw) {
                        mSkipNextPreDraw = false;
                        return true;
                    }
                    if (sync(false)) {
                        host.postInvalidateOnAnimation();
                    }
                    mForceRedraw = false;
                    return true;
                }
            };

    protected AbsBlurEngine(View host) {
        mHost = host;
        mPreviewPaint.setStyle(Paint.Style.FILL);
        initPixelCopyThread();
    }

    @Override
    public void attach() {
        mDecorView = findActivityDecorView();
        if (mDecorView == null) {
            return;
        }
        initPixelCopyThread();
        mDecorView.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
        mDifferentRoot = mDecorView.getRootView() != mHost.getRootView();
        mFirstDraw = true;
        mForceRedraw = true;
    }

    @Override
    public void detach() {
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
            mDecorView = null;
        }
        release();
    }

    @Override
    public void release() {
        releaseBitmap();
        mBlur.release();
        if (mPixelCopyThread != null) {
            mPixelCopyThread.quitSafely();
            mPixelCopyThread = null;
            mPixelCopyHandler = null;
        }
    }

    @Override
    public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            mDirty = true;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    @Override
    public float getBlurRadius() {
        return mBlurRadius;
    }

    @Override
    public void setBlurRounds(int rounds) {
        rounds = Math.max(1, Math.min(15, rounds));
        if (mBlurRounds != rounds) {
            mBlurRounds = rounds;
            applyBlurRounds();
            mDirty = true;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    protected void applyBlurRounds() {
        if (mBlur instanceof BlurNative) {
            ((BlurNative) mBlur).setBlurRounds(mBlurRounds);
        }
    }

    @Override
    public int getBlurRounds() {
        return mBlurRounds;
    }

    @Override
    public void setDownsampleFactor(float factor) {
        if (mDownsampleFactor != factor && factor >= 0) {
            mDownsampleFactor = factor;
            mDirty = true;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    @Override
    public float getDownsampleFactor() {
        return mDownsampleFactor;
    }

    @Override
    public void setOverlayColor(@ColorInt int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    @Override
    @ColorInt
    public int getOverlayColor() {
        return mOverlayColor;
    }

    @Override
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
            mHost.invalidate();
        }
    }

    @Override
    public float getCornerRadius() {
        return mCornerRadius;
    }

    @Override
    public void setTopLeftCornerRadius(float radius) {
        if (radius >= 0 && mTopLeftCornerRadius != radius) {
            mTopLeftCornerRadius = radius;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    @Override
    public float getTopLeftCornerRadius() {
        return mTopLeftCornerRadius;
    }

    @Override
    public void setTopRightCornerRadius(float radius) {
        if (radius >= 0 && mTopRightCornerRadius != radius) {
            mTopRightCornerRadius = radius;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    @Override
    public float getTopRightCornerRadius() {
        return mTopRightCornerRadius;
    }

    @Override
    public void setBottomLeftCornerRadius(float radius) {
        if (radius >= 0 && mBottomLeftCornerRadius != radius) {
            mBottomLeftCornerRadius = radius;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    @Override
    public float getBottomLeftCornerRadius() {
        return mBottomLeftCornerRadius;
    }

    @Override
    public void setBottomRightCornerRadius(float radius) {
        if (radius >= 0 && mBottomRightCornerRadius != radius) {
            mBottomRightCornerRadius = radius;
            mForceRedraw = true;
            mHost.invalidate();
        }
    }

    @Override
    public float getBottomRightCornerRadius() {
        return mBottomRightCornerRadius;
    }

    @Override
    public boolean hasCornerRadius() {
        return mTopLeftCornerRadius > 0 || mTopRightCornerRadius > 0
                || mBottomLeftCornerRadius > 0 || mBottomRightCornerRadius > 0;
    }

    @Override
    public void setMaxFps(int fps) {
        mMaxFps = Math.max(0, fps);
    }

    @Override
    public int getMaxFps() {
        return mMaxFps;
    }

    @Override
    public boolean sync(boolean forced) {
        View host = mHost;
        if (host == null || !host.isShown() || mDecorView == null) {
            return false;
        }
        int width = host.getWidth();
        int height = host.getHeight();
        if (width <= 0 || height <= 0) {
            return false;
        }

        if (!forced && !mFirstDraw && mMaxFps > 0) {
            long minInterval = 1000L / mMaxFps;
            if (SystemClock.uptimeMillis() - mLastSyncTime < minInterval) {
                return false;
            }
        }

        Trace.beginSection("BlurEngine.sync");
        try {
            Bitmap oldResult = resultBitmap();
            if (!prepare(width, height)) {
                return false;
            }
            boolean contentChanged = resultBitmap() != oldResult;
            mLastSyncTime = SystemClock.uptimeMillis();

            if (mUsePixelCopyFallback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                performPixelCopyBlur(width, height);
                return contentChanged || mDifferentRoot || mForceRedraw;
            }

            if (!capture(width, height)) {
                return false;
            }
            onCaptured(width, height);
            return contentChanged || mDifferentRoot || mForceRedraw;
        } finally {
            Trace.endSection();
        }
    }

    @Override
    public void updateBlurImmediately() {
        mForceRedraw = true;
        mSkipNextPreDraw = true;
        mHost.invalidate();
    }

    @Override
    public void drawBlur(Canvas canvas, int width, int height) {
        if (Utils.sIsGlobalCapturing && !mIsCapturing) {
            return;
        }
        if (width <= 0 || height <= 0) {
            return;
        }
        if (mFirstDraw || mForceRedraw) {
            sync(true);
            mFirstDraw = false;
            mForceRedraw = false;
        }
        Trace.beginSection("BlurEngine.draw");
        try {
            drawResult(canvas, width, height);
        } finally {
            Trace.endSection();
        }
    }

    @Override
    public void drawPreview(Canvas canvas, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        mPreviewPaint.setColor(mOverlayColor);
        if (hasCornerRadius()) {
            mClipRect.set(0, 0, width, height);
            updateClipPath(mClipRect);
            canvas.drawPath(mClipPath, mPreviewPaint);
        } else {
            canvas.drawRect(0, 0, width, height, mPreviewPaint);
        }
    }

    @Override
    public void clipRoundedCorners(Canvas canvas, int width, int height) {
        mClipRect.set(0, 0, width, height);
        updateClipPath(mClipRect);
        canvas.clipPath(mClipPath);
    }

    @Override
    public boolean isCapturing() {
        return mIsCapturing;
    }

    protected abstract boolean prepare(int width, int height);

    protected abstract void onCaptured(int width, int height);

    protected abstract void drawResult(Canvas canvas, int width, int height);

    @Nullable
    protected abstract Bitmap resultBitmap();

    protected void onBuffersReleased() {
    }

    protected float[] blurParams() {
        float downsampleFactor = mDownsampleFactor > 0 ? mDownsampleFactor : DEFAULT_DOWNSAMPLE_FACTOR;
        float radius = mBlurRadius / downsampleFactor;
        if (mDownsampleFactor <= 0 && radius > MAX_BLUR_RADIUS) {
            downsampleFactor *= radius / MAX_BLUR_RADIUS;
            radius = MAX_BLUR_RADIUS;
        }
        mBlurParams[0] = downsampleFactor;
        mBlurParams[1] = radius;
        return mBlurParams;
    }

    protected Bitmap allocateBitmap(int width, int height) {
        return Utils.ensureSoftwareBitmap(
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
    }

    protected void releaseBitmap() {
        if (mBitmapToBlur != null) {
            mBitmapToBlur.recycle();
            mBitmapToBlur = null;
        }
        mBlurringCanvas = null;
        onBuffersReleased();
    }

    protected boolean beginClip(Canvas canvas, int width, int height) {
        if (hasCornerRadius()) {
            canvas.save();
            mClipRect.set(0, 0, width, height);
            updateClipPath(mClipRect);
            canvas.clipPath(mClipPath);
            return true;
        }
        return false;
    }

    protected void endClip(Canvas canvas, boolean clipped) {
        if (clipped) {
            canvas.restore();
        }
    }

    protected void updateClipPath(RectF rect) {
        Utils.roundedRectPath(rect,
                mTopLeftCornerRadius, mTopRightCornerRadius,
                mBottomLeftCornerRadius, mBottomRightCornerRadius,
                mClipPath);
    }

    protected boolean capture(int width, int height) {
        if (mBitmapToBlur == null || mBlurringCanvas == null) {
            return false;
        }
        mDecorView.getLocationOnScreen(mLocDecor);
        mHost.getLocationOnScreen(mLocSelf);
        int offsetX = mLocSelf[0] - mLocDecor[0];
        int offsetY = mLocSelf[1] - mLocDecor[1];

        mBitmapToBlur.eraseColor(0);
        int saveCount = mBlurringCanvas.save();
        mIsCapturing = true;
        Utils.sIsGlobalCapturing = true;
        try {
            float scaleX = (float) mBitmapToBlur.getWidth() / width;
            float scaleY = (float) mBitmapToBlur.getHeight() / height;
            mBlurringCanvas.scale(scaleX, scaleY);
            mBlurringCanvas.translate(-offsetX, -offsetY);
            Trace.beginSection("BlurEngine.captureDecor");
            try {
                mDecorView.draw(mBlurringCanvas);
            } catch (IllegalArgumentException e) {
                if (isHardwareBitmapError(e)) {
                    Log.w(TAG, "Hardware bitmap detected in capture, retrying with software rendering");
                    Utils.disableHardwareBitmapsInView(mDecorView);
                    try {
                        mBlurringCanvas.restoreToCount(saveCount);
                        saveCount = mBlurringCanvas.save();
                        mBlurringCanvas.scale(scaleX, scaleY);
                        mBlurringCanvas.translate(-offsetX, -offsetY);
                        mDecorView.draw(mBlurringCanvas);
                    } catch (Exception retryError) {
                        Log.e(TAG, "Software fallback also failed, switching to PixelCopy", retryError);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mUsePixelCopyFallback = true;
                            performPixelCopyBlur(width, height);
                        }
                        return false;
                    }
                } else {
                    throw e;
                }
            } catch (IndexOutOfBoundsException e) {
                Log.w(TAG, "Capture failed (IndexOutOfBounds), switching to PixelCopy", e);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mUsePixelCopyFallback = true;
                    performPixelCopyBlur(width, height);
                }
                return false;
            } finally {
                Trace.endSection();
            }
            Trace.beginSection("BlurEngine.compositeLiveViews");
            try {
                drawLiveViews(mDecorView, mBlurringCanvas);
            } finally {
                Trace.endSection();
            }
            return true;
        } finally {
            mIsCapturing = false;
            Utils.sIsGlobalCapturing = false;
            try {
                if (mBlurringCanvas != null && saveCount >= 0) {
                    mBlurringCanvas.restoreToCount(saveCount);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to restore capture canvas", e);
            }
        }
    }

    private boolean isHardwareBitmapError(IllegalArgumentException e) {
        String message = e.getMessage();
        return message != null && message.contains("Hardware");
    }

    private void initPixelCopyThread() {
        if (mPixelCopyThread != null) {
            return;
        }
        mPixelCopyThread = new HandlerThread("BlurEngine-PixelCopy");
        mPixelCopyThread.start();
        mPixelCopyHandler = new Handler(mPixelCopyThread.getLooper());
    }

    protected void performPixelCopyBlur(int width, int height) {
        Activity activity = getActivity();
        if (activity == null || mIsPixelCopyPending) {
            return;
        }
        initPixelCopyThread();
        float[] params = blurParams();
        int scaledWidth = Math.max(1, Math.round(width / params[0]));
        int scaledHeight = Math.max(1, Math.round(height / params[0]));
        if (mBitmapToBlur == null
                || mBitmapToBlur.getWidth() != scaledWidth
                || mBitmapToBlur.getHeight() != scaledHeight) {
            releaseBitmap();
            try {
                mBitmapToBlur = allocateBitmap(scaledWidth, scaledHeight);
                mBlurringCanvas = new Canvas(mBitmapToBlur);
            } catch (OutOfMemoryError | IllegalArgumentException e) {
                release();
                return;
            }
        }
        if (mDirty && mBlur.prepare(mBitmapToBlur, params[1])) {
            mDirty = false;
        }
        mIsPixelCopyPending = true;
        int traceCookie = ++mPixelCopyTraceCookie;
        Trace.beginAsyncSection("BlurEngine.pixelCopy", traceCookie);
        try {
            PixelCopy.request(activity.getWindow(), mBitmapToBlur,
                    copyResult -> {
                        Trace.endAsyncSection("BlurEngine.pixelCopy", traceCookie);
                        mIsPixelCopyPending = false;
                        if (copyResult == PixelCopy.SUCCESS) {
                            onCaptured(width, height);
                            mHost.invalidate();
                        } else {
                            Log.w(TAG, "PixelCopy failed with result: " + copyResult);
                        }
                    }, mPixelCopyHandler);
        } catch (Exception e) {
            Trace.endAsyncSection("BlurEngine.pixelCopy", traceCookie);
            mIsPixelCopyPending = false;
            Log.w(TAG, "PixelCopy request failed", e);
        }
    }

    protected void blurWithRetry(Bitmap input, Bitmap output) {
        Trace.beginSection("BlurEngine.nativeBlur");
        try {
            mBlur.blur(input, output);
        } catch (IllegalArgumentException e) {
            if (isHardwareBitmapError(e)) {
                Log.w(TAG, "Hardware bitmap during blur, retrying with software bitmaps");
                Bitmap softwareInput = Utils.ensureSoftwareBitmap(input);
                Bitmap softwareOutput = Utils.ensureSoftwareBitmap(output);
                if (mBlur.prepare(softwareInput, blurParams()[1])) {
                    mBlur.blur(softwareInput, softwareOutput);
                }
            } else {
                throw e;
            }
        } finally {
            Trace.endSection();
        }
    }

    protected void drawLiveViews(View view, Canvas canvas) {
        if (view instanceof TextureView) {
            TextureView textureView = (TextureView) view;
            if (textureView.getVisibility() == View.VISIBLE && textureView.isAvailable()) {
                mDecorView.getLocationOnScreen(mLocDecor);
                textureView.getLocationOnScreen(mLocSelf);

                int left = mLocSelf[0] - mLocDecor[0];
                int top = mLocSelf[1] - mLocDecor[1];

                Bitmap bitmap = textureView.getBitmap();
                if (bitmap != null) {
                    bitmap = Utils.ensureSoftwareBitmap(bitmap);
                    canvas.save();
                    canvas.translate(left, top);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    canvas.restore();
                    bitmap.recycle();
                }
            }
        } else if (view instanceof SurfaceView) {
            SurfaceView surfaceView = (SurfaceView) view;
            if (surfaceView.getVisibility() == View.VISIBLE) {
                if (!mConfiguredSurfaceViews.contains(surfaceView)) {
                    try {
                        surfaceView.setZOrderMediaOverlay(true);
                        Log.i(TAG, "Automatically configured SurfaceView with setZOrderMediaOverlay(true) for proper blur rendering");
                        mConfiguredSurfaceViews.add(surfaceView);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to auto-configure SurfaceView: " + e.getMessage());
                    }
                }

                if (!mSurfaceViewWarningLogged) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        Log.w(TAG, "SurfaceView blur requires Android 7.0+ (API 24). " +
                                "Current API level: " + Build.VERSION.SDK_INT + ". " +
                                "SurfaceView content will NOT be blurred. Consider using TextureView instead.");
                    } else {
                        Log.i(TAG, "SurfaceView detected and automatically configured for blur. " +
                                "Note: There may be a slight lag (1-2 frames) due to asynchronous PixelCopy.");
                    }
                    mSurfaceViewWarningLogged = true;
                }

                Bitmap cachedBitmap = mSurfaceViewBitmaps.get(surfaceView);
                if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
                    mDecorView.getLocationOnScreen(mLocDecor);
                    surfaceView.getLocationOnScreen(mLocSelf);

                    int left = mLocSelf[0] - mLocDecor[0];
                    int top = mLocSelf[1] - mLocDecor[1];

                    canvas.save();
                    canvas.translate(left, top);
                    canvas.drawBitmap(cachedBitmap, 0, 0, null);
                    canvas.restore();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        && !Boolean.TRUE.equals(mPendingPixelCopies.get(surfaceView))) {
                    if (surfaceView.getWidth() > 0 && surfaceView.getHeight() > 0) {
                        if (surfaceView.getHolder().getSurface() != null
                                && surfaceView.getHolder().getSurface().isValid()) {
                            final Bitmap bitmap = Bitmap.createBitmap(surfaceView.getWidth(),
                                    surfaceView.getHeight(), Bitmap.Config.ARGB_8888);
                            mPendingPixelCopies.put(surfaceView, true);
                            try {
                                Handler handler = mPixelCopyHandler != null ? mPixelCopyHandler : mHandler;
                                PixelCopy.request(surfaceView, bitmap, copyResult -> {
                                    mHandler.post(() -> {
                                        mPendingPixelCopies.put(surfaceView, false);
                                        if (copyResult == PixelCopy.SUCCESS) {
                                            Bitmap old = mSurfaceViewBitmaps.put(surfaceView, bitmap);
                                            if (old != null) {
                                                old.recycle();
                                            }
                                            mHost.invalidate();
                                        } else {
                                            Log.w(TAG, "PixelCopy failed. Result: " + copyResult);

                                            if (copyResult == PixelCopy.ERROR_SOURCE_NO_DATA
                                                    || copyResult == PixelCopy.ERROR_UNKNOWN
                                                    || copyResult == PixelCopy.ERROR_TIMEOUT) {
                                                mHost.postInvalidateDelayed(100);
                                            }
                                            bitmap.recycle();
                                        }
                                    });
                                }, handler);
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "PixelCopy request failed: " + e.getMessage() +
                                        ". Make sure surfaceView.setZOrderMediaOverlay(true) is called.");
                                mPendingPixelCopies.put(surfaceView, false);
                                bitmap.recycle();
                            }
                        } else {
                            mHost.postInvalidateDelayed(100);
                        }
                    }
                }
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                drawLiveViews(group.getChildAt(i), canvas);
            }
        }
    }

    @Nullable
    protected View findActivityDecorView() {
        Activity activity = getActivity();
        return activity != null ? activity.getWindow().getDecorView() : null;
    }

    @Nullable
    protected Activity getActivity() {
        Context context = mHost.getContext();
        for (int i = 0; i < 4 && !(context instanceof Activity) && context instanceof ContextWrapper; i++) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        return context instanceof Activity ? (Activity) context : null;
    }
}
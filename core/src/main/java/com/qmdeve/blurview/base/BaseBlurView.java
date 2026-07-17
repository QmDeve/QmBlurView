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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.tracing.Trace;

import com.qmdeve.blurview.Blur;
import com.qmdeve.blurview.BlurNative;
import com.qmdeve.blurview.util.Utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class BaseBlurView extends View {
    private static final String TAG = "BaseBlurView";

    protected int mOverlayColor;
    protected float mBlurRadius;
    protected float mDownsampleFactor = 0f;
    protected final Blur mBlur;
    protected boolean mDirty = true;
    protected Bitmap mBitmapToBlur;
    public Bitmap mBlurredBitmap;
    protected Canvas mBlurringCanvas;
    protected final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    public final Rect mRectSrc = new Rect();
    public final Rect mRectDst = new Rect();
    public View mDecorView;
    public boolean mDifferentRoot;
    protected boolean mIsRendering;
    public float mCornerRadius;
    public float mTopLeftCornerRadius;
    public float mTopRightCornerRadius;
    public float mBottomLeftCornerRadius;
    public float mBottomRightCornerRadius;
    public final RectF mClipRect = new RectF();
    public final Path mG3Path = new Path();

    private final Map<SurfaceView, Bitmap> mSurfaceViewBitmaps = new WeakHashMap<>();
    private final Map<SurfaceView, Boolean> mPendingPixelCopies = new WeakHashMap<>();
    private final Set<SurfaceView> mConfiguredSurfaceViews = Collections.newSetFromMap(new WeakHashMap<>());
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private android.os.HandlerThread mPixelCopyThread;
    private Handler mPixelCopyHandler;

    private boolean mFirstDraw = true;
    private boolean mForceRedraw = false;
    private boolean mSurfaceViewWarningLogged = false;
    private boolean mUsePixelCopyFallback = false;
    private boolean mIsPixelCopyPending = false;
    private int mPixelCopyTraceCookie = 0;

    public BaseBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBlur = new BlurNative();
        initPixelCopyThread();
        initAttributes(context, attrs);
    }

    private void initPixelCopyThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPixelCopyThread = new android.os.HandlerThread("BlurViewPixelCopy");
            mPixelCopyThread.start();
            mPixelCopyHandler = new Handler(mPixelCopyThread.getLooper());
        }
    }

    protected void initAttributes(Context context, AttributeSet attrs) {}

    /**
     * Ensure bitmap is software-compatible for blur processing.
     * Converts hardware bitmaps to software bitmaps to prevent
     * "Software rendering doesn't support hardware bitmaps" error.
     *
     * @param bitmap The bitmap to check
     * @return Software-compatible bitmap
     */
    private Bitmap ensureSoftwareBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        // Hardware bitmaps were introduced in Android O (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                Log.d(TAG, "Converting hardware bitmap to software bitmap for blur processing");
                try {
                    return bitmap.copy(Bitmap.Config.ARGB_8888, false);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to convert hardware bitmap: " + e.getMessage());
                    return bitmap; // Return original if copy fails
                }
            }
        }

        return bitmap;
    }

    /**
     * Recursively disable hardware bitmaps in a view hierarchy.
     * This prevents "Software rendering doesn't support hardware bitmaps" errors
     * when views are drawn onto software canvases for blur processing.
     */
    private void disableHardwareBitmapsInView(View view) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        try {
            // Handle ImageView specifically
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Drawable drawable = imageView.getDrawable();

                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    if (bitmap != null && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                        Log.d(TAG, "Converting hardware bitmap in ImageView to software");
                        Bitmap softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                        if (softwareBitmap != null) {
                            imageView.setImageBitmap(softwareBitmap);
                        }
                    }
                }
            }

            // Recursively process children if it's a ViewGroup
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    disableHardwareBitmapsInView(viewGroup.getChildAt(i));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disabling hardware bitmaps: " + e.getMessage());
        }
    }

    /**
     * TextureView/SurfaceView render into their own hardware surfaces, so the software
     * decor capture sees them as holes; this walk pastes their content into the capture.
     * One combined recursion instead of the previous two (one per view type): a view is
     * never both types, so each view triggers the same action as before — but the tree
     * is traversed once, not twice, per blur view per frame. Pasting follows tree order,
     * which matches window z-order (the old code drew all TextureViews first).
     */
    private void drawLiveViews(View view, Canvas canvas) {
        if (view instanceof TextureView) {
            TextureView textureView = (TextureView) view;
            if (textureView.getVisibility() == View.VISIBLE && textureView.isAvailable()) {
                int[] locDecor = new int[2];
                mDecorView.getLocationOnScreen(locDecor);

                int[] locTexture = new int[2];
                textureView.getLocationOnScreen(locTexture);

                int left = locTexture[0] - locDecor[0];
                int top = locTexture[1] - locDecor[1];

                Bitmap bitmap = textureView.getBitmap();
                if (bitmap != null) {
                    bitmap = ensureSoftwareBitmap(bitmap);
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
                // Automatically configure SurfaceView for proper z-ordering
                if (!mConfiguredSurfaceViews.contains(surfaceView)) {
                    try {
                        surfaceView.setZOrderMediaOverlay(true);
                        Log.i(TAG, "Automatically configured SurfaceView with setZOrderMediaOverlay(true) for proper blur rendering");
                        mConfiguredSurfaceViews.add(surfaceView);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to auto-configure SurfaceView: " + e.getMessage());
                    }
                }

                // Log helpful warning if SurfaceView blur might not work properly
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

                // Draw the last known bitmap if available
                Bitmap cachedBitmap = mSurfaceViewBitmaps.get(surfaceView);
                if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
                    int[] locDecor = new int[2];
                    mDecorView.getLocationOnScreen(locDecor);

                    int[] locSurface = new int[2];
                    surfaceView.getLocationOnScreen(locSurface);

                    int left = locSurface[0] - locDecor[0];
                    int top = locSurface[1] - locDecor[1];

                    canvas.save();
                    canvas.translate(left, top);
                    canvas.drawBitmap(cachedBitmap, 0, 0, null);
                    canvas.restore();
                }

                // Request a new snapshot if not already pending
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Boolean.TRUE.equals(mPendingPixelCopies.get(surfaceView))) {
                    if (surfaceView.getWidth() > 0 && surfaceView.getHeight() > 0) {
                        // Check if surface is valid before requesting PixelCopy
                        if (surfaceView.getHolder().getSurface() != null && surfaceView.getHolder().getSurface().isValid()) {
                            final Bitmap bitmap = Bitmap.createBitmap(surfaceView.getWidth(), surfaceView.getHeight(), Bitmap.Config.ARGB_8888);
                            mPendingPixelCopies.put(surfaceView, true);
                            try {
                                // Use dedicated handler for PixelCopy to avoid main thread contention
                                Handler handler = mPixelCopyHandler != null ? mPixelCopyHandler : mHandler;
                                PixelCopy.request(surfaceView, bitmap, copyResult -> {
                                    // Callback runs on handler thread, post to main thread for UI updates
                                    mHandler.post(() -> {
                                        mPendingPixelCopies.put(surfaceView, false);
                                        if (copyResult == PixelCopy.SUCCESS) {
                                            Bitmap old = mSurfaceViewBitmaps.put(surfaceView, bitmap);
                                            if (old != null) old.recycle(); // Recycle the old one
                                            invalidate();
                                        } else {
                                            Log.w(TAG, "PixelCopy failed. Result: " + copyResult);
                                            
                                            // Retry on common transient errors
                                            if (copyResult == PixelCopy.ERROR_SOURCE_NO_DATA || 
                                                copyResult == PixelCopy.ERROR_UNKNOWN || 
                                                copyResult == PixelCopy.ERROR_TIMEOUT) {
                                                postInvalidateDelayed(100);
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
                            // Surface not valid yet, try again later
                            postInvalidateDelayed(100);
                        }
                    }
                }
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                drawLiveViews(group.getChildAt(i), canvas);
            }
        }
    }

    public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            mDirty = true;
            mForceRedraw = true;
            invalidate();
        }
    }

    /**
     * Set the number of blur rounds (iterations) for BlurNative
     * More rounds = stronger blur effect
     * @param rounds Number of blur rounds (1-10)
     */
    public void setBlurRounds(int rounds) {
        if (mBlur instanceof BlurNative) {
            ((BlurNative) mBlur).setBlurRounds(rounds);
            mDirty = true;
            mForceRedraw = true;
            invalidate();
        }
    }

    /**
     * Get the current number of blur rounds
     * @return Current blur rounds, or -1 if not using BlurNative
     */
    public int getBlurRounds() {
        if (mBlur instanceof BlurNative) {
            return ((BlurNative) mBlur).getBlurRounds();
        }
        return -1;
    }

    public void setDownsampleFactor(float factor) {
        if (mDownsampleFactor != factor && factor >= 0) {
            mDownsampleFactor = factor;
            mDirty = true;
            mForceRedraw = true;
            invalidate();
        }
    }

    public void setOverlayColor(int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            mForceRedraw = true;
            invalidate();
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
            invalidate();
        }
    }

    public void setTopLeftCornerRadius(float radius) {
        if (mTopLeftCornerRadius != radius && radius >= 0) {
            mTopLeftCornerRadius = radius;
            mForceRedraw = true;
            invalidate();
        }
    }

    public void setTopRightCornerRadius(float radius) {
        if (mTopRightCornerRadius != radius && radius >= 0) {
            mTopRightCornerRadius = radius;
            mForceRedraw = true;
            invalidate();
        }
    }

    public void setBottomLeftCornerRadius(float radius) {
        if (mBottomLeftCornerRadius != radius && radius >= 0) {
            mBottomLeftCornerRadius = radius;
            mForceRedraw = true;
            invalidate();
        }
    }

    public void setBottomRightCornerRadius(float radius) {
        if (mBottomRightCornerRadius != radius && radius >= 0) {
            mBottomRightCornerRadius = radius;
            mForceRedraw = true;
            invalidate();
        }
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

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public Bitmap getBlurredBitmap() {
        return mBlurredBitmap;
    }

    public int getOverlayColor() {
        return mOverlayColor;
    }

    protected void releaseBitmap() {
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

    protected boolean prepare() {
        Trace.beginSection("BlurView.prepare");
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

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return false;

        int scaledWidth = Math.max(1, Math.round(width / downsampleFactor));
        int scaledHeight = Math.max(1, Math.round(height / downsampleFactor));

        boolean dirty = mDirty;

        if (mBlurredBitmap == null
                || mBlurredBitmap.getWidth() != scaledWidth
                || mBlurredBitmap.getHeight() != scaledHeight) {
            dirty = true;
            releaseBitmap();

            try {
                mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                mBlurringCanvas = new Canvas(mBitmapToBlur);
                mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

                // Ensure software bitmaps for compatibility
                mBitmapToBlur = Utils.ensureSoftwareBitmap(mBitmapToBlur);
                mBlurredBitmap = Utils.ensureSoftwareBitmap(mBlurredBitmap);
            } catch (OutOfMemoryError e) {
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

    protected void blur(Bitmap input, Bitmap output) {
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

    private void performPixelCopyBlur() {
        if (mIsPixelCopyPending || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        android.view.Window window = getActivityWindow();
        if (window == null) return;

        // Marks that the API>=O PixelCopy fallback branch is active (vs. the software captureDecorView branch).
        Trace.beginSection("BlurView.pixelCopyRequest");
        try {
            int[] locWindow = new int[2];
            getLocationInWindow(locWindow);

            // Rect to capture (in window coordinates)
            Rect rect = new Rect(locWindow[0], locWindow[1], locWindow[0] + getWidth(), locWindow[1] + getHeight());

            mIsPixelCopyPending = true;

            // Async span: PixelCopy is non-blocking, so measure request -> copy-complete across threads.
            final int cookie = ++mPixelCopyTraceCookie;
            Trace.beginAsyncSection("BlurView.pixelCopyAsync", cookie);

            try {
                Handler handler = mPixelCopyHandler != null ? mPixelCopyHandler : mHandler;
                // PixelCopy.request(Window) is available since API 24, but we use O (26) check for safety regarding hardware bitmaps
                PixelCopy.request(window, rect, mBitmapToBlur, copyResult -> {
                    // Runs on the PixelCopy handler thread once the copy finishes.
                    Trace.endAsyncSection("BlurView.pixelCopyAsync", cookie);
                    mHandler.post(() -> {
                        mIsPixelCopyPending = false;
                        if (copyResult == PixelCopy.SUCCESS) {
                            Trace.beginSection("BlurView.pixelCopyBlur");
                            try {
                                blur(mBitmapToBlur, mBlurredBitmap);
                                invalidate();
                            } finally {
                                Trace.endSection();
                            }
                        } else {
                            Log.w(TAG, "PixelCopy fallback failed: " + copyResult);
                        }
                    });
                }, handler);
            } catch (IllegalArgumentException e) {
                mIsPixelCopyPending = false;
                Trace.endAsyncSection("BlurView.pixelCopyAsync", cookie);
                Log.e(TAG, "PixelCopy fallback exception: " + e.getMessage());
            }
        } finally {
            Trace.endSection();
        }
    }

    private boolean performBlurSync() {
        if (!isShown() || mDecorView == null) return false;

        Trace.beginSection("BlurView.performBlurSync");
        try {
        Bitmap old = mBlurredBitmap;

        if (!prepare()) return false;

        boolean redrawBitmap = mBlurredBitmap != old;

        if (mUsePixelCopyFallback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            performPixelCopyBlur();
            return redrawBitmap || mDifferentRoot || mForceRedraw;
        }

        int[] locDecor = new int[2];
        int[] locSelf = new int[2];
        mDecorView.getLocationOnScreen(locDecor);
        getLocationOnScreen(locSelf);

        int offsetX = locSelf[0] - locDecor[0];
        int offsetY = locSelf[1] - locDecor[1];

        mBitmapToBlur.eraseColor(0);

        int saveCount = mBlurringCanvas.save();
        mIsRendering = true;
        Utils.sIsGlobalCapturing = true;
        try {
            float scaleX = 1f * mBitmapToBlur.getWidth() / getWidth();
            float scaleY = 1f * mBitmapToBlur.getHeight() / getHeight();
            mBlurringCanvas.scale(scaleX, scaleY);
            mBlurringCanvas.translate(-offsetX, -offsetY);

            Trace.beginSection("BlurView.captureDecorView");
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
                            mUsePixelCopyFallback = true;
                            performPixelCopyBlur();
                            return false;
                        }
                    }
                } else {
                    throw e;
                }
            } catch (IndexOutOfBoundsException e) {
                Log.w(Utils.TAG, "View hierarchy changed during blur operation: " + e.getMessage());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mUsePixelCopyFallback = true;
                    performPixelCopyBlur();
                    return false;
                }
                return false;
            } finally {
                Trace.endSection();
            }

            Trace.beginSection("BlurView.compositeSpecialViews");
            try {
                drawLiveViews(mDecorView, mBlurringCanvas);
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

        Trace.beginSection("BlurView.blur");
        try {
            blur(mBitmapToBlur, mBlurredBitmap);
        } finally {
            Trace.endSection();
        }

        return redrawBitmap || mDifferentRoot || mForceRedraw;
        } finally {
            Trace.endSection();
        }
    }

    public final ViewTreeObserver.OnPreDrawListener preDrawListener = () -> {
        if (!isShown()) return true;

        if (performBlurSync()) {
            postInvalidateOnAnimation();
        }

        mForceRedraw = false;
        return true;
    };

    public View getActivityDecorView() {
        Context ctx = getContext();
        for (int i = 0; i < 4 && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return (ctx instanceof Activity) ? ((Activity) ctx).getWindow().getDecorView() : null;
    }

    private android.view.Window getActivityWindow() {
        Context ctx = getContext();
        for (int i = 0; i < 4 && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return (ctx instanceof Activity) ? ((Activity) ctx).getWindow() : null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDecorView = getActivityDecorView();
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            mDifferentRoot = mDecorView.getRootView() != getRootView();
            mFirstDraw = true;
            mForceRedraw = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            mDecorView = null;
        }
        release();
        super.onDetachedFromWindow();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!mIsRendering) {
            if (mFirstDraw || mForceRedraw) {
                performBlurSync();
                mFirstDraw = false;
                mForceRedraw = false;
            }
            super.draw(canvas);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBlurredBitmap(canvas);
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

    public void drawBlurredBitmap(Canvas canvas) {
        if (Utils.sIsGlobalCapturing && !mIsRendering) {
            return;
        }
        Trace.beginSection("BlurView.drawResult");
        try {
        // The blurred bitmap and the overlay share one clip: previously each did its own
        // save → rebuild the rounded path → clipPath → draw → restore, rebuilding the
        // SAME bezier path and pushing the clip stack twice per frame. Both draws cover
        // the identical rounded rect, so one save/clip pair around both is equivalent.
        // Measured (Pixel 6): within benchmark noise — kept because it halves the
        // per-frame path builds/clip ops at zero risk, not for a headline win.
        mRectDst.set(0, 0, getWidth(), getHeight());
        boolean clipped = hasCornerRadius();
        if (clipped) {
            canvas.save();
            mClipRect.set(mRectDst);
            Trace.beginSection("BlurView.drawResult.clipPath");
            try {
                updatePath(mClipRect);
                canvas.clipPath(mG3Path);
            } finally {
                Trace.endSection();
            }
        }

        Trace.beginSection("BlurView.drawResult.bitmap");
        try {
        if (mBlurredBitmap != null) {
            mRectSrc.set(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());
            canvas.drawBitmap(mBlurredBitmap, mRectSrc, mRectDst, null);
        }
        } finally {
            Trace.endSection();
        }

        Trace.beginSection("BlurView.drawResult.overlay");
        try {
        mPaint.setColor(mOverlayColor);
        canvas.drawRect(mRectDst, mPaint);
        } finally {
            Trace.endSection();
        }

        if (clipped) {
            canvas.restore();
        }
        } finally {
            Trace.endSection();
        }
    }

    public void drawPreviewBackground(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;

        Paint previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStyle(Paint.Style.FILL);
        int previewColor = mOverlayColor;
        previewPaint.setColor(previewColor);
        if (hasCornerRadius()) {
            mClipRect.set(0, 0, getWidth(), getHeight());
            updatePath(mClipRect);
            canvas.drawPath(mG3Path, previewPaint);
        } else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), previewPaint);
        }
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }
}
package com.qmdeve.blurview.base;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import com.qmdeve.blurview.Blur;
import com.qmdeve.blurview.BlurNative;
import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

public class BaseBlurViewGroup {
    private int mOverlayColor;
    private float mBlurRadius;
    private final Blur mBlur;
    private boolean mDirty = true;
    private Bitmap mBitmapToBlur, mBlurredBitmap;
    private Canvas mBlurringCanvas;
    private boolean mIsRendering;
    private float mCornerRadius;
    private final RectF mClipRect = new RectF();
    private final Path mG3Path = new Path();
    private View mDecorView;
    private boolean mDifferentRoot;
    private View mHostView;
    private boolean mFirstDraw = true;
    private boolean mForceRedraw = false;
    private boolean mSkipNextPreDraw = false;

    public BaseBlurViewGroup(Context context, AttributeSet attrs) {
        mBlur = new BlurNative();
        initAttributes(context, attrs);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurView);
        mBlurRadius = a.getDimension(
                R.styleable.BlurView_blurRadius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics())
        );
        mOverlayColor = a.getColor(R.styleable.BlurView_overlayColor, 0xAAFFFFFF);
        mCornerRadius = a.getDimension(R.styleable.BlurView_cornerRadius, 0);
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
        if (mCornerRadius != radius && radius >= 0) {
            mCornerRadius = radius;
            mForceRedraw = true;
            if (mHostView != null) {
                mHostView.invalidate();
            }
        }
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
    }

    private boolean prepare(int width, int height) {
        if (mBlurRadius <= 0) {
            release();
            return false;
        }

        float downsampleFactor = 2.52f;
        float radius = mBlurRadius / downsampleFactor;
        if (radius > 25) {
            downsampleFactor *= radius / 25;
            radius = 25;
        }

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

        if (!prepare(width, height)) {
            return false;
        }

        Bitmap old = mBlurredBitmap;
        boolean redrawBitmap = mBlurredBitmap != old;

        int[] locDecor = new int[2];
        int[] locSelf = new int[2];
        mDecorView.getLocationOnScreen(locDecor);
        mHostView.getLocationOnScreen(locSelf);

        int offsetX = locSelf[0] - locDecor[0];
        int offsetY = locSelf[1] - locDecor[1];

        mBitmapToBlur.eraseColor(0);

        int saveCount = mBlurringCanvas.save();
        mIsRendering = true;
        try {
            float scaleX = 1f * mBitmapToBlur.getWidth() / width;
            float scaleY = 1f * mBitmapToBlur.getHeight() / height;
            mBlurringCanvas.scale(scaleX, scaleY);
            mBlurringCanvas.translate(-offsetX, -offsetY);

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
                        Log.e(Utils.TAG, "Retry after hardware bitmap conversion failed: " + retryError.getMessage());
                    }
                } else {
                    throw e;
                }
            }
        } finally {
            mIsRendering = false;
            mBlurringCanvas.restoreToCount(saveCount);
        }

        blur(mBitmapToBlur, mBlurredBitmap);

        return redrawBitmap || mDifferentRoot || mForceRedraw;
    }

    public boolean ensureBlurReady(int width, int height) {
        if (mFirstDraw || mForceRedraw) {
            boolean result = performBlurSync(width, height);
            mFirstDraw = false;
            mForceRedraw = false;
            return result;
        }
        return false;
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

    public void drawBlurredBitmap(Canvas canvas, int width, int height) {
        ensureBlurReady(width, height);

        if (mBlurredBitmap != null) {
            android.graphics.Rect srcRect = new android.graphics.Rect(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());
            android.graphics.Rect dstRect = new android.graphics.Rect(0, 0, width, height);

            if (mCornerRadius > 0) {
                canvas.save();
                mClipRect.set(dstRect);
                Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
                canvas.clipPath(mG3Path);
                canvas.drawBitmap(mBlurredBitmap, srcRect, dstRect, null);
                canvas.restore();
            } else {
                canvas.drawBitmap(mBlurredBitmap, srcRect, dstRect, null);
            }
        }

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(mOverlayColor);

        android.graphics.Rect dstRect = new android.graphics.Rect(0, 0, width, height);
        if (mCornerRadius > 0) {
            canvas.save();
            mClipRect.set(dstRect);
            Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
            canvas.clipPath(mG3Path);
            canvas.drawRect(dstRect, paint);
            canvas.restore();
        } else {
            canvas.drawRect(dstRect, paint);
        }
    }

    public void drawPreviewBackground(Canvas canvas, int width, int height) {
        if (width == 0 || height == 0) return;

        android.graphics.Paint previewPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStyle(android.graphics.Paint.Style.FILL);
        int previewColor = mOverlayColor;
        previewPaint.setColor(previewColor);
        if (mCornerRadius > 0) {
            mClipRect.set(0, 0, width, height);
            Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
            canvas.drawPath(mG3Path, previewPaint);
        } else {
            canvas.drawRect(0, 0, width, height, previewPaint);
        }
    }

    public void clipCanvasWithRoundedCorner(Canvas canvas, int width, int height) {
        mClipRect.set(0, 0, width, height);
        Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
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
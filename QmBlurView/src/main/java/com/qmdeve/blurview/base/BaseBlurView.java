package com.qmdeve.blurview.base;

import android.annotation.SuppressLint;
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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.Blur;
import com.qmdeve.blurview.BlurNative;
import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

public abstract class BaseBlurView extends View {
    protected int mOverlayColor;
    protected float mBlurRadius;
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
    public final RectF mClipRect = new RectF();
    public final Path mG3Path = new Path();

    public BaseBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBlur = new BlurNative();
        initAttributes(context, attrs);
    }

    protected void initAttributes(Context context, AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurView);
        mBlurRadius = a.getDimension(
                R.styleable.BlurView_blurRadius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())
        );
        mOverlayColor = a.getColor(R.styleable.BlurView_overlayColor, 0xAAFFFFFF);
        mCornerRadius = a.getDimension(R.styleable.BlurView_cornerRadius, 0);
        a.recycle();
    }

    public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            mDirty = true;
            invalidate();
        }
    }

    public void setOverlayColor(int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            invalidate();
        }
    }

    public void setCornerRadius(float radius) {
        if (mCornerRadius != radius && radius >= 0) {
            mCornerRadius = radius;
            invalidate();
        }
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
    }

    protected boolean prepare() {
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

    protected void blur(Bitmap input, Bitmap output) {
        mBlur.blur(input, output);
    }

    public final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (!isShown()) return true;

            Bitmap old = mBlurredBitmap;
            View decor = mDecorView;

            if (decor != null && prepare()) {
                boolean redrawBitmap = mBlurredBitmap != old;

                int[] locDecor = new int[2];
                int[] locSelf = new int[2];
                decor.getLocationOnScreen(locDecor);
                getLocationOnScreen(locSelf);

                int offsetX = locSelf[0] - locDecor[0];
                int offsetY = locSelf[1] - locDecor[1];

                mBitmapToBlur.eraseColor(0);

                int saveCount = mBlurringCanvas.save();
                mIsRendering = true;
                try {
                    float scaleX = 1f * mBitmapToBlur.getWidth() / getWidth();
                    float scaleY = 1f * mBitmapToBlur.getHeight() / getHeight();
                    mBlurringCanvas.scale(scaleX, scaleY);
                    mBlurringCanvas.translate(-offsetX, -offsetY);
                    decor.draw(mBlurringCanvas);
                } finally {
                    mIsRendering = false;
                    mBlurringCanvas.restoreToCount(saveCount);
                }

                blur(mBitmapToBlur, mBlurredBitmap);

                if (redrawBitmap || mDifferentRoot) {
                    postInvalidateOnAnimation();
                }
            }
            return true;
        }
    };

    public View getActivityDecorView() {
        Context ctx = getContext();
        for (int i = 0; i < 4 && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return (ctx instanceof Activity) ? ((Activity) ctx).getWindow().getDecorView() : null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDecorView = getActivityDecorView();
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            mDifferentRoot = mDecorView.getRootView() != getRootView();
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
        if (!mIsRendering) super.draw(canvas);
    }

    public void drawBlurredBitmap(Canvas canvas) {
        if (mBlurredBitmap != null) {
            mRectSrc.set(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());
            mRectDst.set(0, 0, getWidth(), getHeight());

            if (mCornerRadius > 0) {
                canvas.save();
                mClipRect.set(mRectDst);
                Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
                canvas.clipPath(mG3Path);
                canvas.drawBitmap(mBlurredBitmap, mRectSrc, mRectDst, null);
                canvas.restore();
            } else {
                canvas.drawBitmap(mBlurredBitmap, mRectSrc, mRectDst, null);
            }
        }

        mPaint.setColor(mOverlayColor);

        if (mCornerRadius > 0) {
            canvas.save();
            mClipRect.set(mRectDst);
            Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
            canvas.clipPath(mG3Path);
            canvas.drawRect(mRectDst, mPaint);
            canvas.restore();
        } else {
            canvas.drawRect(mRectDst, mPaint);
        }
    }

    public void drawPreviewBackground(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;

        Paint previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStyle(Paint.Style.FILL);
        int previewColor = mOverlayColor;
        previewPaint.setColor(previewColor);
        if (mCornerRadius > 0) {
            mClipRect.set(0, 0, getWidth(), getHeight());
            Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
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
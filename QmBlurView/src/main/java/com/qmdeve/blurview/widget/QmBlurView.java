package com.qmdeve.blurview.widget;

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

public class QmBlurView extends View {
    private float mDownsampleFactor;
    private int mOverlayColor;
    private float mBlurRadius;
    private final Blur mBlur;
    private boolean mDirty = true;
    private Bitmap mBitmapToBlur, mBlurredBitmap;
    private Canvas mBlurringCanvas;
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Rect mRectSrc = new Rect(), mRectDst = new Rect();
    private View mDecorView;
    private boolean mDifferentRoot;
    private boolean mIsRendering;
    private float mCornerRadius;
    private final Path mClipPath = new Path();
    private final RectF mClipRect = new RectF();

    public QmBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBlur = new BlurNative();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QmBlurView);
        mBlurRadius = a.getDimension(
                R.styleable.QmBlurView_qmBlurRadius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())
        );
        mDownsampleFactor = Math.max(1, a.getFloat(R.styleable.QmBlurView_qmDownsampleFactor, 2));
        mOverlayColor = a.getColor(R.styleable.QmBlurView_qmOverlayColor, 0xAAFFFFFF);
        mCornerRadius = a.getDimension(R.styleable.QmBlurView_qmCornerRadius, 0);
        a.recycle();
    }

    public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            mDirty = true;
            invalidate();
        }
    }

    public void setDownsampleFactor(float factor) {
        if (factor > 0 && mDownsampleFactor != factor) {
            mDownsampleFactor = factor;
            mDirty = true;
            releaseBitmap();
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

    protected boolean prepare() {
        if (mBlurRadius <= 0) {
            release();
            return false;
        }

        float downsampleFactor = mDownsampleFactor;
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

    private final ViewTreeObserver.OnPreDrawListener preDrawListener = () -> {
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
    };

    private View getActivityDecorView() {
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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBlurredBitmap(canvas);
    }

    private void drawBlurredBitmap(Canvas canvas) {
        if (mBlurredBitmap != null) {
            mRectSrc.set(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());
            mRectDst.set(0, 0, getWidth(), getHeight());

            if (mCornerRadius > 0) {
                canvas.save();
                mClipRect.set(mRectDst);
                mClipPath.reset();
                mClipPath.addRoundRect(mClipRect, mCornerRadius, mCornerRadius, Path.Direction.CW);
                canvas.clipPath(mClipPath);
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
            mClipPath.reset();
            mClipPath.addRoundRect(mClipRect, mCornerRadius, mCornerRadius, Path.Direction.CW);
            canvas.clipPath(mClipPath);
            canvas.drawRect(mRectDst, mPaint);
            canvas.restore();
        } else {
            canvas.drawRect(mRectDst, mPaint);
        }
    }
}
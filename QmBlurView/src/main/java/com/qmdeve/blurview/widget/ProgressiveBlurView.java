package com.qmdeve.blurview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.R;

public class ProgressiveBlurView extends BlurView {
    public static final int DIRECTION_BOTTOM_TO_TOP = 0;
    public static final int DIRECTION_TOP_TO_BOTTOM = 1;
    public static final int DIRECTION_RIGHT_TO_LEFT = 2;
    public static final int DIRECTION_LEFT_TO_RIGHT = 3;
    private final Rect mRectSrc = new Rect(), mRectDst = new Rect();
    private int mGradientDirection = DIRECTION_TOP_TO_BOTTOM;
    private final Paint mBlendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mOverlayColor;
    private float mBlurRadius = 25f;

    public ProgressiveBlurView(Context context) {
        this(context, null);
    }

    public ProgressiveBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setCornerRadius(0);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressiveBlurView);
            mGradientDirection = a.getInt(R.styleable.ProgressiveBlurView_progressiveDirection, DIRECTION_TOP_TO_BOTTOM);
            mOverlayColor = a.getInt(R.styleable.ProgressiveBlurView_progressiveOverlayColor, 0xAAFFFFFF);
            mBlurRadius = a.getDimension(
                    R.styleable.ProgressiveBlurView_progressiveBlurRadius,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics())
            );
            a.recycle();
        }

        super.setBlurRadius(mBlurRadius);
        mBlendPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    public void setGradientDirection(int direction) {
        if (direction >= DIRECTION_TOP_TO_BOTTOM && direction <= DIRECTION_RIGHT_TO_LEFT) {
            if (mGradientDirection != direction) {
                mGradientDirection = direction;
                invalidate();
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()) {
            drawPreviewProgressiveBackground(canvas);
            return;
        }

        Bitmap blurredBitmap = getBlurredBitmap();
        if (blurredBitmap == null) return;

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        int saveCount = canvas.saveLayer(0, 0, width, height, null);

        mRectSrc.set(0, 0, blurredBitmap.getWidth(), blurredBitmap.getHeight());
        mRectDst.set(0, 0, width, height);
        canvas.drawBitmap(blurredBitmap, mRectSrc, mRectDst, null);
        LinearGradient gradient = createIntensityGradient(width, height);
        mBlendPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, mBlendPaint);
        LinearGradient overlayGradient = createOverlayGradient(width, height);
        mOverlayPaint.setShader(overlayGradient);
        canvas.drawRect(0, 0, width, height, mOverlayPaint);

        canvas.restoreToCount(saveCount);
    }

    private LinearGradient createIntensityGradient(int width, int height) {
        int endAlpha = (int) (255 * 1f);
        int[] colors = new int[]{Color.argb(0, 0, 0, 0), Color.argb(endAlpha, 0, 0, 0)};
        float[] pos = new float[]{0f, 1f};

        switch (mGradientDirection) {
            case DIRECTION_BOTTOM_TO_TOP:
                return new LinearGradient(0, height, 0, 0, colors, pos, Shader.TileMode.CLAMP);
            case DIRECTION_LEFT_TO_RIGHT:
                return new LinearGradient(0, 0, width, 0, colors, pos, Shader.TileMode.CLAMP);
            case DIRECTION_RIGHT_TO_LEFT:
                return new LinearGradient(width, 0, 0, 0, colors, pos, Shader.TileMode.CLAMP);
            default:
                return new LinearGradient(0, 0, 0, height, colors, pos, Shader.TileMode.CLAMP);
        }
    }

    private LinearGradient createOverlayGradient(int width, int height) {
        int transparentColor = mOverlayColor & 0x00FFFFFF;
        int solidColor = mOverlayColor;

        switch (mGradientDirection) {
            case DIRECTION_BOTTOM_TO_TOP:
                return new LinearGradient(0, height, 0, 0, new int[]{transparentColor, solidColor}, new float[]{0f, 1f}, Shader.TileMode.CLAMP);
            case DIRECTION_LEFT_TO_RIGHT:
                return new LinearGradient(0, 0, width, 0, new int[]{transparentColor, solidColor}, new float[]{0f, 1f}, Shader.TileMode.CLAMP);
            case DIRECTION_RIGHT_TO_LEFT:
                return new LinearGradient(width, 0, 0, 0, new int[]{transparentColor, solidColor}, new float[]{0f, 1f}, Shader.TileMode.CLAMP);
            default:
                return new LinearGradient(0, 0, 0, height, new int[]{transparentColor, solidColor}, new float[]{0f, 1f}, Shader.TileMode.CLAMP);
        }
    }

    private void drawPreviewProgressiveBackground(Canvas canvas) {
        int width = getWidth(), height = getHeight();
        Paint p = new Paint();
        LinearGradient g = createOverlayGradient(width, height);
        p.setShader(g);
        canvas.drawRect(0, 0, width, height, p);
    }

    @Override
    public void setCornerRadius(float radius) {
        super.setCornerRadius(0);
    }

    @Override
    public void setOverlayColor(int color) {
    }

    @Override
    public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            super.setBlurRadius(radius);
            invalidate();
        }
    }
}
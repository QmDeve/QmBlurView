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
 * Created Date: 2026-06-06
 * Author: Donny Yale
 * GitHub: https://github.com/QmDeve/QmBlurView
 * Website: https://blurview.qmdeve.com
 * ===========================================
 */

package com.qmdeve.blurview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.tracing.Trace;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

public class ProgressiveBlurViewGroup extends BlurViewGroup {
    public static final int DIRECTION_BOTTOM_TO_TOP = 0;
    public static final int DIRECTION_TOP_TO_BOTTOM = 1;
    public static final int DIRECTION_RIGHT_TO_LEFT = 2;
    public static final int DIRECTION_LEFT_TO_RIGHT = 3;

    private final Rect mRectSrc = new Rect();
    private final Rect mRectDst = new Rect();
    private final RectF mClipRect = new RectF();
    private final Path mClipPath = new Path();
    private final Paint mBlendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LinearGradient mCachedIntensityGradient;
    private LinearGradient mCachedOverlayGradient;
    private int mCachedIntensityWidth = -1;
    private int mCachedIntensityHeight = -1;
    private int mCachedIntensityDirection = -1;
    private int mCachedOverlayWidth = -1;
    private int mCachedOverlayHeight = -1;
    private int mCachedOverlayDirection = -1;
    private int mCachedOverlayColor = 0;
    private int mGradientDirection = DIRECTION_TOP_TO_BOTTOM;
    private int mOverlayColor = 0xAAFFFFFF;
    private float mBlurRadius = 25f;

    public ProgressiveBlurViewGroup(Context context) {
        this(context, null);
    }

    public ProgressiveBlurViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setCornerRadius(0);

        mGradientDirection = DIRECTION_TOP_TO_BOTTOM;
        mOverlayColor = 0xAAFFFFFF;
        mBlurRadius = Utils.dp2px(getResources(), 25);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressiveBlurView);
            mGradientDirection = a.getInt(
                    R.styleable.ProgressiveBlurView_progressiveDirection,
                    DIRECTION_TOP_TO_BOTTOM
            );
            mOverlayColor = a.getInt(
                    R.styleable.ProgressiveBlurView_progressiveOverlayColor,
                    0xAAFFFFFF
            );
            mBlurRadius = a.getDimension(
                    R.styleable.ProgressiveBlurView_progressiveBlurRadius,
                    Utils.dp2px(getResources(), 25)
            );
            a.recycle();
        }

        super.setBlurRadius(mBlurRadius);
        mBlendPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    public void setGradientDirection(int direction) {
        if (direction < DIRECTION_BOTTOM_TO_TOP || direction > DIRECTION_LEFT_TO_RIGHT) {
            return;
        }

        if (mGradientDirection != direction) {
            mGradientDirection = direction;
            invalidateGradientCache();
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        invalidateGradientCache();
    }

    @Override
    protected void drawBlurLayer(@NonNull Canvas canvas, int width, int height, boolean isEditMode, boolean shouldDrawBlur) {
        if (width == 0 || height == 0) {
            return;
        }

        boolean hasCornerRadius = hasAnyCornerRadius();
        if (hasCornerRadius) {
            canvas.save();
            clipCanvasWithRoundedCorners(canvas, width, height);
        }

        if (isEditMode) {
            drawPreviewProgressiveBackground(canvas, width, height);
        } else if (shouldDrawBlur) {
            drawProgressiveBlur(canvas, width, height);
        }

        if (hasCornerRadius) {
            canvas.restore();
        }
    }

    private void drawProgressiveBlur(Canvas canvas, int width, int height) {
        Bitmap blurredBitmap = getBlurredBitmap();
        if (blurredBitmap == null) {
            return;
        }

        Trace.beginSection("ProgressiveBlurViewGroup.compositeGradient");
        try {
        int saveCount = canvas.saveLayer(0, 0, width, height, null);

        mRectSrc.set(0, 0, blurredBitmap.getWidth(), blurredBitmap.getHeight());
        mRectDst.set(0, 0, width, height);
        canvas.drawBitmap(blurredBitmap, mRectSrc, mRectDst, null);

        mBlendPaint.setShader(createIntensityGradient(width, height));
        canvas.drawRect(0, 0, width, height, mBlendPaint);

        mOverlayPaint.setShader(createOverlayGradient(width, height));
        canvas.drawRect(0, 0, width, height, mOverlayPaint);

        canvas.restoreToCount(saveCount);
        } finally {
            Trace.endSection();
        }
    }

    private LinearGradient createIntensityGradient(int width, int height) {
        if (mCachedIntensityGradient != null
                && mCachedIntensityWidth == width
                && mCachedIntensityHeight == height
                && mCachedIntensityDirection == mGradientDirection) {
            return mCachedIntensityGradient;
        }

        int[] colors = new int[]{Color.argb(0, 0, 0, 0), Color.argb(255, 0, 0, 0)};
        float[] positions = new float[]{0f, 1f};

        switch (mGradientDirection) {
            case DIRECTION_BOTTOM_TO_TOP:
                mCachedIntensityGradient = new LinearGradient(0, height, 0, 0, colors, positions, Shader.TileMode.CLAMP);
                break;
            case DIRECTION_LEFT_TO_RIGHT:
                mCachedIntensityGradient = new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP);
                break;
            case DIRECTION_RIGHT_TO_LEFT:
                mCachedIntensityGradient = new LinearGradient(width, 0, 0, 0, colors, positions, Shader.TileMode.CLAMP);
                break;
            default:
                mCachedIntensityGradient = new LinearGradient(0, 0, 0, height, colors, positions, Shader.TileMode.CLAMP);
                break;
        }

        mCachedIntensityWidth = width;
        mCachedIntensityHeight = height;
        mCachedIntensityDirection = mGradientDirection;
        return mCachedIntensityGradient;
    }

    private LinearGradient createOverlayGradient(int width, int height) {
        if (mCachedOverlayGradient != null
                && mCachedOverlayWidth == width
                && mCachedOverlayHeight == height
                && mCachedOverlayDirection == mGradientDirection
                && mCachedOverlayColor == mOverlayColor) {
            return mCachedOverlayGradient;
        }

        int transparentColor = mOverlayColor & 0x00FFFFFF;
        int solidColor = mOverlayColor;

        switch (mGradientDirection) {
            case DIRECTION_BOTTOM_TO_TOP:
                mCachedOverlayGradient = new LinearGradient(
                        0,
                        height,
                        0,
                        0,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                );
                break;
            case DIRECTION_LEFT_TO_RIGHT:
                mCachedOverlayGradient = new LinearGradient(
                        0,
                        0,
                        width,
                        0,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                );
                break;
            case DIRECTION_RIGHT_TO_LEFT:
                mCachedOverlayGradient = new LinearGradient(
                        width,
                        0,
                        0,
                        0,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                );
                break;
            default:
                mCachedOverlayGradient = new LinearGradient(
                        0,
                        0,
                        0,
                        height,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                );
                break;
        }

        mCachedOverlayWidth = width;
        mCachedOverlayHeight = height;
        mCachedOverlayDirection = mGradientDirection;
        mCachedOverlayColor = mOverlayColor;
        return mCachedOverlayGradient;
    }

    private void drawPreviewProgressiveBackground(Canvas canvas, int width, int height) {
        mOverlayPaint.setShader(createOverlayGradient(width, height));
        canvas.drawRect(0, 0, width, height, mOverlayPaint);
    }

    private void invalidateGradientCache() {
        mCachedIntensityGradient = null;
        mCachedOverlayGradient = null;
    }

    private boolean hasAnyCornerRadius() {
        return getTopLeftCornerRadius() > 0
                || getTopRightCornerRadius() > 0
                || getBottomLeftCornerRadius() > 0
                || getBottomRightCornerRadius() > 0;
    }

    private void clipCanvasWithRoundedCorners(Canvas canvas, int width, int height) {
        mClipRect.set(0, 0, width, height);
        mClipPath.reset();
        Utils.roundedRectPath(
                mClipRect,
                getTopLeftCornerRadius(),
                getTopRightCornerRadius(),
                getBottomLeftCornerRadius(),
                getBottomRightCornerRadius(),
                mClipPath
        );
        canvas.clipPath(mClipPath);
    }

    @Override
    public void setCornerRadius(float radius) {
        super.setCornerRadius(0);
    }

    @Override
    public void setOverlayColor(int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            mCachedOverlayGradient = null;
            invalidate();
        }
    }

    public void setOverlayColorRes(@ColorRes int colorResId) {
        int color;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            color = getContext().getColor(colorResId);
        } else {
            color = getResources().getColor(colorResId);
        }

        if (mOverlayColor != color) {
            mOverlayColor = color;
            mCachedOverlayGradient = null;
            invalidate();
        }
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
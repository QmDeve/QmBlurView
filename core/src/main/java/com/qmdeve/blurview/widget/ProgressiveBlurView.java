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

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.tracing.Trace;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

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
            mBlurRadius = a.getDimension(R.styleable.ProgressiveBlurView_progressiveBlurRadius, Utils.dp2px(getResources(), 25));
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
        if (Utils.sIsGlobalCapturing && !mIsRendering) {
            return;
        }

        if (isInEditMode()) {
            drawPreviewProgressiveBackground(canvas);
            return;
        }

        Bitmap blurredBitmap = getBlurredBitmap();
        if (blurredBitmap == null) return;

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        Trace.beginSection("ProgressiveBlurView.compositeGradient");
        try {
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
        } finally {
            Trace.endSection();
        }
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
        if (mOverlayColor != color) {
            mOverlayColor = color;
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
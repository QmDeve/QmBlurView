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

package com.qmdeve.blurview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.tracing.Trace;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.engine.BlurEngine;
import com.qmdeve.blurview.engine.BlurEngines;
import com.qmdeve.blurview.util.Utils;

public class ProgressiveBlurView extends View {
    public static final int DIRECTION_BOTTOM_TO_TOP = 0;
    public static final int DIRECTION_TOP_TO_BOTTOM = 1;
    public static final int DIRECTION_RIGHT_TO_LEFT = 2;
    public static final int DIRECTION_LEFT_TO_RIGHT = 3;
    private int mGradientDirection = DIRECTION_TOP_TO_BOTTOM;
    private final Paint mOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPreviewPaint = new Paint();
    private final ProgressiveCompositor mCompositor = new ProgressiveCompositor();
    private int mOverlayColor;
    private float mBlurRadius = 25f;
    private LinearGradient mCachedOverlayGradient;
    private int mCachedOverlayW, mCachedOverlayH, mCachedOverlayDir = -1, mCachedOverlayColor;
    protected final BlurEngine mBlurEngine;

    public ProgressiveBlurView(Context context) {
        this(context, null);
    }

    public ProgressiveBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBlurEngine = BlurEngines.createNative(this);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mBlurEngine.setCornerRadius(0);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressiveBlurView);
            mGradientDirection = a.getInt(R.styleable.ProgressiveBlurView_progressiveDirection, DIRECTION_TOP_TO_BOTTOM);
            mOverlayColor = a.getInt(R.styleable.ProgressiveBlurView_progressiveOverlayColor, 0xAAFFFFFF);
            mBlurRadius = a.getDimension(R.styleable.ProgressiveBlurView_progressiveBlurRadius, Utils.dp2px(getResources(), 25));
            a.recycle();
        }

        mBlurEngine.setBlurRadius(mBlurRadius);
        mBlurEngine.setMaxFps(30);
    }

    public void setGradientDirection(int direction) {
        if (direction >= DIRECTION_BOTTOM_TO_TOP && direction <= DIRECTION_LEFT_TO_RIGHT) {
            if (mGradientDirection != direction) {
                mGradientDirection = direction;
                invalidate();
            }
        }
    }

    public int getGradientDirection() {
        return mGradientDirection;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (Utils.sIsGlobalCapturing && !mBlurEngine.isCapturing()) {
            return;
        }

        if (isInEditMode()) {
            drawPreviewProgressiveBackground(canvas);
            return;
        }

        Bitmap blurredBitmap = mBlurEngine.getBlurredBitmap();
        if (blurredBitmap == null || blurredBitmap.isRecycled()) return;

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        Trace.beginSection("ProgressiveBlurView.compositeGradient");
        try {
            mCompositor.draw(canvas, blurredBitmap, width, height, mGradientDirection);
            mOverlayPaint.setShader(overlayGradient(width, height));
            canvas.drawRect(0, 0, width, height, mOverlayPaint);
        } finally {
            Trace.endSection();
        }
    }

    private LinearGradient overlayGradient(int width, int height) {
        if (mCachedOverlayGradient == null
                || width != mCachedOverlayW || height != mCachedOverlayH
                || mGradientDirection != mCachedOverlayDir
                || mOverlayColor != mCachedOverlayColor) {
            mCachedOverlayGradient = createOverlayGradient(width, height);
            mCachedOverlayW = width;
            mCachedOverlayH = height;
            mCachedOverlayDir = mGradientDirection;
            mCachedOverlayColor = mOverlayColor;
        }
        return mCachedOverlayGradient;
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
        mPreviewPaint.setShader(overlayGradient(width, height));
        canvas.drawRect(0, 0, width, height, mPreviewPaint);
    }

        public void setCornerRadius(float radius) {
        mBlurEngine.setCornerRadius(0);
    }

        public void setOverlayColor(@ColorInt int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            invalidate();
        }
    }

    @ColorInt
    public int getOverlayColor() {
        return mOverlayColor;
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

        public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            mBlurEngine.setBlurRadius(radius);
            invalidate();
        }
    }

    public float getBlurRadius() {
        return mBlurRadius;
    }

    public void setBlurRounds(int rounds) {
        mBlurEngine.setBlurRounds(rounds);
    }

    public int getBlurRounds() {
        return mBlurEngine.getBlurRounds();
    }

    public void setDownsampleFactor(float factor) {
        mBlurEngine.setDownsampleFactor(factor);
    }

    public float getDownsampleFactor() {
        return mBlurEngine.getDownsampleFactor();
    }

    public void setMaxFps(int fps) {
        mBlurEngine.setMaxFps(fps);
    }

    public int getMaxFps() {
        return mBlurEngine.getMaxFps();
    }

    @Nullable
    public Bitmap getBlurredBitmap() {
        return mBlurEngine.getBlurredBitmap();
    }

    public void updateBlurImmediately() {
        mBlurEngine.updateBlurImmediately();
    }

    public void release() {
        mCompositor.release();
        mBlurEngine.release();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!mBlurEngine.isCapturing()) {
            super.draw(canvas);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBlurEngine.attach();
    }

    @Override
    protected void onDetachedFromWindow() {
        mBlurEngine.detach();
        super.onDetachedFromWindow();
    }
}
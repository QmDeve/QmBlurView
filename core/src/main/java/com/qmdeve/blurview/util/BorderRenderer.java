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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.qmdeve.blurview.engine.BlurEngine;
import com.qmdeve.blurview.util.Utils;

final class BorderRenderer {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();
    private final Path mPath = new Path();

    private float mWidth;
    private int mColor = Color.TRANSPARENT;

    BorderRenderer() {
        mPaint.setStyle(Paint.Style.STROKE);
    }

    void setWidth(float widthPx) {
        mWidth = Math.max(0f, widthPx);
        mPaint.setStrokeWidth(mWidth);
    }

    float getWidth() {
        return mWidth;
    }

    void setColor(@ColorInt int color) {
        mColor = color;
        mPaint.setColor(color);
    }

    @ColorInt
    int getColor() {
        return mColor;
    }

    boolean hasBorder() {
        return mWidth > 0 && mColor != Color.TRANSPARENT;
    }

    void draw(@NonNull Canvas canvas, @NonNull BlurEngine engine, int width, int height) {
        if (!hasBorder() || width <= 0 || height <= 0) {
            return;
        }
        float half = mWidth / 2f;
        mRect.set(half, half, width - half, height - half);
        Utils.roundedRectPath(mRect,
                engine.getTopLeftCornerRadius() - half,
                engine.getTopRightCornerRadius() - half,
                engine.getBottomLeftCornerRadius() - half,
                engine.getBottomRightCornerRadius() - half,
                mPath);
        canvas.drawPath(mPath, mPaint);
    }
}
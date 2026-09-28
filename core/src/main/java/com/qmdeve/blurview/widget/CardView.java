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
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

public class CardView extends BlurViewGroup {

    private final BorderRenderer mBorder = new BorderRenderer();

    public CardView(Context context) {
        this(context, null);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardView);
            mBorder.setWidth(a.getDimension(
                    R.styleable.CardView_cardBorderWidth, 0f));
            mBorder.setColor(a.getColor(
                    R.styleable.CardView_cardBorderColor, Color.TRANSPARENT));
            float cardElevation = a.getDimension(
                    R.styleable.CardView_cardElevation, 0f);
            a.recycle();

            if (cardElevation > 0) {
                setElevation(cardElevation);
            }
        }
    }

    @Override
    protected void drawBlurLayer(@NonNull Canvas canvas, int width, int height,
                                 boolean isEditMode, boolean shouldDrawBlur) {
        super.drawBlurLayer(canvas, width, height, isEditMode, shouldDrawBlur);
        mBorder.draw(canvas, mBlurEngine, width, height);
    }

    public void setBorderWidth(float widthPx) {
        if (mBorder.getWidth() != widthPx) {
            mBorder.setWidth(widthPx);
            invalidate();
        }
    }

    public float getBorderWidth() {
        return mBorder.getWidth();
    }

    public void setBorderColor(@ColorInt int color) {
        if (mBorder.getColor() != color) {
            mBorder.setColor(color);
            invalidate();
        }
    }

    @ColorInt
    public int getBorderColor() {
        return mBorder.getColor();
    }

    public void setBorderWidthDp(float widthDp) {
        setBorderWidth(Utils.dp2px(getResources(), widthDp));
    }
}
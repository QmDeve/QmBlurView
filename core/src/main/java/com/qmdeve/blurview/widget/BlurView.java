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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.qmdeve.blurview.engine.BlurAttrs;
import com.qmdeve.blurview.engine.BlurEngine;
import com.qmdeve.blurview.engine.BlurEngines;

public class BlurView extends View {

    protected final BlurEngine mBlurEngine;

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBlurEngine = createBlurEngine();
        BlurAttrs.apply(mBlurEngine, context, attrs);
    }

    protected BlurEngine createBlurEngine() {
        return BlurEngines.create(this);
    }

    public void setBlurRadius(float radius) {
        mBlurEngine.setBlurRadius(radius);
    }

    public float getBlurRadius() {
        return mBlurEngine.getBlurRadius();
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

    public void setOverlayColor(@ColorInt int color) {
        mBlurEngine.setOverlayColor(color);
    }

    @ColorInt
    public int getOverlayColor() {
        return mBlurEngine.getOverlayColor();
    }

    public void setCornerRadius(float radius) {
        mBlurEngine.setCornerRadius(radius);
    }

    public float getCornerRadius() {
        return mBlurEngine.getCornerRadius();
    }

    public void setTopLeftCornerRadius(float radius) {
        mBlurEngine.setTopLeftCornerRadius(radius);
    }

    public float getTopLeftCornerRadius() {
        return mBlurEngine.getTopLeftCornerRadius();
    }

    public void setTopRightCornerRadius(float radius) {
        mBlurEngine.setTopRightCornerRadius(radius);
    }

    public float getTopRightCornerRadius() {
        return mBlurEngine.getTopRightCornerRadius();
    }

    public void setBottomLeftCornerRadius(float radius) {
        mBlurEngine.setBottomLeftCornerRadius(radius);
    }

    public float getBottomLeftCornerRadius() {
        return mBlurEngine.getBottomLeftCornerRadius();
    }

    public void setBottomRightCornerRadius(float radius) {
        mBlurEngine.setBottomRightCornerRadius(radius);
    }

    public float getBottomRightCornerRadius() {
        return mBlurEngine.getBottomRightCornerRadius();
    }

    public boolean hasCornerRadius() {
        return mBlurEngine.hasCornerRadius();
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
        mBlurEngine.release();
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

    @Override
    public void draw(Canvas canvas) {
        if (!mBlurEngine.isCapturing()) {
            super.draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            mBlurEngine.drawPreview(canvas, getWidth(), getHeight());
        } else {
            mBlurEngine.drawBlur(canvas, getWidth(), getHeight());
        }
    }
}
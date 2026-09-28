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

package com.qmdeve.blurview.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public interface BlurEngine {

    void attach();
    void detach();
    void release();
    void setBlurRadius(float radius);
    float getBlurRadius();
    
    void setBlurRounds(int rounds);
    int getBlurRounds();
    
    void setDownsampleFactor(float factor);
    float getDownsampleFactor();

    void setOverlayColor(@ColorInt int color);
    
    @ColorInt
    int getOverlayColor();

    void setCornerRadius(float radius);
    float getCornerRadius();

    void setTopLeftCornerRadius(float radius);
    float getTopLeftCornerRadius();

    void setTopRightCornerRadius(float radius);
    float getTopRightCornerRadius();

    void setBottomLeftCornerRadius(float radius);
    float getBottomLeftCornerRadius();

    void setBottomRightCornerRadius(float radius);
    float getBottomRightCornerRadius();

    boolean hasCornerRadius();

    void setMaxFps(int fps);

    int getMaxFps();

    boolean sync(boolean forced);

    void updateBlurImmediately();

    void drawBlur(Canvas canvas, int width, int height);

    void drawPreview(Canvas canvas, int width, int height);

    void clipRoundedCorners(Canvas canvas, int width, int height);

    @Nullable
    Bitmap getBlurredBitmap();

    boolean isCapturing();

    boolean isUsingRenderEffect();
}
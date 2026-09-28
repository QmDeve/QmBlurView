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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

public final class BlurAttrs {

    private BlurAttrs() {
    }

    public static void apply(BlurEngine engine, Context context, AttributeSet attrs) {
        apply(engine, context, attrs, 25f, 0xAAFFFFFF);
    }

    public static void apply(BlurEngine engine, Context context, AttributeSet attrs,
                             float defaultRadiusDp, @ColorInt int defaultOverlay) {
        if (attrs == null) {
            engine.setBlurRadius(Utils.dp2px(context.getResources(), defaultRadiusDp));
            engine.setOverlayColor(defaultOverlay);
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurView);
        try {
            engine.setBlurRadius(a.getDimension(R.styleable.BlurView_blurRadius,
                    Utils.dp2px(context.getResources(), defaultRadiusDp)));
            engine.setOverlayColor(a.getColor(R.styleable.BlurView_overlayColor, defaultOverlay));

            float cornerRadius = a.getDimension(R.styleable.BlurView_cornerRadius, 0f);
            engine.setCornerRadius(cornerRadius);
            engine.setTopLeftCornerRadius(a.getDimension(
                    R.styleable.BlurView_topLeftCornerRadius, cornerRadius));
            engine.setTopRightCornerRadius(a.getDimension(
                    R.styleable.BlurView_topRightCornerRadius, cornerRadius));
            engine.setBottomLeftCornerRadius(a.getDimension(
                    R.styleable.BlurView_bottomLeftCornerRadius, cornerRadius));
            engine.setBottomRightCornerRadius(a.getDimension(
                    R.styleable.BlurView_bottomRightCornerRadius, cornerRadius));

            engine.setDownsampleFactor(a.getFloat(R.styleable.BlurView_downsampleFactor, 0f));
        } finally {
            a.recycle();
        }
    }
}
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.base.BaseBlurView;
import com.qmdeve.blurview.util.Utils;

/**
 * Blur view component
 * Extends BaseBlurView to implement Gaussian blur effect
 */
public class BlurView extends BaseBlurView {

    /**
     * Constructor
     * @param context Context
     * @param attrs Attribute set from XML
     */
    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Initialize view attributes
     * Reads custom attribute values from XML layout file
     * @param context Context
     * @param attrs Attribute set from XML
     */
    @Override
    protected void initAttributes(Context context, AttributeSet attrs) {
        // Get custom attribute values
        @SuppressLint("CustomViewStyleable")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurView);

        // Blur radius, default 25dp
        mBlurRadius = a.getDimension(R.styleable.BlurView_blurRadius, Utils.dp2px(getResources(), 25));

        // Overlay color, default semi-transparent white
        mOverlayColor = a.getColor(R.styleable.BlurView_overlayColor, 0xAAFFFFFF);

        // Corner radius, default no rounding
        mCornerRadius = a.getDimension(R.styleable.BlurView_cornerRadius, 0);

        // Inherit of corner radius
        mTopLeftCornerRadius = a.getDimension(R.styleable.BlurView_topLeftCornerRadius, mCornerRadius);
        mTopRightCornerRadius = a.getDimension(R.styleable.BlurView_topRightCornerRadius, mCornerRadius);
        mBottomLeftCornerRadius = a.getDimension(R.styleable.BlurView_bottomLeftCornerRadius, mCornerRadius);
        mBottomRightCornerRadius = a.getDimension(R.styleable.BlurView_bottomRightCornerRadius, mCornerRadius);

        // Downsample factor, default no downsampling
        mDownsampleFactor = a.getFloat(R.styleable.BlurView_downsampleFactor, 0f);

        // Release TypedArray resources
        a.recycle();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()) {
            drawPreviewBackground(canvas);
            return;
        }

        super.onDraw(canvas);
    }
}
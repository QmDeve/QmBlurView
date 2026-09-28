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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;

import androidx.annotation.NonNull;

final class ProgressiveCompositor {

    private final Rect mSrc = new Rect();
    private final Rect mDst = new Rect();
    private final Paint mMaskPaint = new Paint();

    private Bitmap mMasked;
    private Canvas mMaskedCanvas;

    private LinearGradient mGradient;
    private int mGradientW = -1;
    private int mGradientH = -1;
    private int mGradientDir = -1;

    ProgressiveCompositor() {
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    void draw(@NonNull Canvas canvas, @NonNull Bitmap blurred,
              int width, int height, int direction) {
        int bw = blurred.getWidth();
        int bh = blurred.getHeight();
        if (bw <= 0 || bh <= 0 || width <= 0 || height <= 0) {
            return;
        }
        if (mMasked == null || mMasked.isRecycled()
                || mMasked.getWidth() != bw || mMasked.getHeight() != bh) {
            release();
            mMasked = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
            mMaskedCanvas = new Canvas(mMasked);
            mGradientDir = -1;
        }

        mMasked.eraseColor(0);
        mMaskedCanvas.drawBitmap(blurred, 0, 0, null);
        mMaskPaint.setShader(intensityGradient(bw, bh, direction));
        mMaskedCanvas.drawRect(0, 0, bw, bh, mMaskPaint);

        mSrc.set(0, 0, bw, bh);
        mDst.set(0, 0, width, height);
        canvas.drawBitmap(mMasked, mSrc, mDst, null);
    }

    private LinearGradient intensityGradient(int width, int height, int direction) {
        if (mGradient == null || width != mGradientW || height != mGradientH
                || direction != mGradientDir) {
            int[] colors = new int[]{Color.argb(0, 0, 0, 0), Color.argb(255, 0, 0, 0)};
            float[] pos = new float[]{0f, 1f};
            switch (direction) {
                case 0: // bottom-to-top
                    mGradient = new LinearGradient(0, height, 0, 0, colors, pos, Shader.TileMode.CLAMP);
                    break;
                case 2: // right-to-left
                    mGradient = new LinearGradient(width, 0, 0, 0, colors, pos, Shader.TileMode.CLAMP);
                    break;
                case 3: // left-to-right
                    mGradient = new LinearGradient(0, 0, width, 0, colors, pos, Shader.TileMode.CLAMP);
                    break;
                default: // top-to-bottom
                    mGradient = new LinearGradient(0, 0, 0, height, colors, pos, Shader.TileMode.CLAMP);
                    break;
            }
            mGradientW = width;
            mGradientH = height;
            mGradientDir = direction;
        }
        return mGradient;
    }

    void release() {
        if (mMasked != null) {
            mMasked.recycle();
            mMasked = null;
        }
        mMaskedCanvas = null;
        mGradient = null;
        mGradientDir = -1;
    }
}
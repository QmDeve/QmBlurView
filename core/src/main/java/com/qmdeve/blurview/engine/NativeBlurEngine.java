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
import android.view.View;

import androidx.annotation.Nullable;
import androidx.tracing.Trace;

public class NativeBlurEngine extends AbsBlurEngine {

    private Bitmap mBlurredBitmap;

    public NativeBlurEngine(View host) {
        super(host);
    }

    @Override
    protected boolean prepare(int width, int height) {
        Trace.beginSection("NativeBlurEngine.prepare");
        try {
            if (mBlurRadius <= 0) {
                releaseBitmap();
                return false;
            }
            float[] params = blurParams();
            int scaledWidth = Math.max(1, Math.round(width / params[0]));
            int scaledHeight = Math.max(1, Math.round(height / params[0]));

            boolean dirty = mDirty;
            if (mBlurredBitmap == null
                    || mBlurredBitmap.getWidth() != scaledWidth
                    || mBlurredBitmap.getHeight() != scaledHeight) {
                dirty = true;
                releaseBitmap();
                try {
                    mBitmapToBlur = allocateBitmap(scaledWidth, scaledHeight);
                    mBlurringCanvas = new Canvas(mBitmapToBlur);
                    mBlurredBitmap = allocateBitmap(scaledWidth, scaledHeight);
                } catch (OutOfMemoryError | IllegalArgumentException e) {
                    release();
                    return false;
                }
            }
            if (dirty && mBlur.prepare(mBitmapToBlur, params[1])) {
                mDirty = false;
            }
            return true;
        } finally {
            Trace.endSection();
        }
    }

    @Override
    protected void onCaptured(int width, int height) {
        blurWithRetry(mBitmapToBlur, mBlurredBitmap);
    }

    @Override
    protected void drawResult(Canvas canvas, int width, int height) {
        boolean clipped = beginClip(canvas, width, height);
        if (mBlurredBitmap != null) {
            mRectSrc.set(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());
            mRectDst.set(0, 0, width, height);
            canvas.drawBitmap(mBlurredBitmap, mRectSrc, mRectDst, null);
        }
        mPaint.setColor(mOverlayColor);
        canvas.drawRect(0, 0, width, height, mPaint);
        endClip(canvas, clipped);
    }

    @Override
    @Nullable
    protected Bitmap resultBitmap() {
        return mBlurredBitmap;
    }

    @Override
    protected void onBuffersReleased() {
        if (mBlurredBitmap != null) {
            mBlurredBitmap.recycle();
            mBlurredBitmap = null;
        }
    }

    @Override
    @Nullable
    public Bitmap getBlurredBitmap() {
        return mBlurredBitmap;
    }

    @Override
    public boolean isUsingRenderEffect() {
        return false;
    }
}
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
import android.graphics.RecordingCanvas;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.tracing.Trace;

@RequiresApi(Build.VERSION_CODES.S)
public class RenderEffectBlurEngine extends AbsBlurEngine {

    private final RenderNode mRenderNode = new RenderNode("QmBlurView");
    private RenderEffect mRenderEffect;
    private float mEffectRadius = -1f;
    private int mEffectRounds = -1;

    public RenderEffectBlurEngine(View host) {
        super(host);
    }

    @Override
    protected boolean prepare(int width, int height) {
        if (mBlurRadius <= 0) {
            releaseBitmap();
            return false;
        }
        float[] params = blurParams();
        int scaledWidth = Math.max(1, Math.round(width / params[0]));
        int scaledHeight = Math.max(1, Math.round(height / params[0]));

        if (mBitmapToBlur == null
                || mBitmapToBlur.getWidth() != scaledWidth
                || mBitmapToBlur.getHeight() != scaledHeight) {
            releaseBitmap();
            try {
                mBitmapToBlur = allocateBitmap(scaledWidth, scaledHeight);
                mBlurringCanvas = new Canvas(mBitmapToBlur);
            } catch (OutOfMemoryError | IllegalArgumentException e) {
                release();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCaptured(int width, int height) {
        Trace.beginSection("RenderEffectBlurEngine.record");
        try {
            int rounds = Math.max(1, mBlurRounds);
            if (mRenderEffect == null || mEffectRadius != mBlurRadius || mEffectRounds != rounds) {
                mEffectRadius = mBlurRadius;
                mEffectRounds = rounds;
                mRenderEffect = buildBlurEffect(mBlurRadius, rounds);
                mRenderNode.setRenderEffect(mRenderEffect);
            }
            mRenderNode.setPosition(0, 0, width, height);
            RecordingCanvas recordingCanvas = mRenderNode.beginRecording();
            try {
                mRectSrc.set(0, 0, mBitmapToBlur.getWidth(), mBitmapToBlur.getHeight());
                mRectDst.set(0, 0, width, height);
                recordingCanvas.drawBitmap(mBitmapToBlur, mRectSrc, mRectDst, null);
            } finally {
                mRenderNode.endRecording();
            }
        } finally {
            Trace.endSection();
        }
    }

    @Override
    protected void drawResult(Canvas canvas, int width, int height) {
        boolean clipped = beginClip(canvas, width, height);
        if (canvas.isHardwareAccelerated()) {
            canvas.drawRenderNode(mRenderNode);
        } else if (mBitmapToBlur != null && !mBitmapToBlur.isRecycled()) {
            mRectSrc.set(0, 0, mBitmapToBlur.getWidth(), mBitmapToBlur.getHeight());
            mRectDst.set(0, 0, width, height);
            canvas.drawBitmap(mBitmapToBlur, mRectSrc, mRectDst, null);
        }
        mPaint.setColor(mOverlayColor);
        canvas.drawRect(0, 0, width, height, mPaint);
        endClip(canvas, clipped);
    }

    private static RenderEffect buildBlurEffect(float radius, int rounds) {
        RenderEffect effect = RenderEffect.createBlurEffect(
                radius, radius, Shader.TileMode.CLAMP);
        for (int i = 1; i < rounds; i++) {
            effect = RenderEffect.createChainEffect(
                    RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP),
                    effect);
        }
        return effect;
    }

    @Override
    protected void onBuffersReleased() {
        mRenderNode.discardDisplayList();
    }

    @Override
    @Nullable
    protected Bitmap resultBitmap() {
        return mBitmapToBlur;
    }

    @Override
    @Nullable
    public Bitmap getBlurredBitmap() {
        return null;
    }

    @Override
    protected void applyBlurRounds() {
    }

    @Override
    public boolean isUsingRenderEffect() {
        return true;
    }
}
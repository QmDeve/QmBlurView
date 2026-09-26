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

package com.qmdeve.blurview.transform.glide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.qmdeve.blurview.BlurNative;

import java.security.MessageDigest;

public class BlurTransformation extends BitmapTransformation {

    private static final String ID = "com.qmdeve.blurview.transform.glide.BlurTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);
    private static final BlurNative BLUR_NATIVE = new BlurNative();
    private final float blurRadius;
    private final float roundedCorners;

    public BlurTransformation() {
        this(25f, 0f);
    }

    public BlurTransformation(float blurRadius) {
        this(blurRadius, 0f);
    }

    public BlurTransformation(float blurRadius, float roundedCorners) {
        this.blurRadius = blurRadius;
        this.roundedCorners = roundedCorners;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.isRecycled()) {
            return toTransform;
        }

        int width = toTransform.getWidth();
        int height = toTransform.getHeight();

        // Get a bitmap from the pool to reuse memory
        Bitmap.Config config = getSafeConfig(toTransform);
        Bitmap blurred = pool.get(width, height, config);

        boolean blurSuccess = false;

        // Prepare and apply blur
        if (BLUR_NATIVE.prepare(blurred, blurRadius)) {
            BLUR_NATIVE.blur(toTransform, blurred);
            blurSuccess = true;
        }

        if (!blurSuccess) {
            if (blurred != toTransform) {
                pool.put(blurred);
            }
            return toTransform;
        }

        // Apply corner radius if needed
        if (roundedCorners > 0) {
            Bitmap rounded = applyCornerRadius(pool, blurred, roundedCorners);
            if (rounded != blurred) {
                pool.put(blurred); // Return the blurred bitmap to pool
            }
            return rounded;
        }

        return blurred;
    }

    /**
     * Apply corner radius to bitmap using Glide's BitmapPool
     */
    private Bitmap applyCornerRadius(BitmapPool pool, Bitmap bitmap, float radius) {
        if (bitmap.isRecycled()) {
            return bitmap;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap.Config config = getSafeConfig(bitmap);

        // Get bitmap from pool for output
        Bitmap output = pool.get(width, height, config);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(0, 0, width, height);

        // Draw rounded rectangle as mask
        canvas.drawRoundRect(rect, radius, radius, paint);

        // Use PorterDuff mode to apply the rounded mask
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return output;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        // Include blur radius and corner radius in cache key
        messageDigest.update(Float.toString(blurRadius).getBytes(CHARSET));
        messageDigest.update(Float.toString(roundedCorners).getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlurTransformation) {
            BlurTransformation other = (BlurTransformation) o;
            return Math.abs(blurRadius - other.blurRadius) < 0.01f &&
                    Math.abs(roundedCorners - other.roundedCorners) < 0.01f;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() +
                Float.valueOf(blurRadius).hashCode() +
                Float.valueOf(roundedCorners).hashCode();
    }

    /**
     * Get safe config for the bitmap
     */
    private Bitmap.Config getSafeConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }
}
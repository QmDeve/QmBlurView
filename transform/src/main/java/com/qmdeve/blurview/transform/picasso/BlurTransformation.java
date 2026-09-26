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

package com.qmdeve.blurview.transform.picasso;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import androidx.annotation.NonNull;

import com.qmdeve.blurview.BlurNative;
import com.squareup.picasso.Transformation;

public class BlurTransformation implements Transformation {
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

    @NonNull
    @Override
    public Bitmap transform(@NonNull Bitmap source) {
        Bitmap result = createBlurredBitmap(source);

        if (result != source) {
            source.recycle();
        }

        return result;
    }

    private Bitmap createBlurredBitmap(@NonNull Bitmap source) {
        if (source.isRecycled()) {
            return source;
        }

        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap blurred = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        try {
            if (BLUR_NATIVE.prepare(blurred, blurRadius)) {
                BLUR_NATIVE.blur(source, blurred);
            } else {
                blurred.recycle();
                return source;
            }
        } catch (Exception e) {
            blurred.recycle();
            return source;
        }

        if (roundedCorners > 0) {
            Bitmap rounded = applyCornerRadius(blurred, roundedCorners);
            if (rounded != blurred) {
                blurred.recycle();
            }
            return rounded;
        }

        return blurred;
    }

    private Bitmap applyCornerRadius(Bitmap bitmap, float radius) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(0, 0, width, height);

        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return output;
    }

    @Override
    public String key() {
        return "blur_" + blurRadius + "_corner_" + roundedCorners;
    }
}
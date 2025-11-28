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
    private final float blurRadius;
    private final float roundedCorners;
    private final BlurNative blurNative;

    public BlurTransformation(float blurRadius, float roundedCorners) {
        this.blurRadius = blurRadius;
        this.roundedCorners = roundedCorners;
        this.blurNative = new BlurNative();
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
            if (blurNative.prepare(blurred, blurRadius)) {
                blurNative.blur(source, blurred);
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
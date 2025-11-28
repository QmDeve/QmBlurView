package com.qmdeve.blurview.transform;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.qmdeve.blurview.BlurNative;

import java.security.MessageDigest;

/**
 * Glide BitmapTransformation for applying blur effect
 */
public class BlurTransformation extends BitmapTransformation {

    private static final String ID = "com.qmdeve.blurview.transform.BlurTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final float blurRadius;
    private final BlurNative blurNative;

    public BlurTransformation() {
        this(25f);
    }

    public BlurTransformation(float blurRadius) {
        this.blurRadius = blurRadius;
        this.blurNative = new BlurNative();
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform,
                               int outWidth, int outHeight) {

        if (toTransform.isRecycled()) {
            return toTransform;
        }

        int width = toTransform.getWidth();
        int height = toTransform.getHeight();

        // Get a bitmap from the pool to reuse memory
        Bitmap.Config config = getSafeConfig(toTransform);
        Bitmap blurred = pool.get(width, height, config);

        if (blurred == null) {
            blurred = Bitmap.createBitmap(width, height, config);
        }

        try {
            // Prepare and apply blur
            if (blurNative.prepare(blurred, blurRadius)) {
                blurNative.blur(toTransform, blurred);
            }
        } catch (Exception e) {
            // If blur fails, return original bitmap
            if (blurred != null && blurred != toTransform) {
                pool.put(blurred);
            }
            return toTransform;
        }

        return blurred;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        // Include blur radius in cache key
        messageDigest.update(Float.toString(blurRadius).getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlurTransformation) {
            BlurTransformation other = (BlurTransformation) o;
            return Math.abs(blurRadius - other.blurRadius) < 0.01f;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + Float.valueOf(blurRadius).hashCode();
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
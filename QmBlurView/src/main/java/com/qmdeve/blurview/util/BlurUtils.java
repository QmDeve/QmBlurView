package com.qmdeve.blurview.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Resources;

import com.qmdeve.blurview.Native;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @deprecated The BlurUtils class is Deprecated and will be removed in future versions
 */
@Deprecated(since = "1.0.4.5-Beta1", forRemoval = true)
public class BlurUtils {
    private static final int DEFAULT_RADIUS = 24;
    private static final int DEFAULT_THREAD_COUNT = 4;
    private static final int DEFAULT_ROUND = 1;

    public static Bitmap blurBitmap(Bitmap bitmap, int radius) {
        return blurBitmap(bitmap, radius, DEFAULT_THREAD_COUNT, DEFAULT_ROUND);
    }

    public static Bitmap blurBitmap(Bitmap bitmap) {
        return blurBitmap(bitmap, DEFAULT_RADIUS, DEFAULT_THREAD_COUNT, DEFAULT_ROUND);
    }

    public static Bitmap blurBitmap(Bitmap bitmap, int radius, int threadCount, int round) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        try {
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            for (int i = 0; i < threadCount; i++) {
                Native.blur(mutableBitmap, radius, threadCount, i, round);
            }
            return mutableBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap blurFile(File file, int radius) {
        if (file == null || !file.exists()) {
            return null;
        }

        Bitmap bitmap;
        try (FileInputStream fis = new FileInputStream(file)) {
            bitmap = BitmapFactory.decodeStream(fis);
            return blurBitmap(bitmap, radius);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap blurFile(File file) {
        return blurFile(file, DEFAULT_RADIUS);
    }

    public static Bitmap blurAssets(Context context, String path, int radius) {
        if (context == null || path == null) {
            return null;
        }

        Bitmap bitmap;
        InputStream is = null;
        try {
            AssetManager assetManager = context.getAssets();
            is = assetManager.open(path);
            bitmap = BitmapFactory.decodeStream(is);
            return blurBitmap(bitmap, radius);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap blurAssets(Context context, String path) {
        return blurAssets(context, path, DEFAULT_RADIUS);
    }

    public static Bitmap blurResId(Context context, int resId, int radius) {
        if (context == null || resId == 0) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            Resources resources = context.getResources();
            bitmap = BitmapFactory.decodeResource(resources, resId);
            return blurBitmap(bitmap, radius);
        } catch (Exception e) {
            e.printStackTrace();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return null;
        }
    }

    public static Bitmap blurResId(Context context, int resId) {
        return blurResId(context, resId, DEFAULT_RADIUS);
    }

    public static Bitmap blurResId(Context context, int resId, float scale, int radius) {
        if (context == null || resId == 0 || scale <= 0) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            Resources resources = context.getResources();
            bitmap = BitmapFactory.decodeResource(resources, resId);
            return blurWithScale(bitmap, scale, radius);
        } catch (Exception e) {
            e.printStackTrace();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return null;
        }
    }

    public static Bitmap blurWithScale(Bitmap bitmap, float scale, int radius) {
        if (bitmap == null || bitmap.isRecycled() || scale <= 0) {
            return null;
        }

        try {
            int width = Math.max(1, (int) (bitmap.getWidth() * scale));
            int height = Math.max(1, (int) (bitmap.getHeight() * scale));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            Bitmap blurredBitmap = blurBitmap(scaledBitmap, radius);
            return blurredBitmap != null ? blurredBitmap : scaledBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}
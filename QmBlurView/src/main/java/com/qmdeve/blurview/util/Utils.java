package com.qmdeve.blurview.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@RestrictTo(LIBRARY_GROUP)
public class Utils {
    public static final String TAG = "BaseBlurView";
    public static boolean sIsGlobalCapturing = false;

    public static float dp2px(Resources res, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static int getNavigationBarHeight(View view) {
        WindowInsetsCompat rootWindowInsets = ViewCompat.getRootWindowInsets(view);
        if (rootWindowInsets != null) {
            Insets navigationBars = rootWindowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            return navigationBars.bottom;
        }
        return 0;
    }

    public static void roundedRectPath(RectF rect, float radius, Path path) {
        path.reset();

        if (radius <= 0) {
            path.addRect(rect, Path.Direction.CW);
            return;
        }

        float maxRadius = Math.min(rect.width(), rect.height()) / 2f;
        radius = Math.min(radius, maxRadius);
        float controlOffset = radius * 0.5522847498f;

        path.moveTo(rect.left + radius, rect.top);
        path.lineTo(rect.right - radius, rect.top);
        path.cubicTo(rect.right - radius + controlOffset, rect.top,
                rect.right, rect.top + radius - controlOffset,
                rect.right, rect.top + radius);
        path.lineTo(rect.right, rect.bottom - radius);
        path.cubicTo(rect.right, rect.bottom - radius + controlOffset,
                rect.right - radius + controlOffset, rect.bottom,
                rect.right - radius, rect.bottom);
        path.lineTo(rect.left + radius, rect.bottom);
        path.cubicTo(rect.left + radius - controlOffset, rect.bottom,
                rect.left, rect.bottom - radius + controlOffset,
                rect.left, rect.bottom - radius);
        path.lineTo(rect.left, rect.top + radius);
        path.cubicTo(rect.left, rect.top + radius - controlOffset,
                rect.left + radius - controlOffset, rect.top,
                rect.left + radius, rect.top);
        path.close();
    }

    /**
     * Ensure bitmap is software-compatible for blur processing.
     * Converts hardware bitmaps to software bitmaps to prevent
     * "Software rendering doesn't support hardware bitmaps" error.
     *
     * @param bitmap The bitmap to check
     * @return Software-compatible bitmap
     */
    public static Bitmap ensureSoftwareBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        // Hardware bitmaps were introduced in Android O (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                Log.d(TAG, "Converting hardware bitmap to software bitmap for blur processing");
                try {
                    return bitmap.copy(Bitmap.Config.ARGB_8888, false);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to convert hardware bitmap: " + e.getMessage());
                    return bitmap; // Return original if copy fails
                }
            }
        }

        return bitmap;
    }

    /**
     * Recursively disable hardware bitmaps in a view hierarchy.
     * This prevents "Software rendering doesn't support hardware bitmaps" errors
     * when views are drawn onto software canvases for blur processing.
     */
    public static void disableHardwareBitmapsInView(View view) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        try {
            // Handle ImageView specifically
            if (view instanceof android.widget.ImageView) {
                android.widget.ImageView imageView = (android.widget.ImageView) view;
                android.graphics.drawable.Drawable drawable = imageView.getDrawable();

                if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                    android.graphics.drawable.BitmapDrawable bitmapDrawable =
                            (android.graphics.drawable.BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    if (bitmap != null && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                        Log.d(TAG, "Converting hardware bitmap in ImageView to software");
                        Bitmap softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                        if (softwareBitmap != null) {
                            imageView.setImageBitmap(softwareBitmap);
                        }
                    }
                }
            }

            // Recursively process children if it's a ViewGroup
            if (view instanceof android.view.ViewGroup) {
                android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    disableHardwareBitmapsInView(viewGroup.getChildAt(i));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disabling hardware bitmaps: " + e.getMessage());
        }
    }
}
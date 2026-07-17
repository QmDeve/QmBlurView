/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Donny Yale
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
 * Created Date: 2025-10-21
 * Author: Donny Yale
 * GitHub: https://github.com/QmDeve/QmBlurView
 * Website: https://blurview.qmdeve.com
 * ===========================================
 */

package com.qmdeve.blurview.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.RestrictTo;
import androidx.tracing.Trace;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@RestrictTo(LIBRARY_GROUP)
public class Utils {
    public static final String TAG = "BaseBlurView";
    public static boolean sIsGlobalCapturing = false;

    /**
     * Convert dp to px
     * @param res Resources
     * @param dp The dp value to be converted
     * @return The px value after the conversion is completed
     */
    public static float dp2px(Resources res, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    /**
     * Convert sp to px
     * @param res Resources
     * @param sp The sp value to be converted
     * @return The px value after the conversion is completed
     */
    public static float sp2px(Resources res, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, res.getDisplayMetrics());
    }
    /**
     * Get the height of the navigation bar at the bottom of the mobile phone
     * @param view Root View
     * @return The height of the navigation bar at the bottom of the mobile phone
     */
    public static int getNavigationBarHeight(View view) {
        WindowInsetsCompat rootWindowInsets = ViewCompat.getRootWindowInsets(view);
        if (rootWindowInsets != null) {
            Insets navigationBars = rootWindowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            return navigationBars.bottom;
        }
        return 0;
    }

    public static void roundedRectPath(RectF rect, float radius, Path path) {
        roundedRectPath(rect, radius, radius, radius, radius, path);
    }

    public static void roundedRectPath(RectF rect, float topLeft, float topRight, float bottomLeft, float bottomRight, Path path) {
        path.reset();

        if (topLeft <= 0 && topRight <= 0 && bottomLeft <= 0 && bottomRight <= 0) {
            path.addRect(rect, Path.Direction.CW);
            return;
        }

        float maxRadius = Math.min(rect.width(), rect.height()) / 2f;
        topLeft = Math.max(0, Math.min(topLeft, maxRadius));
        topRight = Math.max(0, Math.min(topRight, maxRadius));
        bottomLeft = Math.max(0, Math.min(bottomLeft, maxRadius));
        bottomRight = Math.max(0, Math.min(bottomRight, maxRadius));

        float tlOffset = topLeft * 0.5522847498f;
        float trOffset = topRight * 0.5522847498f;
        float brOffset = bottomRight * 0.5522847498f;
        float blOffset = bottomLeft * 0.5522847498f;

        path.moveTo(rect.left + topLeft, rect.top);
        path.lineTo(rect.right - topRight, rect.top);
        path.cubicTo(rect.right - topRight + trOffset, rect.top,
                rect.right, rect.top + topRight - trOffset,
                rect.right, rect.top + topRight);
        path.lineTo(rect.right, rect.bottom - bottomRight);
        path.cubicTo(rect.right, rect.bottom - bottomRight + brOffset,
                rect.right - bottomRight + brOffset, rect.bottom,
                rect.right - bottomRight, rect.bottom);
        path.lineTo(rect.left + bottomLeft, rect.bottom);
        path.cubicTo(rect.left + bottomLeft - blOffset, rect.bottom,
                rect.left, rect.bottom - bottomLeft + blOffset,
                rect.left, rect.bottom - bottomLeft);
        path.lineTo(rect.left, rect.top + topLeft);
        path.cubicTo(rect.left, rect.top + topLeft - tlOffset,
                rect.left + topLeft - tlOffset, rect.top,
                rect.left + topLeft, rect.top);
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
        Trace.beginSection("Utils.ensureSoftwareBitmap");
        try {
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
        } finally {
            Trace.endSection();
        }
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
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Drawable drawable = imageView.getDrawable();

                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
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
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
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
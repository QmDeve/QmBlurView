package com.qmdeve.blurview.util;

import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Utils {
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
}
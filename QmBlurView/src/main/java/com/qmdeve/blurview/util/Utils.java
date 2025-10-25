package com.qmdeve.blurview.util;

import android.content.res.Resources;
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
}
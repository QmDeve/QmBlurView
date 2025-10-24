package com.qmdeve.blurview.util;

import android.content.res.Resources;
import android.util.TypedValue;

public class Utils {
    public static float dp2px(Resources res, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
}
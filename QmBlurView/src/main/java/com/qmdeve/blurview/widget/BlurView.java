package com.qmdeve.blurview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.base.BaseBlurView;

public class BlurView extends BaseBlurView {

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()) {
            drawPreviewBackground(canvas);
        }

        if (!isInEditMode()) {
            super.onDraw(canvas);
            drawBlurredBitmap(canvas);
        }
    }
}
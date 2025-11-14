package com.qmdeve.blurview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.base.BaseBlurView;
import com.qmdeve.blurview.util.Utils;

public class BlurView extends BaseBlurView {

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initAttributes(Context context, AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurView);
        mBlurRadius = a.getDimension(R.styleable.BlurView_blurRadius, Utils.dp2px(getResources(), 25));
        mOverlayColor = a.getColor(R.styleable.BlurView_overlayColor, 0xAAFFFFFF);
        mCornerRadius = a.getDimension(R.styleable.BlurView_cornerRadius, 0);
        a.recycle();
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
package com.qmdeve.blurview.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.base.BaseBlurViewGroup;

public class BlurViewGroup extends ViewGroup {

    private final BaseBlurViewGroup mBaseBlurViewGroup;

    public BlurViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mBaseBlurViewGroup = new BaseBlurViewGroup(context, attrs);
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }

    public void setBlurRadius(float radius) {
        mBaseBlurViewGroup.setBlurRadius(radius);
    }

    public void setOverlayColor(int color) {
        mBaseBlurViewGroup.setOverlayColor(color);
    }

    public void setCornerRadius(float radius) {
        mBaseBlurViewGroup.setCornerRadius(radius);
    }

    public Bitmap getBlurredBitmap() {
        return mBaseBlurViewGroup.getBlurredBitmap();
    }

    public int getOverlayColor() {
        return mBaseBlurViewGroup.getOverlayColor();
    }

    public void release() {
        mBaseBlurViewGroup.release();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBaseBlurViewGroup.onAttachedToWindow(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        mBaseBlurViewGroup.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!mBaseBlurViewGroup.isRendering()) super.draw(canvas);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        if (!isInEditMode()) {
            mBaseBlurViewGroup.drawBlurredBitmap(canvas, getWidth(), getHeight());
        } else {
            mBaseBlurViewGroup.drawPreviewBackground(canvas, getWidth(), getHeight());
        }

        if (mBaseBlurViewGroup.getCornerRadius() > 0) {
            canvas.save();
            mBaseBlurViewGroup.clipCanvasWithRoundedCorner(canvas, getWidth(), getHeight());
            super.dispatchDraw(canvas);
            canvas.restore();
        } else {
            super.dispatchDraw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }
}
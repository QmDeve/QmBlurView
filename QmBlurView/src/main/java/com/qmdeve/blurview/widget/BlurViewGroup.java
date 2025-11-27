package com.qmdeve.blurview.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.base.BaseBlurViewGroup;
import com.qmdeve.blurview.util.Utils;

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

    public void setDownsampleFactor(float factor) {
        mBaseBlurViewGroup.setDownsampleFactor(factor);
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
        boolean shouldDrawBlur = true;
        if (Utils.sIsGlobalCapturing && !mBaseBlurViewGroup.isRendering()) {
            shouldDrawBlur = false;
        }

        if (!isInEditMode() && shouldDrawBlur) {
            mBaseBlurViewGroup.drawBlurredBitmap(canvas, getWidth(), getHeight());
        } else if (isInEditMode()) {
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthUsed = getPaddingLeft() + getPaddingRight();
        int heightUsed = getPaddingTop() + getPaddingBottom();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, widthUsed,
                        heightMeasureSpec, heightUsed);
            }
        }

        setMeasuredDimension(
                resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                child.layout(getPaddingLeft() + lp.leftMargin,
                        getPaddingTop() + lp.topMargin,
                        getPaddingLeft() + lp.leftMargin + child.getMeasuredWidth(),
                        getPaddingTop() + lp.topMargin + child.getMeasuredHeight());
            }
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}
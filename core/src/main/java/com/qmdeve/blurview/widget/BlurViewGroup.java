/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Donny Yang
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
 * Author: Donny Yang
 * GitHub: https://github.com/QmDeve/QmBlurView
 * Website: https://blurview.qmdeve.com
 * ===========================================
 */

package com.qmdeve.blurview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmdeve.blurview.engine.BlurAttrs;
import com.qmdeve.blurview.engine.BlurEngine;
import com.qmdeve.blurview.engine.BlurEngines;
import com.qmdeve.blurview.util.Utils;

public class BlurViewGroup extends ViewGroup {

    protected final BlurEngine mBlurEngine;

    public BlurViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mBlurEngine = createBlurEngine();
        BlurAttrs.apply(mBlurEngine, context, attrs, 10f, 0xAAFFFFFF);
    }

    protected BlurEngine createBlurEngine() {
        return BlurEngines.create(this);
    }

    public void setBlurRadius(float radius) {
        mBlurEngine.setBlurRadius(radius);
    }

    public float getBlurRadius() {
        return mBlurEngine.getBlurRadius();
    }

    public void setBlurRounds(int rounds) {
        mBlurEngine.setBlurRounds(rounds);
    }

    public int getBlurRounds() {
        return mBlurEngine.getBlurRounds();
    }

    public void setDownsampleFactor(float factor) {
        mBlurEngine.setDownsampleFactor(factor);
    }

    public float getDownsampleFactor() {
        return mBlurEngine.getDownsampleFactor();
    }

    public void setOverlayColor(@ColorInt int color) {
        mBlurEngine.setOverlayColor(color);
    }

    @ColorInt
    public int getOverlayColor() {
        return mBlurEngine.getOverlayColor();
    }

    public void setCornerRadius(float radius) {
        mBlurEngine.setCornerRadius(radius);
    }

    public float getCornerRadius() {
        return mBlurEngine.getCornerRadius();
    }

    public void setTopLeftCornerRadius(float radius) {
        mBlurEngine.setTopLeftCornerRadius(radius);
    }

    public void setTopRightCornerRadius(float radius) {
        mBlurEngine.setTopRightCornerRadius(radius);
    }

    public void setBottomLeftCornerRadius(float radius) {
        mBlurEngine.setBottomLeftCornerRadius(radius);
    }

    public void setBottomRightCornerRadius(float radius) {
        mBlurEngine.setBottomRightCornerRadius(radius);
    }

    public float getTopLeftCornerRadius() {
        return mBlurEngine.getTopLeftCornerRadius();
    }

    public float getTopRightCornerRadius() {
        return mBlurEngine.getTopRightCornerRadius();
    }

    public float getBottomLeftCornerRadius() {
        return mBlurEngine.getBottomLeftCornerRadius();
    }

    public float getBottomRightCornerRadius() {
        return mBlurEngine.getBottomRightCornerRadius();
    }

    public boolean hasCornerRadius() {
        return mBlurEngine.hasCornerRadius();
    }

    public void setMaxFps(int fps) {
        mBlurEngine.setMaxFps(fps);
    }

    public int getMaxFps() {
        return mBlurEngine.getMaxFps();
    }

    @Nullable
    public Bitmap getBlurredBitmap() {
        return mBlurEngine.getBlurredBitmap();
    }

    public void updateBlurImmediately() {
        mBlurEngine.updateBlurImmediately();
    }

    public void release() {
        mBlurEngine.release();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBlurEngine.attach();
    }

    @Override
    protected void onDetachedFromWindow() {
        mBlurEngine.detach();
        super.onDetachedFromWindow();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!mBlurEngine.isCapturing()) {
            super.draw(canvas);
        }
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        boolean shouldDrawBlur = !Utils.sIsGlobalCapturing || mBlurEngine.isCapturing();

        drawBlurLayer(canvas, getWidth(), getHeight(), isInEditMode(), shouldDrawBlur);
        drawChildrenWithClip(canvas);
    }

    protected void drawBlurLayer(@NonNull Canvas canvas, int width, int height, boolean isEditMode, boolean shouldDrawBlur) {
        if (!isEditMode && shouldDrawBlur) {
            mBlurEngine.drawBlur(canvas, width, height);
        } else if (isEditMode) {
            mBlurEngine.drawPreview(canvas, width, height);
        }
    }

    protected void drawChildrenWithClip(@NonNull Canvas canvas) {
        if (mBlurEngine.hasCornerRadius()) {
            canvas.save();
            mBlurEngine.clipRoundedCorners(canvas, getWidth(), getHeight());
            super.dispatchDraw(canvas);
            canvas.restore();
        } else {
            super.dispatchDraw(canvas);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int maxChildWidth = 0;
        int maxChildHeight = 0;
        int childState = 0;

        int[] childMeasuredWidths = new int[count];
        int[] childMeasuredHeights = new int[count];
        boolean[] childMeasured = new boolean[count];

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final ViewGroup.LayoutParams rawLp = child.getLayoutParams();
                final LayoutParams lp;

                if (rawLp instanceof LayoutParams) {
                    lp = (LayoutParams) rawLp;
                } else {
                    ViewGroup.LayoutParams generatedLp = generateLayoutParams(rawLp);
                    if (generatedLp instanceof LayoutParams) {
                        lp = (LayoutParams) generatedLp;
                    } else {
                        lp = new LayoutParams(generatedLp.width, generatedLp.height);
                    }
                    child.setLayoutParams(lp);
                }

                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                childMeasuredWidths[i] = child.getMeasuredWidth();
                childMeasuredHeights[i] = child.getMeasuredHeight();
                childMeasured[i] = true;

                maxChildWidth = Math.max(maxChildWidth, childMeasuredWidths[i] + lp.leftMargin + lp.rightMargin);
                maxChildHeight = Math.max(maxChildHeight, childMeasuredHeights[i] + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }

        maxChildWidth += getPaddingLeft() + getPaddingRight();
        maxChildHeight += getPaddingTop() + getPaddingBottom();
        maxChildWidth = Math.max(maxChildWidth, getSuggestedMinimumWidth());
        maxChildHeight = Math.max(maxChildHeight, getSuggestedMinimumHeight());

        int measuredWidth;
        int measuredHeight;

        if (widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            measuredWidth = Math.min(maxChildWidth, widthSize);
        } else {
            measuredWidth = maxChildWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            measuredHeight = Math.min(maxChildHeight, heightSize);
        } else {
            measuredHeight = maxChildHeight;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE && childMeasured[i]) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                boolean needRemasure = false;
                int newChildWidthMeasureSpec = 0;
                int newChildHeightMeasureSpec = 0;

                if (lp.width == LayoutParams.MATCH_PARENT) {
                    int availableWidth = measuredWidth - getPaddingLeft() - getPaddingRight() - lp.leftMargin - lp.rightMargin;
                    if (availableWidth > 0 && availableWidth != childMeasuredWidths[i]) {
                        newChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY);
                        needRemasure = true;
                    }
                }

                if (lp.height == LayoutParams.MATCH_PARENT) {
                    int availableHeight = measuredHeight - getPaddingTop() - getPaddingBottom() - lp.topMargin - lp.bottomMargin;
                    if (availableHeight > 0 && availableHeight != childMeasuredHeights[i]) {
                        newChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.EXACTLY);
                        needRemasure = true;
                    }
                }

                if (lp.gravity != -1) {
                    if ((lp.gravity & Gravity.FILL_HORIZONTAL) == Gravity.FILL_HORIZONTAL &&
                            lp.width != LayoutParams.MATCH_PARENT) {
                        int availableWidth = measuredWidth - getPaddingLeft() - getPaddingRight() - lp.leftMargin - lp.rightMargin;
                        if (availableWidth > 0 && availableWidth != childMeasuredWidths[i]) {
                            newChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY);
                            needRemasure = true;
                        }
                    }

                    if ((lp.gravity & Gravity.FILL_VERTICAL) == Gravity.FILL_VERTICAL &&
                            lp.height != LayoutParams.MATCH_PARENT) {
                        int availableHeight = measuredHeight - getPaddingTop() - getPaddingBottom() - lp.topMargin - lp.bottomMargin;
                        if (availableHeight > 0 && availableHeight != childMeasuredHeights[i]) {
                            newChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.EXACTLY);
                            needRemasure = true;
                        }
                    }
                }

                if (needRemasure) {
                    if (newChildWidthMeasureSpec == 0) {
                        newChildWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
                    }

                    if (newChildHeightMeasureSpec == 0) {
                        newChildHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
                    }

                    child.measure(newChildWidthMeasureSpec, newChildHeightMeasureSpec);
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = r - l - getPaddingRight();
        final int parentBottom = b - t - getPaddingBottom();
        final int parentWidth = parentRight - parentLeft;
        final int parentHeight = parentBottom - parentTop;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.START;
                }
                final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                int childLeft;
                int childTop;

                switch (horizontalGravity) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentWidth - childWidth) / 2 + lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = parentRight - childWidth - lp.rightMargin;
                        break;
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                        break;
                }

                switch (verticalGravity) {
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentHeight - childHeight) / 2 + lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - childHeight - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                        break;
                }

                int maxLeft = parentRight - Math.min(childWidth, parentWidth);
                childLeft = Math.max(parentLeft, Math.min(childLeft, maxLeft));

                int maxTop = parentBottom - Math.min(childHeight, parentHeight);
                childTop = Math.max(parentTop, Math.min(childTop, maxTop));

                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int gravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, new int[]{ android.R.attr.layout_gravity });
            gravity = a.getInt(0, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
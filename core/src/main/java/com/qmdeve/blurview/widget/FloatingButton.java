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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.engine.BlurAttrs;
import com.qmdeve.blurview.engine.BlurEngine;
import com.qmdeve.blurview.engine.BlurEngines;
import com.qmdeve.blurview.util.Utils;

public class FloatingButton extends View {

    public static final int POSITION_LEFT = 0;
    public static final int POSITION_RIGHT = 1;

    private int mPosition = POSITION_RIGHT;
    private Drawable mIconDrawable;
    private float mButtonSize = 55;
    private float mIconSize = 30;
    private int mIconTint = 0xFF333333;
    private float mRippleCornerRadius = 30;

    public interface OnLongPressListener {
        void onLongPress(FloatingButton view);
    }

    private OnLongPressListener mLongPressListener;
    protected final BlurEngine mBlurEngine;

    public FloatingButton(Context context) {
        this(context, null);
    }

    public FloatingButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mBlurEngine = BlurEngines.create(this);
        BlurAttrs.apply(mBlurEngine, context, attrs);
        init(context);
    }

    private void init(Context context) {
        mIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_add);

        setClickable(true);
        setFocusable(true);
        setLongClickable(true);

        mRippleCornerRadius = Utils.dp2px(getResources(), 12);
        setCornerRadius(mRippleCornerRadius);
        setBlurRadius(Utils.dp2px(getResources(), 16));
        setOverlayColor(0xFFFFFFFF);
        setElevation(Utils.dp2px(getResources(), 5));

        applyRippleEffect();

        super.setOnLongClickListener(v -> {
            if (mLongPressListener != null) {
                mLongPressListener.onLongPress(this);
                return true;
            }
            return false;
        });
    }

    private void applyRippleEffect() {
        int rippleColor = 0x22000000;
        float[] outerRadii = new float[]{
                mRippleCornerRadius, mRippleCornerRadius,
                mRippleCornerRadius, mRippleCornerRadius,
                mRippleCornerRadius, mRippleCornerRadius,
                mRippleCornerRadius, mRippleCornerRadius
        };
        ShapeDrawable mask = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        RippleDrawable ripple = new RippleDrawable(ColorStateList.valueOf(rippleColor), null, mask);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setForeground(ripple);
        }

        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRippleCornerRadius);
            }
        });
        setClipToOutline(true);
    }

    public void setPosition(int position) {
        this.mPosition = position == POSITION_LEFT ? POSITION_LEFT : POSITION_RIGHT;
        requestLayout();
    }

    public void setIcon(@DrawableRes int resId) {
        mIconDrawable = ContextCompat.getDrawable(getContext(), resId);
        applyIconTint();
        invalidate();
    }

    public void setIconDrawable(@Nullable Drawable drawable) {
        this.mIconDrawable = drawable;
        applyIconTint();
        invalidate();
    }

    public void setIconTint(@ColorInt int color) {
        int rgb = color & 0x00FFFFFF;
        mIconTint = (int) (0.80f * 255) << 24 | rgb;
        applyIconTint();
        invalidate();
    }

    public void setIconSize(float dp) {
        this.mIconSize = dp;
        invalidate();
    }

    public void setButtonSize(float dp) {
        this.mButtonSize = dp;
        requestLayout();
    }

    private void applyIconTint() {
        if (mIconDrawable != null) {
            mIconDrawable.mutate();
            mIconDrawable.setColorFilter(mIconTint, PorterDuff.Mode.SRC_IN);
        }
    }

        public void setCornerRadius(float radius) {
        mBlurEngine.setCornerRadius(radius);
        mRippleCornerRadius = radius;
        post(this::applyRippleEffect);
    }

        public void setOverlayColor(@ColorInt int color) {
        int rgb = color & 0x00FFFFFF;
        int finalColor = (int) (0.72f * 255) << 24 | rgb;
        mBlurEngine.setOverlayColor(finalColor);
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

    @ColorInt
    public int getOverlayColor() {
        return mBlurEngine.getOverlayColor();
    }

    public float getCornerRadius() {
        return mBlurEngine.getCornerRadius();
    }

    public void setTopLeftCornerRadius(float radius) {
        mBlurEngine.setTopLeftCornerRadius(radius);
    }

    public float getTopLeftCornerRadius() {
        return mBlurEngine.getTopLeftCornerRadius();
    }

    public void setTopRightCornerRadius(float radius) {
        mBlurEngine.setTopRightCornerRadius(radius);
    }

    public float getTopRightCornerRadius() {
        return mBlurEngine.getTopRightCornerRadius();
    }

    public void setBottomLeftCornerRadius(float radius) {
        mBlurEngine.setBottomLeftCornerRadius(radius);
    }

    public float getBottomLeftCornerRadius() {
        return mBlurEngine.getBottomLeftCornerRadius();
    }

    public void setBottomRightCornerRadius(float radius) {
        mBlurEngine.setBottomRightCornerRadius(radius);
    }

    public float getBottomRightCornerRadius() {
        return mBlurEngine.getBottomRightCornerRadius();
    }

    public void setMaxFps(int fps) {
        mBlurEngine.setMaxFps(fps);
    }

    public int getMaxFps() {
        return mBlurEngine.getMaxFps();
    }

    public void updateBlurImmediately() {
        mBlurEngine.updateBlurImmediately();
    }

    public void release() {
        mBlurEngine.release();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()) {
            mBlurEngine.drawPreview(canvas, getWidth(), getHeight());
        } else {
            mBlurEngine.drawBlur(canvas, getWidth(), getHeight());
        }
        if (mIconDrawable != null) {
            int size = (int) Utils.dp2px(getResources(), mIconSize);
            int left = (getWidth() - size) / 2;
            int top = (getHeight() - size) / 2;
            mIconDrawable.setBounds(left, top, left + size, top + size);
            mIconDrawable.draw(canvas);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!mBlurEngine.isCapturing()) {
            super.draw(canvas);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBlurEngine.attach();
        post(() -> {
            View parent = (View) getParent();
            if (parent != null) {
                int parentHeight = parent.getHeight();
                int parentWidth = parent.getWidth();
                float bottomMargin = Utils.dp2px(getResources(), 32)
                        + Utils.getNavigationBarHeight(this);
                float side = Utils.dp2px(getResources(), 34);

                float x = (mPosition == POSITION_RIGHT)
                        ? parentWidth - getWidth() - side
                        : side;
                float y = parentHeight - getHeight() - bottomMargin;

                setX(x);
                setY(y);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) Utils.dp2px(getResources(), mButtonSize);
        setMeasuredDimension(size, size);
    }

    public void setOnLongPressListener(@Nullable OnLongPressListener listener) {
        this.mLongPressListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        mBlurEngine.detach();
        super.onDetachedFromWindow();
    }
}
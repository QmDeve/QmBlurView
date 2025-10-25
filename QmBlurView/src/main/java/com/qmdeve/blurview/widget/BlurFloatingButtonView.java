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
import com.qmdeve.blurview.util.Utils;

public class BlurFloatingButtonView extends BlurView {

    public static final int POSITION_LEFT = 0;
    public static final int POSITION_RIGHT = 1;

    private int mPosition = POSITION_RIGHT;
    private Drawable mIconDrawable;
    private float mButtonSize = 55;
    private float mIconSize = 30;
    private int mIconTint = 0xFF333333;
    private float mRippleCornerRadius = 30;

    public interface OnLongPressListener {
        void onLongPress(BlurFloatingButtonView view);
    }

    private OnLongPressListener mLongPressListener;

    public BlurFloatingButtonView(Context context) {
        this(context, null);
    }

    public BlurFloatingButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
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

    @Override
    public void setCornerRadius(float radius) {
        super.setCornerRadius(radius);
        mRippleCornerRadius = radius;
        post(this::applyRippleEffect);
    }

    @Override
    public void setOverlayColor(@ColorInt int color) {
        int rgb = color & 0x00FFFFFF;
        int finalColor = (int) (0.72f * 255) << 24 | rgb;
        super.setOverlayColor(finalColor);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (mIconDrawable != null) {
            int size = (int) Utils.dp2px(getResources(), mIconSize);
            int left = (getWidth() - size) / 2;
            int top = (getHeight() - size) / 2;
            mIconDrawable.setBounds(left, top, left + size, top + size);
            mIconDrawable.draw(canvas);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
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
}
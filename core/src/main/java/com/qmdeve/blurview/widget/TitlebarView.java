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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.engine.BlurAttrs;
import com.qmdeve.blurview.engine.BlurEngine;
import com.qmdeve.blurview.engine.BlurEngines;
import com.qmdeve.blurview.util.Utils;

public class TitlebarView extends View {

    private String mTitle, mSubtitle, mMenuText;
    private boolean mShowBack, mCenterTitle;
    private Drawable mBackIcon, mMenuIcon;
    private int mTitleColor, mSubtitleColor, mMenuTextColor;
    private int mBackIconTint, mMenuIconTint;
    private final Paint mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mSubtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mMenuTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mTextBounds = new Rect();
    private final Rect mBackTouch = new Rect();
    private final Rect mMenuTouch = new Rect();
    private float mStatusBarHeight, mContentTopOffset;
    private static final float FIXED_HEIGHT_DP = 55f;
    private float mTitleOffsetX = 0f;
    private float mSubtitleOffsetX = 0f;
    private ValueAnimator mTitleAnimator;
    private ValueAnimator mSubtitleAnimator;

    public interface OnBackClickListener {
        void onBackClick();
    }

    public interface OnMenuClickListener {
        void onMenuClick();
    }

    private OnBackClickListener mOnBackClickListener;
    private OnMenuClickListener mOnMenuClickListener;
    protected final BlurEngine mBlurEngine;

    public TitlebarView(Context context) {
        this(context, null);
    }

    public TitlebarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mBlurEngine = BlurEngines.create(this);
        BlurAttrs.apply(mBlurEngine, context, attrs);
        setClickable(true);
        setFocusable(true);
        initAttrs(context, attrs);
        setCornerRadius(0);
        post(this::adjustForStatusBar);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitlebarView);
        mTitle = a.getString(R.styleable.TitlebarView_titleText);
        mSubtitle = a.getString(R.styleable.TitlebarView_subtitleText);
        mTitleColor = a.getColor(R.styleable.TitlebarView_titleTextColor, Color.TRANSPARENT);
        mSubtitleColor = a.getColor(R.styleable.TitlebarView_subtitleTextColor, Color.TRANSPARENT);
        mShowBack = a.getBoolean(R.styleable.TitlebarView_showBack, false);
        mCenterTitle = a.getBoolean(R.styleable.TitlebarView_centerTitle, false);

        if (mCenterTitle) {
            if (getWidth() == 0 || mTitle == null) {
                post(() -> setTitlePositionImmediately(mCenterTitle));
                post(() -> setSubtitlePositionImmediately(mCenterTitle));
            } else {
                setTitlePositionImmediately(mCenterTitle);
                setSubtitlePositionImmediately(mCenterTitle);
            }
        }

        mBackIconTint = a.getColor(R.styleable.TitlebarView_backIconTint, Color.TRANSPARENT);
        mMenuIconTint = a.getColor(R.styleable.TitlebarView_menuIconTint, Color.TRANSPARENT);

        int backIconRes = a.getResourceId(R.styleable.TitlebarView_backIcon, 0);
        if (backIconRes != 0) mBackIcon = ContextCompat.getDrawable(context, backIconRes);

        mMenuText = a.getString(R.styleable.TitlebarView_menuText);
        mMenuTextColor = a.getColor(R.styleable.TitlebarView_menuTextColor, Color.TRANSPARENT);

        int menuIconRes = a.getResourceId(R.styleable.TitlebarView_menuIcon, 0);
        if (menuIconRes != 0) mMenuIcon = ContextCompat.getDrawable(context, menuIconRes);
        a.recycle();

        mTitlePaint.setTextSize(Utils.dp2px(getResources(), 18));
        mTitlePaint.setFakeBoldText(true);
        mSubtitlePaint.setTextSize(Utils.dp2px(getResources(), 13));
        mMenuTextPaint.setTextSize(Utils.dp2px(getResources(), 16));
        mMenuTextPaint.setFakeBoldText(true);

        updateTextColorByOverlay();
    }

    private void updateTextColorByOverlay() {
        int brightness = (int) (Color.red(getOverlayColor()) * 0.299 + Color.green(getOverlayColor()) * 0.587 + Color.blue(getOverlayColor()) * 0.114);
        boolean isDark = brightness < 128;
        int autoColor = isDark ? Color.WHITE : Color.BLACK;
        if (mTitleColor == Color.TRANSPARENT) mTitleColor = autoColor;
        if (mSubtitleColor == Color.TRANSPARENT) mSubtitleColor = adjustAlpha(autoColor, 0.7f);
        if (mMenuTextColor == Color.TRANSPARENT) mMenuTextColor = autoColor;
        if (mBackIconTint == Color.TRANSPARENT) mBackIconTint = autoColor;
        if (mMenuIconTint == Color.TRANSPARENT) mMenuIconTint = autoColor;
        mTitlePaint.setColor(mTitleColor);
        mSubtitlePaint.setColor(mSubtitleColor);
        mMenuTextPaint.setColor(mMenuTextColor);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private void adjustForStatusBar() {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        if (loc[1] < getStatusBarHeight()) {
            mStatusBarHeight = getStatusBarHeight();
            getLayoutParams().height = (int) (Utils.dp2px(getResources(), FIXED_HEIGHT_DP) + mStatusBarHeight);
            requestLayout();
        }
        mContentTopOffset = mStatusBarHeight;
    }

    private int getStatusBarHeight() {
        @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (Utils.dp2px(getResources(), FIXED_HEIGHT_DP) + mStatusBarHeight);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()) {
            mBlurEngine.drawPreview(canvas, getWidth(), getHeight());
        } else {
            mBlurEngine.drawBlur(canvas, getWidth(), getHeight());
        }

        float viewHeight = Utils.dp2px(getResources(), FIXED_HEIGHT_DP);
        float contentTop = mContentTopOffset;
        float centerY = contentTop + viewHeight / 2f;

        float leftPad = Utils.dp2px(getResources(), 16);
        float iconSize = Utils.dp2px(getResources(), 20);
        if (mShowBack) {
            float top = centerY - iconSize / 2;
            mBackTouch.set((int) leftPad, (int) top, (int) (leftPad + iconSize * 1.5f), (int) (top + iconSize));
            if (mBackIcon != null) {
                Drawable d = DrawableCompat.wrap(mBackIcon.mutate());
                DrawableCompat.setTint(d, mBackIconTint);
                d.setBounds(mBackTouch);
                d.draw(canvas);
            } else {
                @SuppressLint("DrawAllocation")
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(mBackIconTint);
                p.setStrokeWidth(Utils.dp2px(getResources(), 2));
                canvas.drawLine(leftPad + Utils.dp2px(getResources(), 8), centerY - Utils.dp2px(getResources(), 7), leftPad, centerY, p);
                canvas.drawLine(leftPad + Utils.dp2px(getResources(), 8), centerY + Utils.dp2px(getResources(), 7), leftPad, centerY, p);
            }
        }

        float rightX = getWidth() - Utils.dp2px(getResources(), 16);
        if (mMenuIcon != null) {
            float top = centerY - iconSize / 2;
            mMenuTouch.set((int) (rightX - iconSize), (int) top, (int) rightX, (int) (top + iconSize));
            Drawable d = DrawableCompat.wrap(mMenuIcon.mutate());
            DrawableCompat.setTint(d, mMenuIconTint);
            d.setBounds(mMenuTouch);
            d.draw(canvas);
        } else if (mMenuText != null) {
            mMenuTextPaint.getTextBounds(mMenuText, 0, mMenuText.length(), mTextBounds);
            float tw = mTextBounds.width(), th = mTextBounds.height();
            float textY = centerY + th / 2 - Utils.dp2px(getResources(), 1);
            canvas.drawText(mMenuText, rightX - tw, textY, mMenuTextPaint);
            mMenuTouch.set((int) (rightX - tw - Utils.dp2px(getResources(), 8)), (int) (centerY - Utils.dp2px(getResources(), 20)), (int) (rightX + Utils.dp2px(getResources(), 8)), (int) (centerY + Utils.dp2px(getResources(), 20)));
        }

        float baseXLeft = mShowBack ? Utils.dp2px(getResources(), 48) : Utils.dp2px(getResources(), 16);

        if (mTitle != null) {
            mTitlePaint.getTextBounds(mTitle, 0, mTitle.length(), mTextBounds);
            float titleH = mTextBounds.height();
            float currentTitleX = baseXLeft + mTitleOffsetX;
            if (mSubtitle == null || mSubtitle.isEmpty()) {
                Paint.FontMetrics fm = mTitlePaint.getFontMetrics();
                float textHeight = fm.descent - fm.ascent;
                float textCenterOffset = (textHeight / 2f) - fm.descent;
                float visualAdjust = Utils.dp2px(getResources(), 1.5f);
                float titleY = centerY + textCenterOffset + visualAdjust;
                canvas.drawText(mTitle, currentTitleX, titleY, mTitlePaint);
            } else {
                mSubtitlePaint.getTextBounds(mSubtitle, 0, mSubtitle.length(), mTextBounds);
                float subH = mTextBounds.height();
                float totalH = titleH + Utils.dp2px(getResources(), 3) + subH;
                float baseY = centerY - totalH / 2 + titleH;
                float currentSubtitleX = baseXLeft + mSubtitleOffsetX;

                canvas.drawText(mTitle, currentTitleX, baseY - Utils.dp2px(getResources(), 1), mTitlePaint);
                canvas.drawText(mSubtitle, currentSubtitleX, baseY + Utils.dp2px(getResources(), 3) + subH, mSubtitlePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) e.getX(), y = (int) e.getY();
            if (mShowBack && mBackTouch.contains(x, y)) {
                if (mOnBackClickListener != null) mOnBackClickListener.onBackClick();
                else if (getContext() instanceof Activity)
                    ((Activity) getContext()).onBackPressed();
                return true;
            }
            if ((mMenuIcon != null || mMenuText != null) && mMenuTouch.contains(x, y)) {
                if (mOnMenuClickListener != null) mOnMenuClickListener.onMenuClick();
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    private void setTitlePositionImmediately(boolean center) {
        if (mTitle == null) return;

        mTitlePaint.getTextBounds(mTitle, 0, mTitle.length(), mTextBounds);
        float titleW = mTextBounds.width();
        float baseXLeft = mShowBack ? Utils.dp2px(getResources(), 48) : Utils.dp2px(getResources(), 16);
        float titleCenterX = getWidth() / 2f - titleW / 2f;
        mTitleOffsetX = center ? (titleCenterX - baseXLeft) : 0;
        invalidate();
    }

    private void setSubtitlePositionImmediately(boolean center) {
        if (mSubtitle == null) return;

        mSubtitlePaint.getTextBounds(mSubtitle, 0, mSubtitle.length(), mTextBounds);
        float subtitleW = mTextBounds.width();
        float baseXLeft = mShowBack ? Utils.dp2px(getResources(), 48) : Utils.dp2px(getResources(), 16);
        float subtitleCenterX = getWidth() / 2f - subtitleW / 2f;
        mSubtitleOffsetX = center ? (subtitleCenterX - baseXLeft) : 0;
        invalidate();
    }

    private void animateTitleToCenter(boolean center) {
        if (mTitle == null) return;

        mTitlePaint.getTextBounds(mTitle, 0, mTitle.length(), mTextBounds);
        float titleW = mTextBounds.width();
        float baseXLeft = mShowBack ? Utils.dp2px(getResources(), 48) : Utils.dp2px(getResources(), 16);
        float titleCenterX = getWidth() / 2f - titleW / 2f;
        float targetOffset = center ? (titleCenterX - baseXLeft) : 0;

        if (mTitleAnimator != null && mTitleAnimator.isRunning()) mTitleAnimator.cancel();
        mTitleAnimator = ValueAnimator.ofFloat(mTitleOffsetX, targetOffset);
        mTitleAnimator.setInterpolator(new DecelerateInterpolator());
        mTitleAnimator.setDuration(300);
        mTitleAnimator.addUpdateListener(a -> {
            mTitleOffsetX = (float) a.getAnimatedValue();
            invalidate();
        });
        mTitleAnimator.start();
    }

    private void animateSubtitleToCenter(boolean center) {
        if (mSubtitle == null) return;

        mSubtitlePaint.getTextBounds(mSubtitle, 0, mSubtitle.length(), mTextBounds);
        float subtitleW = mTextBounds.width();
        float baseXLeft = mShowBack ? Utils.dp2px(getResources(), 48) : Utils.dp2px(getResources(), 16);
        float subtitleCenterX = getWidth() / 2f - subtitleW / 2f;
        float targetOffset = center ? (subtitleCenterX - baseXLeft) : 0;

        if (mSubtitleAnimator != null && mSubtitleAnimator.isRunning()) mSubtitleAnimator.cancel();
        mSubtitleAnimator = ValueAnimator.ofFloat(mSubtitleOffsetX, targetOffset);
        mSubtitleAnimator.setInterpolator(new DecelerateInterpolator());
        mSubtitleAnimator.setDuration(300);
        mSubtitleAnimator.addUpdateListener(a -> {
            mSubtitleOffsetX = (float) a.getAnimatedValue();
            invalidate();
        });
        mSubtitleAnimator.start();
    }

    public void setCenterTitle(boolean center) {
        if (mCenterTitle == center) return;
        mCenterTitle = center;

        if (getWidth() == 0 || mTitle == null) {
            post(() -> animateTitleToCenter(center));
            post(() -> animateSubtitleToCenter(center));
        } else {
            animateTitleToCenter(center);
            animateSubtitleToCenter(center);
        }
    }

    public void setTitle(String title) {
        mTitle = title;
        invalidate();
    }

    public void setSubtitle(String subtitle) {
        mSubtitle = subtitle;
        invalidate();
    }

    public void setShowBack(boolean show) {
        mShowBack = show;
        invalidate();
    }

    public void setBackIcon(Drawable d) {
        mBackIcon = d;
        invalidate();
    }

    public void setBackIconTint(int color) {
        mBackIconTint = color;
        invalidate();
    }

    public void setMenuText(String text) {
        mMenuText = text;
        invalidate();
    }

    public void setMenuIcon(Drawable d) {
        mMenuIcon = d;
        invalidate();
    }

    public void setMenuIconTint(int color) {
        mMenuIconTint = color;
        invalidate();
    }

    public void setOnBackClickListener(OnBackClickListener l) {
        mOnBackClickListener = l;
    }

    public void setOnMenuClickListener(OnMenuClickListener l) {
        mOnMenuClickListener = l;
    }

    // ------------------------------------------------------------------
    // Blur configuration — delegated to the engine (was inherited from BlurView).
    // ------------------------------------------------------------------

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

    /**
     * Caps the blur refresh rate (capture + blur per second). {@code 0} = unlimited.
     */
    public void setMaxFps(int fps) {
        mBlurEngine.setMaxFps(fps);
    }

    public int getMaxFps() {
        return mBlurEngine.getMaxFps();
    }

    /** Forces a full blur refresh on the next draw. */
    public void updateBlurImmediately() {
        mBlurEngine.updateBlurImmediately();
    }

    public void release() {
        mBlurEngine.release();
    }

    @Override
    public void draw(Canvas canvas) {
        // Don't draw ourselves into our own capture.
        if (!mBlurEngine.isCapturing()) {
            super.draw(canvas);
        }
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
}
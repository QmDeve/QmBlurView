package com.qmdeve.blurview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.qmdeve.blurview.R;

public class BlurButtonView extends BlurView {
    private static final float DEFAULT_TEXT_SIZE = 16f;
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_ICON_SIZE = 24;
    private static final int DEFAULT_ICON_PADDING = 8;
    private static final int DEFAULT_HORIZONTAL_PADDING = 16;
    private static final int DEFAULT_VERTICAL_PADDING = 12;
    private CharSequence mText;
    private TextPaint mTextPaint;
    private int mTextColor;
    private int mTextColorPressed;
    private int mTextColorDisabled;
    private float mTextSize;
    private boolean mTextBold;
    private Drawable mIcon;
    private int mIconSize;
    private int mIconPadding;
    private ColorStateList mIconTint;
    private int mGravity;
    private final Rect mTextBounds = new Rect();
    private final Rect mIconBounds = new Rect();
    private final Rect mContentBounds = new Rect();
    private OnClickListener mOnClickListener;
    private boolean mIsPressed = false;
    private float mButtonCornerRadius;
    private int mContentWidth = 0;
    private int mContentHeight = 0;
    private Paint mHighlightPaint;
    private final float mHighlightAlpha = 0.3f;
    private final Rect mHighlightRect = new Rect();
    private float mCurrentHighlightAlpha = 0f;
    private float mTargetHighlightAlpha = 0f;
    private long mAnimationStartTime = 0;
    private boolean mIsAnimating = false;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private boolean mIsInButtonBounds = false;
    private final int mTouchSlop;
    private float mTouchDownX, mTouchDownY;

    public BlurButtonView(Context context) {
        this(context, null);
    }

    public BlurButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init(context, attrs);

        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                setFixedMargin();
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
            }
        });
    }

    private void init(Context context, AttributeSet attrs) {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = getResources().getDisplayMetrics().density;
        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setStyle(Paint.Style.FILL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurButtonView);

        mText = a.getText(R.styleable.BlurButtonView_android_text);
        mTextSize = a.getDimension(R.styleable.BlurButtonView_android_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, getResources().getDisplayMetrics()));
        mTextColor = a.getColor(R.styleable.BlurButtonView_android_textColor, DEFAULT_TEXT_COLOR);
        mTextColorPressed = a.getColor(R.styleable.BlurButtonView_buttonTextColorPressed, mTextColor);
        mTextColorDisabled = a.getColor(R.styleable.BlurButtonView_buttonTextColorDisabled,
                applyAlpha(mTextColor, 0.5f));
        mTextBold = a.getBoolean(R.styleable.BlurButtonView_buttonTextBold, true);
        mIcon = a.getDrawable(R.styleable.BlurButtonView_android_icon);
        mIconSize = a.getDimensionPixelSize(R.styleable.BlurButtonView_buttonIconSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ICON_SIZE, getResources().getDisplayMetrics()));
        mIconPadding = a.getDimensionPixelSize(R.styleable.BlurButtonView_buttonIconPadding,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ICON_PADDING, getResources().getDisplayMetrics()));
        mIconTint = a.getColorStateList(R.styleable.BlurButtonView_buttonIconTint);
        mGravity = a.getInt(R.styleable.BlurButtonView_android_gravity, Gravity.CENTER);
        mButtonCornerRadius = a.getDimension(R.styleable.BlurButtonView_buttonCornerRadius, 0);

        a.recycle();

        updateTextStyle();
        setClickable(true);
        setFocusable(true);

        setCornerRadius(mButtonCornerRadius);

        int horizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_HORIZONTAL_PADDING, getResources().getDisplayMetrics());
        int verticalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_VERTICAL_PADDING, getResources().getDisplayMetrics());
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        calculateContentSize();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            int desiredWidth = mContentWidth + getPaddingLeft() + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = desiredWidth;
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            int desiredHeight = mContentHeight + getPaddingTop() + getPaddingBottom();
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }
        }

        setMeasuredDimension(width, height);
    }

    private void calculateContentSize() {
        float textWidth = 0;
        float textHeight = 0;

        if (!TextUtils.isEmpty(mText)) {
            mTextPaint.getTextBounds(mText.toString(), 0, mText.length(), mTextBounds);
            textWidth = mTextPaint.measureText(mText.toString());

            Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
            textHeight = fontMetrics.descent - fontMetrics.ascent;
        }

        int iconWidth = 0;
        int iconHeight = 0;
        if (mIcon != null) {
            iconWidth = mIconSize;
            iconHeight = mIconSize;
        }

        mContentWidth = (int) textWidth + iconWidth;
        if (iconWidth > 0 && textWidth > 0) {
            mContentWidth += mIconPadding;
        }

        mContentHeight = Math.max((int) textHeight, iconHeight);
    }

    private void updateTextStyle() {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setFakeBoldText(mTextBold);
        updateTextColor();
    }

    private void updateTextColor() {
        if (!isEnabled()) {
            mTextPaint.setColor(mTextColorDisabled);
        } else if (mIsPressed) {
            mTextPaint.setColor(mTextColorPressed);
        } else {
            mTextPaint.setColor(mTextColor);
        }
    }

    private int applyAlpha(int color, float alpha) {
        int alphaValue = Math.round(Color.alpha(color) * alpha);
        return Color.argb(alphaValue, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        setCornerRadius(mButtonCornerRadius);
        super.onDraw(canvas);

        drawButtonContent(canvas);
        if (mCurrentHighlightAlpha > 0) {
            drawHighlight(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHighlightRect.set(0, 0, w, h);
    }

    private void drawHighlight(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;

        int mHighlightColor = Color.WHITE;
        int highlightColorWithAlpha = applyAlpha(mHighlightColor, mCurrentHighlightAlpha);
        mHighlightPaint.setColor(highlightColorWithAlpha);

        canvas.drawRoundRect(
                mHighlightRect.left,
                mHighlightRect.top,
                mHighlightRect.right,
                mHighlightRect.bottom,
                mButtonCornerRadius,
                mButtonCornerRadius,
                mHighlightPaint
        );
    }

    private void updateHighlightAnimation() {
        if (!mIsAnimating) return;

        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        long elapsed = currentTime - mAnimationStartTime;
        int mAnimationDuration = 150;
        if (elapsed >= mAnimationDuration) {
            mCurrentHighlightAlpha = mTargetHighlightAlpha;
            mIsAnimating = false;
            invalidate();
            return;
        }

        float input = (float) elapsed / mAnimationDuration;
        float interpolation = mInterpolator.getInterpolation(input);
        float startAlpha = mTargetHighlightAlpha == mHighlightAlpha ? 0f : mHighlightAlpha;
        float endAlpha = mTargetHighlightAlpha;
        mCurrentHighlightAlpha = startAlpha + (endAlpha - startAlpha) * interpolation;
        invalidate();
    }

    private void startHighlightAnimation(boolean show) {
        mTargetHighlightAlpha = show ? mHighlightAlpha : 0f;
        mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        mIsAnimating = true;

        if (!show && mCurrentHighlightAlpha == 0f) {
            mIsAnimating = false;
            return;
        }

        if (show && mCurrentHighlightAlpha >= mHighlightAlpha) {
            mIsAnimating = false;
            return;
        }

        invalidate();
    }

    private void drawButtonContent(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;

        calculateContentBounds();
        drawIcon(canvas);
        drawText(canvas);
    }

    public void setButtonCornerRadius(float radius) {
        if (mButtonCornerRadius != radius) {
            mButtonCornerRadius = radius;
            setCornerRadius(mButtonCornerRadius);
            invalidate();
        }
    }

    private void calculateContentBounds() {
        int width = getWidth();
        int height = getHeight();

        int contentWidth = width - getPaddingLeft() - getPaddingRight();
        int contentHeight = height - getPaddingTop() - getPaddingBottom();

        int left = getPaddingLeft();
        int top = getPaddingTop();

        switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                left += (contentWidth - mContentWidth) / 2;
                break;
            case Gravity.RIGHT:
                left += contentWidth - mContentWidth;
                break;
            default:
                break;
        }

        switch (mGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.CENTER_VERTICAL:
                top += (contentHeight - mContentHeight) / 2;
                break;
            case Gravity.BOTTOM:
                top += contentHeight - mContentHeight;
                break;
            default:
                break;
        }

        mContentBounds.set(left, top, left + mContentWidth, top + mContentHeight);

        if (mIcon != null) {
            int iconLeft = mContentBounds.left;
            int iconTop = mContentBounds.top + (mContentBounds.height() - mIconSize) / 2;
            mIconBounds.set(iconLeft, iconTop, iconLeft + mIconSize, iconTop + mIconSize);
        }

        if (!TextUtils.isEmpty(mText)) {
            int textLeft = mContentBounds.left;
            if (mIcon != null) {
                textLeft += mIconSize + mIconPadding;
            }
            int textTop = mContentBounds.top + (mContentBounds.height() - mTextBounds.height()) / 2;
            mTextBounds.set(textLeft, textTop, (int) (textLeft + mTextPaint.measureText(mText.toString())), textTop + mTextBounds.height());
        }
    }

    private void drawIcon(Canvas canvas) {
        if (mIcon == null) return;

        if (mIconTint != null) {
            Drawable wrappedIcon = DrawableCompat.wrap(mIcon.mutate());
            DrawableCompat.setTintList(wrappedIcon, mIconTint);
            mIcon = wrappedIcon;
        }

        mIcon.setBounds(mIconBounds);
        mIcon.draw(canvas);
    }

    private void drawText(Canvas canvas) {
        if (TextUtils.isEmpty(mText)) return;
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        float textBaseline = mContentBounds.top + (mContentBounds.height() - textHeight) / 2f - fontMetrics.ascent;
        textBaseline += Math.abs(fontMetrics.ascent) * 0.1f;
        float textLeft = mContentBounds.left;
        if (mIcon != null) {
            textLeft += mIconSize + mIconPadding;
        }

        canvas.drawText(mText.toString(), textLeft, textBaseline, mTextPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !isClickable()) {
            return super.onTouchEvent(event);
        }

        float x = event.getX();
        float y = event.getY();
        boolean isInside = x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = x;
                mTouchDownY = y;
                mIsInButtonBounds = true;
                mIsPressed = true;
                startHighlightAnimation(true);
                updateTextColor();
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsInButtonBounds) {
                    float dx = Math.abs(x - mTouchDownX);
                    float dy = Math.abs(y - mTouchDownY);
                    if (dx > mTouchSlop || dy > mTouchSlop) {
                        mIsInButtonBounds = isInside;
                        startHighlightAnimation(mIsInButtonBounds);
                    }
                } else {
                    if (isInside) {
                        mIsInButtonBounds = true;
                        startHighlightAnimation(true);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsPressed && mIsInButtonBounds) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(this);
                    }
                }
                mIsPressed = false;
                mIsInButtonBounds = false;
                startHighlightAnimation(false);
                updateTextColor();
                break;

            case MotionEvent.ACTION_CANCEL:
                mIsPressed = false;
                mIsInButtonBounds = false;
                startHighlightAnimation(false);
                updateTextColor();
                break;
        }

        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mIsAnimating) {
            updateHighlightAnimation();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateTextColor();
        if (!enabled) {
            mIsPressed = false;
            mIsInButtonBounds = false;
            startHighlightAnimation(false);
        }
        invalidate();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
        setClickable(listener != null);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        requestLayout();
    }

    public void setText(CharSequence text) {
        if (!TextUtils.equals(mText, text)) {
            mText = text;
            requestLayout();
            invalidate();
        }
    }

    public CharSequence getText() {
        return mText;
    }

    public void setTextSize(float size) {
        float newSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics());
        if (mTextSize != newSize) {
            mTextSize = newSize;
            mTextPaint.setTextSize(mTextSize);
            requestLayout();
            invalidate();
        }
    }

    public void setTextColor(int color) {
        if (mTextColor != color) {
            mTextColor = color;
            updateTextColor();
            invalidate();
        }
    }

    public void setTextColorPressed(int color) {
        if (mTextColorPressed != color) {
            mTextColorPressed = color;
            updateTextColor();
            invalidate();
        }
    }

    public void setTextBold(boolean bold) {
        if (mTextBold != bold) {
            mTextBold = bold;
            mTextPaint.setFakeBoldText(bold);
            requestLayout();
            invalidate();
        }
    }

    public void setIcon(Drawable icon) {
        if (mIcon != icon) {
            mIcon = icon;
            requestLayout();
            invalidate();
        }
    }

    public void setIconResource(int resId) {
        setIcon(ResourcesCompat.getDrawable(getResources(), resId, getContext().getTheme()));
    }

    public void setIconSize(int size) {
        int newSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources().getDisplayMetrics());
        if (mIconSize != newSize) {
            mIconSize = newSize;
            requestLayout();
            invalidate();
        }
    }

    public void setIconPadding(int padding) {
        int newPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, getResources().getDisplayMetrics());
        if (mIconPadding != newPadding) {
            mIconPadding = newPadding;
            requestLayout();
            invalidate();
        }
    }

    public void setIconTint(ColorStateList tint) {
        if (mIconTint != tint) {
            mIconTint = tint;
            invalidate();
        }
    }

    public void setIconTintColor(int color) {
        setIconTint(ColorStateList.valueOf(color));
    }

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            invalidate();
        }
    }

    private void setFixedMargin() {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            int fixedMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());

            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            if (marginParams.leftMargin == 0) {
                marginParams.leftMargin = fixedMargin;
            }
            if (marginParams.topMargin == 0) {
                marginParams.topMargin = fixedMargin;
            }
            if (marginParams.rightMargin == 0) {
                marginParams.rightMargin = fixedMargin;
            }
            if (marginParams.bottomMargin == 0) {
                marginParams.bottomMargin = fixedMargin;
            }

            setLayoutParams(marginParams);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFixedMargin();
    }
}
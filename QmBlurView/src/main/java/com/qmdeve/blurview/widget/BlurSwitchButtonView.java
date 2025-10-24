package com.qmdeve.blurview.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmdeve.blurview.R;

public class BlurSwitchButtonView extends BlurView {
    private static final float WIDTH_HEIGHT_RATIO = 2.0f;
    private boolean isChecked = false;
    private final Paint mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mTrackPath = new Path();
    private final Path mThumbPath = new Path();
    private float thumbCenterX = 0f;
    private float thumbRadius = 0f;
    private float thumbStartX = 0f, thumbEndX = 0f;
    private int mBaseColor = 0xFF0161F2;
    private int mTrackOnColor;
    private int mTrackOffColor;
    private int mCurrentTrackColor;
    private float mHighlightAlpha = 0f;
    private ValueAnimator mThumbAnimator, mColorAnimator, mSpringAnimator;
    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private static final long BASE_ANIM_DURATION = 200L;
    private boolean isDragging = false;
    private float lastTouchX;
    private float dragStartX;
    private boolean dragStarted = false;
    private static final float DRAG_THRESHOLD = 4f;
    private boolean dimensionsCalculated = false;

    private OnCheckedChangeListener listener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isChecked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public BlurSwitchButtonView(Context context) {
        this(context, null);
    }

    public BlurSwitchButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        @SuppressLint("Recycle")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurSwitchButtonView);
        mBaseColor = a.getColor(R.styleable.BlurSwitchButtonView_baseColor, 0xFF0161F2);


        setBlurRadius(16f);
        setCornerRadius(100f);
        setOverlayColor(0x10FFFFFF);
        calculateColorsFromBase();
        mTrackPaint.setStyle(Paint.Style.FILL);
        mThumbPaint.setStyle(Paint.Style.FILL);
        mHighlightPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int measuredWidth, measuredHeight;

        if (widthMode == View.MeasureSpec.EXACTLY && heightMode == View.MeasureSpec.EXACTLY) {
            float widthBasedOnHeight = heightSize * WIDTH_HEIGHT_RATIO;
            float heightBasedOnWidth = widthSize / WIDTH_HEIGHT_RATIO;

            if (widthBasedOnHeight <= widthSize) {
                measuredWidth = (int) widthBasedOnHeight;
                measuredHeight = heightSize;
            } else {
                measuredWidth = widthSize;
                measuredHeight = (int) heightBasedOnWidth;
            }
        } else if (widthMode == View.MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
            measuredHeight = (int) (widthSize / WIDTH_HEIGHT_RATIO);
        } else if (heightMode == View.MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;
            measuredWidth = (int) (heightSize * WIDTH_HEIGHT_RATIO);
        } else {
            int defaultSize = (int) (60 * getResources().getDisplayMetrics().density);
            if (widthMode == View.MeasureSpec.AT_MOST) {
                measuredWidth = Math.min(defaultSize, widthSize);
            } else {
                measuredWidth = defaultSize;
            }
            measuredHeight = (int) (measuredWidth / WIDTH_HEIGHT_RATIO);
            if (heightMode == View.MeasureSpec.AT_MOST && measuredHeight > heightSize) {
                measuredHeight = heightSize;
                measuredWidth = (int) (measuredHeight * WIDTH_HEIGHT_RATIO);
            }
        }

        if (measuredWidth <= 0) measuredWidth = 1;
        if (measuredHeight <= 0) measuredHeight = 1;

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private void calculateColorsFromBase() {
        int pureColor = mBaseColor;
        if (Color.alpha(mBaseColor) < 255) {
            pureColor = mBaseColor | 0xFF000000;
        }

        mTrackOnColor = (pureColor & 0x00FFFFFF) | 0x90000000;
        mTrackOffColor = makeColorDarkerAndTransparent(pureColor);
    }

    private int makeColorDarkerAndTransparent(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        float darkenFactor = 0.75f;
        red = (int) (red * darkenFactor);
        green = (int) (green * darkenFactor);
        blue = (int) (blue * darkenFactor);
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));
        int alpha = (int) (255 * (float) 0.40);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            calculateDimensions(w, h);
            updateThumbPositionToCurrentState(false);
            dimensionsCalculated = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && getWidth() > 0 && getHeight() > 0 && !dimensionsCalculated) {
            calculateDimensions(getWidth(), getHeight());
            updateThumbPositionToCurrentState(false);
            dimensionsCalculated = true;
        }
    }

    private void calculateDimensions(float w, float h) {
        thumbRadius = h / 2f * 0.75f;
        float thumbMargin = h * 0.15f;
        thumbStartX = thumbMargin + thumbRadius;
        thumbEndX = w - thumbMargin - thumbRadius;
    }

    private void updateThumbPositionToCurrentState(boolean animate) {
        if (!dimensionsCalculated) {
            return;
        }

        float targetX = isChecked ? thumbEndX : thumbStartX;
        int targetColor = isChecked ? mTrackOnColor : mTrackOffColor;

        if (!animate) {
            thumbCenterX = targetX;
            mCurrentTrackColor = targetColor;
            invalidate();
        } else {
            startSwitchAnimation();
        }
    }

    private Path createG3RoundedRectPath(float right, float bottom, float radius) {
        Path path = new Path();

        if (radius <= 0) {
            path.addRect((float) 0, (float) 0, right, bottom, Path.Direction.CW);
            return path;
        }

        float maxRadius = Math.min((right - (float) 0) / 2, (bottom - (float) 0) / 2);
        radius = Math.min(radius, maxRadius);
        final float controlOffset = radius * 0.5522847498f;

        path.moveTo((float) 0 + radius, (float) 0);

        path.lineTo(right - radius, (float) 0);

        path.cubicTo(
                right - radius + controlOffset, (float) 0,
                right, (float) 0 + radius - controlOffset,
                right, (float) 0 + radius
        );

        path.lineTo(right, bottom - radius);

        path.cubicTo(
                right, bottom - radius + controlOffset,
                right - radius + controlOffset, bottom,
                right - radius, bottom
        );

        path.lineTo((float) 0 + radius, bottom);

        path.cubicTo(
                (float) 0 + radius - controlOffset, bottom,
                (float) 0, bottom - radius + controlOffset,
                (float) 0, bottom - radius
        );

        path.lineTo((float) 0, (float) 0 + radius);

        path.cubicTo(
                (float) 0, (float) 0 + radius - controlOffset,
                (float) 0 + radius - controlOffset, (float) 0,
                (float) 0 + radius, (float) 0
        );

        path.close();
        return path;
    }

    private Path createG3RoundedCirclePath(float centerX, float centerY, float radius) {
        Path path = new Path();

        if (radius <= 0) {
            path.addCircle(centerX, centerY, 1, Path.Direction.CW);
            return path;
        }

        final float controlOffset = radius * 0.5522847498f;

        path.moveTo(centerX, centerY - radius);

        path.cubicTo(
                centerX + controlOffset, centerY - radius,
                centerX + radius, centerY - controlOffset,
                centerX + radius, centerY
        );

        path.cubicTo(
                centerX + radius, centerY + controlOffset,
                centerX + controlOffset, centerY + radius,
                centerX, centerY + radius
        );

        path.cubicTo(
                centerX - controlOffset, centerY + radius,
                centerX - radius, centerY + controlOffset,
                centerX - radius, centerY
        );

        path.cubicTo(
                centerX - radius, centerY - controlOffset,
                centerX - controlOffset, centerY - radius,
                centerX, centerY - radius
        );

        path.close();
        return path;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) return;

        if (!dimensionsCalculated) {
            calculateDimensions(w, h);
            dimensionsCalculated = true;
        }

        if (thumbCenterX == 0) {
            thumbCenterX = isChecked ? thumbEndX : thumbStartX;
            mCurrentTrackColor = isChecked ? mTrackOnColor : mTrackOffColor;
        }

        if (thumbCenterX < thumbStartX) thumbCenterX = thumbStartX;
        if (thumbCenterX > thumbEndX) thumbCenterX = thumbEndX;
        float trackRadius = h / 2f;
        mTrackPath.reset();
        mTrackPath.set(createG3RoundedRectPath(w, h, trackRadius));
        mTrackPaint.setColor(mCurrentTrackColor);
        canvas.drawPath(mTrackPath, mTrackPaint);

        mThumbPath.reset();
        mThumbPath.set(createG3RoundedCirclePath(thumbCenterX, h / 2f, thumbRadius));
        mThumbPaint.setColor(0xBFFFFFFF);
        canvas.drawPath(mThumbPath, mThumbPaint);

        if (thumbRadius > 0) {
            float highlightLeft = thumbCenterX - thumbRadius * 0.6f;
            float highlightRight = thumbCenterX + thumbRadius * 0.6f;
            @SuppressLint("DrawAllocation")
            LinearGradient highlightGradient = new LinearGradient(
                    highlightLeft, 0, highlightRight, 0,
                    new int[]{0x00FFFFFF, 0x33FFFFFF, 0x00FFFFFF},
                    null, Shader.TileMode.CLAMP
            );
            mHighlightPaint.setShader(highlightGradient);
            mHighlightPaint.setAlpha((int) (mHighlightAlpha * 255));
            canvas.drawPath(mThumbPath, mHighlightPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!dimensionsCalculated) {
            return super.onTouchEvent(event);
        }

        float currentX = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = currentX;
                dragStartX = currentX;
                dragStarted = false;
                isDragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaX = currentX - lastTouchX;
                if (!dragStarted && Math.abs(currentX - dragStartX) > DRAG_THRESHOLD) {
                    dragStarted = true;
                    isDragging = true;
                    cancelAnimations();
                }

                if (isDragging) {
                    thumbCenterX += deltaX;
                    thumbCenterX = Math.max(thumbStartX, Math.min(thumbEndX, thumbCenterX));
                    float progress = (thumbCenterX - thumbStartX) / (thumbEndX - thumbStartX);
                    mCurrentTrackColor = (int) argbEvaluator.evaluate(progress, mTrackOffColor, mTrackOnColor);

                    invalidate();
                }

                lastTouchX = currentX;
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    float switchCenter = (thumbStartX + thumbEndX) / 2;
                    boolean newChecked = thumbCenterX > switchCenter;
                    if (isChecked != newChecked) {
                        isChecked = newChecked;
                        if (listener != null) {
                            listener.onCheckedChanged(isChecked);
                        }
                    }
                    updateThumbPositionToCurrentState(true);
                } else {
                    toggle();
                }

                isDragging = false;
                dragStarted = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void cancelAnimations() {
        if (mThumbAnimator != null) {
            mThumbAnimator.cancel();
            mThumbAnimator = null;
        }
        if (mColorAnimator != null) {
            mColorAnimator.cancel();
            mColorAnimator = null;
        }
        if (mSpringAnimator != null) {
            mSpringAnimator.cancel();
            mSpringAnimator = null;
        }
    }

    public void toggle() {
        setChecked(!isChecked, true);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked, boolean animate) {
        if (isChecked == checked) return;
        isChecked = checked;

        updateThumbPositionToCurrentState(animate);

        if (listener != null) {
            listener.onCheckedChanged(isChecked);
        }
    }

    private void startSwitchAnimation() {
        if (!dimensionsCalculated) {
            return;
        }

        float startX = thumbCenterX;
        float endX = isChecked ? thumbEndX : thumbStartX;

        int startColor = mCurrentTrackColor;
        int endColor = isChecked ? mTrackOnColor : mTrackOffColor;

        cancelAnimations();

        mThumbAnimator = ValueAnimator.ofFloat(startX, endX);
        mThumbAnimator.setInterpolator(new DecelerateInterpolator());
        mThumbAnimator.setDuration(BASE_ANIM_DURATION);
        mThumbAnimator.addUpdateListener(a -> {
            thumbCenterX = (float) a.getAnimatedValue();
            invalidate();
        });

        mColorAnimator = ValueAnimator.ofObject(argbEvaluator, startColor, endColor);
        mColorAnimator.setDuration(BASE_ANIM_DURATION);
        mColorAnimator.addUpdateListener(a -> {
            mCurrentTrackColor = (int) a.getAnimatedValue();
            invalidate();
        });

        ValueAnimator highlightAnim = ValueAnimator.ofFloat(0f, 1f, 0f);
        highlightAnim.setDuration(BASE_ANIM_DURATION + 80);
        highlightAnim.addUpdateListener(a -> {
            mHighlightAlpha = (float) a.getAnimatedValue();
            invalidate();
        });

        mSpringAnimator = createSpringAnimator(startX, endX);

        mThumbAnimator.start();
        mColorAnimator.start();
        highlightAnim.start();
    }

    private ValueAnimator createSpringAnimator(float startX, float endX) {
        ValueAnimator springAnim = ValueAnimator.ofFloat(startX, endX);
        springAnim.setInterpolator(new SpringInterpolator(0.4f, 1.2f));
        springAnim.setDuration(250);

        springAnim.addUpdateListener(a -> {
            thumbCenterX = (float) a.getAnimatedValue();
            invalidate();
        });
        return springAnim;
    }

    private static class SpringInterpolator implements Interpolator {
        private final float factor;
        private final float overshoot;

        SpringInterpolator(float factor, float overshoot) {
            this.factor = factor;
            this.overshoot = overshoot;
        }

        @Override
        public float getInterpolation(float input) {
            return (float)(-Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1 + overshoot * Math.min(input, 0.5f));
        }
    }

    public void setBaseColor(int color) {
        this.mBaseColor = color;
        calculateColorsFromBase();
        mCurrentTrackColor = isChecked ? mTrackOnColor : mTrackOffColor;
        invalidate();
    }
}
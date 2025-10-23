package com.qmdeve.blurview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.R;

public class ProgressiveBlurView extends BlurView {
    public static final int DIRECTION_BOTTOM_TO_TOP = 0;
    public static final int DIRECTION_TOP_TO_BOTTOM = 1;
    public static final int DIRECTION_RIGHT_TO_LEFT = 2;
    public static final int DIRECTION_LEFT_TO_RIGHT = 3;
    private final Rect mRectSrc = new Rect(), mRectDst = new Rect();
    private int mGradientDirection = DIRECTION_TOP_TO_BOTTOM;
    private final Paint mGradientPaint = new Paint();
    private final Paint mOverlayGradientPaint = new Paint();
    private int mOverlayColor;
    private float mBlurRadius = 25f;

    public ProgressiveBlurView(Context context) {
        this(context, null);
    }

    public ProgressiveBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setCornerRadius(0);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressiveBlurView);
            mGradientDirection = a.getInt(
                    R.styleable.ProgressiveBlurView_progressiveDirection,
                    DIRECTION_TOP_TO_BOTTOM
            );
            mOverlayColor = a.getInt(R.styleable.ProgressiveBlurView_progressiveOverlayColor, 0xAAFFFFFF);
            mBlurRadius = a.getDimension(
                    R.styleable.ProgressiveBlurView_progressiveBlurRadius,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics())
            );

            a.recycle();
        }

        mGradientPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        super.setBlurRadius(mBlurRadius);
    }

    public void setGradientDirection(int direction) {
        if (direction >= DIRECTION_TOP_TO_BOTTOM && direction <= DIRECTION_RIGHT_TO_LEFT) {
            if (mGradientDirection != direction) {
                mGradientDirection = direction;
                invalidate();
            }
        }
    }

    public void setProgressiveOverlayColor(int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            invalidate();
        }
    }

    public void setProgressiveBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            super.setBlurRadius(radius);
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (isInEditMode()) {
            drawPreviewProgressiveBackground(canvas);
            return;
        }

        drawBlurredBitmapWithProgressiveEffect(canvas);
    }

    private void drawBlurredBitmapWithProgressiveEffect(Canvas canvas) {
        Bitmap blurredBitmap = getBlurredBitmap();
        if (blurredBitmap == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        int saveCount = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);

        mRectSrc.set(0, 0, blurredBitmap.getWidth(), blurredBitmap.getHeight());
        mRectDst.set(0, 0, width, height);
        canvas.drawBitmap(blurredBitmap, mRectSrc, mRectDst, null);
        applyProgressiveBlurGradient(canvas, width, height);
        applyProgressiveOverlayColor(canvas, width, height);
        canvas.restoreToCount(saveCount);
    }

    private void applyProgressiveBlurGradient(Canvas canvas, int width, int height) {
        LinearGradient blurGradient = createBlurGradient(width, height);
        mGradientPaint.setShader(blurGradient);
        canvas.drawRect(0, 0, width, height, mGradientPaint);
    }

    private LinearGradient createBlurGradient(int width, int height) {
        int[] colors;
        float[] positions;

        switch (mGradientDirection) {
            case DIRECTION_BOTTOM_TO_TOP:
                colors = new int[]{0x00000000, 0xFF000000};
                positions = new float[]{0f, 0.8f};
                return new LinearGradient(0, height, 0, 0, colors, positions, Shader.TileMode.CLAMP);
            case DIRECTION_LEFT_TO_RIGHT:
                colors = new int[]{0x00000000, 0xFF000000};
                positions = new float[]{0f, 0.8f};
                return new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP);
            case DIRECTION_RIGHT_TO_LEFT:
                colors = new int[]{0x00000000, 0xFF000000};
                positions = new float[]{0f, 0.8f};
                return new LinearGradient(width, 0, 0, 0, colors, positions, Shader.TileMode.CLAMP);
            default:
                colors = new int[]{0x00000000, 0xFF000000};
                positions = new float[]{0f, 0.8f};
                return new LinearGradient(0, 0, 0, height, colors, positions, Shader.TileMode.CLAMP);
        }
    }

    private void applyProgressiveOverlayColor(Canvas canvas, int width, int height) {
        LinearGradient overlayGradient = createOverlayGradient(width, height);
        mOverlayGradientPaint.setShader(overlayGradient);
        canvas.drawRect(0, 0, width, height, mOverlayGradientPaint);
    }

    private LinearGradient createOverlayGradient(int width, int height) {
        int transparentColor = mOverlayColor & 0x00FFFFFF;
        int solidColor = mOverlayColor;

        switch (mGradientDirection) {
            case DIRECTION_BOTTOM_TO_TOP:
                return new LinearGradient(
                        0, height, 0, 0,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 0.8f},
                        Shader.TileMode.CLAMP
                );

            case DIRECTION_LEFT_TO_RIGHT:
                return new LinearGradient(
                        0, 0, width, 0,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 0.8f},
                        Shader.TileMode.CLAMP
                );

            case DIRECTION_RIGHT_TO_LEFT:
                return new LinearGradient(
                        width, 0, 0, 0,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 0.8f},
                        Shader.TileMode.CLAMP
                );

            default:
                return new LinearGradient(
                        0, 0, 0, height,
                        new int[]{transparentColor, solidColor},
                        new float[]{0f, 0.8f},
                        Shader.TileMode.CLAMP
                );
        }
    }

    private void drawPreviewProgressiveBackground(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;
        int width = getWidth();
        int height = getHeight();
        Paint previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStyle(Paint.Style.FILL);
        previewPaint.setColor(mOverlayColor);
        canvas.drawRect(0, 0, width, height, previewPaint);

        float minWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 65, getResources().getDisplayMetrics());
        if (width < minWidth) {
            return;
        }

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(isColorDark(mOverlayColor) ? Color.WHITE : Color.BLACK);

        String[] textOptions = {
                "Progressive Blur Area Preview",
                "Progressive Blur Preview",
                "Progressive Preview",
                "Area Preview",
                "Preview"
        };
        String previewText = "Preview";
        for (String text : textOptions) {
            float textWidth = textPaint.measureText(text);
            if (textWidth < width * 0.8f) {
                previewText = text;
                break;
            }
        }

        float x = width / 2f;
        float y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(previewText, x, y, textPaint);
    }

    private boolean isColorDark(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        double luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        return luminance < 0.5;
    }

    @Override
    public void setCornerRadius(float radius) {
        super.setCornerRadius(0);
    }

    @Override
    public void setOverlayColor(int color) {
    }

    @Override
    public void setBlurRadius(float radius) {
        setProgressiveBlurRadius(radius);
    }
}
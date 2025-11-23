package com.qmdeve.blurview.base;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.qmdeve.blurview.Blur;
import com.qmdeve.blurview.BlurNative;
import com.qmdeve.blurview.util.Utils;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class BaseBlurView extends View {
    private static final String TAG = "BaseBlurView";

    protected int mOverlayColor;
    protected float mBlurRadius;
    protected final Blur mBlur;
    protected boolean mDirty = true;
    protected Bitmap mBitmapToBlur;
    public Bitmap mBlurredBitmap;
    protected Canvas mBlurringCanvas;
    protected final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    public final Rect mRectSrc = new Rect();
    public final Rect mRectDst = new Rect();
    public View mDecorView;
    public boolean mDifferentRoot;
    protected boolean mIsRendering;
    public float mCornerRadius;
    public final RectF mClipRect = new RectF();
    public final Path mG3Path = new Path();

    private final Map<SurfaceView, Bitmap> mSurfaceViewBitmaps = new WeakHashMap<>();
    private final Map<SurfaceView, Boolean> mPendingPixelCopies = new WeakHashMap<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public BaseBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBlur = new BlurNative();
        initAttributes(context, attrs);
    }

    protected void initAttributes(Context context, AttributeSet attrs) {}

    /**
     * Ensure bitmap is software-compatible for blur processing.
     * Converts hardware bitmaps to software bitmaps to prevent
     * "Software rendering doesn't support hardware bitmaps" error.
     *
     * @param bitmap The bitmap to check
     * @return Software-compatible bitmap
     */
    private Bitmap ensureSoftwareBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        // Hardware bitmaps were introduced in Android O (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                Log.d(TAG, "Converting hardware bitmap to software bitmap for blur processing");
                try {
                    return bitmap.copy(Bitmap.Config.ARGB_8888, false);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to convert hardware bitmap: " + e.getMessage());
                    return bitmap; // Return original if copy fails
                }
            }
        }

        return bitmap;
    }

    /**
     * Recursively disable hardware bitmaps in a view hierarchy.
     * This prevents "Software rendering doesn't support hardware bitmaps" errors
     * when views are drawn onto software canvases for blur processing.
     */
    private void disableHardwareBitmapsInView(View view) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        try {
            // Handle ImageView specifically
            if (view instanceof android.widget.ImageView) {
                android.widget.ImageView imageView = (android.widget.ImageView) view;
                android.graphics.drawable.Drawable drawable = imageView.getDrawable();

                if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                    android.graphics.drawable.BitmapDrawable bitmapDrawable =
                        (android.graphics.drawable.BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    if (bitmap != null && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                        Log.d(TAG, "Converting hardware bitmap in ImageView to software");
                        Bitmap softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                        if (softwareBitmap != null) {
                            imageView.setImageBitmap(softwareBitmap);
                        }
                    }
                }
            }

            // Recursively process children if it's a ViewGroup
            if (view instanceof android.view.ViewGroup) {
                android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    disableHardwareBitmapsInView(viewGroup.getChildAt(i));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disabling hardware bitmaps: " + e.getMessage());
        }
    }

    private void drawTextureViews(View view, Canvas canvas) {
        if (view instanceof TextureView) {
            TextureView textureView = (TextureView) view;
            if (textureView.getVisibility() == View.VISIBLE && textureView.isAvailable()) {
                int[] locDecor = new int[2];
                mDecorView.getLocationOnScreen(locDecor);

                int[] locTexture = new int[2];
                textureView.getLocationOnScreen(locTexture);

                int left = locTexture[0] - locDecor[0];
                int top = locTexture[1] - locDecor[1];

                Bitmap bitmap = textureView.getBitmap();
                if (bitmap != null) {
                    bitmap = ensureSoftwareBitmap(bitmap);
                    canvas.save();
                    canvas.translate(left, top);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    canvas.restore();
                    bitmap.recycle();
                }
            }
        } else if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                drawTextureViews(group.getChildAt(i), canvas);
            }
        }
    }

    private void drawSurfaceViews(View view, Canvas canvas) {
        if (view instanceof SurfaceView) {
            SurfaceView surfaceView = (SurfaceView) view;
            if (surfaceView.getVisibility() == View.VISIBLE) {
                // Draw the last known bitmap if available
                Bitmap cachedBitmap = mSurfaceViewBitmaps.get(surfaceView);
                if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
                    int[] locDecor = new int[2];
                    mDecorView.getLocationOnScreen(locDecor);

                    int[] locSurface = new int[2];
                    surfaceView.getLocationOnScreen(locSurface);

                    int left = locSurface[0] - locDecor[0];
                    int top = locSurface[1] - locDecor[1];

                    canvas.save();
                    canvas.translate(left, top);
                    canvas.drawBitmap(cachedBitmap, 0, 0, null);
                    canvas.restore();
                }

                // Request a new snapshot if not already pending
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Boolean.TRUE.equals(mPendingPixelCopies.get(surfaceView))) {
                    if (surfaceView.getWidth() > 0 && surfaceView.getHeight() > 0) {
                        final Bitmap bitmap = Bitmap.createBitmap(surfaceView.getWidth(), surfaceView.getHeight(), Bitmap.Config.ARGB_8888);
                        mPendingPixelCopies.put(surfaceView, true);
                        try {
                            PixelCopy.request(surfaceView, bitmap, copyResult -> {
                                mPendingPixelCopies.put(surfaceView, false);
                                if (copyResult == PixelCopy.SUCCESS) {
                                    Bitmap old = mSurfaceViewBitmaps.put(surfaceView, bitmap);
                                    if (old != null) old.recycle(); // Recycle the old one
                                    invalidate();
                                } else {
                                    bitmap.recycle();
                                }
                            }, mHandler);
                        } catch (IllegalArgumentException e) {
                            mPendingPixelCopies.put(surfaceView, false);
                            bitmap.recycle();
                        }
                    }
                }
            }
        } else if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                drawSurfaceViews(group.getChildAt(i), canvas);
            }
        }
    }

    public void setBlurRadius(float radius) {
        if (mBlurRadius != radius && radius >= 0) {
            mBlurRadius = radius;
            mDirty = true;
            invalidate();
        }
    }

    public void setOverlayColor(int color) {
        if (mOverlayColor != color) {
            mOverlayColor = color;
            invalidate();
        }
    }

    public void setCornerRadius(float radius) {
        if (mCornerRadius != radius && radius >= 0) {
            mCornerRadius = radius;
            invalidate();
        }
    }

    public Bitmap getBlurredBitmap() {
        return mBlurredBitmap;
    }

    public int getOverlayColor() {
        return mOverlayColor;
    }

    protected void releaseBitmap() {
        if (mBitmapToBlur != null) {
            mBitmapToBlur.recycle();
            mBitmapToBlur = null;
        }
        if (mBlurredBitmap != null) {
            mBlurredBitmap.recycle();
            mBlurredBitmap = null;
        }
        mBlurringCanvas = null;
    }

    public void release() {
        releaseBitmap();
        mBlur.release();
    }

    protected boolean prepare() {
        if (mBlurRadius <= 0) {
            release();
            return false;
        }

        float downsampleFactor = 2.52f;
        float radius = mBlurRadius / downsampleFactor;
        if (radius > 25) {
            downsampleFactor *= radius / 25;
            radius = 25;
        }

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return false;

        int scaledWidth = Math.max(1, Math.round(width / downsampleFactor));
        int scaledHeight = Math.max(1, Math.round(height / downsampleFactor));

        boolean dirty = mDirty;

        if (mBlurredBitmap == null
                || mBlurredBitmap.getWidth() != scaledWidth
                || mBlurredBitmap.getHeight() != scaledHeight) {
            dirty = true;
            releaseBitmap();

            try {
                mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                mBlurringCanvas = new Canvas(mBitmapToBlur);
                mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

                // Ensure software bitmaps for compatibility
                mBitmapToBlur = Utils.ensureSoftwareBitmap(mBitmapToBlur);
                mBlurredBitmap = Utils.ensureSoftwareBitmap(mBlurredBitmap);
            } catch (OutOfMemoryError e) {
                release();
                return false;
            }
        }

        if (dirty && mBlur.prepare(mBitmapToBlur, radius)) {
            mDirty = false;
        }

        return true;
    }

    protected void blur(Bitmap input, Bitmap output) {
        try {
            // Ensure input is software bitmap
            Bitmap softwareInput = Utils.ensureSoftwareBitmap(input);
            mBlur.blur(softwareInput, output);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null &&
                e.getMessage().contains("Software rendering doesn't support hardware bitmaps")) {
                Log.e(Utils.TAG, "Hardware bitmap error detected, converting and retrying");
                // Force conversion and retry
                Bitmap softwareInput = input.copy(Bitmap.Config.ARGB_8888, false);
                Bitmap softwareOutput = output.copy(Bitmap.Config.ARGB_8888, false);
                if (softwareInput != null && softwareOutput != null) {
                    mBlur.blur(softwareInput, softwareOutput);
                } else {
                    throw new RuntimeException("Failed to convert hardware bitmaps for blur processing", e);
                }
            } else {
                throw e;
            }
        }
    }

    public final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (!isShown()) return true;

            Bitmap old = mBlurredBitmap;
            View decor = mDecorView;

            if (decor != null && prepare()) {
                boolean redrawBitmap = mBlurredBitmap != old;

                int[] locDecor = new int[2];
                int[] locSelf = new int[2];
                decor.getLocationOnScreen(locDecor);
                getLocationOnScreen(locSelf);

                int offsetX = locSelf[0] - locDecor[0];
                int offsetY = locSelf[1] - locDecor[1];

                mBitmapToBlur.eraseColor(0);

                int saveCount = mBlurringCanvas.save();
                mIsRendering = true;
                try {
                    float scaleX = 1f * mBitmapToBlur.getWidth() / getWidth();
                    float scaleY = 1f * mBitmapToBlur.getHeight() / getHeight();
                    mBlurringCanvas.scale(scaleX, scaleY);
                    mBlurringCanvas.translate(-offsetX, -offsetY);

                    try {
                        decor.draw(mBlurringCanvas);
                    } catch (IllegalArgumentException e) {
                        if (e.getMessage() != null &&
                            e.getMessage().contains("Software rendering doesn't support hardware bitmaps")) {
                            Log.w(Utils.TAG, "Hardware bitmap detected during draw, converting and retrying");
                            // Convert hardware bitmaps in the view hierarchy
                            Utils.disableHardwareBitmapsInView(decor);
                            // Retry the draw
                            try {
                                mBlurringCanvas.restoreToCount(saveCount);
                                saveCount = mBlurringCanvas.save();
                                mBlurringCanvas.scale(scaleX, scaleY);
                                mBlurringCanvas.translate(-offsetX, -offsetY);
                                decor.draw(mBlurringCanvas);
                            } catch (Exception retryError) {
                                Log.e(Utils.TAG, "Retry after hardware bitmap conversion failed: " + retryError.getMessage());
                            }
                        } else {
                            throw e;
                        }
                    }

                    drawTextureViews(decor, mBlurringCanvas);
                    drawSurfaceViews(decor, mBlurringCanvas);
                } finally {
                    mIsRendering = false;
                    mBlurringCanvas.restoreToCount(saveCount);
                }

                blur(mBitmapToBlur, mBlurredBitmap);

                if (redrawBitmap || mDifferentRoot) {
                    postInvalidateOnAnimation();
                }
            }
            return true;
        }
    };

    public View getActivityDecorView() {
        Context ctx = getContext();
        for (int i = 0; i < 4 && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return (ctx instanceof Activity) ? ((Activity) ctx).getWindow().getDecorView() : null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDecorView = getActivityDecorView();
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            mDifferentRoot = mDecorView.getRootView() != getRootView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mDecorView != null) {
            mDecorView.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
            mDecorView = null;
        }
        release();
        super.onDetachedFromWindow();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!mIsRendering) super.draw(canvas);
    }

    public void drawBlurredBitmap(Canvas canvas) {
        if (mBlurredBitmap != null) {
            mRectSrc.set(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());
            mRectDst.set(0, 0, getWidth(), getHeight());

            if (mCornerRadius > 0) {
                canvas.save();
                mClipRect.set(mRectDst);
                Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
                canvas.clipPath(mG3Path);
                canvas.drawBitmap(mBlurredBitmap, mRectSrc, mRectDst, null);
                canvas.restore();
            } else {
                canvas.drawBitmap(mBlurredBitmap, mRectSrc, mRectDst, null);
            }
        }

        mPaint.setColor(mOverlayColor);

        if (mCornerRadius > 0) {
            canvas.save();
            mClipRect.set(mRectDst);
            Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
            canvas.clipPath(mG3Path);
            canvas.drawRect(mRectDst, mPaint);
            canvas.restore();
        } else {
            canvas.drawRect(mRectDst, mPaint);
        }
    }

    public void drawPreviewBackground(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) return;

        Paint previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStyle(Paint.Style.FILL);
        int previewColor = mOverlayColor;
        previewPaint.setColor(previewColor);
        if (mCornerRadius > 0) {
            mClipRect.set(0, 0, getWidth(), getHeight());
            Utils.roundedRectPath(mClipRect, mCornerRadius, mG3Path);
            canvas.drawPath(mG3Path, previewPaint);
        } else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), previewPaint);
        }
    }

    @Override
    public boolean isInEditMode() {
        return super.isInEditMode();
    }
}

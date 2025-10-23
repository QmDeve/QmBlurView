package com.qmdeve.blurview.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.qmdeve.blurview.R;

public class BlurTitlebarView extends BlurView {

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
    public interface OnBackClickListener { void onBackClick(); }
    public interface OnMenuClickListener { void onMenuClick(); }
    private OnBackClickListener mOnBackClickListener;
    private OnMenuClickListener mOnMenuClickListener;

    public BlurTitlebarView(Context context) { this(context, null); }
    public BlurTitlebarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        setCornerRadius(0);
        post(this::adjustForStatusBar);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurTitlebarView);
        mTitle = a.getString(R.styleable.BlurTitlebarView_titleText);
        mSubtitle = a.getString(R.styleable.BlurTitlebarView_subtitleText);
        mTitleColor = a.getColor(R.styleable.BlurTitlebarView_titleTextColor, Color.TRANSPARENT);
        mSubtitleColor = a.getColor(R.styleable.BlurTitlebarView_subtitleTextColor, Color.TRANSPARENT);
        mShowBack = a.getBoolean(R.styleable.BlurTitlebarView_showBack, false);
        mCenterTitle = a.getBoolean(R.styleable.BlurTitlebarView_centerTitle, false);

        if (mCenterTitle) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getWidth() == 0 || mTitle == null) {
                    post(() -> animateTitleToCenter(mCenterTitle));
                    post(() -> animateSubtitleToCenter(mCenterTitle));
                } else {
                    animateTitleToCenter(mCenterTitle);
                    animateSubtitleToCenter(mCenterTitle);
                }
            }, 400);
        }

        mBackIconTint = a.getColor(R.styleable.BlurTitlebarView_backIconTint, Color.TRANSPARENT);
        mMenuIconTint = a.getColor(R.styleable.BlurTitlebarView_menuIconTint, Color.TRANSPARENT);

        int backIconRes = a.getResourceId(R.styleable.BlurTitlebarView_backIcon, 0);
        if (backIconRes != 0) mBackIcon = ContextCompat.getDrawable(context, backIconRes);

        mMenuText = a.getString(R.styleable.BlurTitlebarView_menuText);
        mMenuTextColor = a.getColor(R.styleable.BlurTitlebarView_menuTextColor, Color.TRANSPARENT);

        int menuIconRes = a.getResourceId(R.styleable.BlurTitlebarView_menuIcon, 0);
        if (menuIconRes != 0) mMenuIcon = ContextCompat.getDrawable(context, menuIconRes);
        a.recycle();

        mTitlePaint.setTextSize(dp(18));
        mTitlePaint.setFakeBoldText(true);
        mSubtitlePaint.setTextSize(dp(13));
        mMenuTextPaint.setTextSize(dp(16));
        mMenuTextPaint.setFakeBoldText(true);

        updateTextColorByOverlay();
    }

    private void updateTextColorByOverlay() {
        int brightness = (int)(Color.red(getOverlayColor())*0.299 + Color.green(getOverlayColor())*0.587 + Color.blue(getOverlayColor())*0.114);
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
    private float dp(float v){ return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,v,getResources().getDisplayMetrics()); }

    private void adjustForStatusBar() {
        int[] loc = new int[2]; getLocationOnScreen(loc);
        if (loc[1] < getStatusBarHeight()) {
            mStatusBarHeight = getStatusBarHeight();
            getLayoutParams().height = (int)(dp(FIXED_HEIGHT_DP) + mStatusBarHeight);
            requestLayout();
        }
        mContentTopOffset = mStatusBarHeight;
    }
    private int getStatusBarHeight(){
        @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
        int id=getResources().getIdentifier("status_bar_height","dimen","android");
        return id>0?getResources().getDimensionPixelSize(id):0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=(int)(dp(FIXED_HEIGHT_DP)+mStatusBarHeight);
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float viewHeight = dp(FIXED_HEIGHT_DP);
        float contentTop = mContentTopOffset;
        float centerY = contentTop + viewHeight / 2f;

        float leftPad = dp(16);
        float iconSize = dp(20);
        if (mShowBack) {
            float top = centerY - iconSize / 2;
            mBackTouch.set((int)leftPad,(int)top,(int)(leftPad+iconSize*1.5f),(int)(top+iconSize));
            if (mBackIcon != null) {
                Drawable d = DrawableCompat.wrap(mBackIcon.mutate());
                DrawableCompat.setTint(d, mBackIconTint);
                d.setBounds(mBackTouch);
                d.draw(canvas);
            } else {
                @SuppressLint("DrawAllocation")
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(mBackIconTint); p.setStrokeWidth(dp(2));
                canvas.drawLine(leftPad+dp(8),centerY-dp(7),leftPad,centerY,p);
                canvas.drawLine(leftPad+dp(8),centerY+dp(7),leftPad,centerY,p);
            }
        }

        float rightPad = dp(16);
        float rightX = getWidth() - rightPad;
        if (mMenuIcon != null) {
            float top=centerY-iconSize/2;
            mMenuTouch.set((int)(rightX-iconSize),(int)top,(int)rightX,(int)(top+iconSize));
            Drawable d = DrawableCompat.wrap(mMenuIcon.mutate());
            DrawableCompat.setTint(d, mMenuIconTint);
            d.setBounds(mMenuTouch);
            d.draw(canvas);
        } else if (mMenuText != null) {
            mMenuTextPaint.getTextBounds(mMenuText,0,mMenuText.length(),mTextBounds);
            float tw=mTextBounds.width(),th=mTextBounds.height();
            float textY=centerY+th/2-dp(1);
            canvas.drawText(mMenuText,rightX-tw,textY,mMenuTextPaint);
            mMenuTouch.set((int)(rightX-tw-dp(8)),(int)(centerY-dp(20)),(int)(rightX+dp(8)),(int)(centerY+dp(20)));
        }

        float baseXLeft = mShowBack ? dp(48) : dp(16);

        if (mTitle != null) {
            mTitlePaint.getTextBounds(mTitle,0,mTitle.length(),mTextBounds);
            float titleH = mTextBounds.height();
            float currentTitleX = baseXLeft + mTitleOffsetX;
            float titleY = centerY + titleH / 2f - dp(1);
            if (mSubtitle == null || mSubtitle.isEmpty()) {
                canvas.drawText(mTitle, currentTitleX, titleY, mTitlePaint);
            } else {
                mSubtitlePaint.getTextBounds(mSubtitle, 0, mSubtitle.length(), mTextBounds);
                float subH = mTextBounds.height();
                float totalH = titleH + dp(3) + subH;
                float baseY = centerY - totalH/2 + titleH;
                float currentSubtitleX = baseXLeft + mSubtitleOffsetX;

                canvas.drawText(mTitle, currentTitleX, baseY - dp(1), mTitlePaint);
                canvas.drawText(mSubtitle, currentSubtitleX, baseY + dp(3) + subH, mSubtitlePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()==MotionEvent.ACTION_UP){
            int x=(int)e.getX(),y=(int)e.getY();
            if(mShowBack && mBackTouch.contains(x,y)){
                if(mOnBackClickListener!=null) mOnBackClickListener.onBackClick();
                else if(getContext() instanceof Activity)((Activity)getContext()).onBackPressed();
                return true;
            }
            if((mMenuIcon!=null||mMenuText!=null)&&mMenuTouch.contains(x,y)){
                if(mOnMenuClickListener!=null) mOnMenuClickListener.onMenuClick();
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    private void animateTitleToCenter(boolean center) {
        if (mTitle == null) return;

        mTitlePaint.getTextBounds(mTitle, 0, mTitle.length(), mTextBounds);
        float titleW = mTextBounds.width();
        float baseXLeft = mShowBack ? dp(48) : dp(16);
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
        float baseXLeft = mShowBack ? dp(48) : dp(16);
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

    public void setTitle(String title){ mTitle=title; invalidate(); }
    public void setSubtitle(String subtitle){ mSubtitle=subtitle; invalidate(); }
    public void setShowBack(boolean show){ mShowBack=show; invalidate(); }
    public void setBackIcon(Drawable d){ mBackIcon=d; invalidate(); }
    public void setBackIconTint(int color){ mBackIconTint=color; invalidate(); }
    public void setMenuText(String text){ mMenuText=text; invalidate(); }
    public void setMenuIcon(Drawable d){ mMenuIcon=d; invalidate(); }
    public void setMenuIconTint(int color){ mMenuIconTint=color; invalidate(); }
    public void setOnBackClickListener(OnBackClickListener l){ mOnBackClickListener=l; }
    public void setOnMenuClickListener(OnMenuClickListener l){ mOnMenuClickListener=l; }
}
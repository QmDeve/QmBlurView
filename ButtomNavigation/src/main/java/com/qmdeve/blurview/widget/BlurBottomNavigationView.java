package com.qmdeve.blurview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.qmdeve.blurview.TabView;
import com.qmdeve.blurview.TabViewManager;
import com.qmdeve.blurview.base.BaseBlurView;
import com.qmdeve.blurview.bottomnavigation.R;
import com.qmdeve.blurview.util.MenuUtils;
import com.qmdeve.blurview.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class BlurBottomNavigationView extends BaseBlurView {
    private int mMenuResId;
    private int mSelectedColor;
    private int mUnselectedColor;
    private float mIconSize;
    private float mTextSize;
    private boolean mTextBold;
    private List<MenuUtils.MenuItem> mMenuItems;
    private List<TabView> mTabViews;
    private int mCurrentSelected = 0;
    private final int mFixedHeightPx;
    private OnTabSelectedListener mOnTabSelectedListener;
    private ViewPager mViewPager;
    private ViewPager2 mViewPager2;
    public int mNavigationBarHeight = 0;
    public boolean mIsObscuredByNavigationBar = false;
    private final TabViewManager mTabViewManager;

    public BlurBottomNavigationView(Context context) {
        this(context, null);
    }

    public BlurBottomNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mFixedHeightPx = (int) Utils.dp2px(getResources(), 60);
        mTabViews = new ArrayList<>();
        mTabViewManager = new TabViewManager(this);
    }

    @Override
    protected void initAttributes(Context context, AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurBottomNavigationView);
        mBlurRadius = a.getDimension(R.styleable.BlurBottomNavigationView_navBlurRadius, Utils.dp2px(getResources(), 25));
        mCornerRadius = 0;
        mOverlayColor = a.getColor(R.styleable.BlurBottomNavigationView_navOverlayColor, 0xAAFFFFFF);
        mMenuResId = a.getResourceId(R.styleable.BlurBottomNavigationView_menu, 0);
        mSelectedColor = a.getColor(R.styleable.BlurBottomNavigationView_navSelectedColor, Color.BLUE);
        mUnselectedColor = a.getColor(R.styleable.BlurBottomNavigationView_navUnselectedColor, Color.GRAY);
        mIconSize = a.getDimension(R.styleable.BlurBottomNavigationView_item_iconSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mTextSize = a.getDimension(R.styleable.BlurBottomNavigationView_item_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));
        mTextBold = a.getBoolean(R.styleable.BlurBottomNavigationView_item_textBold, false);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalHeight = mFixedHeightPx + mNavigationBarHeight;

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY);
        } else {
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize + mNavigationBarHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initMenuIfNeeded();
        checkObscuredByNavigationBar(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mNavigationBarHeight = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
        } else {
            mNavigationBarHeight = insets.getSystemWindowInsetBottom();
        }

        mIsObscuredByNavigationBar = mNavigationBarHeight > 0;
        requestLayout();
        return super.onApplyWindowInsets(insets);
    }

    private static void checkObscuredByNavigationBar(BlurBottomNavigationView navigationView) {
        boolean hasSystemWindowInset = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets windowInsets = navigationView.getRootWindowInsets();
            if (windowInsets != null) {
                int systemWindowInsetBottom = windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom;
                hasSystemWindowInset = systemWindowInsetBottom > 0;
                navigationView.mNavigationBarHeight = systemWindowInsetBottom;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            WindowInsets windowInsets = navigationView.getRootWindowInsets();
            if (windowInsets != null) {
                int systemWindowInsetBottom = windowInsets.getSystemWindowInsetBottom();
                hasSystemWindowInset = systemWindowInsetBottom > 0;
                navigationView.mNavigationBarHeight = systemWindowInsetBottom;
            }
        }

        navigationView.mIsObscuredByNavigationBar = hasSystemWindowInset;

        if (navigationView.mIsObscuredByNavigationBar) {
            navigationView.requestLayout();
        }
    }

    private void initMenuIfNeeded() {
        if (mMenuResId != 0 && (mMenuItems == null || mMenuItems.isEmpty())) {
            initMenu();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        initMenuIfNeeded();
        drawBlurredBitmap(canvas);

        if (!mTabViews.isEmpty()) {
            mTabViewManager.drawTabs(canvas, mTabViews, mCurrentSelected, mFixedHeightPx);
        } else {
            drawPreviewBackground(canvas);
        }
    }

    private void initMenu() {
        mMenuItems = MenuUtils.parseMenu(getContext(), mMenuResId);

        if (mMenuItems.isEmpty()) {
            return;
        }

        mTabViews.clear();
        for (int i = 0; i < mMenuItems.size(); i++) {
            TabView tabView = new TabView(getContext(), mMenuItems.get(i), i, mIconSize);
            mTabViews.add(tabView);
        }

        setSelectedTab(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mTabViewManager.handleTouchEvent(event, mTabViews, mFixedHeightPx);
    }

    private void setViewPager1Listener(ViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setSelectedTab(position);
            }
        });
    }

    private void setViewPager2Listener(ViewPager2 viewPager2) {
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setSelectedTab(position);
            }
        });
    }

    private void setInitialSelectedTab(Object viewPager) {
        int currentItem = -1;
        int itemCount = 0;

        if (viewPager instanceof ViewPager) {
            ViewPager vp1 = (ViewPager) viewPager;
            if (vp1.getAdapter() != null) {
                currentItem = vp1.getCurrentItem();
                itemCount = vp1.getAdapter().getCount();
            }
        } else if (viewPager instanceof ViewPager2) {
            ViewPager2 vp2 = (ViewPager2) viewPager;
            if (vp2.getAdapter() != null) {
                currentItem = vp2.getCurrentItem();
                itemCount = vp2.getAdapter().getItemCount();
            }
        }

        if (currentItem >= 0 && itemCount > 0) {
            setSelectedTab(currentItem);
        }
    }

    /**
     * Set the selected tab
     * @param position int
     */
    public void setSelectedTab(int position) {
        if (position < 0 || position >= mTabViews.size() || position == mCurrentSelected) {
            return;
        }

        int oldPosition = mCurrentSelected;
        mCurrentSelected = position;

        if (mViewPager != null) {
            mViewPager.setCurrentItem(position, true);
        }

        if (mViewPager2 != null) {
            mViewPager2.setCurrentItem(position, true);
        }

        if (mOnTabSelectedListener != null) {
            mOnTabSelectedListener.onTabSelected(position, oldPosition);
        }

        invalidate();
    }

    /**
     * Set the Menu
     * @param menuResId int
     */
    public void setMenu(int menuResId) {
        this.mMenuResId = menuResId;
        initMenu();
        invalidate();
    }

    /**
     * Set the Selected Color
     * @param color int
     */
    public void setSelectedColor(int color) {
        this.mSelectedColor = color;
        invalidate();
    }

    /**
     * Set the Unselected Color
     * @param color int
     */
    public void setUnselectedColor(int color) {
        this.mUnselectedColor = color;
        invalidate();
    }

    /**
     * Set the Icon Size
     * @param size float
     */
    public void setIconSize(float size) {
        this.mIconSize = size;
        invalidate();
    }

    /**
     * Set the Text Size
     * @param size float
     */
    public void setTextSize(float size) {
        this.mTextSize = size;
        invalidate();
    }

    /**
     * Set the Text Bold
     * @param bold boolean
     */
    public void setTextBold(boolean bold) {
        this.mTextBold = bold;
        invalidate();
    }

    /**
     * Bind the {@link androidx.viewpager.widget.ViewPager} or {@link androidx.viewpager2.widget.ViewPager2}
     * @param viewPager Object instance of ViewPager or ViewPager2
     */
    public void bind(Object viewPager) {
        if (viewPager instanceof ViewPager) {
            this.mViewPager = (ViewPager) viewPager;
            setViewPager1Listener((ViewPager) viewPager);
        } else if (viewPager instanceof ViewPager2) {
            this.mViewPager2 = (ViewPager2) viewPager;
            setViewPager2Listener((ViewPager2) viewPager);
        } else {
            throw new IllegalArgumentException("Parameter must be instance of ViewPager or ViewPager2");
        }

        // Set initial selected tab
        setInitialSelectedTab(viewPager);
    }

    /**
     * Setting up a listener for the Tab
     *
     * <p>A callback method will be triggered when interacting with the Tab</p>
     * <ul>
     *     <li>{@link OnTabSelectedListener#onTabSelected(int, int)}</li>
     * </ul>
     *
     * @param listener OnTabSelectedListener
     *
     */
    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.mOnTabSelectedListener = listener;
    }

    public int getCurrentSelected() {
        return mCurrentSelected;
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public int getUnselectedColor() {
        return mUnselectedColor;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public boolean isTextBold() {
        return mTextBold;
    }

    @Override
    public void release() {
        super.release();
        if (mTabViews != null) {
            mTabViews.clear();
        }
        if (mMenuItems != null) {
            mMenuItems.clear();
        }
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int newPosition, int oldPosition);
    }
}
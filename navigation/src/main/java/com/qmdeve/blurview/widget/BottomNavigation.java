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
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.qmdeve.blurview.TabView;
import com.qmdeve.blurview.TabViewManager;
import com.qmdeve.blurview.base.BaseBlurView;
import com.qmdeve.blurview.navigation.R;
import com.qmdeve.blurview.util.MenuUtils;
import com.qmdeve.blurview.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom bottom navigation view with blur background support.
 * <p>
 * This component displays a set of tabs parsed from a menu resource and renders them using
 * {@link TabView}. It supports icon and text customization, integrates with {@link ViewPager}
 * and {@link ViewPager2}, and automatically adjusts its height when obscured by the system
 * navigation bar.
 * </p>
 *
 * <p>
 * The view handles:
 * <ul>
 *     <li>Blurred background drawing</li>
 *     <li>Menu parsing and tab initialization</li>
 *     <li>Tab selection and state updates</li>
 *     <li>Touch event delegation to {@link TabViewManager}</li>
 *     <li>Insets handling for navigation bars</li>
 * </ul>
 * </p>
 */
public class BottomNavigation extends BaseBlurView {
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

    /**
     * Constructs the navigation view programmatically.
     *
     * @param context the context used to initialize the view
     */
    public BottomNavigation(Context context) {
        this(context, null);
    }

    /**
     * Constructs the navigation view with attributes from XML.
     *
     * @param context the context
     * @param attrs   the attribute set from XML
     */
    public BottomNavigation(Context context, AttributeSet attrs) {
        super(context, attrs);

        mFixedHeightPx = (int) Utils.dp2px(getResources(), 60);
        mTabViews = new ArrayList<>();
        mTabViewManager = new TabViewManager(this);
    }

    /**
     * Initializes custom attributes defined for this view.
     *
     * @param context the context used to access resources
     * @param attrs   the attribute set passed from XML
     */
    @Override
    protected void initAttributes(Context context, AttributeSet attrs) {
        @SuppressLint("CustomViewStyleable")
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigation);
        mBlurRadius = a.getDimension(R.styleable.BottomNavigation_navBlurRadius, Utils.dp2px(getResources(), 25));
        mCornerRadius = 0;
        mOverlayColor = a.getColor(R.styleable.BottomNavigation_navOverlayColor, 0xAAFFFFFF);
        mMenuResId = a.getResourceId(R.styleable.BottomNavigation_menu, 0);
        mSelectedColor = a.getColor(R.styleable.BottomNavigation_navSelectedColor, Color.BLUE);
        mUnselectedColor = a.getColor(R.styleable.BottomNavigation_navUnselectedColor, Color.GRAY);
        mIconSize = a.getDimension(
                R.styleable.BottomNavigation_item_iconSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics())
        );
        mTextSize = a.getDimension(
                R.styleable.BottomNavigation_item_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics())
        );
        mTextBold = a.getBoolean(R.styleable.BottomNavigation_item_textBold, false);
        a.recycle();
    }

    /**
     * Measures the view's height based on its fixed tab height and system navigation bar height.
     *
     * @param widthMeasureSpec  horizontal measurement specification
     * @param heightMeasureSpec vertical measurement specification
     */
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

    /**
     * Called when the size of this view changes. Used here to initialize menu resources
     * and check navigation bar overlap.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initMenuIfNeeded();
        checkObscuredByNavigationBar(this);
    }

    /**
     * Receives window insets and updates navigation bar height and layout adjustment.
     *
     * @param insets the window insets containing system UI dimensions
     * @return the applied insets
     */
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

    /**
     * Checks whether the view is overlapped by the system navigation bar and updates state.
     *
     * @param navigationView the navigation view being checked
     */
    private static void checkObscuredByNavigationBar(BottomNavigation navigationView) {
        boolean hasSystemWindowInset = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets windowInsets = navigationView.getRootWindowInsets();
            if (windowInsets != null) {
                int systemWindowInsetBottom = windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom;
                hasSystemWindowInset = systemWindowInsetBottom > 0;
                navigationView.mNavigationBarHeight = systemWindowInsetBottom;
            }
        } else if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.M) {
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

    /**
     * Initializes the menu and tab items if needed.
     */
    private void initMenuIfNeeded() {
        if (mMenuResId != 0 && (mMenuItems == null || mMenuItems.isEmpty())) {
            initMenu();
        }
    }

    /**
     * Draws the blurred background and tabs (or preview state when no menu is set).
     *
     * @param canvas the canvas on which the view is drawn
     */
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

    /**
     * Parses the menu resource and initializes corresponding {@link TabView} instances.
     */
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

    /**
     * Delegates touch events to {@link TabViewManager}.
     *
     * @param event the motion event
     * @return always {@code true}, indicating the event was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mTabViewManager.handleTouchEvent(event, mTabViews, mFixedHeightPx);
    }

    /**
     * Adds a selection listener to {@link ViewPager}.
     *
     * @param viewPager the ViewPager instance
     */
    private void setViewPager1Listener(ViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setSelectedTab(position);
            }
        });
    }

    /**
     * Adds a selection listener to {@link ViewPager2}.
     *
     * @param viewPager2 the ViewPager2 instance
     */
    private void setViewPager2Listener(ViewPager2 viewPager2) {
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setSelectedTab(position);
            }
        });
    }

    /**
     * Synchronizes the initial selected tab with the page of the attached ViewPager.
     *
     * @param viewPager instance of ViewPager or ViewPager2
     */
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
     * Updates the currently selected tab and triggers page navigation and callbacks.
     *
     * @param position index of the tab to select
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
     * Sets the menu resource used to create tabs.
     *
     * @param menuResId resource ID of the menu
     */
    public void setMenu(int menuResId) {
        this.mMenuResId = menuResId;
        initMenu();
        invalidate();
    }

    /**
     * Sets the selected tab color.
     *
     * @param color the color value
     */
    public void setSelectedColor(int color) {
        this.mSelectedColor = color;
        invalidate();
    }

    /**
     * Sets the unselected tab color.
     *
     * @param color the color value
     */
    public void setUnselectedColor(int color) {
        this.mUnselectedColor = color;
        invalidate();
    }

    /**
     * Sets the icon size for all tabs.
     *
     * @param size icon size in pixels
     */
    public void setIconSize(float size) {
        this.mIconSize = size;
        invalidate();
    }

    /**
     * Sets the text size for tab labels.
     *
     * @param size text size in pixels
     */
    public void setTextSize(float size) {
        this.mTextSize = size;
        invalidate();
    }

    /**
     * Enables or disables bold text for tab labels.
     *
     * @param bold {@code true} to enable bold text
     */
    public void setTextBold(boolean bold) {
        this.mTextBold = bold;
        invalidate();
    }

    /**
     * Binds a ViewPager or ViewPager2 and synchronizes selection changes.
     *
     * @param viewPager an instance of ViewPager or ViewPager2
     * @throws IllegalArgumentException if the argument is neither ViewPager nor ViewPager2
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

        setInitialSelectedTab(viewPager);
    }

    /**
     * Registers a listener for tab selection events.
     *
     * @param listener the listener to register
     */
    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.mOnTabSelectedListener = listener;
    }

    /**
     * @return the index of the currently selected tab
     */
    public int getCurrentSelected() {
        return mCurrentSelected;
    }

    /**
     * @return the color applied to the selected tab
     */
    public int getSelectedColor() {
        return mSelectedColor;
    }

    /**
     * @return the color applied to unselected tabs
     */
    public int getUnselectedColor() {
        return mUnselectedColor;
    }

    /**
     * @return the text size used for tab labels
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * @return {@code true} if tab text is bold
     */
    public boolean isTextBold() {
        return mTextBold;
    }

    /**
     * Releases view resources and clears internal lists.
     */
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

    /**
     * Callback interface for receiving tab selection events.
     */
    public interface OnTabSelectedListener {

        /**
         * Called when a new tab is selected.
         *
         * @param newPosition index of the newly selected tab
         * @param oldPosition index of the previously selected tab
         */
        void onTabSelected(int newPosition, int oldPosition);
    }
}
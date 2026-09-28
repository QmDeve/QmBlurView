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

package com.qmdeve.blurview;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.qmdeve.blurview.widget.BottomTabBar;

import java.util.List;

/**
 * Manages the rendering and interaction logic for a collection of {@link TabView} instances
 * within a {@link BlurBottomNavigationView}. This manager is responsible for delegating
 * drawing operations to each tab and handling touch events to update the selected state.
 */
public class TabViewManager {
    private final BottomTabBar mNavigationView;

    /**
     * Constructs a new {@code TabViewManager} associated with the specified
     * {@link BlurBottomNavigationView}.
     *
     * @param navigationView the bottom navigation view that hosts the tab items
     */
    public TabViewManager(BottomTabBar navigationView) {
        this.mNavigationView = navigationView;
    }

    /**
     * Draws all tab views onto the provided {@link Canvas}. Each tab receives its
     * calculated width and the fixed height, and is visually rendered based on
     * whether it is currently selected.
     *
     * @param canvas          the canvas on which the tab views are drawn
     * @param tabViews        a list of {@link TabView} objects to be rendered
     * @param currentSelected the index of the currently selected tab
     * @param fixedHeight     the fixed height allocated for each tab's drawing area
     */
    public void drawTabs(Canvas canvas, List<TabView> tabViews, int currentSelected, int fixedHeight) {
        if (tabViews.isEmpty()) return;

        int tabWidth = mNavigationView.getWidth() / tabViews.size();

        for (int i = 0; i < tabViews.size(); i++) {
            TabView tabView = tabViews.get(i);
            tabView.draw(
                    canvas,
                    i * tabWidth,
                    0,
                    tabWidth,
                    fixedHeight,
                    i == currentSelected,
                    mNavigationView.getSelectedColor(),
                    mNavigationView.getUnselectedColor(),
                    mNavigationView.getTextSize(),
                    mNavigationView.isTextBold()
            );
        }
    }

    /**
     * Handles touch events for tab interaction. When a touch-up event occurs within
     * the area allocated for tabs, this method determines which tab (if any) is
     * being activated and updates the selected tab in the associated navigation view.
     *
     * @param event       the touch event to be processed
     * @param tabViews    the list of tab views that can respond to touch interaction
     * @param fixedHeight the vertical boundary within which a tab can be selected
     * @return always returns {@code true} to indicate the event has been handled
     */
    public boolean handleTouchEvent(MotionEvent event, List<TabView> tabViews, int fixedHeight) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();

            if (y <= fixedHeight) {
                for (int i = 0; i < tabViews.size(); i++) {
                    TabView tabView = tabViews.get(i);
                    if (tabView.contains(x, y, tabViews.size(), mNavigationView.getWidth(), fixedHeight)) {
                        mNavigationView.setSelectedTab(i);
                        break;
                    }
                }
            }
        }
        return true;
    }
}
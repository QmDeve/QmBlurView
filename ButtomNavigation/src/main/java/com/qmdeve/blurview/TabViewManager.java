package com.qmdeve.blurview;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.qmdeve.blurview.widget.BlurBottomNavigationView;

import java.util.List;

public class TabViewManager {
    private final BlurBottomNavigationView mNavigationView;

    public TabViewManager(BlurBottomNavigationView navigationView) {
        this.mNavigationView = navigationView;
    }

    public void drawTabs(Canvas canvas, List<TabView> tabViews, int currentSelected, int fixedHeight) {
        if (tabViews.isEmpty()) return;

        int tabWidth = mNavigationView.getWidth() / tabViews.size();
        int tabHeight = fixedHeight;

        for (int i = 0; i < tabViews.size(); i++) {
            TabView tabView = tabViews.get(i);
            tabView.draw(canvas, i * tabWidth, 0, tabWidth, tabHeight,
                    i == currentSelected,
                    mNavigationView.getSelectedColor(),
                    mNavigationView.getUnselectedColor(),
                    mNavigationView.getTextSize(),
                    mNavigationView.isTextBold());
        }
    }

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
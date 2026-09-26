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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.qmdeve.blurview.util.MenuUtils;
import com.qmdeve.blurview.util.Utils;

/**
 * Represents a single tab element used within a BlurBottomNavigation layout.
 * <p>
 * A {@code TabView} is responsible for drawing both the icon and the title
 * of a menu entry onto a supplied {@link Canvas}. It determines how elements
 * are aligned, how colors are applied based on state, and how touch hit
 * detection is evaluated for interaction handling. This class does not
 * participate in the Android view hierarchy; instead, it operates as a
 * lightweight rendering helper managed by its parent container.
 * </p>
 *
 * <p>
 * Icon resources referenced by the associated {@link MenuUtils.MenuItem}
 * may be resolved from theme attributes, drawable resources, or mipmap
 * resources. When no valid icon can be resolved, a placeholder icon is
 * generated internally. The class also supports rendering of text labels
 * with optional bold styling, and calculates spacing based on configured
 * icon size and text size.
 * </p>
 *
 * <p>
 * Primary responsibilities include:
 * <ul>
 *     <li>Loading and preparing icon drawables</li>
 *     <li>Rendering icon-only, text-only, or combined layouts</li>
 *     <li>Applying selection colors through color filters</li>
 *     <li>Computing vertical layout alignment and spacing</li>
 *     <li>Determining whether a pointer event falls within the tab area</li>
 * </ul>
 * </p>
 */
public class TabView {

    /**
     * The Android context used to resolve resources and theme attributes.
     */
    private Context mContext;

    /**
     * The menu item associated with this tab, providing title and icon reference information.
     */
    private MenuUtils.MenuItem mMenuItem;

    /**
     * Index of this tab within the overall navigation layout.
     */
    private int mIndex;

    /**
     * Drawable instance representing the tab icon, if any.
     */
    private Drawable mIcon;

    /**
     * Size of the icon in pixels.
     */
    private float mIconSize;

    /**
     * Temporary rectangle used internally to compute text bounds.
     */
    private final Rect mTextBounds;

    /**
     * Creates a new {@code TabView} instance.
     *
     * @param context   the Android context used for resource resolution
     * @param menuItem  menu item providing text and icon references
     * @param index     the tab index within the navigation container
     * @param iconSize  the desired icon size in pixels
     */
    public TabView(Context context, MenuUtils.MenuItem menuItem, int index, float iconSize) {
        this.mContext = context;
        this.mMenuItem = menuItem;
        this.mIndex = index;
        this.mIconSize = iconSize;
        this.mTextBounds = new Rect();

        if (menuItem.getIcon() != null && !menuItem.getIcon().isEmpty()) {
            mIcon = loadIcon(menuItem.getIcon());
        }

        if (mIcon == null) {
            mIcon = createPlaceholderIcon();
        }
    }

    /**
     * Loads an icon drawable based on the string reference provided by the menu item.
     * <p>
     * The method attempts to resolve the reference from theme attributes,
     * drawable resources, and mipmap resources, in that order.
     * </p>
     *
     * @param iconRef the icon resource reference string
     * @return a resolved {@link Drawable}, or {@code null} if resolution fails
     */
    private Drawable loadIcon(String iconRef) {
        try {
            if (iconRef.startsWith("?")) {
                String attrIdStr = iconRef.substring(1);
                try {
                    int attrId = Integer.parseInt(attrIdStr);
                    TypedValue typedValue = new TypedValue();
                    if (mContext.getTheme().resolveAttribute(attrId, typedValue, true)) {
                        return ContextCompat.getDrawable(mContext, typedValue.resourceId);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            int iconResId = mContext.getResources().getIdentifier(iconRef, "drawable", mContext.getPackageName());
            if (iconResId != 0) {
                return ContextCompat.getDrawable(mContext, iconResId);
            }

            iconResId = mContext.getResources().getIdentifier(iconRef, "mipmap", mContext.getPackageName());
            if (iconResId != 0) {
                return ContextCompat.getDrawable(mContext, iconResId);
            }
        } catch (Exception e) {
            Log.e("BlurBottomNavigation", "Error loading icon: " + iconRef, e);
        }
        return null;
    }

    /**
     * Creates a simple placeholder icon when no valid resource can be resolved.
     *
     * @return a placeholder {@link Drawable} representing a solid colored square
     */
    private Drawable createPlaceholderIcon() {
        ColorDrawable placeholder = new ColorDrawable(Color.LTGRAY);
        placeholder.setBounds(0, 0, (int) mIconSize, (int) mIconSize);
        return placeholder;
    }

    /**
     * Draws this tab onto the supplied {@link Canvas}.
     * <p>
     * Depending on the menu item content, the method draws the icon only,
     * the text only, or both elements vertically aligned. Selection state
     * affects the color used for both icon and text.
     * </p>
     *
     * @param canvas         the canvas onto which the tab is rendered
     * @param left           left coordinate of the tab area
     * @param top            top coordinate of the tab area
     * @param width          width of the tab area
     * @param height         height of the tab area
     * @param isSelected     whether the tab is currently selected
     * @param selectedColor  color applied when selected
     * @param unselectedColor color applied when not selected
     * @param textSize       text size in pixels
     * @param textBold       {@code true} to use bold typeface, {@code false} otherwise
     */
    public void draw(Canvas canvas, int left, int top, int width, int height, boolean isSelected, int selectedColor, int unselectedColor, float textSize, boolean textBold) {
        int color = isSelected ? selectedColor : unselectedColor;

        boolean hasIcon = hasIcon();
        boolean hasTitle = hasTitle();

        int contentHeight = calculateContentHeight(height, hasIcon, hasTitle, textSize);
        int contentTop = top + (height - contentHeight) / 2;

        if (hasIcon && hasTitle) {
            drawIconAndText(canvas, left, contentTop, width, contentHeight, color, textSize, textBold);
        } else if (hasIcon) {
            drawIconOnly(canvas, left, contentTop, width, contentHeight, color);
        } else if (hasTitle) {
            drawTextOnly(canvas, left, contentTop, width, contentHeight, color, textSize, textBold);
        }
    }

    /**
     * Renders the icon and text elements together in a stacked vertical layout.
     *
     * @param canvas    drawable canvas
     * @param left      left position of tab
     * @param top       top position of content
     * @param width     available width
     * @param height    calculated content height
     * @param color     color applied to both icon and text
     * @param textSize  size of text in pixels
     * @param textBold  whether text uses a bold typeface
     */
    private void drawIconAndText(Canvas canvas, int left, int top, int width, int height, int color, float textSize, boolean textBold) {
        int iconLeft = left + (width - (int) mIconSize) / 2;

        if (mIcon != null) {
            mIcon.setBounds(iconLeft, top, iconLeft + (int) mIconSize, top + (int) mIconSize);
            mIcon.mutate();
            mIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mIcon.draw(canvas);
            mIcon.clearColorFilter();
        }

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(color);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(textBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.getTextBounds(mMenuItem.getTitle(), 0, mMenuItem.getTitle().length(), mTextBounds);

        float textX = left + width / 2f;
        float textY = top + (int) mIconSize + mTextBounds.height() + TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics());

        canvas.drawText(mMenuItem.getTitle(), textX, textY, textPaint);
    }

    /**
     * Renders only the icon centered within the available area.
     *
     * @param canvas canvas for rendering
     * @param left   left coordinate
     * @param top    top coordinate
     * @param width  width of the tab
     * @param height height of the tab
     * @param color  applied icon color
     */
    private void drawIconOnly(Canvas canvas, int left, int top, int width, int height, int color) {
        int iconLeft = left + (width - (int) mIconSize) / 2;
        int iconTop = top + (height - (int) mIconSize) / 2;

        if (mIcon != null) {
            mIcon.setBounds(iconLeft, iconTop, iconLeft + (int) mIconSize, iconTop + (int) mIconSize);
            mIcon.mutate();
            mIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mIcon.draw(canvas);
            mIcon.clearColorFilter();
        }
    }

    /**
     * Renders only the text centered both horizontally and vertically.
     *
     * @param canvas   canvas for rendering
     * @param left     left coordinate
     * @param top      top coordinate
     * @param width    width of the tab
     * @param height   height of the tab
     * @param color    applied text color
     * @param textSize size of the text in pixels
     * @param textBold whether the typeface is bold
     */
    private void drawTextOnly(Canvas canvas, int left, int top, int width, int height, int color, float textSize, boolean textBold) {
        Paint textPaint = new Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(color);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(textBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.getTextBounds(mMenuItem.getTitle(), 0, mMenuItem.getTitle().length(), mTextBounds);

        float textX = left + width / 2f;
        float textY = top + height / 2f + mTextBounds.height() / 2f;

        canvas.drawText(mMenuItem.getTitle(), textX, textY, textPaint);
    }

    /**
     * Calculates the total vertical content height based on whether icon
     * and/or text elements are present.
     *
     * @param availableHeight total space available
     * @param hasIcon         whether the menu item provides an icon
     * @param hasTitle        whether the menu item provides text
     * @param textSize        text size in pixels
     * @return the required vertical content height
     */
    private int calculateContentHeight(int availableHeight, boolean hasIcon, boolean hasTitle, float textSize) {
        if (hasIcon && hasTitle) {
            Paint textPaint = new Paint();
            textPaint.setTextSize(textSize);
            textPaint.getTextBounds(mMenuItem.getTitle(), 0, mMenuItem.getTitle().length(), mTextBounds);

            return (int) (mIconSize + mTextBounds.height() + Utils.dp2px(mContext.getResources(), 4));
        } else if (hasIcon) {
            return (int) mIconSize;
        } else if (hasTitle) {
            Paint textPaint = new Paint();
            textPaint.setTextSize(textSize);
            textPaint.getTextBounds(mMenuItem.getTitle(), 0, mMenuItem.getTitle().length(), mTextBounds);
            return mTextBounds.height();
        }
        return availableHeight;
    }

    /**
     * Indicates whether the tab contains a drawable icon.
     *
     * @return {@code true} if an icon is available, {@code false} otherwise
     */
    private boolean hasIcon() {
        return mMenuItem.getIcon() != null && !mMenuItem.getIcon().isEmpty() && mIcon != null;
    }

    /**
     * Indicates whether the tab contains a text label.
     *
     * @return {@code true} if title text exists, {@code false} otherwise
     */
    private boolean hasTitle() {
        return mMenuItem.getTitle() != null && !mMenuItem.getTitle().isEmpty();
    }

    /**
     * Determines whether the given coordinates fall within the bounds
     * assigned to this tab.
     *
     * @param x          x-coordinate to test
     * @param y          y-coordinate to test
     * @param tabCount   total number of tabs in the container
     * @param viewWidth  overall width of the navigation container
     * @param fixedHeight fixed height of the navigation bar
     * @return {@code true} if the point lies within this tab, otherwise {@code false}
     */
    public boolean contains(float x, float y, int tabCount, int viewWidth, int fixedHeight) {
        int tabWidth = viewWidth / tabCount;
        int left = mIndex * tabWidth;
        int right = left + tabWidth;
        return x >= left && x <= right && y >= 0 && y <= fixedHeight;
    }
}
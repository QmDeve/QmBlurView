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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.qmdeve.blurview.R;
import com.qmdeve.blurview.util.Utils;

/**
 * A blurred search bar: search icon + editable text field on a live-blurred
 * background. Uses {@link RenderEffect} on API 31+ and the native pipeline
 * below.
 */
public class SearchBar extends BlurViewGroup {

    public interface OnSearchActionListener {
        void onSearchAction(@NonNull String query);
    }

    private final ImageView mIconView;
    private final EditText mEditText;
    private final BorderRenderer mBorder = new BorderRenderer();
    private OnSearchActionListener mSearchListener;
    private int mIconMarginStart;
    private int mIconMarginEnd;

    public SearchBar(Context context) {
        this(context, null);
    }

    public SearchBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mIconView = new ImageView(context);
        mIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(mIconView);

        mEditText = new EditText(context);
        mEditText.setBackground(null);
        mEditText.setSingleLine(true);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        mEditText.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        addView(mEditText);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        float density = context.getResources().getDisplayMetrics().density;

        int iconSize = (int) (20 * density);
        mIconMarginStart = (int) (12 * density);
        mIconMarginEnd = (int) (8 * density);
        int textSizeSp = 15;
        int textColor = Color.WHITE;
        int hintColor = 0xB3FFFFFF;
        int iconTint = Color.WHITE;
        String hint = "";
        Drawable icon = null;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchBar);
            hint = a.getString(R.styleable.SearchBar_searchHint);
            textSizeSp = (int) a.getDimension(R.styleable.SearchBar_searchTextSize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15,
                            context.getResources().getDisplayMetrics()));
            textColor = a.getColor(R.styleable.SearchBar_searchTextColor, textColor);
            hintColor = a.getColor(R.styleable.SearchBar_searchHintColor, hintColor);
            iconTint = a.getColor(R.styleable.SearchBar_searchIconTint, iconTint);
            iconSize = (int) a.getDimension(R.styleable.SearchBar_searchIconSize, iconSize);
            icon = a.getDrawable(R.styleable.SearchBar_searchIcon);
            mBorder.setWidth(a.getDimension(R.styleable.SearchBar_searchBorderWidth, 0f));
            mBorder.setColor(a.getColor(R.styleable.SearchBar_searchBorderColor, Color.TRANSPARENT));
            a.recycle();
        }

        if (icon == null) {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_search);
        }
        if (icon != null) {
            icon = icon.mutate();
            icon.setColorFilter(iconTint, PorterDuff.Mode.SRC_IN);
            mIconView.setImageDrawable(icon);
        }

        ViewGroup.LayoutParams iconLp = mIconView.getLayoutParams();
        if (iconLp == null) {
            iconLp = new ViewGroup.LayoutParams(iconSize, iconSize);
        } else {
            iconLp.width = iconSize;
            iconLp.height = iconSize;
        }
        mIconView.setLayoutParams(iconLp);

        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeSp);
        mEditText.setTextColor(textColor);
        mEditText.setHintTextColor(hintColor);
        if (hint != null) {
            mEditText.setHint(hint);
        }

        mEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (mSearchListener != null) {
                    mSearchListener.onSearchAction(getText());
                }
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int iconSize = mIconView.getLayoutParams().width;

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        mIconView.measure(
                MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY));

        int editWidth = Math.max(0, widthSize - mIconMarginStart - iconSize - mIconMarginEnd);
        mEditText.measure(
                MeasureSpec.makeMeasureSpec(editWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {        int iconSize = mIconView.getMeasuredWidth();
        int height = b - t;

        int iconTop = (height - iconSize) / 2;
        mIconView.layout(mIconMarginStart, iconTop, mIconMarginStart + iconSize, iconTop + iconSize);

        int editLeft = mIconMarginStart + iconSize + mIconMarginEnd;
        mEditText.layout(editLeft, 0, r - l, height);
    }

    /**
     * Draws the border on top of the blur layer (but below the icon/text).
     */
    @Override
    protected void drawBlurLayer(@NonNull Canvas canvas, int width, int height,
                                 boolean isEditMode, boolean shouldDrawBlur) {
        super.drawBlurLayer(canvas, width, height, isEditMode, shouldDrawBlur);
        mBorder.draw(canvas, mBlurEngine, width, height);
    }

    // ------------------------------------------------------------------
    // Search API
    // ------------------------------------------------------------------

    @NonNull
    public String getText() {
        return mEditText.getText() != null ? mEditText.getText().toString() : "";
    }

    public void setText(@Nullable CharSequence text) {
        mEditText.setText(text);
    }

    public void setHint(@Nullable CharSequence hint) {
        mEditText.setHint(hint);
    }

    public void setSearchIcon(@DrawableRes int resId) {
        Drawable icon = ContextCompat.getDrawable(getContext(), resId);
        if (icon != null) {
            mIconView.setImageDrawable(icon);
        }
    }

    public void setIconTint(@ColorInt int color) {
        Drawable icon = mIconView.getDrawable();
        if (icon != null) {
            icon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setOnSearchActionListener(@Nullable OnSearchActionListener listener) {
        mSearchListener = listener;
    }

    public void addTextChangedListener(@NonNull TextWatcher watcher) {
        mEditText.addTextChangedListener(watcher);
    }

    public void removeTextChangedListener(@NonNull TextWatcher watcher) {
        mEditText.removeTextChangedListener(watcher);
    }

    /** Exposes the inner EditText for cursor color, input type, etc. */
    @NonNull
    public EditText getEditText() {
        return mEditText;
    }

    public void setBorderWidth(float widthPx) {
        if (mBorder.getWidth() != widthPx) {
            mBorder.setWidth(widthPx);
            invalidate();
        }
    }

    public float getBorderWidth() {
        return mBorder.getWidth();
    }

    public void setBorderColor(@ColorInt int color) {
        if (mBorder.getColor() != color) {
            mBorder.setColor(color);
            invalidate();
        }
    }

    @ColorInt
    public int getBorderColor() {
        return mBorder.getColor();
    }

    /** Convenience: dp-based border width. */
    public void setBorderWidthDp(float widthDp) {
        setBorderWidth(Utils.dp2px(getResources(), widthDp));
    }

    /** Convenience dp-based default corner radius for a pill shape. */
    public void setPillShape(boolean pill) {
        if (pill) {
            post(() -> mBlurEngine.setCornerRadius(getHeight() / 2f));
        }
    }
}

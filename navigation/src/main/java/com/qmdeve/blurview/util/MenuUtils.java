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

package com.qmdeve.blurview.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
    /**
     * Android attribute namespace used for reading standard menu attributes
     * such as {@code android:title}, {@code android:icon}, and {@code android:id}.
     */
    private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

    /**
     * Parses a menu XML resource and extracts all {@code <item>} elements into a list
     * of {@link MenuItem} objects. Attributes such as title, icon, and id are resolved
     * from either resource references or literal values defined in the XML.
     *
     * <p>Typical usage involves reading an Android menu layout inside a non-UI component
     * or utility class that needs access to the menu structure.</p>
     *
     * @param context   the application context used to access resources
     * @param menuResId the resource ID of the menu XML to parse
     * @return a list of {@link MenuItem} instances extracted from the XML; never {@code null}
     */
    public static List<MenuItem> parseMenu(Context context, int menuResId) {
        List<MenuItem> menuItems = new ArrayList<>();

        if (menuResId == 0) {
            Log.e("MenuUtils", "Invalid menu resource ID: 0");
            return menuItems;
        }

        try {
            XmlResourceParser parser = context.getResources().getXml(menuResId);

            int eventType = parser.getEventType();
            MenuItem currentItem = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("item".equals(tagName)) {
                            currentItem = new MenuItem();

                            // Resolve title attribute
                            int titleRes = parser.getAttributeResourceValue(ANDROID_NAMESPACE, "title", 0);
                            if (titleRes != 0) {
                                currentItem.setTitle(context.getResources().getString(titleRes));
                            } else {
                                currentItem.setTitle(parser.getAttributeValue(ANDROID_NAMESPACE, "title"));
                            }

                            // Resolve icon attribute
                            int iconRes = parser.getAttributeResourceValue(ANDROID_NAMESPACE, "icon", 0);
                            if (iconRes != 0) {
                                currentItem.setIcon(context.getResources().getResourceName(iconRes));
                            } else {
                                currentItem.setIcon(parser.getAttributeValue(ANDROID_NAMESPACE, "icon"));
                            }

                            // Resolve id attribute in raw string form (e.g., "@+id/menu_item")
                            currentItem.setId(parser.getAttributeValue(ANDROID_NAMESPACE, "id"));
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("item".equals(tagName) && currentItem != null) {
                            menuItems.add(currentItem);
                            Log.d("MenuUtils", "Added menu item: " + currentItem);
                            currentItem = null;
                        }
                        break;
                }

                eventType = parser.next();
            }

        } catch (Exception e) {
            Log.e("MenuUtils", "Error parsing menu XML with resource ID: " + menuResId, e);
        }

        Log.d("MenuUtils", "Parsed " + menuItems.size() + " menu items");
        return menuItems;
    }

    /**
     * Represents a menu item parsed from an Android menu XML resource.
     * Contains the resolved values for ID, title, and icon attributes.
     */
    public static class MenuItem {
        private String id;
        private String title;
        private String icon;
        private String showAsAction;

        /**
         * Returns the raw string value of the {@code android:id} attribute.
         *
         * @return the menu item ID string
         */
        public String getId() { return id; }

        /**
         * Sets the raw string representation of the menu item's ID.
         *
         * @param id the ID string extracted from the XML
         */
        public void setId(String id) { this.id = id; }

        /**
         * Returns the resolved menu item title. May be either a resource string
         * or a literal value defined in the XML.
         *
         * @return the menu item title
         */
        public String getTitle() { return title; }

        /**
         * Sets the title for this menu item.
         *
         * @param title the title string
         */
        public void setTitle(String title) { this.title = title; }

        /**
         * Returns the icon reference string. When the icon attribute refers to
         * a drawable resource, this will contain the resource name.
         *
         * @return the icon reference or literal value
         */
        public String getIcon() { return icon; }

        /**
         * Sets the icon value for this menu item.
         *
         * @param icon the icon reference string
         */
        public void setIcon(String icon) { this.icon = icon; }

        /**
         * Returns a string representation of this menu item for debugging purposes.
         *
         * @return formatted menu item information
         */
        @NonNull
        @Override
        public String toString() {
            return "MenuItem{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", icon='" + icon + '\'' +
                    ", showAsAction='" + showAsAction + '\'' +
                    '}';
        }
    }
}
package com.qmdeve.blurview.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
    private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

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

                            int titleRes = parser.getAttributeResourceValue(ANDROID_NAMESPACE, "title", 0);
                            if (titleRes != 0) {
                                currentItem.setTitle(context.getResources().getString(titleRes));
                            } else {
                                currentItem.setTitle(parser.getAttributeValue(ANDROID_NAMESPACE, "title"));
                            }

                            int iconRes = parser.getAttributeResourceValue(ANDROID_NAMESPACE, "icon", 0);
                            if (iconRes != 0) {
                                currentItem.setIcon(context.getResources().getResourceName(iconRes));
                            } else {
                                String iconAttr = parser.getAttributeValue(ANDROID_NAMESPACE, "icon");
                                currentItem.setIcon(iconAttr);
                            }

                            String id = parser.getAttributeValue(ANDROID_NAMESPACE, "id");
                            currentItem.setId(id);
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

    public static class MenuItem {
        private String id;
        private String title;
        private String icon;
        private String showAsAction;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

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
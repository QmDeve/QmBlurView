package com.qmdeve.blurview;

import android.graphics.Bitmap;

public interface Blur {
    boolean prepare(Bitmap buffer, float radius);
    void release();
    void blur(Bitmap input, Bitmap output);
}
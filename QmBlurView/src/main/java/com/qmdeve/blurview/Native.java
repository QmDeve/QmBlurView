package com.qmdeve.blurview;

public class Native {
    static {
        System.loadLibrary("QmBlur");
    }

    public static native void blur(
            Object bitmap,
            int radius,
            int threadCount,
            int threadIndex,
            int round
    );
}

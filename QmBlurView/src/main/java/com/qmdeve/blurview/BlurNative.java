package com.qmdeve.blurview;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlurNative implements Blur {

    private static final int MAX_RADIUS = 25;
    private static final int MIN_RADIUS = 2;
    private static final int THREAD_COUNT;
    private static final ExecutorService EXECUTOR;

    static {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        THREAD_COUNT = Math.max(2, Math.min(5, cpuCount));
        EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT, r -> {
            Thread t = new Thread(r, "NativeBlurThread");
            t.setPriority(Thread.MIN_PRIORITY);
            t.setDaemon(true);
            return t;
        });
        System.loadLibrary("QmBlur");
    }

    private final AtomicBoolean isBlurring = new AtomicBoolean(false);
    private float radius = MAX_RADIUS;

    public static native void blur(
            Object bitmap,
            int radius,
            int threadCount,
            int threadIndex,
            int round
    );

    @Override
    public boolean prepare(Bitmap buffer, float radius) {
        this.radius = clamp(radius);
        return true;
    }

    @Override
    public void release() {
        // Shared executor, do not shutdown
        // But we can clear any thread-local resources if needed
    }

    @Override
    public void blur(Bitmap input, Bitmap output) {
        if (input == null || output == null ||
                input.isRecycled() || output.isRecycled()) return;

        if (!isBlurring.compareAndSet(false, true)) return;

        try {
            if (input != output) {
                // Clear the output bitmap first
                output.eraseColor(0);
                new Canvas(output).drawBitmap(input, 0, 0, null);
            }
            doBlurRound(output, 1);
            doBlurRound(output, 2);
        } catch (Exception e) {
            if (isDebug(null)) e.printStackTrace();
        } finally {
            isBlurring.set(false);
        }
    }

    private void doBlurRound(Bitmap bitmap, int round) {
        int r = (int) radius;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            EXECUTOR.execute(() -> {
                try {
                    blur(bitmap, r, THREAD_COUNT, index, round);
                } catch (Exception e) {
                    if (isDebug(null)) e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static float clamp(float value) {
        return Math.max((float) BlurNative.MIN_RADIUS, Math.min((float) BlurNative.MAX_RADIUS, value));
    }

    private static Boolean DEBUG = null;

    static boolean isDebug(Context ctx) {
        if (DEBUG == null && ctx != null) {
            DEBUG = (ctx.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        return Boolean.TRUE.equals(DEBUG);
    }
}
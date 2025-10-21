package com.qmdeve.blurview;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlurNative implements Blur {

    private static final int DEFAULT_THREAD_COUNT = 4;
    private static final int MAX_RADIUS = 25;
    private static final int MIN_RADIUS = 2;
    private final AtomicBoolean isBlurring = new AtomicBoolean(false);
    private ExecutorService executorService;
    private int threadCount = DEFAULT_THREAD_COUNT;
    private float radius = MAX_RADIUS;

    static {
        System.loadLibrary("QmBlur");
    }

    private native void blur(
            Object bitmap,
            int radius,
            int threadCount,
            int threadIndex,
            int round
    );

    @Override
    public boolean prepare(Bitmap buffer, float radius) {
        this.radius = clamp(radius);

        synchronized (this) {
            if (executorService == null || executorService.isShutdown()) {
                int cpuCount = Runtime.getRuntime().availableProcessors();
                threadCount = Math.max(2, Math.min(4, cpuCount));

                executorService = new ThreadPoolExecutor(
                        threadCount,
                        threadCount,
                        10L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(),
                        r -> {
                            Thread t = new Thread(r, "NativeBlurThread");
                            t.setPriority(Thread.MIN_PRIORITY);
                            t.setDaemon(true);
                            return t;
                        }
                );
            }
        }
        return true;
    }

    @Override
    public void release() {
        synchronized (this) {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
                executorService = null;
            }
        }
    }

    @Override
    public void blur(Bitmap input, Bitmap output) {
        if (input == null || output == null ||
                input.isRecycled() || output.isRecycled()) return;

        if (!isBlurring.compareAndSet(false, true)) return;

        try {
            if (input != output) {
                synchronized (this) {
                    new Canvas(output).drawBitmap(input, 0, 0, null);
                }
            }
            doBlurRound(output, 1);
            doBlurRound(output, 2);
        } finally {
            isBlurring.set(false);
        }
    }

    private void doBlurRound(Bitmap bitmap, int round) {
        if (executorService == null || executorService.isShutdown()) return;

        int r = (int) radius;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    blur(bitmap, r, threadCount, index, round);
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
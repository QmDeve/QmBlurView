/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Donny Yale
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
 * Created Date: 2025-10-21
 * Author: Donny Yale
 * GitHub: https://github.com/QmDeve/QmBlurView
 * Website: https://blurview.qmdeve.com
 * ===========================================
 */

package com.qmdeve.blurview;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import androidx.tracing.Trace;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Native blur implementation,
 * Gaussian blur through JNI call cpp code
 */
public class BlurNative implements Blur {

    // The maximum value of the blur radius
    private static final int MAX_RADIUS = 100;

    // The minimum value of the blur radius
    private static final int MIN_RADIUS = 2;

    // Thread pool configuration
    private static final int THREAD_COUNT;
    private static final ExecutorService EXECUTOR;

    static {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        THREAD_COUNT = Math.max(2, Math.min(5, cpuCount));
        final AtomicInteger threadIdx = new AtomicInteger(0);
        EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT, r -> {
            // Linux thread names (comm) cap at 15 chars, so "NativeBlurThread" (16) truncates
            // identically for every worker -> indistinguishable tracks in Perfetto. Use a short,
            // indexed name that survives truncation so each worker is a labeled, ordered track.
            // While a blur pass runs, the main thread does nothing but wait for these
            // workers (latch.await in doBlurRound). So for that window the workers ARE
            // the frame: every cycle the scheduler denies them directly extends frame
            // time. Giving them the lowest priority (the old MIN_PRIORITY) did the
            // opposite of what the frame needs: background-priority threads are confined
            // to the slow little cores while the UI thread sits blocked waiting on them.
            // Measured on Pixel 6: any background-band priority ≈ 17% janky frames in the
            // 5-view scene; DEFAULT ≈ 2%. Raising further (up to URGENT_DISPLAY) changes
            // nothing measurable, so stay at DEFAULT — no thermal/battery cost and no
            // competition with the render pipeline's own threads. Set via
            // android.os.Process (controls the Linux nice value and core placement;
            // Java's Thread.setPriority maps too coarsely to matter).
            Thread t = new Thread(() -> {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
                r.run();
            }, "NativeBlur-" + threadIdx.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
        System.loadLibrary("QmBlur");
    }

    // SRC xfermode makes drawBitmap replace every destination pixel, alpha included —
    // used by the input copy in blur() so it doesn't need a separate erase pass first.
    private static final Paint COPY_SRC_PAINT = new Paint();
    static {
        COPY_SRC_PAINT.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private final AtomicBoolean isBlurring = new AtomicBoolean(false);
    private float radius = MAX_RADIUS;
    private int blurRounds = 2; // Default to 2 iterations (each = horizontal + vertical pass) for better performance

    /**
     *
     * @param bitmap Bitmap objects to be blurred
     * @param radius Blur radius
     * @param threadCount Total number of threads
     * @param threadIndex Current thread index
     * @param round Pass direction (1 = horizontal, 2 = vertical)
     * @param rounds How many times to apply the pass to this thread's band
     */
    public static native void blur(
            Object bitmap,
            int radius,
            int threadCount,
            int threadIndex,
            int round,
            int rounds
    );

    @Override
    public boolean prepare(Bitmap buffer, float radius) {
        this.radius = clamp(radius);
        return true;
    }

    /**
     * Set the number of blur iterations
     * Each iteration applies both horizontal and vertical blur passes
     * More iterations = stronger blur effect
     * @param rounds Number of blur iterations (1-15)
     */
    public void setBlurRounds(int rounds) {
        this.blurRounds = Math.max(1, Math.min(15, rounds));
    }

    /**
     * Get the current number of blur rounds
     * @return Current blur rounds
     */
    public int getBlurRounds() {
        return blurRounds;
    }

    @Override
    public void release() {
        // Shared executor, do not shutdown
    }

    @Override
    public void blur(Bitmap input, Bitmap output) {
        if (input == null || output == null ||
                input.isRecycled() || output.isRecycled()) return;

        if (!isBlurring.compareAndSet(false, true)) return;

        try {
            if (input != output) {
                // The copy itself is load-bearing: output must only ever hold a finished
                // blur (frames that skip the blur after capture keep presenting the last
                // good result), so the capture is copied rather than blurred in place.
                // But the old two-pass form — eraseColor(0) + SRC_OVER drawBitmap —
                // wrote every output pixel twice; the erase existed only so previous
                // output couldn't bleed through transparent capture pixels. Drawing with
                // a SRC-mode paint replaces destination pixels including alpha in one
                // pass: bit-identical result, half the memory traffic. Measured
                // (Pixel 6): copyInput time −46…−53% across all three benchmark scenes.
                Trace.beginSection("BlurNative.copyInput");
                try {
                    new Canvas(output).drawBitmap(input, 0, 0, COPY_SRC_PAINT);
                } finally {
                    Trace.endSection();
                }
            }
            // Blur rounds are grouped by direction: all horizontal rounds first, then all
            // vertical rounds (H^n·V^n instead of the previous (H·V)^n interleaving).
            // Legal because StackBlur is a linear separable filter — H and V passes
            // commute, so the composite kernel is identical (only per-pass integer
            // rounding differs, ±1 in the low bits). The payoff is dependency structure:
            // a horizontal pass only mixes pixels within a row, so each worker can run
            // ALL H rounds on its own row band inside one dispatch with no inter-round
            // synchronization (same for V rounds on column bands). Previously every round
            // paid two full dispatch+latch barriers (2×rounds per blur); now a blur pays
            // exactly 2 regardless of rounds — extra rounds cost only pixel work, and the
            // repeated rounds run while the worker's band is still hot in cache.
            // Measured vs interleaved (Pixel 6): total blur time −12…−14% at the default
            // rounds=2; at rounds=8 interleaved costs +18% blur time and +21% frame P99.
            Trace.beginSection("BlurNative.blurPass");
            doBlurRound(output, 1, blurRounds); // All horizontal rounds, one dispatch
            doBlurRound(output, 2, blurRounds); // All vertical rounds, one dispatch
            Trace.endSection();
        } catch (Exception e) {
            // Only print stack trace if debug mode is enabled
            // Note: DEBUG may be null if Context was never provided
            if (Boolean.TRUE.equals(DEBUG)) e.printStackTrace();
        } finally {
            isBlurring.set(false);
        }
    }

    /**
     * Perform fuzzy operations
     * @param bitmap Blurry bitmaps are needed
     * @param round Pass direction (1 = horizontal, 2 = vertical)
     * @param rounds How many times each worker applies the pass to its band
     */
    private void doBlurRound(Bitmap bitmap, int round, int rounds) {
        // round 1 = horizontal pass, round 2 = vertical pass. Constant labels (no per-frame allocation).
        Trace.beginSection(round == 1 ? "BlurNative.passH" : "BlurNative.passV");
        try {
        int r = (int) radius;

        // Optimization: For small images or single-core devices, skip thread overhead
        if (THREAD_COUNT == 1) {
            blur(bitmap, r, 1, 0, round, rounds);
            return;
        }

        // NOTE: fusing both passes into one dispatch (worker: H → CyclicBarrier → V)
        // was tried and measured NEUTRAL (dev log #8) — with warm DEFAULT-priority
        // workers the second dispatch is already near-free, and the barrier added
        // deadlock-discipline complexity. Two latch dispatches stay: simpler, as fast.
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT - 1);

        for (int i = 1; i < THREAD_COUNT; i++) {
            final int index = i;
            EXECUTOR.execute(() -> {
                try {
                    blur(bitmap, r, THREAD_COUNT, index, round, rounds);
                } catch (Exception e) {
                    // Only print stack trace if debug mode is enabled
                    // Note: DEBUG may be null if Context was never provided
                    if (Boolean.TRUE.equals(DEBUG)) e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // The main thread computes band 0 itself instead of parking in latch.await():
        // its band starts with zero dispatch latency on the core it already owns, the
        // worker wake-ups overlap with that work, and a previously idle core joins the
        // pass. By the time main reaches await() the latch is usually already open.
        // Output is identical — same bands, same math, just one fewer thread hop.
        // Measured vs the park-and-wait version (Pixel 6): total blur time −8%,
        // vertical pass −11% in the single-view scene; all metrics improved or flat.
        blur(bitmap, r, THREAD_COUNT, 0, round, rounds);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        } finally {
            Trace.endSection();
        }
    }

    private static float clamp(float value) {
        return Math.max((float) BlurNative.MIN_RADIUS, Math.min((float) BlurNative.MAX_RADIUS, value));
    }

    private static Boolean DEBUG = null;

    /**
     * Determine whether it is currently in debugging mode
     * @param ctx Context
     * @return Boolean
     */
    static boolean isDebug(Context ctx) {
        if (DEBUG == null && ctx != null) {
            DEBUG = (ctx.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        return Boolean.TRUE.equals(DEBUG);
    }
}
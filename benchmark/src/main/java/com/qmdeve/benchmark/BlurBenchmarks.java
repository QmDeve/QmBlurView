package com.qmdeve.benchmark;

import android.content.Intent;
import android.os.Bundle;

import androidx.benchmark.macro.FrameTimingGfxInfoMetric;
import androidx.benchmark.macro.MacrobenchmarkScope;
import androidx.benchmark.macro.Metric;
import androidx.benchmark.macro.TraceSectionMetric;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import java.util.Arrays;
import java.util.List;

/**
 * Shared pieces for the blur macrobenchmarks so every scene is measured identically
 * (same metrics, same scroll gesture) and A/B numbers stay comparable across scenes.
 */
final class BlurBenchmarks {

    static final String PACKAGE = "com.qmdeve.blurview.demo";

    private BlurBenchmarks() {
    }

    /** Total time-in-section per iteration — the number an optimization should shrink. */
    private static TraceSectionMetric sum(String section) {
        return new TraceSectionMetric(section, TraceSectionMetric.Mode.Sum.INSTANCE);
    }

    /**
     * The metrics list, shared by every scene.
     * <p>
     * FrameTimingGfxInfoMetric reads frame stats from {@code dumpsys gfxinfo}, independent
     * of the perfetto frame-timeline schema that FrameTimingMetric's duration/overrun
     * metrics choke on under this preview OS. It emits the jank/dropped-frame numbers
     * directly: {@code gfxFrameJankPercent}, {@code deadlineMissedFrameCount},
     * {@code gfxFrameTime50/90/95/99thPercentileMs}, {@code gfxFrameTotalCount}.
     * <p>
     * TraceSectionMetric reads slice durations by name (also schema-independent) for the
     * per-stage blur breakdown; sections mirror the ATrace sections in :core
     * (BaseBlurView / BlurNative), which every blur widget shares.
     */
    static List<Metric> metrics() {
        return Arrays.asList(
                new FrameTimingGfxInfoMetric(),   // jank% + deadlineMissed + frame-time percentiles
                sum("BlurView.performBlurSync"),  // whole per-frame blur
                sum("BlurView.blur"),             // CPU blur
                sum("BlurView.captureDecorView"), // software window capture
                sum("BlurNative.passH"),          // dispatch+await barrier (H)
                sum("BlurNative.passV"),          // dispatch+await barrier (V)
                sum("BlurNative.copyInput"));
    }

    /** Launch a specific (benchmark-exported) demo activity and wait until it is scrollable. */
    static void launch(MacrobenchmarkScope scope, String activityClass) {
        launch(scope, activityClass, null);
    }

    /**
     * Same as {@link #launch(MacrobenchmarkScope, String)} but forwards optional intent
     * extras to the launched activity — used by scenes that need to parametrize the demo
     * (e.g. the high-rounds scene passes {@code blurRounds}).
     */
    static void launch(MacrobenchmarkScope scope, String activityClass, Bundle extras) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(PACKAGE, PACKAGE + "." + activityClass);
        if (extras != null) {
            intent.putExtras(extras);
        }
        scope.startActivityAndWait(intent);
        scope.getDevice().wait(Until.hasObject(By.scrollable(true)), 5_000);
    }

    /**
     * Drive the scroll that forces the images behind the blur view(s) to change — which
     * is what makes each BlurView re-run performBlurSync every frame.
     * <p>
     * We can't use {@code UiObject2.fling}: in the combined scene the blur views overlay
     * the vertical center of the ScrollView, so a center-anchored fling lands on an
     * overlay (titlebar/button) that eats the gesture instead of scrolling. Instead we
     * inject explicit swipes whose touch-DOWN is in a strip that exposes the ScrollView
     * in every scene — the upper strip (below the top titlebar, above the centered block)
     * and the lower strip (below the centered block, above the nav bar). Android's touch
     * capture then routes the whole swipe to the ScrollView even as it crosses overlays.
     * WARM relaunches a fresh top-scrolled activity each iteration, so alternating up/down
     * keeps the content moving deterministically without bottoming out.
     */
    static void scroll(UiDevice device) {
        int x = device.getDisplayWidth() / 2;
        int h = device.getDisplayHeight();
        int lo = (int) (h * 0.22f);   // upper exposed strip (below top titlebar)
        int hi = (int) (h * 0.80f);   // lower exposed strip (below centered block)
        for (int i = 0; i < 3; i++) {
            device.swipe(x, hi, x, lo, 8);   // touch-down at hi -> scroll content up
            device.waitForIdle();
            device.swipe(x, lo, x, hi, 8);   // touch-down at lo -> scroll content down
            device.waitForIdle();
        }
    }
}

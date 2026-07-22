package com.qmdeve.benchmark;

import androidx.benchmark.macro.CompilationMode;
import androidx.benchmark.macro.StartupMode;
import androidx.benchmark.macro.junit4.MacrobenchmarkRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Frame-timing baseline for the "simple BlurView scene" ({@code BlurViewActivity}):
 * a SINGLE centered {@link com.qmdeve.blurview.widget.BlurView} over a vertical
 * {@code ScrollView} of images. Measures per-view blur cost.
 * <p>
 * The BlurView only re-runs {@code performBlurSync} when the pixels behind it change,
 * so the workload is a deterministic scroll of the images underneath. {@link
 * androidx.benchmark.macro.FrameTimingMetric} gives frameCount; per-section timings come
 * from TraceSectionMetric ({@link BlurBenchmarks#metrics()}); frame-duration percentiles
 * come from {@code benchmark/frame_stats.py} (FrameTimingMetric durations are empty on
 * this preview OS).
 */
@RunWith(AndroidJUnit4.class)
public class BlurViewScrollBenchmark {

    private static final String ACTIVITY = "BlurViewActivity";

    @Rule
    public MacrobenchmarkRule mBenchmarkRule = new MacrobenchmarkRule();

    @Test
    public void scrollBlur() {
        mBenchmarkRule.measureRepeated(
                BlurBenchmarks.PACKAGE,
                BlurBenchmarks.metrics(),
                new CompilationMode.Full(),
                StartupMode.WARM,
                // 5 iterations: enough for stable FrameTiming percentiles, fast A/B turnaround.
                5,
                setupScope -> {
                    BlurBenchmarks.launch(setupScope, ACTIVITY);
                    return null;
                },
                measureScope -> {
                    BlurBenchmarks.scroll(measureScope.getDevice());
                    return null;
                });
    }
}

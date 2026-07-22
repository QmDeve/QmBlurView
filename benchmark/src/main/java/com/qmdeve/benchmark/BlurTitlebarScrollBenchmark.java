package com.qmdeve.benchmark;

import androidx.benchmark.macro.CompilationMode;
import androidx.benchmark.macro.StartupMode;
import androidx.benchmark.macro.junit4.MacrobenchmarkRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Frame-timing benchmark for the complex, combined "Blur Titlebar" scene
 * ({@code BlurTitlebarActivity}): FIVE blur views at once — three
 * {@link com.qmdeve.blurview.widget.BlurTitlebarView} plus two
 * {@link com.qmdeve.blurview.widget.BlurButtonView} — over a vertical
 * {@code ScrollView} of images.
 * <p>
 * Every blur view re-runs {@code performBlurSync} synchronously on the main thread each
 * frame, so this scene exposes the sequential per-view stacking that {@link
 * BlurViewScrollBenchmark} (single view) cannot: expect ~5x the performBlurSync count and
 * a far worse frame P99 / jank%. Same metrics + scroll gesture as every scene
 * (see {@link BlurBenchmarks}) so the numbers stay comparable.
 */
@RunWith(AndroidJUnit4.class)
public class BlurTitlebarScrollBenchmark {

    private static final String ACTIVITY = "BlurTitlebarActivity";

    @Rule
    public MacrobenchmarkRule mBenchmarkRule = new MacrobenchmarkRule();

    @Test
    public void scrollBlur() {
        mBenchmarkRule.measureRepeated(
                BlurBenchmarks.PACKAGE,
                BlurBenchmarks.metrics(),
                new CompilationMode.Full(),
                StartupMode.WARM,
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

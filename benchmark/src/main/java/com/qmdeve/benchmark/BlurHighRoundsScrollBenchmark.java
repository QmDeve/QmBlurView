package com.qmdeve.benchmark;

import android.os.Bundle;

import androidx.benchmark.macro.CompilationMode;
import androidx.benchmark.macro.StartupMode;
import androidx.benchmark.macro.junit4.MacrobenchmarkRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Frame-timing benchmark for the "high-rounds" scene: the SAME single-view
 * {@code BlurViewActivity} as {@link BlurViewScrollBenchmark}, but driven at a HIGH
 * blur rounds count (8 instead of the default) via the {@code blurRounds} intent extra.
 * <p>
 * This scene isolates the rounds-scaling cost — 8 rounds over ONE blur view. Rounds are
 * now grouped by direction (H^n·V^n), so a blur pays exactly 2 dispatch+latch barriers
 * regardless of rounds; extra rounds cost only pixel work. The interleaved (H·V)^n scheme
 * instead paid 2×rounds barriers, so at 8 rounds it paid 16 barriers per blur. The
 * grouped optimization should therefore keep dispatch overhead ~flat vs the default-rounds
 * scenes here, where the old scheme scaled it linearly with rounds.
 * <p>
 * Same metrics + scroll gesture as every scene (see {@link BlurBenchmarks}) so the numbers
 * stay comparable.
 */
@RunWith(AndroidJUnit4.class)
public class BlurHighRoundsScrollBenchmark {

    private static final String ACTIVITY = "BlurViewActivity";
    private static final int BLUR_ROUNDS = 8;

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
                    Bundle extras = new Bundle();
                    extras.putInt("blurRounds", BLUR_ROUNDS);
                    BlurBenchmarks.launch(setupScope, ACTIVITY, extras);
                    return null;
                },
                measureScope -> {
                    BlurBenchmarks.scroll(measureScope.getDevice());
                    return null;
                });
    }
}

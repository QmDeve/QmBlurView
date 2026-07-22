#!/usr/bin/env python3
"""
Compare two macrobenchmark benchmarkData.json runs — A/B for a blur optimization.

Usage:
    python3 benchmark/bench_compare.py <baseline.json> <current.json>

Reads the official benchmark output (no trace parsing). For every benchmark and
metric present in both files it prints:  baseline median -> current median, the
% delta, and a verdict that is
  - direction-aware: for timing / jank metrics lower is better; *Count metrics are
    context only (they describe the workload, not its quality);
  - noise-aware: a change under NOISE_PCT, or one whose per-run [min,max] ranges
    still overlap, is flagged "~" rather than a win/regression.
Exit code is non-zero if any metric regressed beyond noise (handy for CI/gating).
"""
import json
import sys

NOISE_PCT = 3.0

# Print order: headline smoothness first, then the blur cost breakdown, counts last.
ORDER = [
    "gfxFrameJankPercent", "deadlineMissedFrameCount", "highInputLatencyFrameCount",
    "gfxFrameTime99thPercentileMs", "gfxFrameTime95thPercentileMs",
    "gfxFrameTime90thPercentileMs", "gfxFrameTime50thPercentileMs",
    "BlurView.performBlurSyncSumMs", "BlurView.blurSumMs",
    "BlurView.captureDecorViewSumMs", "BlurNative.passHSumMs",
    "BlurNative.passVSumMs", "BlurNative.copyInputSumMs",
]


def load(path):
    d = json.load(open(path))
    return {b["className"].split(".")[-1] + "." + b["name"]: b["metrics"]
            for b in d["benchmarks"]}


def is_context(name):
    return name.endswith("Count")


def verdict(base, cur, name):
    bmed, cmed = base.get("median"), cur.get("median")
    if bmed is None or cmed is None:
        return "?", "", 0.0
    pct = 100.0 * (cmed - bmed) / bmed if bmed else 0.0
    if is_context(name):
        return "·", "context", pct
    overlap = (base["minimum"] <= cur["maximum"] and cur["minimum"] <= base["maximum"])
    if abs(pct) < NOISE_PCT or overlap:
        return "~", "noise", pct
    # every non-count metric here is lower-is-better (jank%, frame ms, blur ms)
    return ("✓", "improved", pct) if cmed < bmed else ("✗", "regressed", pct)


def order_key(name):
    return (ORDER.index(name), name) if name in ORDER else (len(ORDER), name)


def main():
    if len(sys.argv) != 3:
        sys.exit("usage: bench_compare.py <baseline.json> <current.json>")
    base, cur = load(sys.argv[1]), load(sys.argv[2])
    regressed = False

    # Snapshots can hold different sets of scenes (e.g. an older baseline predates a
    # newly added benchmark). Compare only the shared scenes; note the rest and move on.
    for bench in sorted(cur.keys() - base.keys()):
        print(f"\n### {bench}\n  (test only in current, skipped — not in baseline)")
    for bench in sorted(base.keys() - cur.keys()):
        print(f"\n### {bench}\n  (test only in baseline, skipped — not in current)")

    for bench in sorted(base.keys() & cur.keys()):
        print(f"\n### {bench}")
        print(f"  {'metric':<32}{'baseline':>12}{'current':>12}{'Δ%':>9}  verdict")
        bm, cm = base[bench], cur[bench]
        for name in sorted(bm.keys() & cm.keys(), key=order_key):
            mark, label, pct = verdict(bm[name], cm[name], name)
            if label == "regressed":
                regressed = True
            print(f"  {name:<32}{bm[name]['median']:>12.2f}"
                  f"{cm[name]['median']:>12.2f}{pct:>+8.1f}%  {mark} {label}")
        only = (bm.keys() ^ cm.keys())
        if only:
            print(f"  (metrics in only one run: {', '.join(sorted(only))})")

    print("\nRegression detected." if regressed else "\nNo regressions.")
    sys.exit(1 if regressed else 0)


if __name__ == "__main__":
    main()

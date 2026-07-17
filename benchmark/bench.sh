#!/usr/bin/env bash
#
# One-command A/B loop for a blur optimization.
#
#   benchmark/bench.sh <label> [reference-label]
#
# Runs the macrobenchmark, snapshots benchmarkData.json to bench-results/<label>.json,
# then diffs it against bench-results/<reference-label>.json (default: "baseline").
#
#   benchmark/bench.sh baseline              # establish the baseline snapshot
#   benchmark/bench.sh 01-native-buffer      # run + compare vs baseline
#   benchmark/bench.sh 02-worker-prio 01-native-buffer   # compare vs a specific prior
#
set -euo pipefail
cd "$(dirname "$0")/.."

LABEL="${1:?usage: bench.sh <label> [reference-label]}"
REF="${2:-baseline}"
OUT="bench-results"
mkdir -p "$OUT"

./gradlew :benchmark:connectedBenchmarkAndroidTest

SRC=$(find benchmark/build/outputs -name 'com.qmdeve.benchmark-benchmarkData.json' | head -1)
[ -n "$SRC" ] || { echo "no benchmarkData.json produced"; exit 1; }
cp "$SRC" "$OUT/$LABEL.json"
echo "snapshot -> $OUT/$LABEL.json"

if [ "$LABEL" != "$REF" ] && [ -f "$OUT/$REF.json" ]; then
    echo "comparing $REF -> $LABEL:"
    python3 benchmark/bench_compare.py "$OUT/$REF.json" "$OUT/$LABEL.json"
else
    echo "(no comparison: reference '$OUT/$REF.json' not found or is self)"
fi

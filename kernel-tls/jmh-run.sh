#!/usr/bin/env bash
set -euo pipefail
MVN=${MVN:-mvn}
OUT=${OUT:-jmh-result.json}
TIMEOUT=${TIMEOUT:-30}
# Build shaded jar with all deps
$MVN -q -DskipTests package
# Run JMH directly from the shaded jar classpath with a fork, to ensure proper JSON emission
JAR="target/ktls-bench-1.0.0.jar"
JMH_OPTS=${JMH_OPTS:-"-wi 0 -i 1 -f 1 -bm SingleShotTime -tu s -rf json -rff ${OUT} com.example.ktlsbench.jmh.ThroughputBench.*"}

run_with_timeout() {
  local t=$1; shift
  if command -v timeout >/dev/null 2>&1; then
    timeout --foreground --kill-after=5s "${t}s" "$@" || {
      rc=$?
      if [ $rc -eq 124 ] || [ $rc -eq 137 ]; then
        echo "[jmh-run.sh] Timed out after ${t}s; killing benchmark." >&2
      fi
      return $rc
    }
  else
    ( "$@" &
      child=$!
      trap 'kill -TERM ${child} 2>/dev/null || true' TERM INT
      (
        sleep "$t"; echo "[jmh-run.sh] Timed out after ${t}s; killing benchmark." >&2; kill -TERM ${child} 2>/dev/null || true; sleep 5; kill -KILL ${child} 2>/dev/null || true
      ) &
      wait ${child}
    )
  fi
}

run_with_timeout "$TIMEOUT" java -cp "$JAR" org.openjdk.jmh.Main ${JMH_OPTS}

echo "JMH JSON written to ${OUT}"
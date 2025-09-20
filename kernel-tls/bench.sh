#!/usr/bin/env bash
set -euo pipefail
# One-command benchmark runner: builds and runs JMH, writes JSON results
# Usage: ./bench.sh
# Optional overrides: THREADS, SIZE, DURATION, HOST, JSSE_PORT, OPENSSL_PORT, OUT, TIMEOUT

MVN=${MVN:-mvn}
OUT=${OUT:-jmh-result.json}
HOST=${HOST:-127.0.0.1}
JSSE_PORT=${JSSE_PORT:-8443}
OPENSSL_PORT=${OPENSSL_PORT:-9443}
THREADS=${THREADS:-4}
SIZE=${SIZE:-16384}
DURATION=${DURATION:-10}
TIMEOUT=${TIMEOUT:-30}

# Build fat jar
$MVN -q -DskipTests package
JAR="target/ktls-bench-1.0.0.jar"

# Run JMH from the fat jar with a fork to ensure proper JSON emission
CMD=(
  java -cp "$JAR" org.openjdk.jmh.Main \
    -wi 0 -i 1 -f 1 -bm SingleShotTime -tu s \
    -p host=${HOST} -p jssePort=${JSSE_PORT} -p opensslPort=${OPENSSL_PORT} \
    -p threads=${THREADS} -p size=${SIZE} -p duration=${DURATION} \
    -rf json -rff "${OUT}" \
    com.example.ktlsbench.jmh.ThroughputBench.*
)

run_with_timeout() {
  local t=$1; shift
  if command -v timeout >/dev/null 2>&1; then
    timeout --foreground --kill-after=5s "${t}s" "$@" || {
      rc=$?
      if [ $rc -eq 124 ] || [ $rc -eq 137 ]; then
        echo "[bench.sh] Timed out after ${t}s; killing benchmark." >&2
      fi
      return $rc
    }
  else
    # Portable fallback watchdog
    ( "$@" &
      child=$!
      trap 'kill -TERM ${child} 2>/dev/null || true' TERM INT
      (
        sleep "$t"; echo "[bench.sh] Timed out after ${t}s; killing benchmark." >&2; kill -TERM ${child} 2>/dev/null || true; sleep 5; kill -KILL ${child} 2>/dev/null || true
      ) &
      wait ${child}
    )
  fi
}

run_with_timeout "$TIMEOUT" "${CMD[@]}"

echo "Results written to ${OUT}"
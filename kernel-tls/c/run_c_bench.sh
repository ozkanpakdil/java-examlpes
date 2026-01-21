#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

PORT=${PORT:-10443}
SIZE=${SIZE:-16384}
DURATION=${DURATION:-10}
CONCURRENCY=${CONCURRENCY:-64}
TLS13_YES_CIPHER=${TLS13_YES_CIPHER:-TLS_AES_128_GCM_SHA256}
TLS13_NO_CIPHER=${TLS13_NO_CIPHER:-TLS_CHACHA20_POLY1305_SHA256}

if ! command -v ab >/dev/null 2>&1; then
  echo "ApacheBench (ab) not found; install 'apache2-utils' (Debian/Ubuntu) or 'httpd-tools' (RHEL/Fedora)." >&2
  exit 1
fi

make -s

run_case() {
  local name="$1"; shift
  local protocol="$1"; shift   # TLS1.2 or TLS1.3
  local cipher="$1"; shift
  local ktls_enable_flag="${1:-0}"; shift || true
  local log="/tmp/ktls_https_server_${name}.log"
  local ab_out="/tmp/ktls_ab_${name}.txt"
  local env_out="/tmp/ktls_case_${name}.env"

  # Start server with chosen protocol/cipher
  if [ "$protocol" = "TLS1.2" ]; then
    PROTOCOL=TLS1.2 TLS12_CIPHER="$cipher" KTLS_ENABLE="$ktls_enable_flag" PORT=$PORT SIZE=$SIZE ./ktls_https_server >"$log" 2>&1 &
  else
    PROTOCOL=TLS1.3 TLS13_CIPHER="$cipher" KTLS_ENABLE="$ktls_enable_flag" PORT=$PORT SIZE=$SIZE ./ktls_https_server >"$log" 2>&1 &
  fi
  SERVER_PID=$!
  trap 'kill $SERVER_PID 2>/dev/null || true' EXIT

  # wait until port is open (timeout ~10s)
  READY=0
  for i in {1..100}; do
    if timeout 0.2 bash -lc "</dev/tcp/127.0.0.1/$PORT" 2>/dev/null; then
      READY=1
      break
    fi
    # if server died, stop early and show logs
    if ! kill -0 $SERVER_PID 2>/dev/null; then
      echo "[${name}] Server process exited early. Logs:"
      echo "---- $log ----"
      tail -n +1 "$log" || true
      exit 1
    fi
    sleep 0.1
  done

  if [ "$READY" -ne 1 ]; then
    echo "[${name}] Timed out waiting for server to open 127.0.0.1:$PORT"
    echo "---- $log ----"
    tail -n +1 "$log" || true
    exit 1
  fi

  local URL="https://127.0.0.1:${PORT}/"
  echo "[${name}] Running ab for ${DURATION}s against ${URL} (c=${CONCURRENCY}, size=${SIZE}, ${protocol} cipher=${cipher})"
  # Use time-based run, disable keep-alive to maximize new connections; request closes each time
  # Force TLS protocol accordingly with -f (not -Z, which is for cipher list)
  ab -s 10 -t ${DURATION} -c ${CONCURRENCY} -f ${protocol} -q -r -S \
     -H 'Host: localhost' -H 'Connection: close' -H 'Accept: */*' \
     ${URL} 2>&1 | tee "$ab_out" || true

  # Collect metrics
  local REQS=$(grep -E "Requests per second" -m1 "$ab_out" | awk '{print $(NF-1)}')
  local TRANS_VAL=$(grep -E "Transfer rate" -m1 "$ab_out" | awk '{print $(NF-1)}')
  local TRANS_UNIT=$(grep -E "Transfer rate" -m1 "$ab_out" | awk '{print $NF}')
  local KTLS_YES=$(grep -c "kTLS active=yes" "$log" || true)
  local KTLS_NO=$(grep -c "kTLS active=no" "$log" || true)
  local CONNS=$(grep -c "TLS handshake complete" "$log" || true)

  # Stop server for this case
  kill $SERVER_PID 2>/dev/null || true
  wait $SERVER_PID 2>/dev/null || true

  echo "\n[${name}] Summary:"
  echo "  Duration:    ${DURATION}s"
  echo "  Requests/s:  ${REQS:-unknown}"
  echo "  Transfer:    ${TRANS_VAL:-unknown} ${TRANS_UNIT:-}"
  echo "  Connections: ${CONNS:-0} (kTLS yes=${KTLS_YES:-0}, no=${KTLS_NO:-0})"
  echo "  Log:         $log"

  # Persist metrics for result.md generation
  cat > "$env_out" <<EOF
NAME_${name}=${name}
CIPHER_${name}=${cipher}
REQS_${name}=${REQS:-0}
TRANS_VAL_${name}=${TRANS_VAL:-0}
TRANS_UNIT_${name}=${TRANS_UNIT:-unknown}
CONNS_${name}=${CONNS:-0}
KTLS_YES_${name}=${KTLS_YES:-0}
KTLS_NO_${name}=${KTLS_NO:-0}
LOG_${name}=${log}
AB_OUT_${name}=${ab_out}
EOF
}

# ktls_on: prefer older path more broadly supported by kTLS (TLS1.2 + AES-GCM)
run_case ktls_on  "TLS1.2" "ECDHE-RSA-AES128-GCM-SHA256" 1
# ktls_off: use TLS1.3 + CHACHA (not offloaded by kTLS)
run_case ktls_off "TLS1.3" "TLS_CHACHA20_POLY1305_SHA256" 0

# Generate result.md at repo root
ON_ENV="/tmp/ktls_case_ktls_on.env"
OFF_ENV="/tmp/ktls_case_ktls_off.env"
[ -f "$ON_ENV" ] && [ -f "$OFF_ENV" ] || { echo "Missing metrics files. Skipping result.md"; exit 0; }
# shellcheck disable=SC1090
source "$ON_ENV"
# shellcheck disable=SC1090
source "$OFF_ENV"

# Determine numeric transfer in KB/s regardless of unit
normalize_kb() {
  local val="$1" unit="$2"
  case "$unit" in
    Kbytes/sec) awk -v v="$val" 'BEGIN{printf "%.2f", v}' ;;
    Mbytes/sec) awk -v v="$val" 'BEGIN{printf "%.2f", v*1024}' ;;
    Gbytes/sec) awk -v v="$val" 'BEGIN{printf "%.2f", v*1024*1024}' ;;
    *) awk -v v="$val" 'BEGIN{printf "%.2f", v}' ;;
  esac
}

KB_ON=$(normalize_kb "$TRANS_VAL_ktls_on" "$TRANS_UNIT_ktls_on")
KB_OFF=$(normalize_kb "$TRANS_VAL_ktls_off" "$TRANS_UNIT_ktls_off")

# Compare Requests/s
REQS_ON=${REQS_ktls_on:-0}
REQS_OFF=${REQS_ktls_off:-0}
CMP=$(awk -v a="$REQS_ON" -v b="$REQS_OFF" 'BEGIN{if (a>b) print "on"; else if (a<b) print "off"; else print "equal"}')
FASTER_CASE="ktls_on"
SLOWER_CASE="ktls_off"
PCT="0"
if [ "$CMP" = "on" ]; then
  if awk -v a="$REQS_OFF" 'BEGIN{exit (a==0)}'; then
    PCT=$(awk -v a="$REQS_ON" -v b="$REQS_OFF" 'BEGIN{printf "%.2f", (a-b)*100.0/b}')
  else
    PCT="inf"
  fi
elif [ "$CMP" = "off" ]; then
  FASTER_CASE="ktls_off"
  SLOWER_CASE="ktls_on"
  if awk -v a="$REQS_ON" 'BEGIN{exit (a==0)}'; then
    PCT=$(awk -v a="$REQS_OFF" -v b="$REQS_ON" 'BEGIN{printf "%.2f", (a-b)*100.0/b}')
  else
    PCT="inf"
  fi
else
  FASTER_CASE="equal"
  SLOWER_CASE="equal"
  PCT="0"
fi

# kTLS activation notes
NOTE_KTLS=""
if [ "${KTLS_YES_ktls_on:-0}" -eq 0 ] && [ "${KTLS_YES_ktls_off:-0}" -eq 0 ]; then
  NOTE_KTLS="Note: No connections reported kTLS active. Your OpenSSL build or kernel may not support kTLS for these ciphers."
fi

# Compute global kTLS activation flag across both scenarios
TOTAL_KTLS_YES=$(( ${KTLS_YES_ktls_on:-0} + ${KTLS_YES_ktls_off:-0} ))
if [ "$TOTAL_KTLS_YES" -gt 0 ]; then
  ANY_KTLS_ACTIVE=yes
else
  ANY_KTLS_ACTIVE=no
fi

# Write result.md to repo root
OUT_MD="../result.md"
NOW_TS=$(date +"%Y-%m-%d %H:%M:%S %Z")
cat > "$OUT_MD" <<MD
# kTLS C Server Benchmark Results

Generated: $NOW_TS

Test parameters:
- Duration: ${DURATION}s
- Concurrency: ${CONCURRENCY}
- Response size: ${SIZE} bytes
- Port: ${PORT}
- Is kTLS active on any connection: ${ANY_KTLS_ACTIVE}

## Scenario A: ktls_on (cipher: ${CIPHER_ktls_on})
- Requests/s: ${REQS_ktls_on}
- Transfer rate: ${TRANS_VAL_ktls_on} ${TRANS_UNIT_ktls_on} (${KB_ON} Kbytes/sec)
- TLS connections: ${CONNS_ktls_on}
- kTLS active: yes=${KTLS_YES_ktls_on}, no=${KTLS_NO_ktls_on}
- Logs: ${LOG_ktls_on}

## Scenario B: ktls_off (cipher: ${CIPHER_ktls_off})
- Requests/s: ${REQS_ktls_off}
- Transfer rate: ${TRANS_VAL_ktls_off} ${TRANS_UNIT_ktls_off} (${KB_OFF} Kbytes/sec)
- TLS connections: ${CONNS_ktls_off}
- kTLS active: yes=${KTLS_YES_ktls_off}, no=${KTLS_NO_ktls_off}
- Logs: ${LOG_ktls_off}

## Comparison
- Faster scenario (by Requests/s): ${FASTER_CASE}
- Speed difference: ${PCT}% relative to the slower case

Interpretation:
- We compare throughput by Requests/s as primary metric. The ktls_on case uses a TLS 1.3 AES-GCM cipher that is typically eligible for kTLS offload. The ktls_off case uses a TLS 1.3 cipher that is typically not offloaded.
- ${NOTE_KTLS}

Reproduce: run ./c/run_c_bench.sh again. This file is regenerated each run.
MD

echo "Wrote benchmark summary to $OUT_MD"
if [ "$ANY_KTLS_ACTIVE" = "yes" ]; then
  echo "Conclusion: kTLS WAS ACTIVE on at least one connection."
else
  echo "Conclusion: kTLS was NOT active on any connection."
fi

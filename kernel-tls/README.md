# kTLS vs JSSE benchmark (JMH) + C kTLS HTTPS server

Compare regular Java TLS (JSSE) and native/OpenSSL TLS (kTLS-capable via Netty/tcnative) and get JSON results. Additionally, includes a minimal C HTTPS server that can leverage Linux Kernel TLS (kTLS) via OpenSSL when available, so you can load test it with curl/ab and see socket speed.

Requirements:
- JDK 17+
- Maven 3.8+
- Linux recommended for kTLS
- For C server: gcc/clang, OpenSSL development libraries (OpenSSL 1.1.1+ or 3.x). To get kTLS offload, OpenSSL must be built with enable-ktls and your kernel must support it (TLS1.3 TX on >=5.11 typically).
- For load tools: apache2-utils (ab) or curl

Run Java JMH benchmark:
```
chmod +x jmh-run.sh bench.sh || true
./jmh-run.sh
```
This builds the project and runs JMH. Output JSON: jmh-result.json. Both jmh-run.sh and bench.sh auto-timeout after 30s by default; override with TIMEOUT env.

Tune parameters via JMH `-p` flags using JMH_OPTS env, e.g.:
```
JMH_OPTS='-p threads=8 -p size=32768 -p duration=15 -rf json -rff result.json com.example.ktlsbench.jmh.ThroughputBench.*' ./jmh-run.sh
```
Available params (defaults):
- host=127.0.0.1
- jssePort=8443
- opensslPort=9443
- threads=4
- size=16384
- duration=10

Notes for Java path:
- Both benchmarks use TLS 1.3 and length-prefixed echo.
- The OpenSSL/Netty path may leverage kTLS on supported kernels and tcnative builds (see console output for kernel support hints).

C kTLS-capable HTTPS server (ab/curl testing):
- Location: c/
- Build: `make -C c`
- Run: `PORT=10443 SIZE=16384 ./c/ktls_https_server`
  - On first run it generates self-signed cert.pem/key.pem in c/.
  - It logs for each connection whether OpenSSL reports kTLS active (best-effort).
- Quick load test with ApacheBench (10s duration each case):
```
chmod +x c/run_c_bench.sh
./c/run_c_bench.sh
# env knobs: PORT, SIZE, DURATION, CONCURRENCY, TLS13_YES_CIPHER, TLS13_NO_CIPHER, PROTOCOL, KTLS_ENABLE
```
  - The script runs two timed (DURATION, default 10s) tests:
    1) kTLS-friendly case: TLS 1.2 + AES-GCM (ECDHE-RSA-AES128-GCM-SHA256), which is broadly eligible for kTLS TX on many kernels. We also set KTLS_ENABLE=1 when supported by your OpenSSL.
    2) kTLS-unfriendly case: TLS 1.3 + CHACHA20 (TLS_CHACHA20_POLY1305_SHA256), which is not offloaded by kTLS.
  - For each case it disables HTTP keep-alive to maximize new connections, then summarizes:
    - Requests/s and Transfer rate (from ab)
    - Total TLS connections and how many had kTLS active yes/no (from server logs)
  - After both runs, a result.md is generated at the repo root comparing the two cases and indicating which is faster and by how much.
- Manual with curl:
```
./c/ktls_https_server &
curl -k https://127.0.0.1:10443/ -o /dev/null -w 'time_total=%{time_total}\n'
```
- Interpreting output: The script’s summaries compare probable kTLS vs non-kTLS by choosing cipher suites. If OpenSSL/Kernel do not support kTLS, you’ll see kTLS active=no for all connections. You can override ciphers via TLS13_YES_CIPHER/TLS13_NO_CIPHER or force TLS 1.2 using TLS12_CIPHER env and ab’s -Z/-f flags if needed.

FAQ: Why do I see kTLS active=yes=0 in both scenarios?
- Short answer: kTLS was not active. The result.md also shows: "Is kTLS active on any connection: no" in that case.
- Common reasons:
  - OpenSSL library not compiled with enable-ktls (server will print at startup: "OpenSSL kTLS support: not compiled in").
  - Linux kernel lacks kTLS support for the chosen protocol/cipher (TLS 1.3 TX typically requires >= 5.11; RX support varies).
  - The selected cipher is not offloadable by kTLS (AES-GCM is usually required; CHACHA20 is not offloaded).
- Quick checks:
  - Run ./c/ktls_https_server and look at its startup banner for OpenSSL version and kTLS support.
  - Ensure the ktls_on case uses TLS_AES_128_GCM_SHA256 (default) and that your kernel supports TLS 1.3 kTLS TX.

License: for benchmarking/experimental use only.

# kTLS vs JSSE benchmark (JMH)

Compare regular Java TLS (JSSE) and native/OpenSSL TLS (kTLS-capable via Netty/tcnative) and get JSON results.

Requirements:
- JDK 17+
- Maven 3.8+
- Linux recommended for kTLS

Run:
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

Notes:
- Both benchmarks use TLS 1.3 and length-prefixed echo.
- OpenSSL path may leverage kTLS on supported kernels and tcnative builds.

License: for benchmarking/experimental use only.

# kTLS C Server Benchmark Results

Generated: 2025-09-22 01:06:09 BST

Test parameters:
- Duration: 10s
- Concurrency: 64
- Response size: 16384 bytes
- Port: 10443
- Is kTLS active on any connection: no

## Scenario A: ktls_on (cipher: ECDHE-RSA-AES128-GCM-SHA256)
- Requests/s: [#/sec]
- Transfer rate: [Kbytes/sec] received (0.00 Kbytes/sec)
- TLS connections: 203
- kTLS active: yes=0, no=202
- Logs: /tmp/ktls_https_server_ktls_on.log

## Scenario B: ktls_off (cipher: TLS_CHACHA20_POLY1305_SHA256)
- Requests/s: [#/sec]
- Transfer rate: [Kbytes/sec] received (0.00 Kbytes/sec)
- TLS connections: 9487
- kTLS active: yes=0, no=9486
- Logs: /tmp/ktls_https_server_ktls_off.log

## Comparison
- Faster scenario (by Requests/s): equal
- Speed difference: 0% relative to the slower case

Interpretation:
- We compare throughput by Requests/s as primary metric. The ktls_on case uses a TLS 1.3 AES-GCM cipher that is typically eligible for kTLS offload. The ktls_off case uses a TLS 1.3 cipher that is typically not offloaded.
- Note: No connections reported kTLS active. Your OpenSSL build or kernel may not support kTLS for these ciphers.

Reproduce: run ./c/run_c_bench.sh again. This file is regenerated each run.

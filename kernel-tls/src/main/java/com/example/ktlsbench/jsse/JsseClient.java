package com.example.ktlsbench.jsse;

import com.example.ktlsbench.util.TlsUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class JsseClient {
    public static Result runClientResult(String host, int port, int threads, int messageSize, int durationSeconds, boolean tls13) throws Exception {
        SSLContext ctx = TlsUtil.createJsseClientContext(tls13);
        SSLSocketFactory factory = ctx.getSocketFactory();

        byte[] payload = new byte[messageSize];
        for (int i = 0; i < payload.length; i++) payload[i] = (byte) (i * 31);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicLong messages = new AtomicLong();
        AtomicLong bytes = new AtomicLong();
        List<Future<?>> futures = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        long endAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(durationSeconds);
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                try (SSLSocket s = (SSLSocket) factory.createSocket(InetAddress.getByName(host), port)) {
                    s.setEnabledProtocols(new String[]{tls13 ? "TLSv1.3" : "TLSv1.2"});
                    s.startHandshake();
                    try (DataInputStream in = new DataInputStream(s.getInputStream());
                         DataOutputStream out = new DataOutputStream(s.getOutputStream())) {
                        startLatch.await();
                        while (System.nanoTime() < endAt) {
                            out.writeInt(payload.length);
                            out.write(payload);
                            out.flush();
                            int len = in.readInt();
                            if (len != payload.length) throw new IllegalStateException("Echo len mismatch");
                            in.readFully(payload, 0, len);
                            messages.incrementAndGet();
                            bytes.addAndGet(4L + len + 4L + len);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        long start = System.nanoTime();
        startLatch.countDown();
        for (Future<?> f : futures) f.get();
        long end = System.nanoTime();
        pool.shutdownNow();

        double seconds = (end - start) / 1_000_000_000.0;
        long msgs = messages.get();
        long totalBytes = bytes.get();
        double mbps = (totalBytes * 8.0) / (seconds * 1_000_000.0);
        return new Result(msgs, totalBytes, seconds, mbps);
    }

    public static void runClient(String host, int port, int threads, int messageSize, int durationSeconds, boolean tls13) throws Exception {
        Result r = runClientResult(host, port, threads, messageSize, durationSeconds, tls13);
        System.out.printf("JSSE client complete: msgs=%d, seconds=%.3f, throughput=%.2f Mbit/s, msg/s=%.0f%n",
                r.messages, r.seconds, r.mbps, r.messages / r.seconds);
    }

    public record Result(long messages, long bytesTotal, double seconds, double mbps) {
    }
}

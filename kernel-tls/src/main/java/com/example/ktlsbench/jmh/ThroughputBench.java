package com.example.ktlsbench.jmh;

import com.example.ktlsbench.jsse.JsseClient;
import com.example.ktlsbench.netty.NettyClient;
import com.example.ktlsbench.util.WaitForPort;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1)
public class ThroughputBench {

    public static ServerProcess startServer(String mode, String host, int port, int threads) throws IOException {
        String javaBin = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "java";
        // Prefer shaded jar to avoid classpath issues when launched from Maven/JMH
        String jar = System.getProperty("user.dir") + java.io.File.separator + "target" + java.io.File.separator + "ktls-bench-1.0.0.jar";
        ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "-jar", jar,
                "--role=server",
                "--mode=" + mode,
                "--host=" + host,
                "--port=" + port,
                "--threads=" + threads,
                "--tls13"
        );
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        return new ServerProcess(pb.start());
    }

    private static boolean isPortFree(String host, int port) {
        try (java.net.ServerSocket ss = new java.net.ServerSocket()) {
            ss.setReuseAddress(true);
            ss.bind(new java.net.InetSocketAddress(host, port));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Benchmark
    public void jsse_throughput(Params p, JsseState s, Metrics m) throws Exception {
        JsseClient.Result r = JsseClient.runClientResult(p.host, p.jssePort, p.threads, p.size, p.duration, true);
        m.msgs = r.messages();
        m.mbps = r.mbps();
    }

    @Benchmark
    public void openssl_throughput(Params p, OpenSslState s, Metrics m) throws Exception {
        NettyClient.Result r = NettyClient.runClientResult(p.host, p.opensslPort, p.threads, p.size, p.duration, true);
        m.msgs = r.messages();
        m.mbps = r.mbps();
    }

    public static class ServerProcess implements AutoCloseable {
        private final Process proc;

        public ServerProcess(Process proc) {
            this.proc = proc;
        }

        @Override
        public void close() {
            if (proc != null) proc.destroy();
            try {
                if (proc != null) proc.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
            if (proc != null && proc.isAlive()) proc.destroyForcibly();
        }
    }

    @AuxCounters(AuxCounters.Type.EVENTS)
    @State(Scope.Thread)
    public static class Metrics {
        public long msgs;
        public double mbps;
    }

    @State(Scope.Benchmark)
    public static class Params {
        @Param({"127.0.0.1"})
        public String host;
        @Param({"8443"})
        public int jssePort;
        @Param({"9443"})
        public int opensslPort;
        @Param({"4"})
        public int threads;
        @Param({"16384"})
        public int size;
        @Param({"10"})
        public int duration;
        public boolean tls13 = true;
    }

    @State(Scope.Benchmark)
    public static class JsseState {
        ServerProcess server;

        @Setup(Level.Trial)
        public void setup(Params p) throws Exception {
            if (!isPortFree(p.host, p.jssePort)) {
                throw new IllegalStateException("Port already in use: " + p.host + ":" + p.jssePort + ". Free it or change -p jssePort=");
            }
            server = startServer("jsse", p.host, p.jssePort, p.threads);
            // Wait until port is open (20s timeout)
            if (!WaitForPort.waitFor(p.host, p.jssePort, 20)) {
                throw new IllegalStateException("Timed out waiting for server on " + p.host + ":" + p.jssePort);
            }
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (server != null) server.close();
        }
    }

    @State(Scope.Benchmark)
    public static class OpenSslState {
        ServerProcess server;

        @Setup(Level.Trial)
        public void setup(Params p) throws Exception {
            server = startServer("openssl", p.host, p.opensslPort, p.threads);
            WaitForPort.main(new String[]{p.host, Integer.toString(p.opensslPort), "20"});
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (server != null) server.close();
        }
    }
}

package com.example.ktlsbench;

import com.example.ktlsbench.jsse.JsseClient;
import com.example.ktlsbench.jsse.JsseServer;
import com.example.ktlsbench.netty.NettyClient;
import com.example.ktlsbench.netty.NettyServer;
import picocli.CommandLine;

import java.util.Locale;
import java.util.concurrent.Callable;

public class BenchmarkRunner implements Callable<Integer> {
    @CommandLine.Option(names = {"-r", "--role"}, required = true, description = "server or client")
    String role;

    @CommandLine.Option(names = {"-m", "--mode"}, required = true, description = "jsse or openssl")
    String mode;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "127.0.0.1", description = "Host to bind/connect")
    String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "8443", description = "Port to bind/connect")
    int port;

    @CommandLine.Option(names = {"-t", "--threads"}, defaultValue = "1", description = "Parallel client connections / server workers")
    int threads;

    @CommandLine.Option(names = {"-s", "--size"}, defaultValue = "1024", description = "Message size (bytes)")
    int messageSize;

    @CommandLine.Option(names = {"-d", "--duration"}, defaultValue = "10", description = "Benchmark duration (seconds)")
    int durationSeconds;

    @CommandLine.Option(names = {"--tls13"}, description = "Use TLSv1.3 if available")
    boolean tls13 = false;

    public static void main(String[] args) {
        System.out.println("BenchmarkRunner ARGS: " + java.util.Arrays.toString(args));
        int exit = new CommandLine(new BenchmarkRunner()).execute(args);
        System.out.flush();
        System.err.flush();
        System.exit(exit);
    }

    @Override
    public Integer call() throws Exception {
        String m = mode.toLowerCase(Locale.ROOT);
        String r = role.toLowerCase(Locale.ROOT);
        System.out.printf("Mode=%s, Role=%s, Host=%s, Port=%d, Threads=%d, Size=%d, Duration=%ds, TLS1.3=%s%n",
                m, r, host, port, threads, messageSize, durationSeconds, tls13);
        switch (m) {
            case "jsse":
                if (r.equals("server")) {
                    try {
                        JsseServer.runServer(host, port, threads, tls13);
                    } catch (java.net.BindException be) {
                        System.err.printf("ERROR: Address already in use for %s:%d (JSSE server). Please free the port or choose another.\n", host, port);
                        return 3;
                    }
                } else if (r.equals("client")) {
                    JsseClient.runClient(host, port, threads, messageSize, durationSeconds, tls13);
                } else {
                    System.err.println("Unknown role: " + role);
                    return 2;
                }
                break;
            case "openssl":
                if (r.equals("server")) {
                    try {
                        NettyServer.runServer(host, port, threads, tls13);
                    } catch (java.net.BindException be) {
                        System.err.printf("ERROR: Address already in use for %s:%d (OpenSSL/Netty server). Please free the port or choose another.\n", host, port);
                        return 3;
                    }
                } else if (r.equals("client")) {
                    NettyClient.runClient(host, port, threads, messageSize, durationSeconds, tls13);
                } else {
                    System.err.println("Unknown role: " + role);
                    return 2;
                }
                break;
            default:
                System.err.println("Unknown mode: " + mode);
                return 2;
        }
        return 0;
    }
}

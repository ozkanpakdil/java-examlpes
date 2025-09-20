package com.example.ktlsbench.util;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Simple helper to wait for a TCP port to accept connections.
 * Usage: WaitForPort <host> <port> <timeoutSeconds>
 * Exits 0 when the port becomes available within timeout; 1 otherwise.
 */
public final class WaitForPort {
    private WaitForPort() {}

    /**
     * Library method: wait for host:port to accept connections within timeout.
     * Returns true if available, false if timed out.
     */
    public static boolean waitFor(String host, int port, int timeoutSeconds) throws InterruptedException {
        final long deadline = System.nanoTime() + timeoutSeconds * 1_000_000_000L;
        while (System.nanoTime() < deadline) {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(host, port), 500);
                return true;
            } catch (Exception ignored) {
                Thread.sleep(200);
            }
        }
        return false;
    }

    // CLI entrypoint that preserves previous behavior
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: WaitForPort <host> <port> <timeoutSeconds>");
            System.exit(1);
        }
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final int timeoutSeconds = Integer.parseInt(args[2]);
        boolean ok = waitFor(host, port, timeoutSeconds);
        System.exit(ok ? 0 : 1);
    }
}

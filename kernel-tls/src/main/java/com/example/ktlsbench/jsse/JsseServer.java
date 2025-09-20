package com.example.ktlsbench.jsse;

import com.example.ktlsbench.util.TlsUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsseServer {
    public static void runServer(String host, int port, int workers, boolean tls13) throws Exception {
        SSLContext ctx = TlsUtil.createJsseServerContext(tls13);
        SSLServerSocketFactory factory = ctx.getServerSocketFactory();

        try (ServerSocket server = factory.createServerSocket(port, 1024, InetAddress.getByName(host))) {
            if (server instanceof SSLServerSocket s) {
                s.setEnabledProtocols(new String[]{tls13 ? "TLSv1.3" : "TLSv1.2"});
                s.setReuseAddress(true);
            }
            System.out.printf("JSSE TLS server listening on %s:%d (workers=%d)\n", host, port, workers);
            ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, workers));
            while (true) {
                Socket accepted = server.accept();
                pool.execute(() -> handle(accepted));
            }
        }
    }

    private static void handle(Socket socket) {
        try (Socket s = socket;
             DataInputStream in = new DataInputStream(s.getInputStream());
             DataOutputStream out = new DataOutputStream(s.getOutputStream())) {
            byte[] buf = null;
            while (true) {
                int len = in.readInt();
                if (len <= 0 || len > (1 << 24)) break;
                if (buf == null || buf.length < len) buf = new byte[len];
                in.readFully(buf, 0, len);
                out.writeInt(len);
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (Exception ignored) {
        }
    }
}

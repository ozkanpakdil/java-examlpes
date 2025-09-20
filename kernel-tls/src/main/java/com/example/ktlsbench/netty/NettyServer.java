package com.example.ktlsbench.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;

public class NettyServer {
    public static void runServer(String host, int port, int workers, boolean tls13) throws Exception {
        SelfSignedCertificate ssc = new SelfSignedCertificate("localhost");
        SslContext sslCtx = buildServerContext(ssc, tls13);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Math.max(1, workers));
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(sslCtx.newHandler(ch.alloc()));
                            p.addLast(new LengthFieldBasedFrameDecoder(16 * 1024 * 1024, 0, 4, 0, 4));
                            p.addLast(new LengthFieldPrepender(4));
                            p.addLast(new SimpleChannelInboundHandler<io.netty.buffer.ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, io.netty.buffer.ByteBuf msg) {
                                    ctx.writeAndFlush(msg.retain());
                                }
                            });
                        }
                    });
            Channel ch = b.bind(new InetSocketAddress(host, port)).sync().channel();
            printNativeInfo(sslCtx, tls13);
            System.out.printf("OpenSSL/Netty TLS server listening on %s:%d (workers=%d)\n", host, port, workers);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static SslContext buildServerContext(SelfSignedCertificate ssc, boolean tls13) throws SSLException {
        SslContextBuilder b = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                .sslProvider(SslProvider.OPENSSL)
                .protocols(tls13 ? new String[]{"TLSv1.3"} : new String[]{"TLSv1.2"});
        if (tls13) {
            b.ciphers(java.util.List.of("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384"));
        }
        return b.build();
    }

    private static void printNativeInfo(SslContext sslCtx, boolean tls13) {
        try {
            Class<?> openSsl = Class.forName("io.netty.handler.ssl.OpenSsl");
            boolean avail = (boolean) openSsl.getMethod("isAvailable").invoke(null);
            boolean tls13sup = false;
            try {
                tls13sup = (boolean) openSsl.getMethod("isTlsv13Supported").invoke(null);
            } catch (Throwable ignore) {
            }
            System.out.printf("Netty OpenSSL available=%s, tls13Supported=%s, ctxClass=%s%n", avail, tls13sup, sslCtx.getClass().getName());
            try {
                Class<?> sslInternal = Class.forName("io.netty.internal.tcnative.SSL");
                boolean ktlsTX = false, ktlsRX = false;
                try {
                    ktlsTX = (boolean) sslInternal.getMethod("kernelSupportsTls13Ktx").invoke(null);
                } catch (Throwable ignore) {
                }
                try {
                    ktlsRX = (boolean) sslInternal.getMethod("kernelSupportsTls13Krx").invoke(null);
                } catch (Throwable ignore) {
                }
                System.out.printf("kTLS kernel support (TX/RX) = %s/%s (TLS1.3 required=%s)%n", ktlsTX, ktlsRX, tls13);
            } catch (Throwable t) {
                System.out.println("kTLS support check not available in this tcnative build.");
            }
        } catch (Throwable t) {
            System.out.println("OpenSSL native info unavailable: " + t);
        }
    }
}

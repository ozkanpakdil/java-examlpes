package com.example.ktlsbench.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class NettyClient {
    public static Result runClientResult(String host, int port, int threads, int messageSize, int durationSeconds, boolean tls13) throws Exception {
        SslContext sslCtx = buildClientContext(tls13);
        byte[] payloadArray = new byte[messageSize];
        for (int i = 0; i < payloadArray.length; i++) payloadArray[i] = (byte) (i * 31);
        ByteBuf payload = Unpooled.wrappedBuffer(payloadArray).asReadOnly();

        AtomicLong messages = new AtomicLong();
        AtomicLong bytes = new AtomicLong();
        List<Future<?>> futures = new ArrayList<>();
        long endAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(durationSeconds);

        EventLoopGroup group = new NioEventLoopGroup(Math.max(1, threads));
        try {
            for (int i = 0; i < threads; i++) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        Bootstrap b = new Bootstrap();
                        b.group(group)
                                .channel(NioSocketChannel.class)
                                .option(ChannelOption.TCP_NODELAY, true)
                                .handler(new ChannelInitializer<Channel>() {
                                    @Override
                                    protected void initChannel(Channel ch) {
                                        ChannelPipeline p = ch.pipeline();
                                        p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                                        p.addLast(new LengthFieldBasedFrameDecoder(16 * 1024 * 1024, 0, 4, 0, 4));
                                        p.addLast(new LengthFieldPrepender(4));
                                        p.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                            @Override
                                            public void channelActive(ChannelHandlerContext ctx) {
                                                sendOnce(ctx);
                                            }

                                            private void sendOnce(ChannelHandlerContext ctx) {
                                                if (System.nanoTime() >= endAt) {
                                                    ctx.close();
                                                    return;
                                                }
                                                ctx.writeAndFlush(payload.retainedDuplicate());
                                            }

                                            @Override
                                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                                messages.incrementAndGet();
                                                bytes.addAndGet(4L + msg.readableBytes() + 4L + msg.readableBytes());
                                                sendOnce(ctx);
                                            }
                                        });
                                    }
                                });
                        Channel ch = b.connect(new InetSocketAddress(host, port)).sync().channel();
                        ch.closeFuture().sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }

            long start = System.nanoTime();
            for (Future<?> f : futures) f.get();
            long end = System.nanoTime();

            double seconds = (end - start) / 1_000_000_000.0;
            long msgs = messages.get();
            long totalBytes = bytes.get();
            double mbps = (totalBytes * 8.0) / (seconds * 1_000_000.0);
            return new Result(msgs, totalBytes, seconds, mbps);
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void runClient(String host, int port, int threads, int messageSize, int durationSeconds, boolean tls13) throws Exception {
        Result r = runClientResult(host, port, threads, messageSize, durationSeconds, tls13);
        System.out.printf("OpenSSL/Netty client complete: msgs=%d, seconds=%.3f, throughput=%.2f Mbit/s, msg/s=%.0f%n",
                r.messages, r.seconds, r.mbps, r.messages / r.seconds);
    }

    private static SslContext buildClientContext(boolean tls13) throws SSLException {
        SslContextBuilder b = SslContextBuilder.forClient()
                .sslProvider(SslProvider.OPENSSL)
                .trustManager(io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE)
                .protocols(tls13 ? new String[]{"TLSv1.3"} : new String[]{"TLSv1.2"});
        if (tls13) {
            b.ciphers(java.util.List.of("TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384"));
        }
        return b.build();
    }

    public record Result(long messages, long bytesTotal, double seconds, double mbps) {
    }
}

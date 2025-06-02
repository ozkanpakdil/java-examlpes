package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;

public class MainVerticle extends VerticleBase {

  @Override
  public Future<?> start() {
    return vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8080).onSuccess(http -> {
      System.out.println("HTTP server started on port 8080");
    });
  }

  public static void main(String[] args) {
    // System.setProperty("java.net.preferIPv4Stack", "true");
    // System.setProperty("java.net.preferIPv6Addresses", "false");
    // System.setProperty("vertx.disableDnsResolver", "true");
    System.setProperty("io.netty.allocator.type", "unpooled");
    System.setProperty("io.netty.noUnsafe", "true");

    Vertx.vertx().deployVerticle(new MainVerticle()).onFailure(Throwable::printStackTrace);
  }
}

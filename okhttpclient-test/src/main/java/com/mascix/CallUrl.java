
package com.mascix;

import java.io.IOException;
import java.util.UUID;

import lombok.extern.java.Log;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Log
public class CallUrl {

  public String changeUserAgent(HttpUrl httpUrl) throws IOException {
    Request request = new Request.Builder().url(httpUrl).header("User-Agent", "TEST-" + UUID.randomUUID()).build();

    return doTheCall(null, request);
  }

  private String doTheCall(OkHttpClient client, Request request) throws IOException {
    log.info("url:" + request.url());
    if (client == null)
      client = new OkHttpClient.Builder().build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }

  public String removeUserAgent(HttpUrl httpUrl) throws IOException {
    OkHttpClient client = new OkHttpClient.Builder()
        .addNetworkInterceptor(chain -> chain.proceed(chain.request().newBuilder().removeHeader("user-agent").build()))
        .build();
    Request request = new Request.Builder().url(httpUrl).build();
    return doTheCall(client, request);
  }

}

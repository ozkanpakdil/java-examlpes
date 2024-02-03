package io.github.ozkanpakdil;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;

public class Main2 {
public static void main(String[] args) throws IOException {
    final ClassicHttpRequest httpGet = ClassicRequestBuilder.get("https://httpbin.org/status/503")
            .build();

    CloseableHttpClient httpClient = HttpClientBuilder
            .create()
            .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(3)))
            .build();

    httpClient.execute(httpGet, response -> {
        System.out.println(response.getCode() + " " + response.getReasonPhrase());
        return null;
    });
}

}
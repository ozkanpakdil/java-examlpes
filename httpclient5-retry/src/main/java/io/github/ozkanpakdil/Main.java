package io.github.ozkanpakdil;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
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

public class Main {
public static void main(String[] args) throws IOException {
    final ClassicHttpRequest httpGet = ClassicRequestBuilder.get("https://httpbin.org/status/500")
            .build();

    CloseableHttpClient httpClient = HttpClientBuilder
            .create()
            .setRetryStrategy(new CustomRetryStrategy(3, TimeValue.ofSeconds(3)))
            .build();

    httpClient.execute(httpGet, response -> {
        System.out.println(response.getCode() + " " + response.getReasonPhrase());
        return null;
    });
}

static class CustomRetryStrategy implements HttpRequestRetryStrategy {
    private final int maxRetries;
    private final TimeValue retryInterval;

    public CustomRetryStrategy(final int maxRetries, final TimeValue retryInterval) {
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval;
    }

    @Override
    public boolean retryRequest(
            final HttpRequest request,
            final IOException exception,
            final int execCount,
            final HttpContext context) {
        Args.notNull(request, "request");
        Args.notNull(exception, "exception");

        if (execCount > this.maxRetries) {
            // Do not retry if over max retries
            return false;
        }
        return true;
    }

    @Override
    public boolean retryRequest(
            final HttpResponse response,
            final int execCount,
            final HttpContext context) {
        Args.notNull(response, "response");

        return execCount <= this.maxRetries;
    }

    @Override
    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
        System.out.println("Retrying HTTP request after " + retryInterval.toString());
        return retryInterval;
    }
}
}
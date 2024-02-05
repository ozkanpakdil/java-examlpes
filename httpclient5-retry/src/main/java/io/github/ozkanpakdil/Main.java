package io.github.ozkanpakdil;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.RequestFailedException;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.InterruptedIOException;

public class Main {
    public static void main(String[] args) throws IOException {
        successfulCall();
        unknownHostException();
    }

    private static void unknownHostException() throws IOException {
        final ClassicHttpRequest httpGet = ClassicRequestBuilder.get("https://ht11tpbin.org/status/500")
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .addExecInterceptorFirst("mainHandler", new CustomExecChainHandler(DefaultHttpRequestRetryStrategy.INSTANCE))
                .setRetryStrategy(new CustomRetryStrategy(0, TimeValue.ofSeconds(2)))
                .build();

        httpClient.execute(httpGet, response -> {
            System.out.println(response.getCode() + " " + response.getReasonPhrase());
            return null;
        });
    }

    private static void successfulCall() throws IOException {
        final ClassicHttpRequest httpGet = ClassicRequestBuilder.get("https://httpbin.org/status/500")
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .addExecInterceptorFirst("mainHandler", new CustomExecChainHandler(DefaultHttpRequestRetryStrategy.INSTANCE))
                .setRetryStrategy(new CustomRetryStrategy(1, TimeValue.ofSeconds(2)))
                .build();

        httpClient.execute(httpGet, response -> {
            System.out.println(response.getCode() + " " + response.getReasonPhrase());
            return null;
        });
    }

    @Slf4j
    static class CustomExecChainHandler implements ExecChainHandler {

        private final HttpRequestRetryStrategy retryStrategy;

        public CustomExecChainHandler(HttpRequestRetryStrategy retryStrategy) {
            this.retryStrategy = retryStrategy;
        }

        @Override
        public ClassicHttpResponse execute(
                final ClassicHttpRequest request,
                final ExecChain.Scope scope,
                final ExecChain chain) throws IOException, HttpException {
            Args.notNull(request, "request");
            Args.notNull(scope, "scope");
            final String exchangeId = scope.exchangeId;
            final HttpRoute route = scope.route;
            final HttpClientContext context = scope.clientContext;
            ClassicHttpRequest currentRequest = request;

            for (int execCount = 1; ; execCount++) {
                final ClassicHttpResponse response;
                try {
                    response = chain.proceed(currentRequest, scope);
                } catch (final Exception ex) {
                    if (scope.execRuntime.isExecutionAborted()) {
                        throw new RequestFailedException("Request aborted");
                    }
                    final HttpEntity requestEntity = request.getEntity();
                    if (requestEntity != null && !requestEntity.isRepeatable()) {
                        if (log.isDebugEnabled()) {
                            log.debug("{} cannot retry non-repeatable request", exchangeId);
                        }
                        throw ex;
                    }
                    if (retryStrategy.retryRequest(request, new IOException(ex), execCount, context)) {
                        if (log.isDebugEnabled()) {
                            log.debug("{} {}", exchangeId, ex.getMessage(), ex);
                        }
                        if (log.isInfoEnabled()) {
                            log.info("Recoverable I/O exception ({}) caught when processing request to {}",
                                    ex.getClass().getName(), route);
                        }
                        final TimeValue nextInterval = retryStrategy.getRetryInterval(request, new IOException(ex), execCount, context);
                        if (TimeValue.isPositive(nextInterval)) {
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("{} wait for {}", exchangeId, nextInterval);
                                }
                                nextInterval.sleep();
                            } catch (final InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new InterruptedIOException();
                            }
                        }
                        currentRequest = ClassicRequestBuilder.copy(scope.originalRequest).build();
                        continue;
                    } else {
                        if (ex instanceof NoHttpResponseException) {
                            final NoHttpResponseException updatedex = new NoHttpResponseException(
                                    route.getTargetHost().toHostString() + " failed to respond");
                            updatedex.setStackTrace(ex.getStackTrace());
                            throw updatedex;
                        }
                        //TODO instead of throwing the exception we can find something better.
                        throw ex;
                    }
                }

                try {
                    final HttpEntity entity = request.getEntity();
                    if (entity != null && !entity.isRepeatable()) {
                        if (log.isDebugEnabled()) {
                            log.debug("{} cannot retry non-repeatable request", exchangeId);
                        }
                        return response;
                    }
                    if (retryStrategy.retryRequest(response, execCount, context)) {
                        final TimeValue nextInterval = retryStrategy.getRetryInterval(response, execCount, context);
                        // Make sure the retry interval does not exceed the response timeout
                        if (TimeValue.isPositive(nextInterval)) {
                            final RequestConfig requestConfig = context.getRequestConfig();
                            final Timeout responseTimeout = requestConfig.getResponseTimeout();
                            if (responseTimeout != null && nextInterval.compareTo(responseTimeout) > 0) {
                                return response;
                            }
                        }
                        response.close();
                        if (TimeValue.isPositive(nextInterval)) {
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("{} wait for {}", exchangeId, nextInterval);
                                }
                                nextInterval.sleep();
                            } catch (final InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new InterruptedIOException();
                            }
                        }
                        currentRequest = ClassicRequestBuilder.copy(scope.originalRequest).build();
                    } else {
                        return response;
                    }
                } catch (final RuntimeException ex) {
                    response.close();
                    throw ex;
                }
            }
        }

    }

    @Slf4j
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

            System.out.println(execCount + " - Exception happened retrying " + exception.getMessage());

            // Do not retry if over max retries
            return execCount <= this.maxRetries;
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
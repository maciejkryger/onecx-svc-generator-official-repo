package org.tkit.onecx.onecxsvcgen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LatestVersionResolverTest {

    @AfterEach
    void clearInterruptedFlag() {
        Thread.interrupted();
    }

    @Test
    void resolveLatestReturnsNormalizedTagWhenGithubReturnsTagName() {
        HttpClient client = new FakeHttpClient(req -> new FakeHttpResponse(200, req, "{\"tag_name\":\"v3.2.1\"}"));
        LatestVersionResolver resolver = new LatestVersionResolver(client, new ObjectMapper());

        String version = resolver.resolveLatest("onecx/onecx-quarkus3-parent", "3.1.0");

        assertEquals("3.2.1", version);
    }

    @Test
    void resolveLatestReturnsFallbackWhenStatusIsNotSuccess() {
        HttpClient client = new FakeHttpClient(req -> new FakeHttpResponse(404, req, "{}"));
        LatestVersionResolver resolver = new LatestVersionResolver(client, new ObjectMapper());

        String version = resolver.resolveLatest("onecx/onecx-quarkus3-parent", "3.1.0");

        assertEquals("3.1.0", version);
    }

    @Test
    void resolveLatestReturnsFallbackAndKeepsInterruptFlagOnInterruptedException() {
        HttpClient client = new ThrowingHttpClient(new InterruptedException("boom"));
        LatestVersionResolver resolver = new LatestVersionResolver(client, new ObjectMapper());

        String version = resolver.resolveLatest("onecx/onecx-quarkus3-parent", "3.1.0");

        assertEquals("3.1.0", version);
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void resolveLatestWithSourceMarksLatestWhenTagIsResolved() {
        HttpClient client = new FakeHttpClient(req -> new FakeHttpResponse(200, req, "{\"tag_name\":\"v3.2.1\"}"));
        LatestVersionResolver resolver = new LatestVersionResolver(client, new ObjectMapper());

        LatestVersionResolver.ResolvedVersion resolved = resolver.resolveLatestWithSource("onecx/onecx-quarkus3-parent", "3.1.0");

        assertEquals("3.2.1", resolved.version());
        assertEquals(LatestVersionResolver.Source.LATEST, resolved.source());
    }

    @Test
    void resolveLatestWithSourceMarksDefaultWhenFallbackIsUsed() {
        HttpClient client = new FakeHttpClient(req -> new FakeHttpResponse(500, req, "{}"));
        LatestVersionResolver resolver = new LatestVersionResolver(client, new ObjectMapper());

        LatestVersionResolver.ResolvedVersion resolved = resolver.resolveLatestWithSource("onecx/onecx-quarkus3-parent", "3.1.0");

        assertEquals("3.1.0", resolved.version());
        assertEquals(LatestVersionResolver.Source.DEFAULT, resolved.source());
    }

    private static final class FakeHttpClient extends HttpClient {
        private final Function<HttpRequest, FakeHttpResponse> responseFactory;

        private FakeHttpClient(Function<HttpRequest, FakeHttpResponse> responseFactory) {
            this.responseFactory = responseFactory;
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            @SuppressWarnings("unchecked")
            HttpResponse<T> response = (HttpResponse<T>) responseFactory.apply(request);
            return response;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, null, new SecureRandom());
                return context;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }
    }

    private static final class ThrowingHttpClient extends HttpClient {
        private final Exception exception;

        private ThrowingHttpClient(Exception exception) {
            this.exception = exception;
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException, InterruptedException {
            if (exception instanceof IOException ioe) {
                throw ioe;
            }
            if (exception instanceof InterruptedException ie) {
                throw ie;
            }
            throw new IOException(exception);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, null, new SecureRandom());
                return context;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }
    }

    private static final class FakeHttpResponse implements HttpResponse<String> {
        private final int status;
        private final HttpRequest request;
        private final String body;

        private FakeHttpResponse(int status, HttpRequest request, String body) {
            this.status = status;
            this.request = request;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return status;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (a, b) -> true);
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}


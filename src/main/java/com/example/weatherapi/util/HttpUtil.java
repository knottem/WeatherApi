package com.example.weatherapi.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Utility class for performing HTTP GET requests.
 * <p>
 * Requests are sent using HTTP/1.1 with predefined connection and request
 * timeouts. Optional request headers can be supplied when needed.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class HttpUtil {

    private HttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final int CONNECTION_TIMEOUT = 3; // 3 seconds
    private static final int REQUEST_TIMEOUT = 6; // 6 seconds

    /**
     * Sends an HTTP GET request to the specified URI.
     *
     * @param uri the URI to send the request to
     * @return the HTTP response with the response body as a string
     * @throws IOException if an I/O error occurs while sending or receiving the request
     * @throws InterruptedException if the request is interrupted
     */
    public static HttpResponse<String> getContentFromUrl(URI uri) throws IOException, InterruptedException {
        return getContentFromUrl(uri, null);
    }

    /**
     * Sends an HTTP GET request to the specified URI with optional request headers.
     *
     * @param uri the URI to send the request to
     * @param headers optional headers to include in the request; may be {@code null}
     * @return the HTTP response with the response body as a string
     * @throws IOException if an I/O error occurs while sending or receiving the request
     * @throws InterruptedException if the request is interrupted
     */
    public static HttpResponse<String> getContentFromUrl(URI uri, Map<String, String> headers) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT))
                .build();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT))
                .GET();

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpRequest request = requestBuilder.build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
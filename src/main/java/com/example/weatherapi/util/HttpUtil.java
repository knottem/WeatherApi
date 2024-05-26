package com.example.weatherapi.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class HttpUtil {

    private HttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final int CONNECTION_TIMEOUT = 3; // 3 seconds
    private static final int REQUEST_TIMEOUT = 6; // 6 seconds

    public static HttpResponse<String> getContentFromUrl(URI uri) throws IOException, InterruptedException {
        return getContentFromUrl(uri, null);
    }

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
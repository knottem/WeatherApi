package com.example.weatherapi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class HttpUtil {

    private HttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final int REQUEST_TIMEOUT = 10000; // 10 seconds

    public static HttpResponse<String> getContentFromUrl(URI uri) throws IOException, InterruptedException {
        return getContentFromUrlWithHeaders(uri, null);
    }

    public static HttpResponse<String> getContentFromUrlWithHeaders(URI uri, Map<String, String> headers) throws IOException, InterruptedException {
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
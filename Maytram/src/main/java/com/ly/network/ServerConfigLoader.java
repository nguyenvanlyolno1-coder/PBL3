package com.ly.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerConfigLoader {

    private static final String GIST_RAW_URL =
            "https://gist.githubusercontent.com/nguyenvanlyolno1-coder/905e9a98eceb65de1628c5c0b09d480d/raw/config.txt";

    public String fetchWebSocketUrl() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GIST_RAW_URL))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        String url = response.body() != null ? response.body().trim() : "";

        if (url.isBlank()) {
            throw new RuntimeException("Không lấy được URL máy chủ.");
        }

        return url;
    }

    public String toHttpBaseUrl(String wsUrl) {
        String httpUrl = wsUrl
                .replace("wss://", "https://")
                .replace("ws://", "http://");

        if (httpUrl.endsWith("/ws-monitor")) {
            httpUrl = httpUrl.substring(0, httpUrl.length() - "/ws-monitor".length());
        }

        return httpUrl;
    }
}
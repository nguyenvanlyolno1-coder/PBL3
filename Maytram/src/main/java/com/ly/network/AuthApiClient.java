package com.ly.network;

import com.google.gson.Gson;
import com.ly.model.StudentInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthApiClient {

    private final HttpClient httpClient;
    private final Gson gson;

    public AuthApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public StudentInfo login(String baseHttpUrl, String username, String password) throws Exception {
        String jsonBody = gson.toJson(Map.of(
                "username", username,
                "password", password
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseHttpUrl + "/api/auth/student"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Đăng nhập thất bại. HTTP status: " + response.statusCode());
        }

        StudentInfo info = gson.fromJson(response.body(), StudentInfo.class);

        if (info == null || !info.isSuccess()) {
            String msg = info != null ? info.getMessage() : "Không nhận được dữ liệu từ server";
            throw new RuntimeException(msg);
        }

        return info;
    }
}
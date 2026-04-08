package com.ly.core;

import com.ly.network.AgentWebSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {

    private static final String GIST_RAW_URL = "https://gist.githubusercontent.com/nguyenvanlyolno1-coder/905e9a98eceb65de1628c5c0b09d480d/raw/config.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=====================================");
        System.out.println("   HỆ THỐNG THI TRỰC TUYẾN DUT       ");
        System.out.println("=====================================\n");

        System.out.print("▶ Vui lòng nhập Mã Sinh Viên của bạn: ");
        String msv = scanner.nextLine().trim();

        System.out.println("\n🔄 Đang tìm kiếm máy chủ phòng thi...");

        // Tự động lên GitHub lấy link Ngrok
        String serverUrl = fetchUrlFromGist();

        if (serverUrl == null || serverUrl.isEmpty()) {
            System.out.println("❌ Lỗi mạng: Không thể lấy thông số cấu hình. Vui lòng kiểm tra Internet!");
            return;
        }

        System.out.println("✅ Đã nhận cấu hình từ Server: " + serverUrl);

        AgentWebSocket agent = new AgentWebSocket();
        agent.connectToServer(serverUrl, msv);

        while (!scanner.nextLine().equalsIgnoreCase("exit")) { }
    }

    private static String fetchUrlFromGist() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(GIST_RAW_URL)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().trim();
        } catch (Exception e) {
            return null;
        }
    }
}
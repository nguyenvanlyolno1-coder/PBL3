//package com.ly.core;
//
//import com.ly.network.AgentWebSocket;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Scanner;
//
//public class Main {
//
//    private static final String GIST_RAW_URL = "https://gist.githubusercontent.com/nguyenvanlyolno1-coder/905e9a98eceb65de1628c5c0b09d480d/raw/config.txt";
//
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("=====================================");
//        System.out.println("   HỆ THỐNG THI TRỰC TUYẾN DUT       ");
//        System.out.println("=====================================\n");
//
//        System.out.print("▶ Vui lòng nhập Mã Sinh Viên của bạn: ");
//        String msv = scanner.nextLine().trim();
//
//        System.out.println("\n🔄 Đang tìm kiếm máy chủ phòng thi...");
//
//        // Tự động lên GitHub lấy link Ngrok
//        String serverUrl = fetchUrlFromGist();
//
//        if (serverUrl == null || serverUrl.isEmpty()) {
//            System.out.println("❌ Lỗi mạng: Không thể lấy thông số cấu hình. Vui lòng kiểm tra Internet!");
//            return;
//        }
//
//        System.out.println("✅ Đã nhận cấu hình từ Server: " + serverUrl);
//
//        AgentWebSocket agent = new AgentWebSocket();
//        agent.connectToServer(serverUrl, msv);
//
//        while (!scanner.nextLine().equalsIgnoreCase("exit")) { }
//    }
//
//    private static String fetchUrlFromGist() {
//        try {
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(GIST_RAW_URL)).build();
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.body().trim();
//        } catch (Exception e) {
//            return null;
//        }
//    }
//}
package com.ly.core;

import com.ly.network.AgentWebSocket;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String GIST_RAW_URL =
            "https://gist.githubusercontent.com/nguyenvanlyolno1-coder/905e9a98eceb65de1628c5c0b09d480d/raw/config.txt";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=====================================");
        System.out.println("   HỆ THỐNG THI TRỰC TUYẾN DUT       ");
        System.out.println("=====================================\n");

        // Lấy URL server từ Gist
        System.out.println("🔄 Đang tìm kiếm máy chủ phòng thi...");
        String serverUrl = fetchUrlFromGist();
        if (serverUrl == null || serverUrl.isEmpty()) {
            System.out.println("❌ Không thể lấy cấu hình. Kiểm tra Internet!");
            return;
        }
        System.out.println("✅ Đã nhận cấu hình: " + serverUrl);

        // Xác thực sinh viên
        System.out.print("\n▶ Tên đăng nhập: ");
        String username = scanner.nextLine().trim();
        System.out.print("▶ Mật khẩu: ");
        String password = scanner.nextLine().trim();

        System.out.println("\n🔄 Đang xác thực...");

        // Chuyển wss:// thành https:// để gọi REST API
        String httpUrl = serverUrl
                .replace("wss://", "https://")
                .replace("ws://", "http://");
        // Bỏ /ws-monitor nếu có
        if (httpUrl.endsWith("/ws-monitor")) {
            httpUrl = httpUrl.replace("/ws-monitor", "");
        }

        String authResult = callAuthApi(httpUrl, username, password);

        if (authResult == null || authResult.contains("\"success\":false")) {
            System.out.println("❌ Xác thực thất bại! Sai tài khoản hoặc mật khẩu.");
            return;
        }

        System.out.println("✅ Xác thực thành công!");
//        System.out.println("✅ Xác thực thành công!");
        System.out.println("DEBUG JSON: " + authResult); // THÊM DÒNG NÀY

        System.out.println("\n📋 Danh sách ca thi của bạn:");

        System.out.println("\n📋 Danh sách ca thi của bạn:");
        System.out.println("─────────────────────────────");

// Parse mảng danhSachCaThi từ JSON
// (dùng hàm parseJsonArray đơn giản bên dưới)
        List<String[]> danhSachCaThi = parseCaThiList(authResult);

        if (danhSachCaThi.isEmpty()) {
            System.out.println("❌ Không có ca thi nào đang diễn ra cho bạn!");
            return;
        }

        for (int i = 0; i < danhSachCaThi.size(); i++) {
            String[] ca = danhSachCaThi.get(i);
            System.out.println((i + 1) + ". " + ca[1] + " [" + ca[3] + "]");
            System.out.println("   Thời gian: " + ca[2]);
        }

        System.out.println("─────────────────────────────");
        System.out.print("▶ Chọn ca thi (nhập số): ");
        int chon = Integer.parseInt(scanner.nextLine().trim()) - 1;

        if (chon < 0 || chon >= danhSachCaThi.size()) {
            System.out.println("❌ Lựa chọn không hợp lệ!");
            return;
        }

        String caThiId = danhSachCaThi.get(chon)[0];
        String tenCaThi = danhSachCaThi.get(chon)[1];
        String allowedUrls = danhSachCaThi.get(chon)[4];
        System.out.println("🌐 URLs được phép: [" + allowedUrls + "]");
        System.out.println("   Số URL: " + (allowedUrls.isEmpty() ? 0 : allowedUrls.split(",").length));
        String maSinhVien = parseJsonField(authResult, "maSinhVien");
        String hoTen = parseJsonField(authResult, "hoTen");
        System.out.println("\n✅ Đã chọn: " + tenCaThi);
        System.out.println("🌐 Đang kết nối vào phòng thi...");

// Kết nối WebSocket kèm caThiId
        AgentWebSocket agent = new AgentWebSocket();
        agent.connectToServer(serverUrl, maSinhVien, hoTen, caThiId, allowedUrls);
    }

    private static String fetchUrlFromGist() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(GIST_RAW_URL)).build();
            return client.send(req, HttpResponse.BodyHandlers.ofString())
                    .body().trim();
        } catch (Exception e) { return null; }
    }

    private static String callAuthApi(String baseUrl, String username, String password) {
        try {
            String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/student"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();
        } catch (Exception e) {
            System.out.println("❌ Lỗi gọi API: " + e.getMessage());
            return null;
        }
    }

    // Parse JSON field đơn giản không cần thư viện
    private static String parseJsonField(String json, String field) {
        try {
            String key = "\"" + field + "\":\"";
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) { return "Unknown"; }
    }
    // Parse JSON array đơn giản không cần thư viện
// Thêm field vào String[] — index 4 = allowedUrls (dạng csv)
    private static List<String[]> parseCaThiList(String json) {
        List<String[]> result = new java.util.ArrayList<>();
        try {
            String key = "\"danhSachCaThi\":[";
            int start = json.indexOf(key) + key.length();

            // Tìm ] đóng của mảng danhSachCaThi (phải tìm đúng cặp ngoặc)
            int depth = 0;
            int end = start;
            for (int i = start; i < json.length(); i++) {
                if (json.charAt(i) == '[') depth++;
                else if (json.charAt(i) == ']') {
                    if (depth == 0) { end = i; break; }
                    depth--;
                }
            }

            String arr = json.substring(start, end).trim();
            if (arr.isEmpty()) return result;

            // Tách từng object {} (xử lý nested array)
            List<String> objects = splitObjects(arr);

            for (String obj : objects) {
                // Parse từng field riêng lẻ — không phụ thuộc thứ tự
                String id   = obj.replaceAll(".*\"caThiId\":(\\d+).*", "$1");
                String ten  = parseJsonField(obj, "tenCaThi");
                String ngay = parseJsonField(obj, "ngayGio");
                String tt   = parseJsonField(obj, "trangThai");
                String urls = parseJsonArray(obj, "allowedUrls");

                result.add(new String[]{id, ten, ngay, tt, urls});
            }
        } catch (Exception e) {
            System.out.println("⚠️ Lỗi parse ca thi: " + e.getMessage());
        }
        return result;
    }

    // Tách các object {} trong mảng, xử lý đúng nested []
    private static List<String> splitObjects(String arr) {
        List<String> objects = new java.util.ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    objects.add(arr.substring(start + 1, i)); // Nội dung trong {}
                    start = -1;
                }
            }
        }
        return objects;
    }

    // Sửa parseJsonArray — tìm đúng cặp ngoặc []
    private static String parseJsonArray(String json, String field) {
        try {
            String key = "\"" + field + "\":[";
            int start = json.indexOf(key);
            if (start == -1) return "";
            start += key.length();

            // Tìm ] đóng đúng cặp
            int depth = 0;
            for (int i = start; i < json.length(); i++) {
                if (json.charAt(i) == '[') depth++;
                else if (json.charAt(i) == ']') {
                    if (depth == 0) {
                        String raw = json.substring(start, i);
                        return raw.replace("\"", "").replace(" ", "").trim();
                    }
                    depth--;
                }
            }
            return "";
        } catch (Exception e) { return ""; }
    }
}
//package com.ly;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.WebSocket;
//import java.util.Scanner;
//import java.util.concurrent.CompletionStage;
//
//public class Main {
//    public static void main(String[] args) {
//        System.out.println("Đang khởi động Agent Máy Thí Sinh...");
//
//        // Kết nối tới Server đang chạy ở localhost cổng 8080
//        HttpClient client = HttpClient.newHttpClient();
//        WebSocket webSocket = client.newWebSocketBuilder()
//                .buildAsync(URI.create("ws://localhost:8080/ws-monitor"), new WebSocket.Listener() {
//
//                    @Override
//                    public void onOpen(WebSocket webSocket) {
//                        System.out.println("🟢 Đã kết nối thành công tới Máy Giám Sát!");
//                        // Yêu cầu Server gửi tin nhắn tiếp theo
//                        webSocket.request(1);
//                    }
//
//                    @Override
//                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
//                        String command = data.toString();
//                        System.out.println("🔔 Nhận được lệnh từ Server: " + command);
//
//                        // Xử lý lệnh nhận được
//                        if (command.equals("BLOCK")) {
//                            System.out.println(">>> ĐANG THỰC THI LỆNH NGẮT MẠNG BẰNG IPTABLES...");
//                            // Sau này code gọi file thực thi của hệ điều hành sẽ nằm ở đây
//                        }
//
//                        // Tiếp tục lắng nghe lệnh mới
//                        webSocket.request(1);
//                        return null;
//                    }
//                }).join();
//
//        // Vòng lặp giữ cho chương trình chạy liên tục không bị tắt
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Nhập 'exit' và ấn Enter để tắt Agent.");
//        while (!scanner.nextLine().equalsIgnoreCase("exit")) {
//            // Đợi lệnh từ bàn phím nếu muốn thoát
//        }
//    }
//}
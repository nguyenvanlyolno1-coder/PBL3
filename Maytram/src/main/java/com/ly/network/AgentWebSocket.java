//package com.ly.network;
//
//import com.ly.system.OsCommand;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.WebSocket;
//import java.util.concurrent.CompletionStage;
//
//public class AgentWebSocket {
//
//    public void connectToServer(String serverUrl) {
//        HttpClient client = HttpClient.newHttpClient();
//
//        client.newWebSocketBuilder()
//                .buildAsync(URI.create(serverUrl), new WebSocket.Listener() {
//
//                    @Override
//                    public void onOpen(WebSocket webSocket) {
//                        System.out.println("🌐 [Network] Đã kết nối thành công!");
//                        // Gửi gói tin định danh ngay khi vừa mở kết nối
//                        webSocket.sendText(OsCommand.getSystemInfo(), true);
//                        webSocket.request(1);
//                    }
//
//                    @Override
//                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
//                        String command = data.toString();
//                        String result = OsCommand.execute(command);
//                        webSocket.sendText(result, true); // Báo cáo kết quả về Server
//                        webSocket.request(1);
//                        return null;
//                    }
//
//                    @Override
//                    public void onError(WebSocket webSocket, Throwable error) {
//                        System.err.println("❌ [Network] Lỗi kết nối: " + error.getMessage());
//                    }
//                }).join();
//    }
//}
package com.ly.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentWebSocket implements WebSocket.Listener {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // MỚI: Thêm một biến để lưu Mã Sinh Viên
    private String currentMsv;
    private String currentHoTen;
    private String currentCaThiId;
    // MỚI: Thêm tham số msv vào hàm kết nối
//    public void connectToServer(String serverUrl, String msv) {
//        this.currentMsv = msv; // Cất mã sinh viên đi để lát nữa gửi
//        com.ly.system.OsCommand.currentServerUrl = serverUrl;
//        HttpClient client = HttpClient.newHttpClient();
//        client.newWebSocketBuilder()
//                .buildAsync(URI.create(serverUrl), this)
//                .join();
//    }
    public void connectToServer(String serverUrl, String msv, String hoTen, String caThiId) {
        this.currentMsv = msv;
        this.currentHoTen = hoTen;
        this.currentCaThiId = caThiId;
        com.ly.system.OsCommand.currentServerUrl = serverUrl;
        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
                .buildAsync(URI.create(serverUrl), this)
                .join();
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("🌐 [Network] Đã kết nối thành công tới Máy Giám Sát!");

        // MỚI: Truyền cái currentMsv vào hàm getSystemInfo
        webSocket.sendText(
                com.ly.system.OsCommand.getSystemInfo(currentMsv, currentHoTen, currentCaThiId), true
        );

        scheduler.scheduleAtFixedRate(() -> {
            try {
                webSocket.sendText("HEARTBEAT", true);
            } catch (Exception e) {
                System.out.println("Lỗi gửi nhịp tim.");
            }
        }, 5, 5, TimeUnit.SECONDS);

        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String command = data.toString();

        if (command.equals("SCREENSHOT")) {
            System.out.println("📸 [Network] Đang chụp màn hình theo yêu cầu...");
            String base64 = com.ly.system.OsCommand.captureScreenBase64();

            // IN RA XEM ẢNH NẶNG BAO NHIÊU KÝ TỰ, HAY LÀ BỊ LỖI
            System.out.println("Độ dài chuỗi Base64: " + base64.length());

            // Bắt lỗi quá trình gửi qua mạng
            webSocket.sendText("IMG|" + base64, true).whenComplete((res, err) -> {
                if (err != null) {
                    System.out.println("❌ LỖI GỬI ẢNH: " + err.getMessage());
                } else {
                    System.out.println("✅ Đã tống ảnh vào đường ống mạng thành công!");
                }
            });
        } else {
            System.out.println("🔔 [Network] Nhận lệnh từ Server: " + command);
            com.ly.system.OsCommand.execute(command);
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("❌ [Network] Đóng kết nối: " + reason);
        scheduler.shutdown(); // Tắt bộ đếm giờ khi mất mạng
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.out.println("⚠️ [Network] Lỗi kết nối: " + error.getMessage());
    }
}
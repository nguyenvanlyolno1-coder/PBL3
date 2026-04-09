//
//package com.ly.maychu.handler;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Map; // Đã thêm dòng này để fix lỗi
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class MonitorHandler extends TextWebSocketHandler {
//
//    // Thay đổi List thành Map để quản lý theo Session ID
//    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
//    private static final Map<String, String> clientDetails = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        sessions.put(session.getId(), session);
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//        String payload = message.getPayload();
//
//        // Nếu nhận được gói tin định danh từ máy trạm
//        if (payload.startsWith("REG|")) {
//            clientDetails.put(session.getId(), payload.replace("REG|", ""));
//            System.out.println("📝 Đăng ký máy: " + clientDetails.get(session.getId()));
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        sessions.remove(session.getId());
//        clientDetails.remove(session.getId());
//        System.out.println("❌ Ngắt kết nối: " + session.getId());
//    }
//
//    // Hàm đếm số lượng máy online (Dùng cho giao diện)
//    public int getConnectedCount() {
//        return sessions.size();
//    }
//
//    // Lấy danh sách chi tiết các máy (Chuẩn bị cho bước vẽ bảng Web)
//    public Map<String, String> getClients() {
//        return clientDetails;
//    }
//
//    // Gửi lệnh đến toàn bộ các máy đang online
//    public void broadcast(String message) {
//        for (WebSocketSession session : sessions.values()) {
//            try {
//                if (session.isOpen()) {
//                    session.sendMessage(new TextMessage(message));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
package com.ly.maychu.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MonitorHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> clientDetails = new ConcurrentHashMap<>();
    // MỚI: Bản đồ lưu thời gian nhận tín hiệu cuối cùng (tính bằng mili-giây)
    private static final Map<String, Long> lastSeen = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        lastSeen.put(session.getId(), System.currentTimeMillis()); // Vừa kết nối là lưu giờ luôn
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        // MỚI: Bất cứ khi nào máy trạm gửi tin (kể cả chữ HEARTBEAT), ta cập nhật lại đồng hồ
        lastSeen.put(session.getId(), System.currentTimeMillis());
        if (payload.startsWith("IMG|")) {
            String base64Image = payload.substring(4);
            // Lưu ảnh vào kho chứa tĩnh của Controller
            com.ly.maychu.controller.CommandController.imageStore.put(session.getId(), base64Image);
            System.out.println("📸 Đã nhận ảnh màn hình từ máy: " + session.getId());
            return; // Nhận xong ảnh thì kết thúc hàm
        }
        if (payload.startsWith("REG|")) {
            clientDetails.put(session.getId(), payload.replace("REG|", ""));
            System.out.println("📝 Đăng ký máy: " + clientDetails.get(session.getId()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // CHỈ xóa session để không gửi nhầm lệnh vào đường ống đã đóng
        sessions.remove(session.getId());

        // Cố tình KHÔNG XÓA clientDetails và lastSeen
        // Để cho giao diện Dashboard có cơ hội tính thời gian và chuyển sang ĐỎ

        System.out.println("❌ Ngắt kết nối thực tế (Chờ Dashboard báo động): " + session.getId());
    }

    public int getConnectedCount() {
        return sessions.size();
    }

    // MỚI: API gom cả Thông tin máy + Thời gian liên lạc cuối để gửi cho Giao diện Web
    public Map<String, Object> getFullStatus() {
        Map<String, Object> statusMap = new ConcurrentHashMap<>();
        for (String id : clientDetails.keySet()) {
            Map<String, String> info = new ConcurrentHashMap<>();
            info.put("details", clientDetails.get(id));
            info.put("lastSeen", String.valueOf(lastSeen.get(id)));
            statusMap.put(id, info);
        }
        return statusMap;
    }

    public void broadcast(String message) {
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) session.sendMessage(new TextMessage(message));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    // MỚI: Hàm gửi lệnh "Điện thoại riêng" cho một máy duy nhất
    public boolean sendToClient(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (Exception e) {
                System.out.println("⚠️ Lỗi gửi tin nhắn riêng: " + e.getMessage());
            }
        }
        return false;
    }
}

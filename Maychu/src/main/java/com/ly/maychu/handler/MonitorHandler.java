////
////package com.ly.maychu.handler;
////
////import org.springframework.stereotype.Component;
////import org.springframework.web.socket.CloseStatus;
////import org.springframework.web.socket.TextMessage;
////import org.springframework.web.socket.WebSocketSession;
////import org.springframework.web.socket.handler.TextWebSocketHandler;
////
////import java.util.Map; // Đã thêm dòng này để fix lỗi
////import java.util.concurrent.ConcurrentHashMap;
////
////@Component
////public class MonitorHandler extends TextWebSocketHandler {
////
////    // Thay đổi List thành Map để quản lý theo Session ID
////    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
////    private static final Map<String, String> clientDetails = new ConcurrentHashMap<>();
////
////    @Override
////    public void afterConnectionEstablished(WebSocketSession session) {
////        sessions.put(session.getId(), session);
////    }
////
////    @Override
////    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
////        String payload = message.getPayload();
////
////        // Nếu nhận được gói tin định danh từ máy trạm
////        if (payload.startsWith("REG|")) {
////            clientDetails.put(session.getId(), payload.replace("REG|", ""));
////            System.out.println("📝 Đăng ký máy: " + clientDetails.get(session.getId()));
////        }
////    }
////
////    @Override
////    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
////        sessions.remove(session.getId());
////        clientDetails.remove(session.getId());
////        System.out.println("❌ Ngắt kết nối: " + session.getId());
////    }
////
////    // Hàm đếm số lượng máy online (Dùng cho giao diện)
////    public int getConnectedCount() {
////        return sessions.size();
////    }
////
////    // Lấy danh sách chi tiết các máy (Chuẩn bị cho bước vẽ bảng Web)
////    public Map<String, String> getClients() {
////        return clientDetails;
////    }
////
////    // Gửi lệnh đến toàn bộ các máy đang online
////    public void broadcast(String message) {
////        for (WebSocketSession session : sessions.values()) {
////            try {
////                if (session.isOpen()) {
////                    session.sendMessage(new TextMessage(message));
////                }
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
////    }
////}
//package com.ly.maychu.handler;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class MonitorHandler extends TextWebSocketHandler {
//
//    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
//    private static final Map<String, String> clientDetails = new ConcurrentHashMap<>();
//    // MỚI: Bản đồ lưu thời gian nhận tín hiệu cuối cùng (tính bằng mili-giây)
//    private static final Map<String, Long> lastSeen = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        // MỚI: Ép thẳng tay giới hạn Text Message của Session này lên 5MB
//        session.setTextMessageSizeLimit(5 * 1024 * 1024);
//
//        sessions.put(session.getId(), session);
//        lastSeen.put(session.getId(), System.currentTimeMillis());
//        System.out.println("🔗 Mới kết nối: " + session.getId());
//    }
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//        String payload = message.getPayload();
//
//        // MỚI: Bất cứ khi nào máy trạm gửi tin (kể cả chữ HEARTBEAT), ta cập nhật lại đồng hồ
//        lastSeen.put(session.getId(), System.currentTimeMillis());
//        if (payload.startsWith("IMG|")) {
//            String base64Image = payload.substring(4);
//            // Lưu ảnh vào kho chứa tĩnh của Controller
//            com.ly.maychu.controller.CommandController.imageStore.put(session.getId(), base64Image);
//            System.out.println("📸 Đã nhận ảnh màn hình từ máy: " + session.getId());
//            return; // Nhận xong ảnh thì kết thúc hàm
//        }
//        if (payload.startsWith("REG|")) {
//            clientDetails.put(session.getId(), payload.replace("REG|", ""));
//            System.out.println("📝 Đăng ký máy: " + clientDetails.get(session.getId()));
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        // CHỈ xóa session để không gửi nhầm lệnh vào đường ống đã đóng
//        sessions.remove(session.getId());
//
//        // Cố tình KHÔNG XÓA clientDetails và lastSeen
//        // Để cho giao diện Dashboard có cơ hội tính thời gian và chuyển sang ĐỎ
//
//        System.out.println("❌ Ngắt kết nối thực tế (Chờ Dashboard báo động): " + session.getId());
//    }
//
//    public int getConnectedCount() {
//        return sessions.size();
//    }
//
//    // MỚI: API gom cả Thông tin máy + Thời gian liên lạc cuối để gửi cho Giao diện Web
//    public Map<String, Object> getFullStatus() {
//        Map<String, Object> statusMap = new ConcurrentHashMap<>();
//        for (String id : clientDetails.keySet()) {
//            Map<String, String> info = new ConcurrentHashMap<>();
//            info.put("details", clientDetails.get(id));
//            info.put("lastSeen", String.valueOf(lastSeen.get(id)));
//            statusMap.put(id, info);
//        }
//        return statusMap;
//    }
//
//    public void broadcast(String message) {
//        for (WebSocketSession session : sessions.values()) {
//            try {
//                if (session.isOpen()) session.sendMessage(new TextMessage(message));
//            } catch (Exception e) { e.printStackTrace(); }
//        }
//    }
//    // MỚI: Hàm gửi lệnh "Điện thoại riêng" cho một máy duy nhất
//    public boolean sendToClient(String sessionId, String message) {
//        WebSocketSession session = sessions.get(sessionId);
//        if (session != null && session.isOpen()) {
//            try {
//                session.sendMessage(new TextMessage(message));
//                return true;
//            } catch (Exception e) {
//                System.out.println("⚠️ Lỗi gửi tin nhắn riêng: " + e.getMessage());
//            }
//        }
//        return false;
//    }
//}
package com.ly.maychu.handler;

import com.ly.maychu.controller.CommandController;
import com.ly.maychu.model.*;
import com.ly.maychu.repository.*;
import com.ly.maychu.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MonitorHandler extends TextWebSocketHandler {

    @Autowired
    private NguoiDungRepository nguoiDungRepo;
    @Autowired
    private CaThiRepository caThiRepo;
    @Autowired
    private ChiTietCaThiRepository chiTietCaThiRepo;
    @Autowired
    private LichSuKetNoiRepository lichSuRepo;
    @Autowired
    private StorageService storageService;
    // Key = maSinhVien (thay vì sessionId)
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, ClientInfo> clientMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.setTextMessageSizeLimit(5 * 1024 * 1024);
        System.out.println("🔗 Kết nối mới (chờ REG): " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        // Cập nhật lastSeen cho mọi tin nhắn
        clientMap.values().stream()
                .filter(c -> c.getSessionId().equals(session.getId()))
                .findFirst()
                .ifPresent(c -> c.setLastSeen(System.currentTimeMillis()));

        // --- Xử lý đăng ký máy trạm ---
        if (payload.startsWith("REG|")) {
            // Format: REG|maSinhVien|hoTen|hostname|ip|caThiId
            String[] parts = payload.split("\\|");
            if (parts.length >= 6) {
                String maSinhVien = parts[1];
                String hoTen      = parts[2];
                String hostname   = parts[3];
                String ip         = parts[4];
                Long caThiId      = Long.parseLong(parts[5]);

                ClientInfo info = new ClientInfo();
                info.setSessionId(session.getId());
                info.setMaSinhVien(maSinhVien);
                info.setHoTen(hoTen);
                info.setHostname(hostname);
                info.setIp(ip);
                info.setLastSeen(System.currentTimeMillis());
                info.setTrangThai("DANG_THI");
                info.setChiTietCaThiId(caThiId);

                sessions.put(maSinhVien, session);
                clientMap.put(maSinhVien, info);

                // Ghi DB
                ghiChiTietCaThi(info, caThiId);

                System.out.println("📝 Đăng ký: " + hoTen + " (" + maSinhVien + ") - Ca thi: " + caThiId);
            }
            return;
        }

        // --- Heartbeat ---
        if (payload.equals("HEARTBEAT")) {
            clientMap.values().stream()
                    .filter(c -> c.getSessionId().equals(session.getId()))
                    .findFirst()
                    .ifPresent(c -> c.setLastSeen(System.currentTimeMillis()));
            return;
        }

        if (payload.startsWith("IMG|")) {
            String base64 = payload.substring(4);

            // Tìm thông tin SV từ session
            clientMap.values().stream()
                    .filter(c -> c.getSessionId().equals(session.getId()))
                    .findFirst()
                    .ifPresent(c -> {
                        // Lưu file vật lý + ghi DB
                        String url = storageService.saveScreenshot(
                                c.getMaSinhVien(),
                                c.getChiTietCaThiId(),
                                base64
                        );

                        // Vẫn giữ trong RAM để dashboard lấy ngay
                        if (url != null) {
                            CommandController.imageStore.put(c.getMaSinhVien(), url);
                        }
                    });

            System.out.println("📸 Nhận + lưu ảnh từ: " + session.getId());
            return;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Tìm SV theo sessionId
        clientMap.entrySet().stream()
                .filter(e -> e.getValue().getSessionId().equals(session.getId()))
                .findFirst()
                .ifPresent(e -> {
                    String maSinhVien = e.getKey();
                    ClientInfo info = e.getValue();
                    info.setTrangThai("MAT_KET_NOI");

                    // Ghi DB: LichSuKetNoi
                    ghiLichSuKetNoi(info, "NGAT_KET_NOI", "Mất kết nối");

                    // Xóa session nhưng giữ clientMap để dashboard thấy trạng thái đỏ
                    sessions.remove(maSinhVien);
                    System.out.println("❌ Mất kết nối: " + info.getHoTen());
                });
    }

    // --- Hàm ghi DB ---

    private void ghiChiTietCaThi(ClientInfo info, Long caThiId) {
        try {
            nguoiDungRepo.findByUsername(info.getMaSinhVien()).ifPresent(sv -> {
                caThiRepo.findById(caThiId).ifPresent(ca -> {
                    // Kiểm tra đã có record chưa (tránh duplicate)
                    Optional<ChiTietCaThi> existing =
                            chiTietCaThiRepo.findBySinhVienAndCaThi(sv, ca);

                    ChiTietCaThi ctct = existing.orElse(new ChiTietCaThi());
                    ctct.setSinhVien(sv);
                    ctct.setCaThi(ca);
//                    ctct.setThoiGianVao(LocalDateTime.now());
//                    ctct.setTrangThai("DANG_THI");
                    ChiTietCaThi saved = chiTietCaThiRepo.save(ctct);
                    info.setChiTietCaThiId(saved.getId());

                    System.out.println("✅ Ghi ChiTietCaThi: " + sv.getTen() + " → Ca " + ca.getTenCaThi());
                });
            });
        } catch (Exception e) {
            System.out.println("⚠️ Lỗi ghi ChiTietCaThi: " + e.getMessage());
        }
    }

    private void ghiLichSuKetNoi(ClientInfo info, String loai, String ghiChu) {
        try {
            if (info.getChiTietCaThiId() == null) return;
            chiTietCaThiRepo.findById(info.getChiTietCaThiId()).ifPresent(ctct -> {
                LichSuKetNoi log = new LichSuKetNoi();
                log.setChiTietCaThi(ctct);
                log.setLoaiSuKien(loai);
                log.setThoiGian(LocalDateTime.now());
                log.setGhiChu(ghiChu);
                lichSuRepo.save(log);
            });
        } catch (Exception e) {
            System.out.println("⚠️ Lỗi ghi LichSuKetNoi: " + e.getMessage());
        }
    }

    // --- Hàm dùng cho Controller ---

    public Map<String, Object> getFullStatus() {
        Map<String, Object> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, ClientInfo> entry : clientMap.entrySet()) {
            ClientInfo info = entry.getValue();
            Map<String, String> data = new ConcurrentHashMap<>();
            data.put("hoTen", info.getHoTen());
            data.put("maSinhVien", info.getMaSinhVien());
            data.put("ip", info.getIp());
            data.put("lastSeen", String.valueOf(info.getLastSeen()));
            data.put("trangThai", info.getTrangThai());
            result.put(entry.getKey(), data);
        }
        return result;
    }

    public int getConnectedCount() { return sessions.size(); }

    public void broadcast(String message) {
        sessions.values().forEach(s -> {
            try {
                if (s.isOpen()) s.sendMessage(new TextMessage(message));
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public boolean sendToClient(String maSinhVien, String message) {
        WebSocketSession session = sessions.get(maSinhVien);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (Exception e) { return false; }
        }
        return false;
    }
    public Map<String, ClientInfo> getClientMap() {
        return clientMap;
    }
}
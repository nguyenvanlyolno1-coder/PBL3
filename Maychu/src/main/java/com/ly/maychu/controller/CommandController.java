package com.ly.maychu.controller;

import com.ly.maychu.handler.MonitorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommandController {
    public static java.util.concurrent.ConcurrentHashMap<String, String> imageStore = new java.util.concurrent.ConcurrentHashMap<>();
    @Autowired
    private MonitorHandler monitorHandler;

    // Đường dẫn: http://localhost:8080/api/send?cmd=BLOCK
    @GetMapping("/send")
    public String sendCommand(@RequestParam String cmd) {
        monitorHandler.broadcast(cmd);
        return "Đã gửi lệnh: " + cmd + " đến tất cả máy trạm!";
    }
    @GetMapping("/image")
    public String getImage(@RequestParam String id) {
        String base64 = imageStore.getOrDefault(id, "");
        // Lấy xong thì xóa luôn cho nhẹ RAM Server
        imageStore.remove(id);
        return base64;
    }
    // Đường dẫn: http://localhost:8080/api/status
    @GetMapping("/status")
    public String getStatus() {
        int count = monitorHandler.getConnectedCount();
        return String.valueOf(count); // Trả về con số dưới dạng chuỗi
    }
    // Đường dẫn: http://localhost:8080/api/clients
//    @GetMapping("/clients")
//    public java.util.Map<String, String> getClients() {
//        return monitorHandler.getClients(); // Lấy danh sách máy từ Handler trả về cho Web
//    }
    // Đường dẫn: http://localhost:8080/api/status-all
    @GetMapping("/status-all")
    public java.util.Map<String, Object> getStatusAll() {
        return monitorHandler.getFullStatus();
    }
    // Đường dẫn: http://localhost:8080/api/send-to?id=xyz&cmd=SCREENSHOT
    @GetMapping("/send-to")
    public String sendToClient(@RequestParam String id, @RequestParam String cmd) {
        boolean success = monitorHandler.sendToClient(id, cmd);
        if (success) {
            return "Đã gửi lệnh [" + cmd + "] thành công đến máy: " + id;
        } else {
            return "Thất bại: Máy không tồn tại hoặc đã mất kết nối!";
        }
    }
}
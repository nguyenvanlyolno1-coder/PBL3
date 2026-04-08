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

    @Autowired
    private MonitorHandler monitorHandler;

    // Đường dẫn: http://localhost:8080/api/send?cmd=BLOCK
    @GetMapping("/send")
    public String sendCommand(@RequestParam String cmd) {
        monitorHandler.broadcast(cmd);
        return "Đã gửi lệnh: " + cmd + " đến tất cả máy trạm!";
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
}
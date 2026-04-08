package com.ly.system;

import java.net.InetAddress;
import java.net.URI;

public class OsCommand {

    // Biến lưu trữ link Ngrok hiện tại
    public static String currentServerUrl = "";

    // TÊN MIỀN TRANG WEB THI (Bạn có thể đổi tên miền trường bạn vào đây)com
    public static final String EXAM_DOMAIN = "http://lms.dut.udn.vn/";

    public static String getSystemInfo(String msv) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return "REG|" + msv + " - " + localHost.getHostName() + "|" + localHost.getHostAddress();
        } catch (Exception e) {
            return "REG|" + msv + " - Unknown|0.0.0.0";
        }
    }

    public static void execute(String command) {
        if (command.equals("BLOCK")) {
            lockNetwork();
        } else if (command.equals("ALLOW")) {
            unlockNetwork();
        }
    }

    private static void lockNetwork() {
        try {
            // Tách lấy domain của Ngrok từ cái link wss://...
            URI uri = new URI(currentServerUrl);
            String ngrokHost = uri.getHost();

            // Chuỗi lệnh iptables "bàn tay sắt"
            String[] cmd = {
                    "/bin/sh", "-c",
                    "iptables -F OUTPUT && " +                                  // 1. Xóa các luật Output cũ
                            "iptables -A OUTPUT -o lo -j ACCEPT && " +                  // 2. Cho phép tiến trình nội bộ chạy
                            "iptables -A OUTPUT -p udp --dport 53 -j ACCEPT && " +      // 3. Mở cổng DNS để dịch tên miền
                            "iptables -A OUTPUT -p tcp --dport 53 -j ACCEPT && " +
                            "iptables -A OUTPUT -d " + EXAM_DOMAIN + " -j ACCEPT && " + // 4. Đục lỗ cho trang thi
                            "iptables -A OUTPUT -d " + ngrokHost + " -j ACCEPT && " +   // 5. Đục lỗ cho Server Ngrok
                            "iptables -P OUTPUT DROP"                                   // 6. KHÓA CHẶT MỌI THỨ CÒN LẠI
            };

            System.out.println("🔒 KÍCH HOẠT KHÓA MẠNG (Whitelist)!");
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

        } catch (Exception e) {
            System.out.println("⚠️ Lỗi khóa mạng: " + e.getMessage());
        }
    }

    private static void unlockNetwork() {
        try {
            String[] cmd = {
                    "/bin/sh", "-c",
                    "iptables -P OUTPUT ACCEPT && iptables -F OUTPUT" // Phục hồi trạng thái mặc định
            };

            System.out.println("🔓 ĐÃ MỞ KHÓA MẠNG TOÀN BỘ!");
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

        } catch (Exception e) {
            System.out.println("⚠️ Lỗi mở mạng: " + e.getMessage());
        }
    }
}
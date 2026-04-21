package com.ly.system;

import java.net.InetAddress;
import java.net.URI;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.util.Base64;
 import javax.imageio.ImageIO;

public class OsCommand {

    // Biến lưu trữ link Ngrok hiện tại
    public static String currentServerUrl = "";

    // TÊN MIỀN TRANG WEB THI (Bạn có thể đổi tên miền trường bạn vào đây)com
    public static final String EXAM_DOMAIN = "lms.dut.udn.vn";
    public static String allowedUrls = "";
    // Sửa trong OsCommand.java
    public static String getSystemInfo(String msv, String hoTen, String caThiId) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return "REG|" + msv + "|" + hoTen + "|"
                    + localHost.getHostName() + "|"
                    + localHost.getHostAddress() + "|"
                    + caThiId;
        } catch (Exception e) {
            return "REG|" + msv + "|" + hoTen + "|Unknown|0.0.0.0|" + caThiId;
        }
    }

    // Thêm static field lưu URLs


    public static void execute(String command) {
        if (command.equals("BLOCK")) {
            lockNetwork(allowedUrls); // Dùng URLs đã lưu
        } else if (command.equals("ALLOW")) {
            unlockNetwork();
        }
    }

    public static void lockNetwork(String allowedUrlsCsv) {
        try {
            URI uri = new URI(currentServerUrl);
            String ngrokHost = uri.getHost();

            StringBuilder rules = new StringBuilder();

            // 1. Dập tắt hoàn toàn "cửa hậu" IPv6 (Puppy Linux / Linux nói chung)
            rules.append("ip6tables -P INPUT DROP && ");
            rules.append("ip6tables -P OUTPUT DROP && ");
            rules.append("ip6tables -P FORWARD DROP && ");

            // 2. Xóa luật cũ của IPv4 và mở localhost (để các dịch vụ nội bộ chạy bình thường)
            rules.append("iptables -F OUTPUT && ");
            rules.append("iptables -F INPUT && ");
            rules.append("iptables -A OUTPUT -o lo -j ACCEPT && ");
            rules.append("iptables -A INPUT -i lo -j ACCEPT && ");

            // 3. Cho phép DNS (cổng 53) để phân giải tên miền thành IP
            rules.append("iptables -A OUTPUT -p udp --dport 53 -j ACCEPT && ");
            rules.append("iptables -A OUTPUT -p tcp --dport 53 -j ACCEPT && ");
            rules.append("iptables -A INPUT -p udp --sport 53 -j ACCEPT && ");
            rules.append("iptables -A INPUT -p tcp --sport 53 -j ACCEPT && ");

            // 4. Phân giải và cấp phép cho trang web thi (LMS)
            if (allowedUrlsCsv != null && !allowedUrlsCsv.isEmpty()) {
                for (String domain : allowedUrlsCsv.split(",")) {
                    domain = domain.trim();
                    if (domain.isEmpty()) continue;

                    try {
                        java.net.InetAddress[] addresses = java.net.InetAddress.getAllByName(domain);
                        for (java.net.InetAddress addr : addresses) {
                            String ip = addr.getHostAddress();

                            // LỌC BỎ IPv6 (Ngăn lỗi "host/network not found")
                            if (ip.contains(":")) continue;

                            System.out.println("  → " + domain + " = " + ip);

                            // Mở đối xứng 2 chiều (đi và về) cho IP này
                            rules.append("iptables -A OUTPUT -d ").append(ip).append(" -j ACCEPT && ");
                            rules.append("iptables -A INPUT -s ").append(ip).append(" -j ACCEPT && ");
                        }
                    } catch (Exception e) {
                        System.out.println("  ⚠️ Không resolve được: " + domain);
                    }
                }
            }

            // 5. Phân giải và cấp phép cho Ngrok Server (giữ kết nối với hệ thống giám sát)
            try {
                java.net.InetAddress[] ngrokAddrs = java.net.InetAddress.getAllByName(ngrokHost);
                for (java.net.InetAddress addr : ngrokAddrs) {
                    String ip = addr.getHostAddress();

                    // LỌC BỎ IPv6
                    if (ip.contains(":")) continue;

                    System.out.println("  → ngrok: " + ngrokHost + " = " + ip);

                    // Mở đối xứng 2 chiều (đi và về)
                    rules.append("iptables -A OUTPUT -d ").append(ip).append(" -j ACCEPT && ");
                    rules.append("iptables -A INPUT -s ").append(ip).append(" -j ACCEPT && ");
                }
            } catch (Exception e) {
                System.out.println("  ⚠️ Không resolve được ngrok: " + ngrokHost);
            }

            // (Bước 6 đã được LƯỢC BỎ vì Puppy Linux không có sẵn module theo dõi trạng thái,
            // và chúng ta cũng đã tự mở đối xứng ở Bước 4 & Bước 5 nên không cần thiết nữa).

            // 7. KHÓA CHẶT TẤT CẢ CÁC KẾT NỐI CÒN LẠI (Chính sách mặc định)
            rules.append("iptables -P OUTPUT DROP && ");
            rules.append("iptables -P INPUT DROP");

            System.out.println("🔒 ĐANG KHÓA MẠNG...");

            // Chạy chuỗi lệnh
            String[] cmd = {"/bin/sh", "-c", rules.toString()};
            Process p = Runtime.getRuntime().exec(cmd);

            // Đọc luồng lỗi (nếu có)
            String stderr = new String(p.getErrorStream().readAllBytes());
            int exitCode = p.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Đã khóa mạng thành công! (Bản tối ưu cho Puppy Linux)");
            } else {
                System.out.println("❌ Lỗi khóa mạng (exit=" + exitCode + "): " + stderr);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Lỗi lockNetwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void unlockNetwork() {
        try {
            StringBuilder rules = new StringBuilder();

            // 1. Phục hồi trạng thái mặc định (ACCEPT) cho IPv4 và xóa sạch các luật cũ
            rules.append("iptables -P INPUT ACCEPT && ");
            rules.append("iptables -P OUTPUT ACCEPT && ");
            rules.append("iptables -P FORWARD ACCEPT && ");
            rules.append("iptables -F && ");

            // 2. Phục hồi trạng thái mặc định (ACCEPT) cho IPv6 và xóa sạch các luật cũ
            rules.append("ip6tables -P INPUT ACCEPT && ");
            rules.append("ip6tables -P OUTPUT ACCEPT && ");
            rules.append("ip6tables -P FORWARD ACCEPT && ");
            rules.append("ip6tables -F");

            String[] cmd = {"/bin/sh", "-c", rules.toString()};

            System.out.println("🔓 ĐANG MỞ KHÓA MẠNG...");
            Process p = Runtime.getRuntime().exec(cmd);

            // Đọc luồng lỗi để kiểm tra xem có sự cố gì không
            String stderr = new String(p.getErrorStream().readAllBytes());
            int exitCode = p.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ ĐÃ MỞ KHÓA MẠNG TOÀN BỘ TRỞ LẠI BÌNH THƯỜNG!");
            } else {
                System.out.println("❌ Lỗi mở mạng (exit=" + exitCode + "): " + stderr);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Lỗi unlockNetwork: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static String captureScreenBase64() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenImage = robot.createScreenCapture(screenRect);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Dùng định dạng JPG để dung lượng ảnh nhẹ, truyền qua mạng nhanh hơn
            ImageIO.write(screenImage, "jpg", baos);

            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
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
            rules.append("iptables -F OUTPUT && ");
            rules.append("iptables -F INPUT && ");
            rules.append("iptables -A OUTPUT -o lo -j ACCEPT && ");
            rules.append("iptables -A INPUT -i lo -j ACCEPT && ");

            // Cho phép DNS trước (cần để resolve được domain)
            rules.append("iptables -A OUTPUT -p udp --dport 53 -j ACCEPT && ");
            rules.append("iptables -A OUTPUT -p tcp --dport 53 -j ACCEPT && ");
            rules.append("iptables -A INPUT -p udp --sport 53 -j ACCEPT && ");
            rules.append("iptables -A INPUT -p tcp --sport 53 -j ACCEPT && ");

            // Resolve và thêm rule cho từng URL được phép
            if (allowedUrlsCsv != null && !allowedUrlsCsv.isEmpty()) {
                for (String domain : allowedUrlsCsv.split(",")) {
                    domain = domain.trim();
                    if (domain.isEmpty()) continue;

                    // Resolve tất cả IP của domain
                    try {
                        java.net.InetAddress[] addresses =
                                java.net.InetAddress.getAllByName(domain);
                        for (java.net.InetAddress addr : addresses) {
                            String ip = addr.getHostAddress();
                            System.out.println("  → " + domain + " = " + ip);
                            rules.append("iptables -A OUTPUT -d ").append(ip).append(" -j ACCEPT && ");
                            rules.append("iptables -A INPUT -s ").append(ip).append(" -j ACCEPT && ");
                        }
                    } catch (Exception e) {
                        System.out.println("  ⚠️ Không resolve được: " + domain);
                    }
                }
            }

            // Resolve và cho phép ngrok server
            try {
                java.net.InetAddress[] ngrokAddrs =
                        java.net.InetAddress.getAllByName(ngrokHost);
                for (java.net.InetAddress addr : ngrokAddrs) {
                    String ip = addr.getHostAddress();
                    System.out.println("  → ngrok: " + ngrokHost + " = " + ip);
                    rules.append("iptables -A OUTPUT -d ").append(ip).append(" -j ACCEPT && ");
                    rules.append("iptables -A INPUT -s ").append(ip).append(" -j ACCEPT && ");
                }
            } catch (Exception e) {
                System.out.println("  ⚠️ Không resolve được ngrok: " + ngrokHost);
            }

            // Cho phép các kết nối ESTABLISHED (response packets)
            rules.append("iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT && ");
            rules.append("iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT && ");

            // KHÓA TẤT CẢ CÒN LẠI
            rules.append("iptables -P OUTPUT DROP && ");
            rules.append("iptables -P INPUT DROP");

            System.out.println("🔒 ĐANG KHÓA MẠNG...");
            System.out.println("   Lệnh iptables: " + rules.toString());

            String[] cmd = {"/bin/sh", "-c", rules.toString()};
            Process p = Runtime.getRuntime().exec(cmd);

            // Đọc stderr để xem lỗi nếu có
            String stderr = new String(p.getErrorStream().readAllBytes());
            int exitCode = p.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Đã khóa mạng thành công!");
            } else {
                System.out.println("❌ Lỗi khóa mạng (exit=" + exitCode + "): " + stderr);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Lỗi lockNetwork: " + e.getMessage());
            e.printStackTrace();
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
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
            rules.append("iptables -A OUTPUT -o lo -j ACCEPT && ");
            rules.append("iptables -A OUTPUT -p udp --dport 53 -j ACCEPT && ");
            rules.append("iptables -A OUTPUT -p tcp --dport 53 -j ACCEPT && ");

            // Thêm rule cho từng URL được phép
            if (allowedUrlsCsv != null && !allowedUrlsCsv.isEmpty()) {
                for (String url : allowedUrlsCsv.split(",")) {
                    url = url.trim();
                    if (!url.isEmpty()) {
                        rules.append("iptables -A OUTPUT -d ").append(url).append(" -j ACCEPT && ");
                    }
                }
            }

            // Luôn cho phép ngrok server
            rules.append("iptables -A OUTPUT -d ").append(ngrokHost).append(" -j ACCEPT && ");
            rules.append("iptables -P OUTPUT DROP");

            String[] cmd = {"/bin/sh", "-c", rules.toString()};
            System.out.println("🔒 KÍCH HOẠT KHÓA MẠNG với " +
                    (allowedUrlsCsv != null ? allowedUrlsCsv.split(",").length : 0) + " URL được phép!");
            Runtime.getRuntime().exec(cmd).waitFor();

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
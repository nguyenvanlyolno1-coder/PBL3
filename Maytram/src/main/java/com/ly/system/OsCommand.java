package com.ly.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
public class OsCommand {

    public static String execute(String commandType) {
        try {
            // Chuẩn bị mảng chứa lệnh Terminal (Dùng bash cho môi trường Linux)
            String[] cmd = {"bash", "-c", ""};

            if (commandType.equals("BLOCK")) {
                System.out.println("⚙️ [System] KÍCH HOẠT CHẾ ĐỘ PHÒNG THI (Khóa mạng)...");
                // Lệnh echo mô phỏng việc cấu hình iptables
                cmd[2] = "echo 'Thực thi: iptables -F && iptables -A OUTPUT -p tcp -d 192.168.1.100 --dport 8080 -j ACCEPT && iptables -P OUTPUT DROP'";
            } else if (commandType.equals("ALLOW")) {
                System.out.println("⚙️ [System] HỦY CHẾ ĐỘ PHÒNG THI (Mở mạng)...");
                cmd[2] = "echo 'Thực thi: iptables -F && iptables -P OUTPUT ACCEPT'";
            } else {
                System.out.println("⚠️ [System] Lệnh không được hỗ trợ: " + commandType);
                return "";
            }

            // Yêu cầu Hệ điều hành chạy lệnh
            Process process = Runtime.getRuntime().exec(cmd);

            // Đọc kết quả mà Terminal trả về (giống như bạn đang gõ trên màn hình đen)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   > [Terminal Output]: " + line);
            }

            process.waitFor(); // Đợi lệnh chạy xong
            System.out.println("✅ [System] Đã thực thi xong lệnh cấp OS.");

        } catch (Exception e) {
            System.err.println("❌ [System] Lỗi khi gọi hệ điều hành: " + e.getMessage());
        }
        return "RESULT|" + commandType + "|SUCCESS";
    }
    public static String getSystemInfo() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return "REG|" + localHost.getHostName() + "|" + localHost.getHostAddress();
        } catch (Exception e) {
            return "REG|Unknown|0.0.0.0";
        }
    }
    // Sửa lại hàm getSystemInfo để nhận msv
    public static String getSystemInfo(String msv) {
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            return "REG|" + msv + " - " + localHost.getHostName() + "|" + localHost.getHostAddress();
        } catch (Exception e) {
            return "REG|" + msv + " - Unknown|0.0.0.0";
        }
    }
}
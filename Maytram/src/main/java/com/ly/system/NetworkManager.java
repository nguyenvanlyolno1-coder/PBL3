package com.ly.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class NetworkManager {

    public boolean hasIpAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();

            return ip != null
                    && !ip.isBlank()
                    && !ip.equals("127.0.0.1")
                    && !ip.equals("0.0.0.0");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canReachInternet() {
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL("https://www.google.com").openConnection();

            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            return code >= 200 && code < 500;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canReachServerConfig() {
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL(
                            "https://gist.githubusercontent.com/nguyenvanlyolno1-coder/905e9a98eceb65de1628c5c0b09d480d/raw/config.txt"
                    ).openConnection();

            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNetworkReady() {
        return hasIpAddress() && canReachServerConfig();
    }

    public void openNetworkSetupTool() {
        String[] commands = {
                "frisbee",
                "sns",
                "network-wizard",
                "connman-gtk",
                "nm-connection-editor"
        };

        for (String command : commands) {
            if (commandExists(command)) {
                runDetached(command);
                System.out.println("✅ Đã mở công cụ mạng: " + command);
                return;
            }
        }

        System.out.println("⚠️ Không tìm thấy công cụ cấu hình mạng phù hợp.");
        System.out.println("Bạn hãy mở Wi-Fi/network setup thủ công trên Puppy Linux.");
    }

    private boolean commandExists(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh", "-c", "command -v " + command
            });

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void runDetached(String command) {
        try {
            Runtime.getRuntime().exec(new String[]{
                    "/bin/sh", "-c", command + " >/dev/null 2>&1 &"
            });
        } catch (Exception e) {
            System.out.println("⚠️ Không mở được công cụ mạng: " + e.getMessage());
        }
    }

    public String getNetworkSummary() {
        StringBuilder sb = new StringBuilder();

        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh", "-c", "ip -4 addr show | grep -oP '(?<=inet\\s)\\d+(\\.\\d+){3}'"
            });

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("127.0.0.1")) {
                    sb.append("IP: ").append(line).append("\n");
                }
            }

            process.waitFor();
        } catch (Exception e) {
            sb.append("Không đọc được IP.\n");
        }

        if (sb.isEmpty()) {
            sb.append("Chưa có địa chỉ IP hợp lệ.\n");
        }

        return sb.toString();
    }
}
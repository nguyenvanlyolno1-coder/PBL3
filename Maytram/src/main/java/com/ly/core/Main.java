package com.ly.core;

import com.ly.model.ExamSessionInfo;
import com.ly.model.StudentInfo;
import com.ly.network.AgentWebSocket;
import com.ly.network.AuthApiClient;
import com.ly.network.ServerConfigLoader;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        SessionContext context = new SessionContext();
        context.setState(ClientState.BOOTING);

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=====================================");
            System.out.println("   HỆ THỐNG THI TRỰC TUYẾN DUT       ");
            System.out.println("=====================================");
            com.ly.system.NetworkManager networkManager = new com.ly.system.NetworkManager();

            context.setState(ClientState.NETWORK_SETUP);

            while (!networkManager.isNetworkReady()) {
                System.out.println("\n⚠️ Chưa có kết nối mạng hoặc chưa truy cập được máy chủ cấu hình.");
                System.out.println(networkManager.getNetworkSummary());

                System.out.println("1. Mở công cụ kết nối Wi-Fi/mạng");
                System.out.println("2. Kiểm tra lại");
                System.out.println("3. Thoát");
                System.out.print("▶ Chọn: ");

                String choice = scanner.nextLine().trim();

                if (choice.equals("1")) {
                    networkManager.openNetworkSetupTool();
                    System.out.println("Sau khi kết nối Wi-Fi xong, quay lại cửa sổ này và chọn kiểm tra lại.");
                } else if (choice.equals("2")) {
                    System.out.println("🔄 Đang kiểm tra lại mạng...");
                } else if (choice.equals("3")) {
                    System.out.println("Đã thoát.");
                    return;
                } else {
                    System.out.println("Lựa chọn không hợp lệ.");
                }
            }

            context.setState(ClientState.NETWORK_READY);
            System.out.println("✅ Mạng đã sẵn sàng.");
            ServerConfigLoader configLoader = new ServerConfigLoader();

            System.out.println("\n🔄 Đang lấy cấu hình máy chủ...");
            String wsUrl = configLoader.fetchWebSocketUrl();
            String httpUrl = configLoader.toHttpBaseUrl(wsUrl);

            context.setWsServerUrl(wsUrl);
            context.setHttpServerUrl(httpUrl);

            System.out.println("✅ WebSocket URL: " + wsUrl);
            System.out.println("✅ HTTP URL: " + httpUrl);

            context.setState(ClientState.LOGIN_SCREEN);

            System.out.print("\n▶ Tên đăng nhập: ");
            String username = scanner.nextLine().trim();

            System.out.print("▶ Mật khẩu: ");
            String password = scanner.nextLine().trim();

            AuthApiClient authApiClient = new AuthApiClient();

            System.out.println("\n🔄 Đang xác thực...");
            StudentInfo studentInfo = authApiClient.login(httpUrl, username, password);

            context.setStudentInfo(studentInfo);
            context.setState(ClientState.AUTHENTICATED);

            System.out.println("✅ Đăng nhập thành công!");
            System.out.println("👤 Sinh viên: " + studentInfo.getHoTen());
            System.out.println("🆔 Mã SV: " + studentInfo.getMaSinhVien());

            List<ExamSessionInfo> exams = studentInfo.getDanhSachCaThi();

            if (exams == null || exams.isEmpty()) {
                System.out.println("❌ Không có ca thi nào đang chuẩn bị.");
                return;
            }

            context.setState(ClientState.SELECTING_EXAM);

            System.out.println("\n📋 Danh sách ca thi:");
            for (int i = 0; i < exams.size(); i++) {
                System.out.println((i + 1) + ". " + exams.get(i));
            }

            System.out.print("\n▶ Chọn ca thi: ");
            int selectedIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (selectedIndex < 0 || selectedIndex >= exams.size()) {
                System.out.println("❌ Lựa chọn không hợp lệ.");
                return;
            }

            ExamSessionInfo selectedExam = exams.get(selectedIndex);
            context.setSelectedExam(selectedExam);
            context.setState(ClientState.WAITING_EXAM);

            System.out.println("\n✅ Đã chọn ca thi: " + selectedExam.getTenCaThi());
            System.out.println("🌐 URL được phép: " + selectedExam.getAllowedUrlsCsv());
            System.out.println("🔌 Đang kết nối WebSocket...");

            AgentWebSocket agent = new AgentWebSocket();
            agent.connectToServer(
                    context.getWsServerUrl(),
                    studentInfo.getMaSinhVien(),
                    studentInfo.getHoTen(),
                    String.valueOf(selectedExam.getCaThiId()),
                    selectedExam.getAllowedUrlsCsv()
            );

        } catch (Exception e) {
            context.setState(ClientState.ERROR);
            System.out.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
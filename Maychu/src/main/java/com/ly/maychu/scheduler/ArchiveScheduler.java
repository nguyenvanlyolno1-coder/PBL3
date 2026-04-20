package com.ly.maychu.scheduler;

import com.ly.maychu.model.Screenshot;
import com.ly.maychu.repository.ScreenshotRepository;
import com.ly.maychu.service.GoogleDriveService;
import com.ly.maychu.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ArchiveScheduler {

    @Autowired private ScreenshotRepository screenshotRepo;
    @Autowired private StorageService storageService;
    @Autowired private GoogleDriveService driveService;

    /**
     * Chạy mỗi ngày lúc 2:00 AM
     * Quét ảnh cũ hơn 24h → nén → upload Drive → xóa local
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void archiveOldScreenshots() {
        System.out.println("🗓️ [Scheduler] Bắt đầu archive ảnh cũ...");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        // Tìm screenshot cũ hơn 24h
        List<Screenshot> oldScreenshots = screenshotRepo.findByCreatedAtBefore(cutoff);
        if (oldScreenshots.isEmpty()) {
            System.out.println("✅ Không có ảnh cũ cần archive.");
            return;
        }

        // Nhóm theo ca thi
        Map<Long, List<Screenshot>> byCtct = oldScreenshots.stream()
                .collect(Collectors.groupingBy(
                        sc -> sc.getChiTietCaThi().getCaThi().getId()
                ));

        for (Map.Entry<Long, List<Screenshot>> entry : byCtct.entrySet()) {
            Long caThiId = entry.getKey();
            try {
                System.out.println("📦 Đang nén ca thi: " + caThiId);

                // Nén thư mục
                File zipFile = storageService.zipFolder(String.valueOf(caThiId));

                // Upload lên Drive
                driveService.uploadZip(zipFile);

                // Xóa file vật lý local
                storageService.deleteFolder(String.valueOf(caThiId));
                zipFile.delete();

                System.out.println("✅ Archive xong ca thi: " + caThiId);

            } catch (Exception e) {
                System.out.println("❌ Lỗi archive ca thi " + caThiId + ": " + e.getMessage());
            }
        }
    }
}
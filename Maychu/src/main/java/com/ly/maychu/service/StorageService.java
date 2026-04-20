package com.ly.maychu.service;

import com.ly.maychu.model.*;
import com.ly.maychu.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class StorageService {

    // Cấu hình trong application.properties
    @Value("${storage.upload-dir:/opt/examguard/uploads}")
    private String uploadDir;

    @Autowired
    private ScreenshotRepository screenshotRepo;

    @Autowired
    private ChiTietCaThiRepository chiTietCaThiRepo;

    /**
     * Nhận Base64 từ MayTram, lưu file vật lý, ghi DB
     * @param maSinhVien  username của SV
     * @param caThiId     ID ca thi
     * @param base64Data  chuỗi Base64 của ảnh JPG
     * @return đường dẫn web để hiển thị ảnh
     */
    public String saveScreenshot(String maSinhVien, Long caThiId, String base64Data) {
        try {
            // 1. Tạo thư mục theo ca thi
            String folderName = "cathi_" + caThiId;
            Path folder = Paths.get(uploadDir, folderName);
            Files.createDirectories(folder);

            // 2. Tên file = maSV_timestamp.jpg
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = maSinhVien + "_" + timestamp + ".jpg";
            Path filePath = folder.resolve(fileName);

            // 3. Decode Base64 → ghi file
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Files.write(filePath, imageBytes);

            // 4. Đường dẫn tương đối để Spring Boot serve static
            String relativePath = "/uploads/" + folderName + "/" + fileName;

            // 5. Ghi vào DB
            chiTietCaThiRepo.findById(caThiId).ifPresent(ctct -> {
                Screenshot sc = new Screenshot();
                sc.setChiTietCaThi(ctct);
                sc.setFilePath(relativePath);
                sc.setCreatedAt(LocalDateTime.now());
                screenshotRepo.save(sc);
            });

            System.out.println("📸 Đã lưu ảnh: " + relativePath);
            return relativePath;

        } catch (Exception e) {
            System.out.println("⚠️ Lỗi lưu ảnh: " + e.getMessage());
            return null;
        }
    }

    /**
     * Nén toàn bộ thư mục ca thi thành file .zip
     */
    public File zipFolder(String caThiId) throws IOException {
        Path sourceDir = Paths.get(uploadDir, "cathi_" + caThiId);
        File zipFile = new File(uploadDir, "cathi_" + caThiId + ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos)) {

            Files.walk(sourceDir).filter(p -> !Files.isDirectory(p)).forEach(p -> {
                try {
                    String entryName = sourceDir.relativize(p).toString();
                    zos.putNextEntry(new java.util.zip.ZipEntry(entryName));
                    Files.copy(p, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    System.out.println("⚠️ Lỗi nén: " + p);
                }
            });
        }
        return zipFile;
    }

    /**
     * Xóa thư mục ca thi sau khi đã upload lên Drive
     */
    public void deleteFolder(String caThiId) throws IOException {
        Path folder = Paths.get(uploadDir, "cathi_" + caThiId);
        Files.walk(folder)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.delete(p); }
                    catch (IOException e) { System.out.println("⚠️ Không xóa được: " + p); }
                });
    }
}
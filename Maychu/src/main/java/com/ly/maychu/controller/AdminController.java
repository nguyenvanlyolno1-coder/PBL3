package com.ly.maychu.controller;

import com.ly.maychu.model.CaThi;
import com.ly.maychu.model.NguoiDung;
import com.ly.maychu.model.WhitelistUrl;
import com.ly.maychu.repository.CaThiRepository;
import com.ly.maychu.repository.NguoiDungRepository;
import com.ly.maychu.repository.WhitelistUrlRepository;
import com.ly.maychu.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private NguoiDungRepository nguoiDungRepo;
    @Autowired private AuthService authService;
    @Autowired
    private WhitelistUrlRepository whitelistRepo;
    @Autowired
    private CaThiRepository caThiRepo;
    // Kiểm tra quyền admin/quan_tri
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_QUAN_TRI"));
    }

    // Lấy danh sách tất cả thành viên
    @GetMapping("/members")
    public ResponseEntity<List<Map<String, Object>>> getDanhSachThanhVien(Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        List<NguoiDung> all = nguoiDungRepo.findAll();
        List<Map<String, Object>> result = all.stream().map(nd -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", nd.getId());
            m.put("ten", nd.getTen());
            m.put("username", nd.getUsername());
            m.put("vaiTro", nd.getVaiTro());
            m.put("email", nd.getEmail());
            m.put("soDienThoai", nd.getSoDienThoai());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // Lấy thông tin 1 thành viên
    @GetMapping("/members/{id}")
    public ResponseEntity<Map<String, Object>> getThanhVien(
            @PathVariable Long id, Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        NguoiDung nd = nguoiDungRepo.findById(id).orElse(null);
        if (nd == null) return ResponseEntity.notFound().build();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", nd.getId());
        m.put("ten", nd.getTen());
        m.put("username", nd.getUsername());
        m.put("vaiTro", nd.getVaiTro());
        m.put("email", nd.getEmail() != null ? nd.getEmail() : "");
        m.put("soDienThoai", nd.getSoDienThoai() != null ? nd.getSoDienThoai() : "");
        return ResponseEntity.ok(m);
    }

    // Cập nhật thông tin thành viên (trừ mật khẩu)
    @PutMapping("/members/{id}")
    public ResponseEntity<Map<String, Object>> capNhatThongTin(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        NguoiDung nd = nguoiDungRepo.findById(id).orElse(null);
        if (nd == null) return ResponseEntity.notFound().build();

        if (body.containsKey("ten") && !body.get("ten").isBlank())
            nd.setTen(body.get("ten"));
        if (body.containsKey("email"))
            nd.setEmail(body.get("email"));
        if (body.containsKey("soDienThoai"))
            nd.setSoDienThoai(body.get("soDienThoai"));

        nguoiDungRepo.save(nd);
        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công!"));
    }

    // Đặt lại mật khẩu
    @PostMapping("/members/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetMatKhau(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        String matKhauMoi = body.get("matKhauMoi");
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mật khẩu phải có ít nhất 6 ký tự!"));
        }

        try {
            authService.doiMatKhau(id, matKhauMoi);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đặt lại mật khẩu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    // ===== TẠO TÀI KHOẢN =====

    @PostMapping("/members/create")
    public ResponseEntity<Map<String, Object>> taoTaiKhoan(
            @RequestBody Map<String, String> body, Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        String username = body.getOrDefault("username", "").trim();
        String matKhau  = body.getOrDefault("matKhau", "").trim();
        String ten      = body.getOrDefault("ten", "").trim();
        String vaiTro   = body.getOrDefault("vaiTro", "").trim();

        if (username.isBlank() || matKhau.length() < 6 || ten.isBlank() || vaiTro.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Thiếu thông tin hoặc mật khẩu < 6 ký tự!"));

        if (nguoiDungRepo.findByUsername(username).isPresent())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Username '" + username + "' đã tồn tại!"));

        NguoiDung nd = new NguoiDung();
        nd.setUsername(username);
        nd.setPass(matKhau);          // taoNguoiDung sẽ encode, chỉ set 1 lần ở đây
        nd.setTen(ten);
        nd.setVaiTro(vaiTro.toUpperCase());
        nd.setEmail(body.getOrDefault("email", ""));
        nd.setSoDienThoai(body.getOrDefault("soDienThoai", ""));
        authService.taoNguoiDung(nd); // Chỉ gọi 1 lần duy nhất

        return ResponseEntity.ok(Map.of("success", true, "message", "Tạo tài khoản thành công!"));
    }
    // Import tài khoản từ file Excel/CSV
    @PostMapping("/members/import")
    public ResponseEntity<Map<String, Object>> importTaiKhoan(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        List<String> errors   = new java.util.ArrayList<>();
        List<String> success  = new java.util.ArrayList<>();
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try {
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                processExcelTaiKhoan(file, success, errors);
            } else if (fileName.endsWith(".csv")) {
                processCsvTaiKhoan(file, success, errors);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Chỉ hỗ trợ file .xlsx hoặc .csv!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Lỗi xử lý file: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "taoMoi", success.size(),
                "loiList", errors,
                "message", String.format("Tạo thành công %d tài khoản, %d lỗi.", success.size(), errors.size())
        ));
    }

    private void processExcelTaiKhoan(org.springframework.web.multipart.MultipartFile file,
                                      List<String> success, List<String> errors) throws Exception {
        try (org.apache.poi.ss.usermodel.Workbook wb =
                     org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua header row 0
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String username = getCellString(row, 0);
                    String matKhau  = getCellString(row, 1);
                    String ten      = getCellString(row, 2);
                    String vaiTro   = getCellString(row, 3);
                    String email    = getCellString(row, 4);
                    String sdt      = getCellString(row, 5);
                    saveTaiKhoan(username, matKhau, ten, vaiTro, email, sdt, success, errors, i + 1);
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
    }

    private void processCsvTaiKhoan(org.springframework.web.multipart.MultipartFile file,
                                    List<String> success, List<String> errors) throws Exception {
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line; int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                if (row == 1) continue; // Bỏ header
                String[] parts = line.split(",", -1);
                if (parts.length < 4) { errors.add("Dòng " + row + ": Thiếu cột"); continue; }
                saveTaiKhoan(
                        parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
                        parts.length > 4 ? parts[4].trim() : "",
                        parts.length > 5 ? parts[5].trim() : "",
                        success, errors, row
                );
            }
        }
    }

    private void saveTaiKhoan(String username, String matKhau, String ten, String vaiTro,
                              String email, String sdt,
                              List<String> success, List<String> errors, int row) {
        if (username.isBlank() || matKhau.isBlank() || ten.isBlank() || vaiTro.isBlank()) {
            errors.add("Dòng " + row + ": Thiếu thông tin bắt buộc (username/matKhau/ten/vaiTro)");
            return;
        }
        if (nguoiDungRepo.findByUsername(username).isPresent()) {
            errors.add("Dòng " + row + ": Username '" + username + "' đã tồn tại");
            return;
        }
        NguoiDung nd = new NguoiDung();
        nd.setUsername(username);
        nd.setPass(matKhau); // sẽ được encode trong taoNguoiDung
        nd.setTen(ten);
        nd.setVaiTro(vaiTro.toUpperCase());
        nd.setEmail(email);
        nd.setSoDienThoai(sdt);
        authService.taoNguoiDung(nd);
        success.add(username);
    }

    private String getCellString(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

// ===== TẠO CA THI =====

    @Autowired private com.ly.maychu.repository.CaThiRepository caThiRepository;
    @Autowired private com.ly.maychu.repository.ChiTietCaThiRepository chiTietCaThiRepository;

    // Lấy danh sách giảng viên (để chọn khi tạo ca thi)
    @GetMapping("/giang-vien")
    public ResponseEntity<List<Map<String, Object>>> getDanhSachGiangVien(Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        List<NguoiDung> gvList = nguoiDungRepo.findAll().stream()
                .filter(nd -> nd.getVaiTro().equals("GIANG_VIEN") || nd.getVaiTro().equals("QUAN_TRI"))
                .toList();

        List<Map<String, Object>> result = gvList.stream().map(gv -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", gv.getId());
            m.put("ten", gv.getTen());
            m.put("username", gv.getUsername());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // Tạo ca thi + import danh sách SV từ file
    @PostMapping("/cathi/create")
    public ResponseEntity<Map<String, Object>> taoCaThi(
            @RequestParam("tenCaThi")   String tenCaThi,
            @RequestParam("ngayGio")    String ngayGio,
            @RequestParam("thoiGian")   Integer thoiGian,
            @RequestParam("giangVienId") Long giangVienId,
            @RequestParam("fileSV")     org.springframework.web.multipart.MultipartFile fileSV,
            Authentication auth) {
        if (!isAdmin(auth)) return ResponseEntity.status(403).build();

        // Tạo ca thi
        NguoiDung gv = nguoiDungRepo.findById(giangVienId).orElse(null);
        if (gv == null) return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Giảng viên không tồn tại!"));

        com.ly.maychu.model.CaThi caThi = new com.ly.maychu.model.CaThi();
        caThi.setTenCaThi(tenCaThi);
        caThi.setNgayGio(java.time.LocalDateTime.parse(ngayGio));
        caThi.setThoiGian(thoiGian);
        caThi.setGiangVien(gv);
        caThi.setTrangThai("CHUAN_BI");
        com.ly.maychu.model.CaThi saved = caThiRepository.save(caThi);

        // Import danh sách SV từ file
        List<String> errors  = new java.util.ArrayList<>();
        List<String> success = new java.util.ArrayList<>();
        String fileName = fileSV.getOriginalFilename() != null ? fileSV.getOriginalFilename().toLowerCase() : "";

        try {
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                importSVFromExcel(fileSV, saved, success, errors);
            } else if (fileName.endsWith(".csv")) {
                importSVFromCsv(fileSV, saved, success, errors);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File danh sách SV phải là .xlsx hoặc .csv!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Lỗi đọc file SV: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "caThiId", saved.getId(),
                "svThemVao", success.size(),
                "loiList", errors,
                "message", String.format("Tạo ca thi thành công! Đã thêm %d sinh viên, %d lỗi.", success.size(), errors.size())
        ));
    }

    private void importSVFromExcel(org.springframework.web.multipart.MultipartFile file,
                                   com.ly.maychu.model.CaThi caThi,
                                   List<String> success, List<String> errors) throws Exception {
        try (org.apache.poi.ss.usermodel.Workbook wb =
                     org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                String maSV = getCellString(row, 0); // Cột A: mã SV (= username)
                addSVToCaThi(maSV, caThi, success, errors, i + 1);
            }
        }
    }

    private void importSVFromCsv(org.springframework.web.multipart.MultipartFile file,
                                 com.ly.maychu.model.CaThi caThi,
                                 List<String> success, List<String> errors) throws Exception {
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line; int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                if (row == 1) continue;
                String maSV = line.split(",")[0].trim();
                addSVToCaThi(maSV, caThi, success, errors, row);
            }
        }
    }

    private void addSVToCaThi(String maSV, com.ly.maychu.model.CaThi caThi,
                              List<String> success, List<String> errors, int row) {
        if (maSV.isBlank()) return;
        NguoiDung sv = nguoiDungRepo.findByUsername(maSV).orElse(null);
        if (sv == null) {
            errors.add("Dòng " + row + ": Không tìm thấy SV '" + maSV + "'");
            return;
        }
        if (!sv.getVaiTro().equals("SINH_VIEN")) {
            errors.add("Dòng " + row + ": '" + maSV + "' không phải sinh viên");
            return;
        }
        // Kiểm tra đã thêm chưa
        if (chiTietCaThiRepository.findBySinhVienAndCaThi(sv, caThi).isPresent()) {
            errors.add("Dòng " + row + ": '" + maSV + "' đã có trong ca thi");
            return;
        }
        com.ly.maychu.model.ChiTietCaThi ctct = new com.ly.maychu.model.ChiTietCaThi();
        ctct.setSinhVien(sv);
        ctct.setCaThi(caThi);
        chiTietCaThiRepository.save(ctct);
        success.add(maSV);
    }


    // Lấy danh sách URL của ca thi
    @GetMapping("/cathi/{id}/whitelist")
    public ResponseEntity<List<Map<String, Object>>> getWhitelist(@PathVariable Long id) {
        CaThi ca = caThiRepo.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();

        List<Map<String, Object>> result = whitelistRepo.findByCaThi(ca).stream()
                .map(w -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", w.getId());
                    m.put("url", w.getUrl());
                    m.put("mota", w.getMota());
                    return m;
                }).toList();

        return ResponseEntity.ok(result);
    }

    // Thêm URL mới
    @PostMapping("/cathi/{id}/whitelist")
    public Map<String, Object> themUrl(@PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        try {
            CaThi ca = caThiRepo.findById(id).orElseThrow();
            WhitelistUrl w = new WhitelistUrl();
            w.setCaThi(ca);
            w.setUrl(body.get("url").trim());
            w.setMota(body.getOrDefault("mota", ""));
            whitelistRepo.save(w);
            return Map.of("success", true, "message", "Đã thêm URL!");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // Xóa URL
    @DeleteMapping("/cathi/{caThiId}/whitelist/{urlId}")
    public Map<String, Object> xoaUrl(@PathVariable Long caThiId,
                                      @PathVariable Long urlId) {
        try {
            whitelistRepo.deleteById(urlId);
            return Map.of("success", true, "message", "Đã xóa URL!");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
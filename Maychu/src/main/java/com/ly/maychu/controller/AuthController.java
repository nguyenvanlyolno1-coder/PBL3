package com.ly.maychu.controller;

import com.ly.maychu.model.CaThi;
import com.ly.maychu.model.ChiTietCaThi;
import com.ly.maychu.model.NguoiDung;
import com.ly.maychu.model.WhitelistUrl;
import com.ly.maychu.repository.CaThiRepository;
import com.ly.maychu.repository.WhitelistUrlRepository;
import com.ly.maychu.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private com.ly.maychu.repository.ChiTietCaThiRepository chiTietRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private WhitelistUrlRepository whitelistRepo;
    @Autowired
    private CaThiRepository caThiRepository;


    @PostMapping("/student")
    public ResponseEntity<Map<String, Object>> xacThucSinhVien(
            @RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        Optional<NguoiDung> opt = authService.xacThucSinhVien(username, password);
        if (opt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Sai tài khoản hoặc mật khẩu!"));
        }

        NguoiDung sv = opt.get();

        // Lấy danh sách ca thi SV có trong ChiTietCaThi + đang CHUAN_BI hoặc DANG_THI
        List<ChiTietCaThi> danhSachCtct = chiTietRepo.findBySinhVien(sv);
        List<Map<String, Object>> danhSachCaThi = danhSachCtct.stream()
                .map(ChiTietCaThi::getCaThi)
                .filter(ca -> ca.getTrangThai().equals("CHUAN_BI"))
                // Sửa phần map ca thi trong /api/auth/student
                .map(ca -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("caThiId", ca.getId());
                    m.put("tenCaThi", ca.getTenCaThi());
                    m.put("ngayGio", ca.getNgayGio().toString());
                    m.put("trangThai", ca.getTrangThai());

                    // THÊM: Lấy danh sách URL được phép
                    List<String> urls = whitelistRepo.findByCaThi(ca)
                            .stream().map(WhitelistUrl::getUrl).toList();
                    m.put("allowedUrls", urls);

                    return m;
                }).toList();
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("svId", sv.getId());
        response.put("hoTen", sv.getTen());
        response.put("maSinhVien", sv.getUsername());
        response.put("danhSachCaThi", danhSachCaThi);

        return ResponseEntity.ok(response);
    }
}
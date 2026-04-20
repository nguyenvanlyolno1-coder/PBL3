//package com.ly.maychu.controller;
//
//import com.ly.maychu.model.CaThi;
//import com.ly.maychu.model.NguoiDung;
//import com.ly.maychu.repository.CaThiRepository;
//import com.ly.maychu.service.AuthService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//
//@Controller
//public class DashboardController {
//
//    @Autowired
//    private AuthService authService;
//
//    @Autowired
//    private CaThiRepository caThiRepository;
//
//    @GetMapping("/login")
//    public String loginPage() {
//        return "login"; // trỏ tới templates/login.html
//    }
//
//    @GetMapping("/dashboard")
//    public String dashboard(Authentication auth, Model model,
//                            @RequestParam(required = false) Long caThiId) {
//
//        // Lấy thông tin GV đang đăng nhập
//        NguoiDung giangVien = authService.findByUsername(auth.getName()).orElseThrow();
//
//        // Load danh sách ca thi của GV này
//        List<CaThi> danhSachCaThi = caThiRepository.findByGiangVien(giangVien);
//        model.addAttribute("danhSachCaThi", danhSachCaThi);
//
//        // Ca thi đang được chọn
//        if (caThiId != null) {
//            CaThi caThiHienTai = caThiRepository.findById(caThiId).orElse(null);
//            model.addAttribute("caThiHienTai", caThiHienTai);
//        }
//
//        return "dashboard"; // trỏ tới templates/dashboard.html
//    }
//}
package com.ly.maychu.controller;

import com.ly.maychu.model.CaThi;
import com.ly.maychu.model.NguoiDung;
import com.ly.maychu.repository.CaThiRepository;
import com.ly.maychu.repository.ChiTietCaThiRepository;
import com.ly.maychu.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired private AuthService authService;
    @Autowired private CaThiRepository caThiRepo;
    @Autowired private ChiTietCaThiRepository chiTietRepo;
    // THÊM ĐOẠN NÀY
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    // Trang 1: Danh sách ca thi
    @GetMapping("/dashboard")
    public String trangDanhSachCaThi(Authentication auth, Model model) {
        NguoiDung gv = authService.findByUsername(auth.getName()).orElseThrow();
        List<CaThi> danhSach = caThiRepo
                .findByGiangVienAndTrangThaiIn(gv, List.of("CHUAN_BI", "DANG_THI"));
        model.addAttribute("danhSachCaThi", danhSach);

        // Truyền flag isAdmin để Thymeleaf hiện/ẩn tab quản lý
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_QUAN_TRI"));
        model.addAttribute("isAdmin", isAdmin);

        return "dashboard";
    }

    // Trang 2: Màn hình phòng thi
    @GetMapping("/dashboard/{caThiId}")
    public String trangPhongThi(@PathVariable Long caThiId,
                                Authentication auth, Model model) {
        NguoiDung gv = authService.findByUsername(auth.getName()).orElseThrow();
        CaThi caThi = caThiRepo.findById(caThiId).orElseThrow();

        // Đếm tổng SV trong ca thi
        long tongSV = chiTietRepo.countByCaThi(caThi);

        model.addAttribute("caThi", caThi);
        model.addAttribute("tongSV", tongSV);
        return "phong-thi"; // templates/phong-thi.html
    }
}
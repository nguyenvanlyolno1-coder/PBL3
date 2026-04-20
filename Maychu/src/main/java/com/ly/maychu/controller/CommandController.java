package com.ly.maychu.controller;

import com.ly.maychu.handler.MonitorHandler;
import com.ly.maychu.model.CaThi;
import com.ly.maychu.model.ChiTietCaThi;
import com.ly.maychu.model.ClientInfo;
import com.ly.maychu.model.NguoiDung;
import com.ly.maychu.repository.CaThiRepository;
import com.ly.maychu.repository.ChiTietCaThiRepository;
import com.ly.maychu.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommandController {
    public static java.util.concurrent.ConcurrentHashMap<String, String> imageStore = new java.util.concurrent.ConcurrentHashMap<>();
    @Autowired
    private MonitorHandler monitorHandler;
    @Autowired private CaThiRepository caThiRepo;
    @Autowired private ChiTietCaThiRepository chiTietRepo;
    @Autowired private NguoiDungRepository nguoiDungRepo;
    @Autowired private com.ly.maychu.repository.CanhBaoRepository canhBaoRepo;
    @Autowired private com.ly.maychu.repository.LichSuKetNoiRepository lichSuRepo;
    // Đường dẫn: http://localhost:8080/api/send?cmd=BLOCK
    @GetMapping("/send")
    public String sendCommand(@RequestParam String cmd) {
        monitorHandler.broadcast(cmd);
        return "Đã gửi lệnh: " + cmd + " đến tất cả máy trạm!";
    }
    @GetMapping("/image")
    public String getImage(@RequestParam String id) {
        String value = imageStore.getOrDefault(id, "");
        imageStore.remove(id);
        return value; // Có thể là URL (/uploads/...) hoặc Base64
    }
    // Đường dẫn: http://localhost:8080/api/status
    @GetMapping("/status")
    public String getStatus() {
        int count = monitorHandler.getConnectedCount();
        return String.valueOf(count); // Trả về con số dưới dạng chuỗi
    }

    @GetMapping("/status-all")
    public java.util.Map<String, Object> getStatusAll() {
        return monitorHandler.getFullStatus();
    }
    // Đường dẫn: http://localhost:8080/api/send-to?id=xyz&cmd=SCREENSHOT
    @GetMapping("/send-to")
    public String sendToClient(@RequestParam String id, @RequestParam String cmd) {
        boolean success = monitorHandler.sendToClient(id, cmd);
        if (success) {
            return "Đã gửi lệnh [" + cmd + "] thành công đến máy: " + id;
        } else {
            return "Thất bại: Máy không tồn tại hoặc đã mất kết nối!";
        }
    }
    // GV bấm "Bắt đầu thi"
    @PostMapping("/cathi/{id}/batdauthi")
    public ResponseEntity<String> batDauThi(@PathVariable Long id) {
        CaThi ca = caThiRepo.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();

        // Đổi trạng thái
        ca.setTrangThai("DANG_THI");
        caThiRepo.save(ca);

        // Gửi lệnh LOCK xuống tất cả máy trạm
        monitorHandler.broadcast("BLOCK");

        return ResponseEntity.ok("Đã bắt đầu thi và khóa mạng!");
    }

    // Lấy danh sách SV trong ca thi + trạng thái online/offline
    @GetMapping("/cathi/{id}/danhsach-sv")
    public ResponseEntity<List<Map<String, Object>>> getDanhSachSV(@PathVariable Long id) {
        com.ly.maychu.model.CaThi ca = caThiRepo.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();

        List<com.ly.maychu.model.ChiTietCaThi> danhSach = chiTietRepo.findByCaThi(ca);
        Map<String, com.ly.maychu.model.ClientInfo> clientMap = monitorHandler.getClientMap();

        List<Map<String, Object>> result = danhSach.stream().map(ctct -> {
            Map<String, Object> item = new java.util.HashMap<>();
            com.ly.maychu.model.NguoiDung sv = ctct.getSinhVien();
            item.put("maSinhVien", sv.getUsername());
            item.put("hoTen", sv.getTen());
//            item.put("trangThaiCaThi", ctct.getTrangThai());

            com.ly.maychu.model.ClientInfo online = clientMap.get(sv.getUsername());
            item.put("online", online != null);
            item.put("lastSeen", online != null ? online.getLastSeen() : null);
            return item;
        }).toList();

        return ResponseEntity.ok(result);
    }
    // Kết thúc ca thi
    @PostMapping("/cathi/{id}/ketthuc")
    public ResponseEntity<String> ketThucThi(@PathVariable Long id) {
        CaThi ca = caThiRepo.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();

        ca.setTrangThai("KET_THUC");
        caThiRepo.save(ca);

        // Gửi lệnh ALLOW để mở mạng lại cho các máy
        monitorHandler.broadcast("ALLOW");

        return ResponseEntity.ok("Ca thi đã kết thúc!");
    }

    // Lịch sử kết nối của ca thi
    @GetMapping("/cathi/{id}/lichsu-ketnoi")
    public ResponseEntity<List<Map<String, Object>>> getLichSuKetNoi(@PathVariable Long id) {
        CaThi ca = caThiRepo.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();

        List<ChiTietCaThi> danhSach = chiTietRepo.findByCaThi(ca);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (ChiTietCaThi ctct : danhSach) {
            List<com.ly.maychu.model.LichSuKetNoi> logs = lichSuRepo.findByChiTietCaThi(ctct);
            for (com.ly.maychu.model.LichSuKetNoi log : logs) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("hoTen", ctct.getSinhVien().getTen());
                item.put("maSinhVien", ctct.getSinhVien().getUsername());
                item.put("loaiSuKien", log.getLoaiSuKien());
                item.put("thoiGian", log.getThoiGian().toString());
                item.put("ghiChu", log.getGhiChu());
                result.add(item);
            }
        }

        // Sắp xếp mới nhất lên đầu
        result.sort((a, b) -> b.get("thoiGian").toString().compareTo(a.get("thoiGian").toString()));
        return ResponseEntity.ok(result);
    }

    // Lịch sử vi phạm (CanhBao) của ca thi
    @GetMapping("/cathi/{id}/lichsu-vi-pham")
    public ResponseEntity<List<Map<String, Object>>> getLichSuViPham(@PathVariable Long id) {
        CaThi ca = caThiRepo.findById(id).orElse(null);
        if (ca == null) return ResponseEntity.notFound().build();

        List<ChiTietCaThi> danhSach = chiTietRepo.findByCaThi(ca);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (ChiTietCaThi ctct : danhSach) {
            List<com.ly.maychu.model.CanhBao> cbs = canhBaoRepo.findByChiTietCaThi(ctct);
            for (com.ly.maychu.model.CanhBao cb : cbs) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("hoTen", ctct.getSinhVien().getTen());
                item.put("maSinhVien", ctct.getSinhVien().getUsername());
                item.put("loaiCanhBao", cb.getLoaiCanhBao());
                item.put("thoiGian", cb.getThoiGian().toString());
                item.put("moTa", cb.getMoTa());
                result.add(item);
            }
        }

        result.sort((a, b) -> b.get("thoiGian").toString().compareTo(a.get("thoiGian").toString()));
        return ResponseEntity.ok(result);
    }

    // Ca thi đã kết thúc của GV đang đăng nhập
    @GetMapping("/cathi/da-ket-thuc")
    public ResponseEntity<List<Map<String, Object>>> getCaThiDaKetThuc(
            org.springframework.security.core.Authentication auth) {
        com.ly.maychu.model.NguoiDung gv = nguoiDungRepo.findByUsername(auth.getName()).orElse(null);
        if (gv == null) return ResponseEntity.status(401).build();

        List<CaThi> danhSach = caThiRepo.findByGiangVienAndTrangThaiIn(gv, List.of("KET_THUC"));
        List<Map<String, Object>> result = danhSach.stream().map(ca -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", ca.getId());
            m.put("tenCaThi", ca.getTenCaThi());
            m.put("ngayGio", ca.getNgayGio().toString());
            m.put("thoiGian", ca.getThoiGian());
            m.put("tongSV", chiTietRepo.countByCaThi(ca));
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }
}
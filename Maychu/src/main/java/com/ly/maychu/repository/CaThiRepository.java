package com.ly.maychu.repository;

import com.ly.maychu.model.CaThi;
import com.ly.maychu.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CaThiRepository extends JpaRepository<CaThi, Long> {
    List<CaThi> findByGiangVien(NguoiDung giangVien);
    List<CaThi> findByGiangVienAndTrangThaiIn(NguoiDung gv, List<String> trangThai);
}
package com.ly.maychu.repository;

import com.ly.maychu.model.ChiTietCaThi;
import com.ly.maychu.model.NguoiDung;
import com.ly.maychu.model.CaThi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChiTietCaThiRepository extends JpaRepository<ChiTietCaThi, Long> {
    Optional<ChiTietCaThi> findBySinhVienAndCaThi(NguoiDung sv, CaThi ca);
    long countByCaThi(CaThi caThi);
    List<ChiTietCaThi> findByCaThi(CaThi caThi);
    List<ChiTietCaThi> findBySinhVien(NguoiDung sv);

}
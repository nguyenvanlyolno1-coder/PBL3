package com.ly.maychu.repository;

import com.ly.maychu.model.LichSuKetNoi;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ly.maychu.model.ChiTietCaThi;
import java.util.List;
public interface LichSuKetNoiRepository extends JpaRepository<LichSuKetNoi, Long> {
    List<LichSuKetNoi> findByChiTietCaThi(ChiTietCaThi chiTietCaThi);
}
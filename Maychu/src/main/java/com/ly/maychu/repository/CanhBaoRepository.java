package com.ly.maychu.repository;

import com.ly.maychu.model.CanhBao;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ly.maychu.model.ChiTietCaThi;
import java.util.List;
public interface CanhBaoRepository extends JpaRepository<CanhBao, Long> {
    List<CanhBao> findByChiTietCaThi(ChiTietCaThi chiTietCaThi);
}
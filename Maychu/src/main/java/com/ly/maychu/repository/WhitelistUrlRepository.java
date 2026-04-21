package com.ly.maychu.repository;

import com.ly.maychu.model.CaThi;
import com.ly.maychu.model.WhitelistUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WhitelistUrlRepository extends JpaRepository<WhitelistUrl, Long> {
    List<WhitelistUrl> findByCaThi(CaThi caThi);
    void deleteByCaThiAndId(CaThi caThi, Long id);
}
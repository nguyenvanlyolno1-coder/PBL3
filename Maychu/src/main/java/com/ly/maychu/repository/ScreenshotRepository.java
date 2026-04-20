package com.ly.maychu.repository;

import com.ly.maychu.model.Screenshot;
import com.ly.maychu.model.ChiTietCaThi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ScreenshotRepository extends JpaRepository<Screenshot, Long> {
    List<Screenshot> findByChiTietCaThi(ChiTietCaThi ctct);
    List<Screenshot> findByCreatedAtBefore(LocalDateTime time);
}
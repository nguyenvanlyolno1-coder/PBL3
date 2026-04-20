package com.ly.maychu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lichsuketnoi")
public class LichSuKetNoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idchitietcathi")
    private ChiTietCaThi chiTietCaThi;

    private String loaiSuKien; // NGAT_KET_NOI, KET_NOI_LAI, CANH_BAO
    private LocalDateTime thoiGian;
    private String ghiChu;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChiTietCaThi getChiTietCaThi() { return chiTietCaThi; }
    public void setChiTietCaThi(ChiTietCaThi c) { this.chiTietCaThi = c; }

    public String getLoaiSuKien() { return loaiSuKien; }
    public void setLoaiSuKien(String loaiSuKien) { this.loaiSuKien = loaiSuKien; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
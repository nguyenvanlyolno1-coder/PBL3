package com.ly.maychu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "canhbao")
public class CanhBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idchitietcathi")
    private ChiTietCaThi chiTietCaThi;

    private String loaiCanhBao; // CHUYEN_TAB, MAT_MANG, THOAT_TRINH_DUYET
    private LocalDateTime thoiGian;
    private String hinhAnh;
    private String moTa;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChiTietCaThi getChiTietCaThi() { return chiTietCaThi; }
    public void setChiTietCaThi(ChiTietCaThi c) { this.chiTietCaThi = c; }

    public String getLoaiCanhBao() { return loaiCanhBao; }
    public void setLoaiCanhBao(String loaiCanhBao) { this.loaiCanhBao = loaiCanhBao; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
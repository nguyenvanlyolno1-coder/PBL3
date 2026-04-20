package com.ly.maychu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cathi")
public class CaThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenCaThi;
    private LocalDateTime ngayGio;
    private Integer thoiGian; // phút

    @ManyToOne
    @JoinColumn(name = "idgiangvien")
    private NguoiDung giangVien;

    private String trangThai; // CHUAN_BI, DANG_THI, KET_THUC

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenCaThi() { return tenCaThi; }
    public void setTenCaThi(String tenCaThi) { this.tenCaThi = tenCaThi; }

    public LocalDateTime getNgayGio() { return ngayGio; }
    public void setNgayGio(LocalDateTime ngayGio) { this.ngayGio = ngayGio; }

    public Integer getThoiGian() { return thoiGian; }
    public void setThoiGian(Integer thoiGian) { this.thoiGian = thoiGian; }

    public NguoiDung getGiangVien() { return giangVien; }
    public void setGiangVien(NguoiDung giangVien) { this.giangVien = giangVien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
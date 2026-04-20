package com.ly.maychu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chitietcathi")
public class ChiTietCaThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idcathi")
    private CaThi caThi;

    @ManyToOne
    @JoinColumn(name = "idsinhhvien")
    private NguoiDung sinhVien;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CaThi getCaThi() { return caThi; }
    public void setCaThi(CaThi caThi) { this.caThi = caThi; }

    public NguoiDung getSinhVien() { return sinhVien; }
    public void setSinhVien(NguoiDung sinhVien) { this.sinhVien = sinhVien; }
}
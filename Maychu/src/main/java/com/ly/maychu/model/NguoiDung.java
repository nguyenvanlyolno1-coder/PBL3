package com.ly.maychu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "nguoidung")
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ten;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String pass;

    @Column(nullable = false)
    private String vaiTro; // GIANG_VIEN, SINH_VIEN, QUAN_TRI

    private String email;
    private String soDienThoai;

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
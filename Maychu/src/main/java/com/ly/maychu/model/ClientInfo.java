package com.ly.maychu.model;

public class ClientInfo {
    private String sessionId;
    private Long sinhVienId;
    private String maSinhVien;
    private String hoTen;
    private String ip;
    private String hostname;
    private Long lastSeen;
    private String trangThai; // DANG_THI, MAT_KET_NOI, VI_PHAM
    private Long chiTietCaThiId;

    // Getters & Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getSinhVienId() { return sinhVienId; }
    public void setSinhVienId(Long sinhVienId) { this.sinhVienId = sinhVienId; }

    public String getMaSinhVien() { return maSinhVien; }
    public void setMaSinhVien(String maSinhVien) { this.maSinhVien = maSinhVien; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public Long getLastSeen() { return lastSeen; }
    public void setLastSeen(Long lastSeen) { this.lastSeen = lastSeen; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Long getChiTietCaThiId() { return chiTietCaThiId; }
    public void setChiTietCaThiId(Long chiTietCaThiId) { this.chiTietCaThiId = chiTietCaThiId; }
}
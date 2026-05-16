package com.ly.model;

import java.util.List;

public class ExamSessionInfo {

    private Long caThiId;
    private String tenCaThi;
    private String ngayGio;
    private String trangThai;
    private List<String> allowedUrls;

    public Long getCaThiId() {
        return caThiId;
    }

    public void setCaThiId(Long caThiId) {
        this.caThiId = caThiId;
    }

    public String getTenCaThi() {
        return tenCaThi;
    }

    public void setTenCaThi(String tenCaThi) {
        this.tenCaThi = tenCaThi;
    }

    public String getNgayGio() {
        return ngayGio;
    }

    public void setNgayGio(String ngayGio) {
        this.ngayGio = ngayGio;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<String> getAllowedUrls() {
        return allowedUrls;
    }

    public void setAllowedUrls(List<String> allowedUrls) {
        this.allowedUrls = allowedUrls;
    }

    public String getAllowedUrlsCsv() {
        if (allowedUrls == null || allowedUrls.isEmpty()) {
            return "";
        }
        return String.join(",", allowedUrls);
    }

    @Override
    public String toString() {
        return tenCaThi + " - " + ngayGio + " [" + trangThai + "]";
    }
}
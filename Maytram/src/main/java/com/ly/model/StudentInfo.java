package com.ly.model;

import java.util.List;

public class StudentInfo {

    private boolean success;
    private Long svId;
    private String hoTen;
    private String maSinhVien;
    private List<ExamSessionInfo> danhSachCaThi;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public Long getSvId() {
        return svId;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getMaSinhVien() {
        return maSinhVien;
    }

    public List<ExamSessionInfo> getDanhSachCaThi() {
        return danhSachCaThi;
    }

    public String getMessage() {
        return message;
    }
}
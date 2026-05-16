package com.ly.core;

import com.ly.model.ExamSessionInfo;
import com.ly.model.StudentInfo;

public class SessionContext {

    private String wsServerUrl;
    private String httpServerUrl;

    private StudentInfo studentInfo;
    private ExamSessionInfo selectedExam;

    private ClientState state = ClientState.BOOTING;

    public String getWsServerUrl() {
        return wsServerUrl;
    }

    public void setWsServerUrl(String wsServerUrl) {
        this.wsServerUrl = wsServerUrl;
    }

    public String getHttpServerUrl() {
        return httpServerUrl;
    }

    public void setHttpServerUrl(String httpServerUrl) {
        this.httpServerUrl = httpServerUrl;
    }

    public StudentInfo getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(StudentInfo studentInfo) {
        this.studentInfo = studentInfo;
    }

    public ExamSessionInfo getSelectedExam() {
        return selectedExam;
    }

    public void setSelectedExam(ExamSessionInfo selectedExam) {
        this.selectedExam = selectedExam;
    }

    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        this.state = state;
    }
}
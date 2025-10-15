package com.app.billing.instabillz.model;

public class AttendanceModel {
    private String employeeName;
    private String date;
    private String loginTime;
    private String logoutTime;
    private String workingHours;

    // ✅ Required empty constructor for Firestore
    public AttendanceModel() {
    }

    // ✅ Full constructor
    public AttendanceModel(String employeeName, String date, String loginTime, String logout_time, String workingHours) {
        this.employeeName = employeeName;
        this.date = date;
        this.loginTime = loginTime;
        this.logoutTime = logout_time;
        this.workingHours = workingHours;
    }

    // ✅ Getters and setters
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(String logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }
}

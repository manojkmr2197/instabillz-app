package com.app.billing.instabillz.model;

public class AttendanceModel {
    private String employee_name;
    private String date;
    private String login_time;
    private String logout_time;
    private String working_hours;

    // ✅ Required empty constructor for Firestore
    public AttendanceModel() {
    }

    // ✅ Full constructor
    public AttendanceModel(String employee_name, String date, String login_time, String logout_time, String working_hours) {
        this.employee_name = employee_name;
        this.date = date;
        this.login_time = login_time;
        this.logout_time = logout_time;
        this.working_hours = working_hours;
    }

    // ✅ Getters and setters
    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }

    public String getLogout_time() {
        return logout_time;
    }

    public void setLogout_time(String logout_time) {
        this.logout_time = logout_time;
    }

    public String getWorking_hours() {
        return working_hours;
    }

    public void setWorking_hours(String working_hours) {
        this.working_hours = working_hours;
    }
}

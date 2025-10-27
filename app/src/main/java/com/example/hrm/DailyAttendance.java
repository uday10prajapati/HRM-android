package com.example.hrm;

public class DailyAttendance {
    private String date;
    private String punchInTime;
    private String punchOutTime;

    public DailyAttendance(String date) {
        this.date = date;
        this.punchInTime = "-"; // Default value
        this.punchOutTime = "-"; // Default value
    }

    public DailyAttendance(String date, String punchInTime, String punchOutTime) {
        this.date = date;
        this.punchInTime = punchInTime;
        this.punchOutTime = punchOutTime;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getPunchInTime() { return punchInTime; }
    public void setPunchInTime(String punchInTime) { this.punchInTime = punchInTime; }
    public String getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(String punchOutTime) { this.punchOutTime = punchOutTime; }
}

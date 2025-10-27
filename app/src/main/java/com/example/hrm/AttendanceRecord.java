package com.example.hrm;

public class AttendanceRecord {
    private String punchType;
    private String timestamp;

    public AttendanceRecord(String punchType, String timestamp) {
        this.punchType = punchType;
        this.timestamp = timestamp;
    }

    public String getPunchType() {
        return punchType;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

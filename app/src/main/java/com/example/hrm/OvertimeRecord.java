package com.example.hrm;

public class OvertimeRecord {
    private String date;
    private double hours;

    public OvertimeRecord(String date, double hours) {
        this.date = date;
        this.hours = hours;
    }

    public String getDate() {
        return date;
    }

    public double getHours() {
        return hours;
    }
}

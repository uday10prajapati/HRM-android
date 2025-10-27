package com.example.hrm;

public class Leave {
    private String type;
    private String startDate;
    private String endDate;
    private String status;
    private String reason;

    public Leave(String type, String startDate, String endDate, String status, String reason) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
    }

    // Getters
    public String getType() { return type; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
}

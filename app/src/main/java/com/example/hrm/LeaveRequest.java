package com.example.hrm;

public class LeaveRequest {
    private String reason;
    private String status;
    private String timestamp;
    private String leaveType;

    public LeaveRequest(String reason, String status, String timestamp, String leaveType) {
        this.reason = reason;
        this.status = status;
        this.timestamp = timestamp;
        this.leaveType = leaveType;
    }

    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
    public String getLeaveType() { return leaveType; }
}

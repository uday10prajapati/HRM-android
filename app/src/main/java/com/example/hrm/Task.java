package com.example.hrm;

import java.io.Serializable;

public class Task implements Serializable {
    private final int id;
    private final String userId;
    private final String title;
    private final String description;
    private final String createdAt;
    private final String status;
    private final String assignedTo;
    private final String customerName;
    private final String customerAddress;
    private final String customerMobile;
    private final String dueDate;

    public Task(int id, String userId, String title, String description, String createdAt, String status,
                String assignedTo, String customerName, String customerAddress, String customerMobile, String dueDate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
        this.assignedTo = assignedTo;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerMobile = customerMobile;
        this.dueDate = dueDate;
    }

    // Getters
    public int getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public String getAssignedTo() { return assignedTo; }
    public String getCustomerName() { return customerName; }
    public String getCustomerAddress() { return customerAddress; }
    public String getCustomerMobile() { return customerMobile; }
    public String getDueDate() { return dueDate; }
}

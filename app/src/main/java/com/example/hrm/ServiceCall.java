package com.example.hrm;

import java.io.Serializable;

public class ServiceCall implements Serializable {
    private final int id;
    private final String customerName;
    private final String customerAddress;
    private final String customerMobile;
    private final String issueDescription;
    private final String status;

    public ServiceCall(int id, String customerName, String customerAddress, String customerMobile, String issueDescription, String status) {
        this.id = id;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerMobile = customerMobile;
        this.issueDescription = issueDescription;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerAddress() { return customerAddress; }
    public String getCustomerMobile() { return customerMobile; }
    public String getIssueDescription() { return issueDescription; }
    public String getStatus() { return status; }
}

package com.example.hrm;

import android.os.Parcel;
import android.os.Parcelable;

public class AssignCall implements Parcelable {
    private long call_id; // The new, unique primary key
    private String id; // This is the user's UUID
    private String createdAt;
    private String name;
    private String role;
    private long mobileNumber;
    private String dairyName;
    private String problem;
    private String description;
    private String status;

    // Constructor
    public AssignCall(long call_id, String id, String createdAt, String name, String role, long mobileNumber, String dairyName, String problem, String description, String status) {
        this.call_id = call_id;
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.role = role;
        this.mobileNumber = mobileNumber;
        this.dairyName = dairyName;
        this.problem = problem;
        this.description = description;
        this.status = status;
    }

    // Getters
    public long getCallId() { return call_id; }
    public String getId() { return id; }
    public String getCreatedAt() { return createdAt; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public long getMobileNumber() { return mobileNumber; }
    public String getDairyName() { return dairyName; }
    public String getProblem() { return problem; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }

    // Parcelable implementation
    protected AssignCall(Parcel in) {
        call_id = in.readLong();
        id = in.readString();
        createdAt = in.readString();
        name = in.readString();
        role = in.readString();
        mobileNumber = in.readLong();
        dairyName = in.readString();
        problem = in.readString();
        description = in.readString();
        status = in.readString();
    }

    public static final Creator<AssignCall> CREATOR = new Creator<AssignCall>() {
        @Override
        public AssignCall createFromParcel(Parcel in) {
            return new AssignCall(in);
        }

        @Override
        public AssignCall[] newArray(int size) {
            return new AssignCall[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(call_id);
        dest.writeString(id);
        dest.writeString(createdAt);
        dest.writeString(name);
        dest.writeString(role);
        dest.writeLong(mobileNumber);
        dest.writeString(dairyName);
        dest.writeString(problem);
        dest.writeString(description);
        dest.writeString(status);
    }
}

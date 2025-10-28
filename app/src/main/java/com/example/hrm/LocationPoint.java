package com.example.hrm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocationPoint {
    private String userId;
    private double latitude;
    private double longitude;
    private String timestamp;

    public LocationPoint(String userId, double latitude, double longitude) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
    }

    public String getUserId() {
        return userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

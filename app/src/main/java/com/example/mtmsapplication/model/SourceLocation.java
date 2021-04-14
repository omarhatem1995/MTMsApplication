package com.example.mtmsapplication.model;

public class SourceLocation {
    String name;
    double latitude;
    double longitude;

    public SourceLocation() {
    }

    public SourceLocation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double logitude) {
        this.longitude = logitude;
    }
}

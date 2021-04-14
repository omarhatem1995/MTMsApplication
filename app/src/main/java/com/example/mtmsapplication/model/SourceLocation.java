package com.example.mtmsapplication.model;

public class SourceLocation {
    String name;
    String latitude;
    String logitude;

    public SourceLocation() {
    }

    public SourceLocation(String name, String latitude, String logitude) {
        this.name = name;
        this.latitude = latitude;
        this.logitude = logitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLogitude() {
        return logitude;
    }

    public void setLogitude(String logitude) {
        this.logitude = logitude;
    }
}

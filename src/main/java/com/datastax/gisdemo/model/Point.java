package com.datastax.gisdemo.model;

public class Point {

    private double distance;
    private float longitude;
    private float latitude;

    public Point(double distance, float longitude, float latitude) {
        this.distance = distance;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }



}

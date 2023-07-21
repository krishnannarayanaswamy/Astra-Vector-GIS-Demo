package com.datastax.gisdemo.model;

public class PostCode {
    private String postCode;
    private Float longitude;
    private Float latitude;
    // This value is not part of the construction, it is generated later for the sorting
    private double distance;

    public PostCode(String postCode, Float longitude, Float latitude) {
        this.postCode = postCode;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }


    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}

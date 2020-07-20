package com.example.specialoffer;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class FenceData implements Serializable {

    private String id;
    private double lat;
    private double lon;
    private String address;
    private String website;
    private float radius;
    private int type;
    private String message;
    private String code;
    private String fenceColor;
    private String logo;

    FenceData(String id, double lat, double lon, String address, String website, float radius, int type, String message, String code, String fenceColor, String logo) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.website = website;
        this.radius = radius;
        this.type = type;
        this.message = message;
        this.code = code;
        this.fenceColor = fenceColor;
        this.logo = logo;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getAddress() {
        return address;
    }

    public String getWebsite() {
        return website;
    }

    public float getRadius() {
        return radius;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public String getFenceColor() {
        return fenceColor;
    }

    public String getLogo() {
        return logo;
    }

    @NonNull
    @Override
    public String toString() {
        return "FenceData{" +
                "id='" + id + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", address='" + address + '\'' +
                ", website='" + website + '\'' +
                ", radius=" + radius +
                ", type=" + type +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", fenceColor='" + fenceColor + '\'' +
                ", logo='" + logo + '\'' +
                '}';
    }
}

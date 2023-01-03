package com.michael.dormie.model;

import java.io.Serializable;
import java.util.List;

public class Tenant implements Serializable {
    private List<String> houseTypes;
    private List<String> amenities;
    private String school;
    private int minDistance;
    private int maxDistance;

    public Tenant(List<String> houseTypes, List<String> amenities, String school, int minDistance, int maxDistance) {
        this.houseTypes = houseTypes;
        this.amenities = amenities;
        this.school = school;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    public List<String> getHouseTypes() {
        return houseTypes;
    }

    public void setHouseTypes(List<String> houseTypes) {
        this.houseTypes = houseTypes;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }
}